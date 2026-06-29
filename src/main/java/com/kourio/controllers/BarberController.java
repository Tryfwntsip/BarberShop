package com.kourio.controllers;

import com.kourio.models.Appointment;
import com.kourio.models.AppointmentStatus;
import com.kourio.models.User;
import com.kourio.daos.AppointmentDAO;
import com.kourio.daos.AuditLogDAO;
import com.kourio.views.MainFrame;
import com.kourio.views.BarberDashboardView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class BarberController {
    private final MainFrame mainFrame;
    private final BarberDashboardView view;
    private final User barber;

    private final AppointmentDAO appointmentDAO;
    private final AuditLogDAO auditLogDAO;

    private List<Appointment> currentSchedule;

    public BarberController(MainFrame mainFrame, BarberDashboardView view, User barber) {
        this.mainFrame = mainFrame;
        this.view = view;
        this.barber = barber;

        this.appointmentDAO = new AppointmentDAO();
        this.auditLogDAO = new AuditLogDAO();

        // Bind events
        this.view.addLogoutListener(new LogoutListener());
        this.view.addLoadScheduleListener(new LoadScheduleListener());
        this.view.addUpdateStatusListener(new UpdateStatusListener());
        this.view.addSearchListener(new SearchListener());
        this.view.addLoadMonthListener(new LoadMonthListener());

        initData();
    }

    private void initData() {
        try {
            refreshSchedule(LocalDate.now());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(view, "Σφάλμα φόρτωσης ημερήσιου προγράμματος: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshSchedule(LocalDate date) throws SQLException {
        currentSchedule = appointmentDAO.getBarberAppointments(barber.getId(), date);
        view.setScheduleTableData(currentSchedule);
    }

    private class LoadScheduleListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String dateStr = view.getScheduleDateInput();
            if (dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ εισάγετε ημερομηνία.");
                return;
            }

            try {
                LocalDate date = LocalDate.parse(dateStr);
                LocalDate today = LocalDate.now();

                // Limit schedule viewing to 30 days ahead (UC-06 2α)
                if (date.isAfter(today.plusDays(30))) {
                    JOptionPane.showMessageDialog(view, "Μπορείτε να προβάλλετε το πρόγραμμα έως 30 ημέρες μπροστά.");
                    return;
                }

                refreshSchedule(date);

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(view, "Μη έγκυρη μορφή ημερομηνίας. Παρακαλώ χρησιμοποιήστε YYYY-MM-DD.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα βάσης δεδομένων κατά τη φόρτωση: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private class UpdateStatusListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Appointment selected = view.getSelectedAppointment(currentSchedule);
            if (selected == null) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ επιλέξτε ένα ραντεβού από το πρόγραμμα.");
                return;
            }

            AppointmentStatus newStatus = view.getSelectedStatus();
            String notes = view.getNotesInput();

            try {
                boolean success = appointmentDAO.updateAppointmentStatus(selected.getId(), newStatus, notes);
                if (success) {
                    JOptionPane.showMessageDialog(view, "Η κατάσταση του ραντεβού ενημερώθηκε επιτυχώς.");
                    
                    // Audit log status change
                    auditLogDAO.logEvent(barber.getUsername(), 
                            "APPOINTMENT_STATUS_UPDATED_ID_" + selected.getId() + "_TO_" + newStatus.name(), 
                            "127.0.0.1");

                    // Refresh
                    String dateStr = view.getScheduleDateInput();
                    LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
                    refreshSchedule(date);
                    
                    // Clear notes field
                    view.setNotesInput("");
                } else {
                    JOptionPane.showMessageDialog(view, "Αποτυχία ενημέρωσης κατάστασης.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα κατά την ενημέρωση: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private class SearchListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String query = view.getSearchQuery();
            if (query.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Παρακαλώ εισάγετε όνομα ή τηλέφωνο για αναζήτηση.");
                return;
            }

            try {
                List<Appointment> results = appointmentDAO.searchCustomerHistory(barber.getId(), query);
                view.setHistoryTableData(results);
                
                if (results.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Δεν βρέθηκαν αποτελέσματα για: " + query);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα κατά την αναζήτηση ιστορικού: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private class LogoutListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                auditLogDAO.logEvent(barber.getUsername(), "LOGOUT", "127.0.0.1");
            } catch (SQLException ignored) {}
            mainFrame.showLogin();
        }
    }

    private class LoadMonthListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int month = view.getSelectedMonth();
            int year = view.getSelectedYear();

            try {
                List<Appointment> monthApps = appointmentDAO.getBarberAppointmentsByMonth(barber.getId(), year, month);
                view.setMonthTableData(monthApps);
                
                if (monthApps.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "Δεν βρέθηκαν ραντεβού για αυτόν τον μήνα.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(view, "Σφάλμα βάσης δεδομένων κατά τη φόρτωση μηνιαίου προγράμματος: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
