package com.kourio.views;

import com.kourio.models.User;
import com.kourio.controllers.LoginController;
import com.kourio.controllers.CustomerController;
import com.kourio.controllers.BarberController;
import com.kourio.controllers.OwnerController;
import com.kourio.daos.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * MainFrame – Απλό window shell. Standard Swing μόνο.
 *
 *  - Επαγγελματικός τίτλος
 *  - Καλαίσθητο default μέγεθος, minimum size και κεντράρισμα στην οθόνη
 *  - Καθαρό app background μέσω UIHelper
 *  - Καθολικό L&F bootstrap (Nimbus → System fallback)
 *
 * Η navigation (CardLayout) διατηρείται όπως πριν.
 */
public class MainFrame extends JFrame {

    private static final String APP_TITLE = "Barber – Σύστημα Διαχείρισης Ραντεβού";

    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    public MainFrame() {
        super(APP_TITLE);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 720);
        setMinimumSize(new Dimension(920, 640));

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(UIHelper.BG_APP);
        setContentPane(mainContainer);

        setLocationRelativeTo(null);
    }

    public void init() {
        showLogin();
        setVisible(true);
    }

    public void showLogin() {
        LoginView loginView = new LoginView();
        new LoginController(this, loginView);
        swap(loginView, "LOGIN");
    }

    public void showCustomerDashboard(User customer) {
        CustomerDashboardView view = new CustomerDashboardView(customer);
        new CustomerController(this, view, customer);
        swap(view, "CUSTOMER");
    }

    public void showBarberDashboard(User barber) {
        BarberDashboardView view = new BarberDashboardView(barber);
        new BarberController(this, view, barber);
        swap(view, "BARBER");
    }

    public void showOwnerDashboard(User owner) {
        OwnerDashboardView view = new OwnerDashboardView(owner);
        new OwnerController(this, view, owner);
        swap(view, "OWNER");
    }

    private void swap(JPanel view, String name) {
        mainContainer.add(view, name);
        cardLayout.show(mainContainer, name);
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    public static void main(String[] args) {
        UIHelper.applyGlobalLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                DBConnection.getConnection().close();
            } catch (SQLException ex) {
                System.err.println("Προειδοποίηση: Αδυναμία σύνδεσης στη βάση δεδομένων: " + ex.getMessage());
            }
            new MainFrame().init();
        });
    }
}
