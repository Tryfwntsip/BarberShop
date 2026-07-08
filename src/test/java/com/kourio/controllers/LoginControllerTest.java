package com.kourio.controllers;

import com.kourio.daos.AuditLogDAO;
import com.kourio.daos.UserDAO;
import com.kourio.models.Role;
import com.kourio.models.User;
import com.kourio.views.LoginView;
import com.kourio.views.MainFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests του LoginController με το mocking framework Mockito.
 *
 * Όλες οι εξαρτήσεις (DAOs, View, MainFrame) είναι mocks: δεν τρέχει βάση
 * δεδομένων ούτε ανοίγει παράθυρο. Έτσι ελέγχεται ΜΟΝΟ η λογική του
 * controller, απομονωμένη — αυτό ακριβώς επιτρέπει το dependency injection
 * στον constructor του LoginController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginController — έλεγχος ροής σύνδεσης με Mockito mocks")
class LoginControllerTest {

    /** BCrypt με χαμηλό cost (4) για γρήγορα tests. */
    private static final String HASH_OF_CORRECT = BCrypt.hashpw("password123", BCrypt.gensalt(4));

    @Mock private MainFrame mainFrame;
    @Mock private LoginView loginView;
    @Mock private UserDAO userDAO;
    @Mock private AuditLogDAO auditLogDAO;

    private ActionListener loginAction;

    @BeforeEach
    void wireControllerAndCaptureListener() {
        new LoginController(mainFrame, loginView, userDAO, auditLogDAO);

        // Ο controller «κρέμασε» τον listener του στο view — τον πιάνουμε για να τον πυροδοτούμε
        ArgumentCaptor<ActionListener> captor = ArgumentCaptor.forClass(ActionListener.class);
        verify(loginView).addLoginListener(captor.capture());
        loginAction = captor.getValue();
    }

    private User user(String username) {
        return new User(1, username, HASH_OF_CORRECT, Role.CUSTOMER,
                "Νίκος", "Γεωργίου", "6970000000", "nikos@kourio.gr");
    }

    @Test
    @DisplayName("Επιτυχής σύνδεση: δρομολόγηση στο σωστό dashboard + audit log")
    void successfulLoginRoutesToDashboard() throws SQLException {
        when(loginView.getUsername()).thenReturn("nikos");
        when(loginView.getPassword()).thenReturn("password123");
        User u = user("nikos");
        when(userDAO.getUserByUsername("nikos")).thenReturn(u);

        loginAction.actionPerformed(null);

        verify(mainFrame).showCustomerDashboard(u);
        verify(auditLogDAO).logEvent(eq("nikos"), eq("LOGIN_SUCCESS"), anyString());
        verify(loginView).clearFields();
    }

    @Test
    @DisplayName("Άγνωστος χρήστης: γενικό μήνυμα σφάλματος, καμία δρομολόγηση")
    void unknownUserIsRejected() throws SQLException {
        when(loginView.getUsername()).thenReturn("ghost");
        when(loginView.getPassword()).thenReturn("whatever");
        when(userDAO.getUserByUsername("ghost")).thenReturn(null);

        loginAction.actionPerformed(null);

        verify(loginView).setErrorMessage(contains("Λανθασμένο"));
        verify(mainFrame, never()).showCustomerDashboard(any());
        verify(auditLogDAO).logEvent(eq("ghost"), eq("LOGIN_FAILURE_UNKNOWN_USER"), anyString());
    }

    @Test
    @DisplayName("Λάθος κωδικός: αυξάνονται οι αποτυχημένες προσπάθειες")
    void wrongPasswordIncrementsFailedLogins() throws SQLException {
        when(loginView.getUsername()).thenReturn("nikos");
        when(loginView.getPassword()).thenReturn("wrong-password");
        when(userDAO.getUserByUsername("nikos")).thenReturn(user("nikos"));

        loginAction.actionPerformed(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userDAO).updateUser(saved.capture());
        assertEquals(1, saved.getValue().getFailedLogins());
        assertNull(saved.getValue().getLockedUntil());
        verify(loginView).setErrorMessage(contains("4"));
        verify(mainFrame, never()).showCustomerDashboard(any());
    }

    @Test
    @DisplayName("5η αποτυχία: ο λογαριασμός κλειδώνει για 5 λεπτά")
    void fifthFailureLocksTheAccount() throws SQLException {
        when(loginView.getUsername()).thenReturn("nikos");
        when(loginView.getPassword()).thenReturn("wrong-password");
        User u = user("nikos");
        u.setFailedLogins(4); // ήδη 4 αποτυχίες — αυτή είναι η 5η
        when(userDAO.getUserByUsername("nikos")).thenReturn(u);

        loginAction.actionPerformed(null);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userDAO).updateUser(saved.capture());
        assertEquals(5, saved.getValue().getFailedLogins());
        assertNotNull(saved.getValue().getLockedUntil(), "Έπρεπε να οριστεί lockedUntil");
        verify(auditLogDAO).logEvent(eq("nikos"), eq("ACCOUNT_LOCKOUT"), anyString());
    }

    @Test
    @DisplayName("Κλειδωμένος λογαριασμός: μπλοκάρεται πριν καν ελεγχθεί ο κωδικός")
    void lockedAccountIsBlocked() throws SQLException {
        when(loginView.getUsername()).thenReturn("nikos");
        when(loginView.getPassword()).thenReturn("password123");
        User u = user("nikos");
        u.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        when(userDAO.getUserByUsername("nikos")).thenReturn(u);

        loginAction.actionPerformed(null);

        verify(loginView).setErrorMessage(contains("κλειδωμένος"));
        verify(mainFrame, never()).showCustomerDashboard(any());
        verify(auditLogDAO).logEvent(eq("nikos"), eq("LOGIN_BLOCKED_LOCKOUT"), anyString());
    }

    @Test
    @DisplayName("Ανενεργός λογαριασμός: απορρίπτεται")
    void inactiveAccountIsRejected() throws SQLException {
        when(loginView.getUsername()).thenReturn("nikos");
        when(loginView.getPassword()).thenReturn("password123");
        User u = user("nikos");
        u.setActive(false);
        when(userDAO.getUserByUsername("nikos")).thenReturn(u);

        loginAction.actionPerformed(null);

        verify(loginView).setErrorMessage(contains("απενεργοποιημένος"));
        verify(mainFrame, never()).showCustomerDashboard(any());
    }

    @Test
    @DisplayName("Κενά πεδία: δεν γίνεται καν κλήση στη βάση")
    void emptyFieldsShortCircuit() throws SQLException {
        when(loginView.getUsername()).thenReturn("");
        when(loginView.getPassword()).thenReturn("");

        loginAction.actionPerformed(null);

        verify(loginView).setErrorMessage(contains("συμπληρώστε"));
        verify(userDAO, never()).getUserByUsername(anyString());
    }
}
