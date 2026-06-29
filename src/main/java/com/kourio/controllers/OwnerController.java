package com.kourio.controllers;

import com.kourio.models.Service;
import com.kourio.models.User;
import com.kourio.models.Role;
import com.kourio.daos.UserDAO;
import com.kourio.daos.ServiceDAO;
import com.kourio.daos.AppointmentDAO;
import com.kourio.daos.AuditLogDAO;
import com.kourio.views.MainFrame;
import com.kourio.views.OwnerDashboardView;
import com.kourio.views.UIHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class OwnerController {
    private final MainFrame mainFrame;
    private final OwnerDashboardView view;
    private final User owner;

    private final UserDAO userDAO;
    private final ServiceDAO serviceDAO;
    private final AppointmentDAO appointmentDAO;
    private final AuditLogDAO auditLogDAO;

    private List<Service> allServices;
    private List<User> allBarbers;

    public OwnerController(MainFrame mainFrame, OwnerDashboardView view, User owner) {
        this.mainFrame = mainFrame;
        this.view = view;
        this.owner = owner;

        this.userDAO = new UserDAO();
        this.serviceDAO = new ServiceDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.auditLogDAO = new AuditLogDAO();

        // Bind Action Listeners
        this.view.addLogoutListener(new LogoutListener());
        
        this.view.addAddServiceListener(new AddServiceListener());
        this.view.addUpdateServiceListener(new UpdateServiceListener());
        this.view.addDeactivateServiceListener(new DeactivateServiceListener());
        this.view.addHardDeleteServiceListener(new HardDeleteServiceListener());

        this.view.addAddBarberListener(new AddBarberListener());
        this.view.addUpdateBarberListener(new UpdateBarberListener());
        this.view.addResetBarberPasswordListener(new ResetBarberPasswordListener());
        this.view.addHardDeleteBarberListener(new HardDeleteBarberListener());

        this.view.addGenerateStatsListener(new GenerateStatsListener());
        this.view.addExportCSVListener(new ExportCSVListener());

        // Bind Table Row Selection Listeners for automatic form filling
        this.view.getServicesTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Service selected = view.getSelectedService(allServices);
                if (selected != null) {
                    view.fillServiceFields(selected);
                }
            }
        });

        this.view.getBarbersTable().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selected = view.getSelectedBarber(allBarbers);
                if (selected != null) {
                    loadAndFillBarberDetails(selected);
                }
            }
        });

        initData();
    }

    private void initData() {
        try {
            refreshServicesList();
            refreshBarbersList();
            runStatisticsQuery();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Σφάλμα φόρτωσης δεδομένων: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshServicesList() throws SQLException {
        allServices = serviceDAO.getAllServices(true); // Include inactive ones for management
        view.setServicesTableData(allServices);
        view.setStatsServicesFilter(allServices);
    }

    private void refreshBarbersList() throws SQLException {
        allBarbers = userDAO.getAllBarbers();
        view.setBarbersTableData(allBarbers);
        view.setStatsBarbersFilter(allBarbers);
    }

    private void loadAndFillBarberDetails(User b) {
        try {
            String specialties = userDAO.getBarberSpecialties(b.getId());
            LocalTime startH = LocalTime.of(9, 0);
            LocalTime endH = LocalTime.of(17, 0);

            UserDAO.WorkingHours hours = userDAO.getBarberWorkingHours(b.getId());
            if (hours != null) {
                startH = hours.start();
                endH = hours.end();
            }
            view.fillBarberFields(b, specialties, startH.toString(), endH.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ==========================================
    // SERVICES MANAGEMENT LISTENERS
    // ==========================================

    private class AddServiceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = view.getServiceNameInput();
            String priceStr = view.getServicePriceInput();
            String durStr = view.getServiceDurationInput();
            String cat = view.getServiceCategoryInput();

            if (name.isEmpty() || priceStr.isEmpty() || durStr.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ συμπληρώστε Όνομα, Τιμή και Διάρκεια.");
                return;
            }

            try {
                BigDecimal price = new BigDecimal(priceStr);
                int duration = Integer.parseInt(durStr);
                
                // UC-09 Validation rules
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(view, "Η τιμή πρέπει να είναι θετικός αριθμός.");
                    return;
                }
                if (duration <= 0 || duration % 15 != 0) {
                    JOptionPane.showMessageDialog(view, "Η διάρκεια πρέπει να είναι θετικό πολλαπλάσιο των 15 λεπτών.");
                    return;
                }

                Service service = new Service();
                service.setName(name);
                service.setPrice(price);
                service.setDurationMinutes(duration);
                service.setCategory(cat);
                service.setActive(view.getServiceActiveInput());

                boolean success = serviceDAO.createService(service);
                if (success) {
                    JOptionPane.showMessageDialog(view, "Η υπηρεσία προστέθηκε με επιτυχία!");
                    refreshServicesList();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Μη έγκυρη τιμή ή διάρκεια.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα βάσης δεδομένων: " + ex.getMessage());
            }
        }
    }

    private class UpdateServiceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Service selected = view.getSelectedService(allServices);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε μια υπηρεσία από τον πίνακα.");
                return;
            }

            try {
                selected.setName(view.getServiceNameInput());
                selected.setPrice(new BigDecimal(view.getServicePriceInput()));
                selected.setDurationMinutes(Integer.parseInt(view.getServiceDurationInput()));
                selected.setCategory(view.getServiceCategoryInput());
                selected.setActive(view.getServiceActiveInput());

                boolean success = serviceDAO.updateService(selected);
                if (success) {
                    JOptionPane.showMessageDialog(view, "Η υπηρεσία ενημερώθηκε επιτυχώς.");
                    refreshServicesList();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα ενημέρωσης: " + ex.getMessage());
            }
        }
    }

    private class DeactivateServiceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Service selected = view.getSelectedService(allServices);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε μια υπηρεσία.");
                return;
            }

            try {
                // Check if service has future active appointments (UC-09 3β Exception Flow)
                boolean hasFuture = serviceDAO.hasActiveFutureAppointments(selected.getId());
                if (hasFuture) {
                    int choice = JOptionPane.showConfirmDialog(view,
                        "Προειδοποίηση: Υπάρχουν ενεργά μελλοντικά ραντεβού για αυτήν την υπηρεσία.\n" +
                        "Το σύστημα προτείνει την Απενεργοποίηση (Soft Delete) αντί της διαγραφής.\n" +
                        "Θέλετε να προχωρήσετε σε απενεργοποίηση της υπηρεσίας;",
                        "Μελλοντικά Ραντεβού", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                boolean success = serviceDAO.deactivateService(selected.getId());
                if (success) {
                    JOptionPane.showMessageDialog(view, "Η υπηρεσία απενεργοποιήθηκε (soft deleted).");
                    refreshServicesList();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα κατά την απενεργοποίηση: " + ex.getMessage());
            }
        }
    }

    private class HardDeleteServiceListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Service selected = view.getSelectedService(allServices);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε μια υπηρεσία.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(view,
                "ΠΡΟΣΟΧΗ: Αυτή η ενέργεια θα διαγράψει ΟΡΙΣΤΙΚΑ την υπηρεσία '" + selected.getName() + "'\n" +
                "καθώς και ΟΛΑ τα σχετικά ραντεβού. Δεν μπορεί να αναιρεθεί!\n\n" +
                "Είστε σίγουροι;",
                "Οριστική Διαγραφή Υπηρεσίας", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;

            try {
                boolean success = serviceDAO.hardDeleteService(selected.getId());
                if (success) {
                    JOptionPane.showMessageDialog(view, "Η υπηρεσία διαγράφηκε οριστικά.");
                    refreshServicesList();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα κατά τη διαγραφή: " + ex.getMessage());
            }
        }
    }

    // ==========================================
    // STAFF (BARBERS) MANAGEMENT LISTENERS
    // ==========================================

    private class AddBarberListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = view.getBarberUsernameInput();
            String password = view.getBarberPasswordInput();
            String fname = view.getBarberFNameInput();
            String lname = view.getBarberLNameInput();
            String phone = view.getBarberPhoneInput();
            String email = view.getBarberEmailInput();
            String startStr = view.getBarberStartHourInput();
            String endStr = view.getBarberEndHourInput();
            String specs = view.getBarberSpecialtiesInput();

            if (username.isEmpty() || password.isEmpty() || fname.isEmpty() || lname.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ συμπληρώστε τα υποχρεωτικά πεδία (Username, Κωδικός, Όνομα, Επώνυμο, Email).");
                return;
            }

            try {
                LocalTime startHour = LocalTime.parse(startStr);
                LocalTime endHour = LocalTime.parse(endStr);

                User barber = new User();
                barber.setUsername(username);
                barber.setRole(Role.BARBER);
                barber.setFirstName(fname);
                barber.setLastName(lname);
                barber.setPhone(phone);
                barber.setEmail(email);
                barber.setActive(view.getBarberActiveInput());

                // Database constraint handles duplicate username/email check (UC-11 5a)
                boolean success = userDAO.createUser(barber, password);
                if (success) {
                    // Create barber details (working hours, specialties)
                    userDAO.createBarberDetails(barber.getId(), startHour, endHour, specs);
                    JOptionPane.showMessageDialog(view, "Ο κουρέας προστέθηκε επιτυχώς!");
                    refreshBarbersList();
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(view, "Μη έγκυρη μορφή ωραρίου. Χρησιμοποιήστε HH:MM.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα βάσης δεδομένων (πιθανόν διπλότυπο Username/Email): " + ex.getMessage());
            }
        }
    }

    private class UpdateBarberListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User selected = view.getSelectedBarber(allBarbers);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε έναν κουρέα από τον πίνακα.");
                return;
            }

            String fname = view.getBarberFNameInput();
            String lname = view.getBarberLNameInput();
            String phone = view.getBarberPhoneInput();
            String email = view.getBarberEmailInput();
            String startStr = view.getBarberStartHourInput();
            String endStr = view.getBarberEndHourInput();
            String specs = view.getBarberSpecialtiesInput();
            boolean active = view.getBarberActiveInput();

            try {
                LocalTime startHour = LocalTime.parse(startStr);
                LocalTime endHour = LocalTime.parse(endStr);

                // If deactivating, check future appointments (UC-11 3γ Exception Flow)
                if (!active && selected.isActive()) {
                    boolean hasFuture = appointmentDAO.hasBarberFutureAppointments(selected.getId());
                    if (hasFuture) {
                        int choice = JOptionPane.showConfirmDialog(view,
                            "Ο κουρέας έχει ενεργά μελλοντικά ραντεβού.\n" +
                            "Θέλετε να τον απενεργοποιήσετε παρόλα αυτά; (Συνίσταται η μεταφορά των ραντεβού)",
                            "Μελλοντικά Ραντεβού Κουρέα", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                }

                selected.setFirstName(fname);
                selected.setLastName(lname);
                selected.setPhone(phone);
                selected.setEmail(email);
                selected.setActive(active);

                boolean successUser = userDAO.updateUser(selected);
                boolean successDetails = userDAO.updateBarberDetails(selected.getId(), startHour, endHour, specs);

                if (successUser && successDetails) {
                    JOptionPane.showMessageDialog(view, "Τα στοιχεία του κουρέα ενημερώθηκαν.");
                    refreshBarbersList();
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(view, "Μη έγκυρη μορφή ωραρίου. Χρησιμοποιήστε HH:MM.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα βάσης δεδομένων: " + ex.getMessage());
            }
        }
    }

    private class ResetBarberPasswordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User selected = view.getSelectedBarber(allBarbers);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε έναν κουρέα.");
                return;
            }

            String newPass = JOptionPane.showInputDialog(view, "Εισάγετε νέο κωδικό πρόσβασης για τον χρήστη " + selected.getUsername() + ":");
            if (newPass == null || newPass.trim().isEmpty()) {
                return;
            }

            // UC-11 3α Reset password with temporary pass
            try {
                boolean success = userDAO.resetPassword(selected.getId(), newPass.trim());
                if (success) {
                    JOptionPane.showMessageDialog(view, "Ο κωδικός πρόσβασης άλλαξε με επιτυχία.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα επαναφοράς κωδικού: " + ex.getMessage());
            }
        }
    }

    private class HardDeleteBarberListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            User selected = view.getSelectedBarber(allBarbers);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε έναν κουρέα.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(view,
                "ΠΡΟΣΟΧΗ: Αυτή η ενέργεια θα διαγράψει ΟΡΙΣΤΙΚΑ τον κουρέα '" + selected.getFullName() + "'\n" +
                "καθώς και ΟΛΑ τα σχετικά ραντεβού. Δεν μπορεί να αναιρεθεί!\n\n" +
                "Είστε σίγουροι;",
                "Οριστική Διαγραφή Κουρέα", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;

            try {
                boolean success = userDAO.hardDeleteBarber(selected.getId());
                if (success) {
                    JOptionPane.showMessageDialog(view, "Ο κουρέας διαγράφηκε οριστικά.");
                    refreshBarbersList();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα κατά τη διαγραφή: " + ex.getMessage());
            }
        }
    }

    // ==========================================
    // STATISTICS LISTENERS
    // ==========================================

    private class GenerateStatsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            runStatisticsQuery();
        }
    }

    private Map<String, Object> runStatisticsQuery() {
        String startStr = view.getStatsStartInput();
        String endStr = view.getStatsEndInput();

        if (startStr.isEmpty() || endStr.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Παρακαλώ εισάγετε ημερομηνίες έναρξης και λήξης.");
            return null;
        }

        try {
            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);

            if (end.isBefore(start)) {
                view.setStatsStatus("Η ημερομηνία λήξης πρέπει να είναι μετά την ημερομηνία έναρξης.", UIHelper.DANGER);
                JOptionPane.showMessageDialog(view, "Η ημερομηνία λήξης πρέπει να είναι μετά την ημερομηνία έναρξης.");
                return null;
            }

            User barber = view.getStatsSelectedBarber();
            Service service = view.getStatsSelectedService();

            Integer bId = (barber == null || barber.getId() == -1) ? null : barber.getId();
            Integer sId = (service == null || service.getId() == -1) ? null : service.getId();

            Map<String, Object> stats = appointmentDAO.getStatistics(start, end, bId, sId);
            view.displayStatistics(stats);
            return stats;

        } catch (DateTimeParseException ex) {
            view.setStatsStatus("Μη έγκυρη ημερομηνία.", UIHelper.DANGER);
            JOptionPane.showMessageDialog(view, "Μη έγκυρη ημερομηνία. Χρησιμοποιήστε YYYY-MM-DD.");
        } catch (SQLException ex) {
            view.setStatsStatus("Σφάλμα βάσης δεδομένων κατά τον υπολογισμό.", UIHelper.DANGER);
            JOptionPane.showMessageDialog(view, "Σφάλμα βάσης δεδομένων κατά τον υπολογισμό: " + ex.getMessage());
        }
        return null;
    }

    private class ExportCSVListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Map<String, Object> stats = runStatisticsQuery();
            if (stats == null) return;

            // Let the user pick where to save (portable across machines)
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Αποθήκευση αναφοράς CSV");
            String defaultName = "kourio_report_" + view.getStatsStartInput() + "_" + view.getStatsEndInput() + ".csv";
            chooser.setSelectedFile(new File(System.getProperty("user.home"), defaultName));
            if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".csv")) {
                target = new File(target.getAbsolutePath() + ".csv");
            }

            try (FileWriter writer = new FileWriter(target)) {
                // Header
                writer.append("Metric,Value\n");
                writer.append("Start Date,").append(view.getStatsStartInput()).append("\n");
                writer.append("End Date,").append(view.getStatsEndInput()).append("\n");
                writer.append("Total Bookings,").append(stats.get("totalBookings").toString()).append("\n");
                writer.append("Completed Bookings,").append(stats.get("completedBookings").toString()).append("\n");
                writer.append("No-Show Bookings,").append(stats.get("noShowBookings").toString()).append("\n");
                writer.append("Total Revenue (€),").append(stats.get("totalRevenue").toString()).append("\n");
                writer.append("Average Revenue per Booking (€),").append(stats.get("avgRevenue").toString()).append("\n");
                writer.append("No-Show Rate (%),").append(String.format("%.1f", (Double) stats.get("noShowRate")).replace(',', '.')).append("\n");
                
                writer.flush();
                JOptionPane.showMessageDialog(view, "Η αναφορά εξήχθη επιτυχώς στο: \n" + target.getAbsolutePath(), "Εξαγωγή CSV", JOptionPane.INFORMATION_MESSAGE);
                
                // Log event
                auditLogDAO.logEvent(owner.getUsername(), "REPORT_EXPORTED_CSV", "127.0.0.1");

            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα κατά την εξαγωγή CSV: " + ex.getMessage());
            }
        }
    }

    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                auditLogDAO.logEvent(owner.getUsername(), "LOGOUT", "127.0.0.1");
            } catch (SQLException ignored) {}
            mainFrame.showLogin();
        }
    }
}
