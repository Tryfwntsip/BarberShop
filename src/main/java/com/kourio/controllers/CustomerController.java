package com.kourio.controllers;

import com.kourio.models.Appointment;
import com.kourio.models.AppointmentStatus;
import com.kourio.models.Service;
import com.kourio.models.User;
import com.kourio.daos.UserDAO;
import com.kourio.daos.ServiceDAO;
import com.kourio.daos.AppointmentDAO;
import com.kourio.daos.AuditLogDAO;
import com.kourio.services.BookingRules;
import com.kourio.views.MainFrame;
import com.kourio.views.CustomerDashboardView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CustomerController {
    private static final String[] STYLE_PREFERENCES = {
        "Δεν έχω προτίμηση",
        "Μοντέρνο κούρεμα / Fade",
        "Κλασικό κούρεμα",
        "Παραδοσιακό ξύρισμα / Γένια",
        "Combo μαλλιά & γένια"
    };

    private final MainFrame mainFrame;
    private final CustomerDashboardView view;
    private final User customer;

    private final UserDAO userDAO;
    private final ServiceDAO serviceDAO;
    private final AppointmentDAO appointmentDAO;
    private final AuditLogDAO auditLogDAO;

    private List<Appointment> customerAppointments;
    private List<User> activeBarbers = new ArrayList<>();
    private final Map<Integer, String> barberSpecialties = new HashMap<>();
    private User recommendedBarber;

    public CustomerController(MainFrame mainFrame, CustomerDashboardView view, User customer) {
        this.mainFrame = mainFrame;
        this.view = view;
        this.customer = customer;

        this.userDAO = new UserDAO();
        this.serviceDAO = new ServiceDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.auditLogDAO = new AuditLogDAO();

        // Bind events
        this.view.addLogoutListener(new LogoutListener());
        this.view.addStylePreferenceListener(new StylePreferenceListener());
        this.view.addUseRecommendedBarberListener(new UseRecommendedBarberListener());
        this.view.addServiceSelectionListener(new ServiceSelectionListener());
        this.view.addCheckAvailabilityListener(new CheckAvailabilityListener());
        this.view.addBookListener(new BookListener());
        this.view.addCancelAppointmentListener(new CancelAppointmentListener());

        initData();
    }

    private void initData() {
        try {
            view.setStylePreferences(STYLE_PREFERENCES);

            // Load Active Barbers
            List<User> barbers = userDAO.getAllBarbers();
            // Filter inactive barbers
            barbers.removeIf(b -> !b.isActive());
            activeBarbers = new ArrayList<>(barbers);
            loadBarberSpecialties(activeBarbers);
            view.setBarbers(activeBarbers);

            // Load Active Services
            List<Service> services = serviceDAO.getAllServices(false);
            view.setServices(services);

            // Trigger preview for first service
            if (!services.isEmpty()) {
                Service first = services.get(0);
                view.updatePriceAndDurationPreview(first.getPrice(), first.getDurationMinutes());
            }

            updateBarberRecommendation();

            // Refresh Appointments list
            refreshAppointmentsList();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Σφάλμα φόρτωσης αρχικών δεδομένων: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshAppointmentsList() throws SQLException {
        customerAppointments = appointmentDAO.getCustomerAppointments(customer.getId());
        customerAppointments.removeIf(app -> app.getCustomerId() != customer.getId());
        view.setAppointmentsTableData(customerAppointments);
    }

    private void loadBarberSpecialties(List<User> barbers) throws SQLException {
        barberSpecialties.clear();
        for (User barber : barbers) {
            String specialties = userDAO.getBarberSpecialties(barber.getId());
            barberSpecialties.put(barber.getId(), specialties == null ? "" : specialties);
        }
    }

    private void updateBarberRecommendation() {
        if (activeBarbers.isEmpty()) {
            recommendedBarber = null;
            view.setBarberRecommendation(null, "Δεν υπάρχουν διαθέσιμοι κουρείς.", false);
            return;
        }

        String preference = view.getSelectedStylePreference();
        Service service = view.getSelectedService();
        recommendedBarber = findRecommendedBarber(preference, service);

        if (recommendedBarber == null) {
            view.setBarberRecommendation(null, "Δεν βρέθηκε καθαρό ταίριασμα. Μπορείτε να επιλέξετε κουρέα χειροκίνητα.", false);
            return;
        }

        String reason = buildRecommendationReason(recommendedBarber, preference, service);
        view.setBarberRecommendation(recommendedBarber, reason, true);
    }

    private User findRecommendedBarber(String preference, Service service) {
        User best = null;
        int bestScore = Integer.MIN_VALUE;

        for (User barber : activeBarbers) {
            int score = scoreBarber(barber, preference, service);
            if (score > bestScore || (score == bestScore && best != null && barber.getId() < best.getId())) {
                best = barber;
                bestScore = score;
            }
        }

        if (bestScore <= 0 && !isNoPreference(preference)) {
            return activeBarbers.get(0);
        }
        return best;
    }

    private int scoreBarber(User barber, String preference, Service service) {
        String specialties = normalize(barberSpecialties.getOrDefault(barber.getId(), ""));
        String pref = normalize(preference);
        String serviceText = service == null ? "" : normalize(service.getName() + " " + service.getCategory());

        int score = 0;

        if (containsAny(pref, "μοντερ", "fade")) {
            score += keywordScore(specialties, "μοντερ", "fade", "σχεδι", "design") * 5;
        }
        if (containsAny(pref, "κλασικ")) {
            score += keywordScore(specialties, "κλασικ", "παραδοσιακ", "φαλτσετα") * 5;
        }
        if (containsAny(pref, "ξυρισ", "γενια", "γενει")) {
            score += keywordScore(specialties, "γενια", "γενει", "ξυρισ", "φαλτσετα", "beard") * 5;
        }
        if (containsAny(pref, "combo", "μαλλια")) {
            score += keywordScore(specialties, "κουρεμα", "γενια", "γενει", "ξυρισ", "combo") * 3;
        }

        if (containsAny(serviceText, "γενια", "γενει", "ξυρισ")) {
            score += keywordScore(specialties, "γενια", "γενει", "ξυρισ", "φαλτσετα") * 2;
        }
        if (containsAny(serviceText, "κουρεμα", "μαλλια")) {
            score += keywordScore(specialties, "κουρεμα", "fade", "κλασικ", "μοντερ") * 2;
        }
        if (containsAny(serviceText, "combo")) {
            score += keywordScore(specialties, "combo", "κουρεμα", "γενια", "ξυρισ") * 2;
        }

        return score;
    }

    private String buildRecommendationReason(User barber, String preference, Service service) {
        String specialties = barberSpecialties.getOrDefault(barber.getId(), "").trim();
        String selectedStyle = isNoPreference(preference) ? "την επιλεγμένη υπηρεσία" : preference;
        String reason = "ταιριάζει με " + selectedStyle;

        if (service != null && service.getName() != null && !service.getName().trim().isEmpty()) {
            reason += " / " + service.getName();
        }
        if (!specialties.isEmpty()) {
            reason += " (" + shorten(specialties, 58) + ")";
        }
        return reason;
    }

    private String buildCustomerPreferenceNotes() {
        String preference = view.getSelectedStylePreference();
        if (preference == null || preference.trim().isEmpty() || isNoPreference(preference)) {
            return "";
        }
        return "Προτίμηση πελάτη: " + preference;
    }

    private boolean isNoPreference(String preference) {
        return preference == null || normalize(preference).contains("δεν εχω");
    }

    private String shorten(String value, int maxChars) {
        if (value.length() <= maxChars) return value;
        return value.substring(0, maxChars - 1).trim() + "…";
    }

    private int keywordScore(String text, String... keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (text.contains(keyword)) score++;
        }
        return score;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) return "";
        String lower = value.toLowerCase(new Locale("el", "GR"));
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
    }

    private class ServiceSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Service selected = view.getSelectedService();
            if (selected != null) {
                view.updatePriceAndDurationPreview(selected.getPrice(), selected.getDurationMinutes());
            }
            updateBarberRecommendation();
        }
    }

    private class StylePreferenceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateBarberRecommendation();
        }
    }

    private class UseRecommendedBarberListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (recommendedBarber != null) {
                view.setSelectedBarber(recommendedBarber);
            }
        }
    }

    private class CheckAvailabilityListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User barber = view.getSelectedBarber();
            Service service = view.getSelectedService();
            String dateStr = view.getDateInput();

            if (barber == null || service == null || dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε κουρέα, υπηρεσία και συμπληρώστε την ημερομηνία.");
                return;
            }

            try {
                LocalDate date = LocalDate.parse(dateStr);
                LocalDate today = LocalDate.now();

                // Validation rules
                if (date.isBefore(today)) {
                    JOptionPane.showMessageDialog(view, "Δεν μπορείτε να επιλέξετε ημερομηνία στο παρελθόν.");
                    return;
                }
                if (date.isAfter(today.plusDays(30))) {
                    JOptionPane.showMessageDialog(view, "Οι κρατήσεις επιτρέπονται μόνο για τις επόμενες 30 ημέρες.");
                    return;
                }

                // Fetch barber working hours from database (fall back to defaults if unset)
                LocalTime workStart = LocalTime.of(9, 0);
                LocalTime workEnd = LocalTime.of(17, 0);
                UserDAO.WorkingHours hours = userDAO.getBarberWorkingHours(barber.getId());
                if (hours != null) {
                    workStart = hours.start();
                    workEnd = hours.end();
                }

                // Generate potential 15-minute slots within barber working hours
                List<LocalTime> availableTimes = new ArrayList<>();
                LocalTime cursor = workStart;
                LocalTime limit = workEnd;

                while (cursor.isBefore(limit)) {
                    if (appointmentDAO.isSlotAvailable(barber.getId(), date, cursor, service.getDurationMinutes())) {
                        availableTimes.add(cursor);
                    }
                    cursor = cursor.plusMinutes(15);
                }

                view.setAvailableTimes(availableTimes);

                if (availableTimes.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Δεν υπάρχουν διαθέσιμες ώρες για τη συγκεκριμένη ημερομηνία.");
                }

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(view, "Μη έγκυρη μορφή ημερομηνίας. Παρακαλώ χρησιμοποιήστε YYYY-MM-DD.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα ελέγχου διαθεσιμότητας: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private class BookListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User barber = view.getSelectedBarber();
            Service service = view.getSelectedService();
            String dateStr = view.getDateInput();
            LocalTime time = view.getSelectedTime();

            if (barber == null || service == null || dateStr.isEmpty() || time == null) {
                JOptionPane.showMessageDialog(view, "Ελλιπή στοιχεία κράτησης.");
                return;
            }

            try {
                LocalDate date = LocalDate.parse(dateStr);
                
                Appointment app = new Appointment();
                app.setCustomerId(customer.getId());
                app.setBarberId(barber.getId());
                app.setServiceId(service.getId());
                app.setAppointmentDate(date);
                app.setStartTime(time);
                app.setEndTime(time.plusMinutes(service.getDurationMinutes()));
                app.setPriceAtBooking(service.getPrice()); // Price stability rule
                app.setDurationAtBooking(service.getDurationMinutes());
                app.setStatus(AppointmentStatus.CONFIRMED);
                app.setNotes(buildCustomerPreferenceNotes());

                boolean success = appointmentDAO.bookAppointment(app);
                if (success) {
                    JOptionPane.showMessageDialog(view, "Η κράτηση ολοκληρώθηκε με επιτυχία!");
                    auditLogDAO.logEvent(customer.getUsername(), "BOOKING_CREATED_ID_" + app.getId(), "127.0.0.1");
                    refreshAppointmentsList();
                    view.setAvailableTimes(new ArrayList<>()); // Clear slots list after booking
                } else {
                    JOptionPane.showMessageDialog(view, "Race Condition: Η ώρα αυτή κρατήθηκε από άλλον χρήστη. Δοκιμάστε ξανά.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα κατά την κράτηση: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private class CancelAppointmentListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Appointment selected = view.getSelectedAppointment(customerAppointments);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε ένα ραντεβού από τον πίνακα.");
                return;
            }

            if (selected.getCustomerId() != customer.getId()) {
                JOptionPane.showMessageDialog(view, "Δεν έχετε δικαίωμα προβολής ή ακύρωσης αυτού του ραντεβού.");
                try {
                    refreshAppointmentsList();
                } catch (SQLException refreshEx) {
                    refreshEx.printStackTrace();
                }
                return;
            }

            try {
                Appointment fresh = appointmentDAO.getCustomerAppointmentById(selected.getId(), customer.getId());
                if (fresh == null) {
                    JOptionPane.showMessageDialog(view, "Το ραντεβού δεν ανήκει στον λογαριασμό σας ή δεν υπάρχει πλέον.");
                    refreshAppointmentsList();
                    return;
                }
                selected = fresh;
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα επιβεβαίωσης ραντεβού: " + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            if (selected.getStatus() == AppointmentStatus.CANCELLED) {
                JOptionPane.showMessageDialog(view, "Το ραντεβού είναι ήδη ακυρωμένο.");
                return;
            }

            if (selected.getStatus() != AppointmentStatus.CONFIRMED) {
                JOptionPane.showMessageDialog(view, "Μπορείτε να ακυρώσετε μόνο επιβεβαιωμένα ραντεβού.");
                return;
            }

            // Cancellation Business Rule (SRS §3.3.2): ο κανόνας ζει στην BookingRules (unit-tested)
            LocalDateTime appDateTime = LocalDateTime.of(selected.getAppointmentDate(), selected.getStartTime());
            LocalDateTime now = LocalDateTime.now();

            if (appDateTime.isBefore(now)) {
                JOptionPane.showMessageDialog(view, "Το ραντεβού έχει ήδη παρέλθει. Δεν είναι δυνατή η ακύρωση.");
                return;
            }

            if (!BookingRules.canCancel(now, appDateTime)) {
                JOptionPane.showMessageDialog(view, "Άρνηση Ακύρωσης: Οι ακυρώσεις επιτρέπονται μόνο έως και 2 ώρες πριν το ραντεβού.");
                return;
            }

            // Double confirmation requirement (§3.3.2)
            int choice = JOptionPane.showConfirmDialog(view,
                    "Είστε σίγουροι ότι θέλετε να ακυρώσετε το ραντεβού στις " + 
                    selected.getAppointmentDate() + " @ " + selected.getStartTime() + ";",
                    "Επιβεβαίωση Ακύρωσης",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    boolean success = appointmentDAO.cancelCustomerAppointment(selected.getId(), customer.getId(), "Ακυρώθηκε από τον πελάτη.");
                    if (success) {
                        auditLogDAO.logEvent(customer.getUsername(), "APPOINTMENT_CANCELLED_ID_" + selected.getId(), "127.0.0.1");
                        JOptionPane.showMessageDialog(view, "Το ραντεβού ακυρώθηκε.");
                        refreshAppointmentsList();
                    } else {
                        JOptionPane.showMessageDialog(view, "Αδυναμία ακύρωσης του ραντεβού.");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(view, "Σφάλμα βάσης δεδομένων κατά την ακύρωση: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                auditLogDAO.logEvent(customer.getUsername(), "LOGOUT", "127.0.0.1");
            } catch (SQLException ignored) {}
            mainFrame.showLogin();
        }
    }
}
