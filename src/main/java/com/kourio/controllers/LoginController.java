package com.kourio.controllers;

import com.kourio.models.User;
import com.kourio.daos.UserDAO;
import com.kourio.daos.AuditLogDAO;
import com.kourio.services.BookingRules;
import com.kourio.views.MainFrame;
import com.kourio.views.LoginView;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;

public class LoginController {
    private final MainFrame mainFrame;
    private final LoginView loginView;
    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;

    public LoginController(MainFrame mainFrame, LoginView loginView) {
        this.mainFrame = mainFrame;
        this.loginView = loginView;
        this.userDAO = new UserDAO();
        this.auditLogDAO = new AuditLogDAO();

        this.loginView.addLoginListener(new LoginActionListener());
        this.loginView.addForgotPasswordListener(new ForgotPasswordActionListener());
    }

    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = loginView.getUsername();
            String password = loginView.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                loginView.setErrorMessage("Παρακαλώ συμπληρώστε όλα τα πεδία.");
                return;
            }

            try {
                User user = userDAO.getUserByUsername(username);

                if (user == null) {
                    // Log failure for non-existent user
                    auditLogDAO.logEvent(username, "LOGIN_FAILURE_UNKNOWN_USER", "127.0.0.1");
                    loginView.setErrorMessage("Λανθασμένο όνομα χρήστη ή κωδικός.");
                    return;
                }

                if (!user.isActive()) {
                    loginView.setErrorMessage("Ο λογαριασμός είναι απενεργοποιημένος.");
                    return;
                }

                // Check lockout policy
                if (user.isLocked()) {
                    LocalDateTime now = LocalDateTime.now();
                    long minutesLeft = Duration.between(now, user.getLockedUntil()).toMinutes() + 1;
                    loginView.setErrorMessage("Ο λογαριασμός είναι κλειδωμένος. Απομένουν " + minutesLeft + " λεπτά.");
                    auditLogDAO.logEvent(username, "LOGIN_BLOCKED_LOCKOUT", "127.0.0.1");
                    return;
                }

                // Check password hash
                boolean passwordMatches = false;
                try {
                    passwordMatches = BCrypt.checkpw(password, user.getPasswordHash());
                } catch (Exception ex) {
                    System.err.println("Password decryption error: " + ex.getMessage());
                }

                if (passwordMatches) {
                    // Success: clear any prior failed-attempt state (only persist if there is something to reset)
                    if (user.getFailedLogins() > 0 || user.getLockedUntil() != null) {
                        user.setFailedLogins(0);
                        user.setLockedUntil(null);
                        userDAO.updateUser(user);
                    }

                    auditLogDAO.logEvent(username, "LOGIN_SUCCESS", "127.0.0.1");
                    loginView.clearFields();

                    // Route based on role
                    switch (user.getRole()) {
                        case OWNER:
                            mainFrame.showOwnerDashboard(user);
                            break;
                        case BARBER:
                            mainFrame.showBarberDashboard(user);
                            break;
                        case CUSTOMER:
                            mainFrame.showCustomerDashboard(user);
                            break;
                    }
                } else {
                    // Failure: Increment failed attempts
                    int failed = user.getFailedLogins() + 1;
                    user.setFailedLogins(failed);

                    if (BookingRules.shouldLockAccount(failed)) {
                        user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
                        auditLogDAO.logEvent(username, "ACCOUNT_LOCKOUT", "127.0.0.1");
                        loginView.setErrorMessage("Υπερβήκατε το όριο δοκιμών. Κλείδωμα για 5 λεπτά.");
                    } else {
                        auditLogDAO.logEvent(username, "LOGIN_FAILURE_WRONG_PASSWORD", "127.0.0.1");
                        int remaining = BookingRules.MAX_FAILED_LOGINS - failed;
                        loginView.setErrorMessage("Λανθασμένος κωδικός. Απομένουν " + remaining + " προσπάθειες.");
                    }
                    userDAO.updateUser(user);
                }

            } catch (SQLException ex) {
                loginView.setErrorMessage("Σφάλμα σύνδεσης στη βάση δεδομένων.");
                ex.printStackTrace();
            }
        }
    }

    private class ForgotPasswordActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(loginView,
                    "Παρακαλώ επικοινωνήστε με τον Ιδιοκτήτη του κουρείου για την επαναφορά του κωδικού σας.",
                    "Επαναφορά Κωδικού",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
