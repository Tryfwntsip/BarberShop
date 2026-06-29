package com.kourio.views;

import com.kourio.models.Service;
import com.kourio.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OwnerDashboardView extends JPanel {
    private final User owner;

    // Header
    private JLabel lblWelcome;
    private JButton btnLogout;

    // Tab 1: Services
    private JTable tblServices;
    private DefaultTableModel tblServicesModel;
    private JTextField txtServiceName;
    private JTextField txtServicePrice;
    private JTextField txtServiceDuration;
    private JTextField txtServiceCategory;
    private JCheckBox chkServiceActive;
    private JButton btnAddService;
    private JButton btnUpdateService;
    private JButton btnDeactivateService;
    private JButton btnHardDeleteService;

    // Tab 2: Barbers
    private JTable tblBarbers;
    private DefaultTableModel tblBarbersModel;
    private JTextField txtBarberUsername;
    private JPasswordField txtBarberPassword;
    private JTextField txtBarberFName;
    private JTextField txtBarberLName;
    private JTextField txtBarberPhone;
    private JTextField txtBarberEmail;
    private JTextField txtBarberStartHour;
    private JTextField txtBarberEndHour;
    private JTextField txtBarberSpecialties;
    private JCheckBox chkBarberActive;
    private JButton btnAddBarber;
    private JButton btnUpdateBarber;
    private JButton btnResetBarberPassword;
    private JButton btnHardDeleteBarber;

    // Tab 3: Statistics
    private JTextField txtStatsStart;
    private JTextField txtStatsEnd;
    private JComboBox<User> cbStatsBarber;
    private JComboBox<Service> cbStatsService;
    private JButton btnGenerateStats;
    private JButton btnExportCSV;

    private JLabel lblTotalBookings;
    private JLabel lblCompletedBookings;
    private JLabel lblNoShowBookings;
    private JLabel lblTotalRevenue;
    private JLabel lblAvgRevenue;
    private JLabel lblNoShowRate;
    private JLabel lblStatsStatus;

    public OwnerDashboardView(User owner) {
        this.owner = owner;
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_APP);

        createHeaderPanel();
        createTabbedPane();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.BG_HEADER_OWNER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        lblWelcome = new JLabel("Διαχειριστής (Owner): " + owner.getFullName());
        lblWelcome.setFont(UIHelper.FONT_HEADING);
        lblWelcome.setForeground(Color.WHITE);
        headerPanel.add(lblWelcome, BorderLayout.WEST);

        btnLogout = UIHelper.createDangerButton("Αποσύνδεση");
        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightWrap.setOpaque(false);
        rightWrap.add(btnLogout);
        headerPanel.add(rightWrap, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        UIHelper.styleTabbedPane(tabbedPane);

        tabbedPane.addTab("Υπηρεσίες", buildServicesPanel());
        tabbedPane.addTab("Προσωπικό (Κουρείς)", buildStaffPanel());
        tabbedPane.addTab("Αναφορές & Στατιστικά", buildReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel buildServicesPanel() {
        JPanel servicesPanel = new JPanel(new BorderLayout(15, 15));
        servicesPanel.setBackground(UIHelper.BG_CARD);
        servicesPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        tblServicesModel = new DefaultTableModel(
            new Object[]{"ID", "Όνομα Υπηρεσίας", "Τιμή (€)", "Διάρκεια (λ.)", "Κατηγορία", "Ενεργή"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblServices = new JTable(tblServicesModel);
        UIHelper.styleTable(tblServices);
        tblServices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel servicesTableCard = new JPanel(new BorderLayout());
        servicesTableCard.setBackground(UIHelper.BG_CARD);
        servicesTableCard.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_SOFT, 1, true));
        servicesTableCard.add(UIHelper.createTableScrollPane(tblServices), BorderLayout.CENTER);
        servicesPanel.add(servicesTableCard, BorderLayout.CENTER);

        JPanel serviceForm = new JPanel(new GridBagLayout());
        serviceForm.setBackground(UIHelper.BG_CARD);
        serviceForm.setBorder(UIHelper.cardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.insets = new Insets(0, 8, 12, 8);
        serviceForm.add(UIHelper.createHeadingLabel("Στοιχεία Υπηρεσίας"), gbc);
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 8, 6, 8);

        gbc.gridx = 0; gbc.gridy = 1; serviceForm.add(UIHelper.createSectionLabel("Όνομα:"), gbc);
        txtServiceName = new JTextField(15); UIHelper.styleTextField(txtServiceName);
        gbc.gridx = 1; serviceForm.add(txtServiceName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; serviceForm.add(UIHelper.createSectionLabel("Τιμή (€):"), gbc);
        txtServicePrice = new JTextField(10); UIHelper.styleTextField(txtServicePrice);
        gbc.gridx = 1; serviceForm.add(txtServicePrice, gbc);

        gbc.gridx = 0; gbc.gridy = 3; serviceForm.add(UIHelper.createSectionLabel("Διάρκεια (λ.):"), gbc);
        txtServiceDuration = new JTextField(10); UIHelper.styleTextField(txtServiceDuration);
        gbc.gridx = 1; serviceForm.add(txtServiceDuration, gbc);

        gbc.gridx = 0; gbc.gridy = 4; serviceForm.add(UIHelper.createSectionLabel("Κατηγορία:"), gbc);
        txtServiceCategory = new JTextField(15); UIHelper.styleTextField(txtServiceCategory);
        gbc.gridx = 1; serviceForm.add(txtServiceCategory, gbc);

        gbc.gridx = 0; gbc.gridy = 5; serviceForm.add(UIHelper.createSectionLabel("Ενεργή:"), gbc);
        chkServiceActive = new JCheckBox("", true);
        UIHelper.styleCheckBox(chkServiceActive);
        gbc.gridx = 1; serviceForm.add(chkServiceActive, gbc);

        JPanel serviceButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        serviceButtons.setOpaque(false);
        btnAddService = UIHelper.createSuccessButton("Προσθήκη");
        btnUpdateService = UIHelper.createPrimaryButton("Ενημέρωση");
        btnDeactivateService = UIHelper.createNeutralButton("Απενεργοποίηση");
        btnHardDeleteService = UIHelper.createDangerButton("Οριστική Διαγραφή");
        serviceButtons.add(btnAddService);
        serviceButtons.add(btnUpdateService);
        serviceButtons.add(btnDeactivateService);
        serviceButtons.add(btnHardDeleteService);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 8, 8, 8);
        serviceForm.add(serviceButtons, gbc);

        servicesPanel.add(serviceForm, BorderLayout.SOUTH);
        return servicesPanel;
    }

    private JPanel buildStaffPanel() {
        JPanel staffPanel = new JPanel(new BorderLayout(15, 15));
        staffPanel.setBackground(UIHelper.BG_CARD);
        staffPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        tblBarbersModel = new DefaultTableModel(
            new Object[]{"ID", "Username", "Όνομα", "Επώνυμο", "Τηλέφωνο", "Email", "Ενεργός"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblBarbers = new JTable(tblBarbersModel);
        UIHelper.styleTable(tblBarbers);
        tblBarbers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel barbersTableCard = new JPanel(new BorderLayout());
        barbersTableCard.setBackground(UIHelper.BG_CARD);
        barbersTableCard.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_SOFT, 1, true));
        barbersTableCard.add(UIHelper.createTableScrollPane(tblBarbers), BorderLayout.CENTER);
        staffPanel.add(barbersTableCard, BorderLayout.CENTER);

        JPanel barberForm = new JPanel(new GridBagLayout());
        barberForm.setBackground(UIHelper.BG_CARD);
        barberForm.setBorder(UIHelper.cardBorder());
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.insets = new Insets(5, 6, 5, 6);
        gbcB.anchor = GridBagConstraints.WEST;
        gbcB.fill = GridBagConstraints.HORIZONTAL;

        gbcB.gridx = 0; gbcB.gridy = 0; gbcB.gridwidth = 4; gbcB.insets = new Insets(0, 6, 12, 6);
        barberForm.add(UIHelper.createHeadingLabel("Στοιχεία Κουρέα"), gbcB);
        gbcB.gridwidth = 1; gbcB.insets = new Insets(5, 6, 5, 6);

        gbcB.gridx = 0; gbcB.gridy = 1; barberForm.add(UIHelper.createSectionLabel("Username:"), gbcB);
        txtBarberUsername = new JTextField(12); UIHelper.styleTextField(txtBarberUsername);
        gbcB.gridx = 1; barberForm.add(txtBarberUsername, gbcB);

        gbcB.gridx = 0; gbcB.gridy = 2; barberForm.add(UIHelper.createSectionLabel("Κωδικός:"), gbcB);
        txtBarberPassword = new JPasswordField(12); UIHelper.styleTextField(txtBarberPassword);
        gbcB.gridx = 1; barberForm.add(txtBarberPassword, gbcB);

        gbcB.gridx = 0; gbcB.gridy = 3; barberForm.add(UIHelper.createSectionLabel("Όνομα:"), gbcB);
        txtBarberFName = new JTextField(12); UIHelper.styleTextField(txtBarberFName);
        gbcB.gridx = 1; barberForm.add(txtBarberFName, gbcB);

        gbcB.gridx = 0; gbcB.gridy = 4; barberForm.add(UIHelper.createSectionLabel("Επώνυμο:"), gbcB);
        txtBarberLName = new JTextField(12); UIHelper.styleTextField(txtBarberLName);
        gbcB.gridx = 1; barberForm.add(txtBarberLName, gbcB);

        gbcB.gridx = 2; gbcB.gridy = 1; barberForm.add(UIHelper.createSectionLabel("Τηλέφωνο:"), gbcB);
        txtBarberPhone = new JTextField(12); UIHelper.styleTextField(txtBarberPhone);
        gbcB.gridx = 3; barberForm.add(txtBarberPhone, gbcB);

        gbcB.gridx = 2; gbcB.gridy = 2; barberForm.add(UIHelper.createSectionLabel("Email:"), gbcB);
        txtBarberEmail = new JTextField(12); UIHelper.styleTextField(txtBarberEmail);
        gbcB.gridx = 3; barberForm.add(txtBarberEmail, gbcB);

        gbcB.gridx = 2; gbcB.gridy = 3; barberForm.add(UIHelper.createSectionLabel("Ωράριο Έναρξης (HH:MM):"), gbcB);
        txtBarberStartHour = new JTextField("09:00", 6); UIHelper.styleTextField(txtBarberStartHour);
        gbcB.gridx = 3; barberForm.add(txtBarberStartHour, gbcB);

        gbcB.gridx = 2; gbcB.gridy = 4; barberForm.add(UIHelper.createSectionLabel("Ωράριο Λήξης (HH:MM):"), gbcB);
        txtBarberEndHour = new JTextField("17:00", 6); UIHelper.styleTextField(txtBarberEndHour);
        gbcB.gridx = 3; barberForm.add(txtBarberEndHour, gbcB);

        gbcB.gridx = 0; gbcB.gridy = 5; barberForm.add(UIHelper.createSectionLabel("Ειδικότητες:"), gbcB);
        txtBarberSpecialties = new JTextField(20); UIHelper.styleTextField(txtBarberSpecialties);
        gbcB.gridx = 1; gbcB.gridwidth = 3; barberForm.add(txtBarberSpecialties, gbcB);

        gbcB.gridwidth = 1;
        gbcB.gridx = 0; gbcB.gridy = 6; barberForm.add(UIHelper.createSectionLabel("Ενεργός:"), gbcB);
        chkBarberActive = new JCheckBox("", true); UIHelper.styleCheckBox(chkBarberActive);
        gbcB.gridx = 1; barberForm.add(chkBarberActive, gbcB);

        JPanel barberButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        barberButtons.setOpaque(false);
        btnAddBarber = UIHelper.createSuccessButton("Προσθήκη");
        btnUpdateBarber = UIHelper.createPrimaryButton("Ενημέρωση");
        btnResetBarberPassword = UIHelper.createNeutralButton("Reset Password");
        btnHardDeleteBarber = UIHelper.createDangerButton("Οριστική Διαγραφή");
        barberButtons.add(btnAddBarber);
        barberButtons.add(btnUpdateBarber);
        barberButtons.add(btnResetBarberPassword);
        barberButtons.add(btnHardDeleteBarber);

        gbcB.gridx = 0; gbcB.gridy = 7; gbcB.gridwidth = 4; gbcB.anchor = GridBagConstraints.CENTER;
        gbcB.insets = new Insets(14, 6, 8, 6);
        barberForm.add(barberButtons, gbcB);

        staffPanel.add(barberForm, BorderLayout.SOUTH);
        return staffPanel;
    }

    private JPanel buildReportsPanel() {
        JPanel reportsPanel = new JPanel(new BorderLayout(15, 15));
        reportsPanel.setBackground(UIHelper.BG_CARD);
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Filter Bar
        JPanel statsFilterBar = new JPanel(new GridLayout(2, 1, 0, 6));
        statsFilterBar.setBackground(UIHelper.BG_CARD);
        statsFilterBar.setBorder(UIHelper.compactCardBorder());

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row1.setOpaque(false);
        row1.add(UIHelper.createSectionLabel("Έναρξη (YYYY-MM-DD):"));
        txtStatsStart = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 9);
        UIHelper.styleTextField(txtStatsStart);
        row1.add(txtStatsStart);

        row1.add(UIHelper.createSectionLabel("Λήξη (YYYY-MM-DD):"));
        txtStatsEnd = new JTextField(LocalDate.now().toString(), 9);
        UIHelper.styleTextField(txtStatsEnd);
        row1.add(txtStatsEnd);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row2.setOpaque(false);
        row2.add(UIHelper.createSectionLabel("Κουρέας:"));
        cbStatsBarber = new JComboBox<>();
        UIHelper.styleComboBox(cbStatsBarber);
        row2.add(cbStatsBarber);

        row2.add(UIHelper.createSectionLabel("Υπηρεσία:"));
        cbStatsService = new JComboBox<>();
        UIHelper.styleComboBox(cbStatsService);
        row2.add(cbStatsService);

        btnGenerateStats = UIHelper.createPrimaryButton("Υπολογισμός");
        row2.add(btnGenerateStats);

        btnExportCSV = UIHelper.createSecondaryButton("Εξαγωγή CSV");
        row2.add(btnExportCSV);

        statsFilterBar.add(row1);
        statsFilterBar.add(row2);

        reportsPanel.add(statsFilterBar, BorderLayout.NORTH);

        // Stats cards container
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(18, 6, 6, 6));

        JPanel cardsContainer = new JPanel(new GridLayout(2, 3, 18, 18));
        cardsContainer.setOpaque(false);

        lblTotalBookings     = createStatCard("Συνολικά Ραντεβού", "0", UIHelper.PRIMARY, cardsContainer);
        lblCompletedBookings = createStatCard("Ολοκληρωμένα",       "0", UIHelper.SUCCESS, cardsContainer);
        lblNoShowBookings    = createStatCard("No-Show",            "0", UIHelper.WARNING, cardsContainer);
        lblTotalRevenue      = createStatCard("Συνολικά Έσοδα",     "€0,00", UIHelper.INFO, cardsContainer);
        lblAvgRevenue        = createStatCard("Μέσο Έσοδο / Ραντεβού", "€0,00", UIHelper.TEXT_SECONDARY, cardsContainer);
        lblNoShowRate        = createStatCard("Ποσοστό No-Show",    "0,0%", UIHelper.DANGER, cardsContainer);

        center.add(cardsContainer, BorderLayout.CENTER);

        lblStatsStatus = new JLabel(" ");
        lblStatsStatus.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatsStatus.setFont(UIHelper.FONT_SMALL);
        lblStatsStatus.setForeground(UIHelper.TEXT_SECONDARY);
        center.add(lblStatsStatus, BorderLayout.SOUTH);

        reportsPanel.add(center, BorderLayout.CENTER);
        return reportsPanel;
    }

    private JLabel createStatCard(String title, String val, Color accent, JPanel container) {
        JPanel card = new JPanel(new BorderLayout(4, 8));
        card.setBackground(UIHelper.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_SOFT, 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
            )
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(UIHelper.FONT_LABEL.getFamily(), Font.PLAIN, 12));
        lblTitle.setForeground(UIHelper.TEXT_SECONDARY);
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblVal = new JLabel(val);
        lblVal.setFont(UIHelper.FONT_STAT_BIG);
        lblVal.setForeground(accent);
        card.add(lblVal, BorderLayout.CENTER);

        container.add(card);
        return lblVal;
    }

    // ===== Services CRUD =====
    public void setServicesTableData(List<Service> list) {
        tblServicesModel.setRowCount(0);
        for (Service s : list) {
            tblServicesModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getPrice(), s.getDurationMinutes(),
                s.getCategory(), s.isActive() ? "Ναι" : "Όχι"
            });
        }
    }

    public Service getSelectedService(List<Service> list) {
        int row = tblServices.getSelectedRow();
        if (row != -1) {
            int id = (Integer) tblServicesModel.getValueAt(row, 0);
            for (Service s : list) if (s.getId() == id) return s;
        }
        return null;
    }

    public void fillServiceFields(Service s) {
        if (s != null) {
            txtServiceName.setText(s.getName());
            txtServicePrice.setText(s.getPrice().toString());
            txtServiceDuration.setText(String.valueOf(s.getDurationMinutes()));
            txtServiceCategory.setText(s.getCategory());
            chkServiceActive.setSelected(s.isActive());
        }
    }

    public String getServiceNameInput()     { return txtServiceName.getText().trim(); }
    public String getServicePriceInput()    { return txtServicePrice.getText().trim(); }
    public String getServiceDurationInput() { return txtServiceDuration.getText().trim(); }
    public String getServiceCategoryInput() { return txtServiceCategory.getText().trim(); }
    public boolean getServiceActiveInput()  { return chkServiceActive.isSelected(); }

    // ===== Barbers CRUD =====
    public void setBarbersTableData(List<User> list) {
        tblBarbersModel.setRowCount(0);
        for (User u : list) {
            tblBarbersModel.addRow(new Object[]{
                u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(),
                u.getPhone(), u.getEmail(), u.isActive() ? "Ναι" : "Όχι"
            });
        }
    }

    public User getSelectedBarber(List<User> list) {
        int row = tblBarbers.getSelectedRow();
        if (row != -1) {
            int id = (Integer) tblBarbersModel.getValueAt(row, 0);
            for (User u : list) if (u.getId() == id) return u;
        }
        return null;
    }

    public void fillBarberFields(User u, String specialties, String startH, String endH) {
        if (u != null) {
            txtBarberUsername.setText(u.getUsername());
            txtBarberPassword.setText("");
            txtBarberFName.setText(u.getFirstName());
            txtBarberLName.setText(u.getLastName());
            txtBarberPhone.setText(u.getPhone());
            txtBarberEmail.setText(u.getEmail());
            txtBarberStartHour.setText(startH);
            txtBarberEndHour.setText(endH);
            txtBarberSpecialties.setText(specialties);
            chkBarberActive.setSelected(u.isActive());
        }
    }

    public String getBarberUsernameInput()    { return txtBarberUsername.getText().trim(); }
    public String getBarberPasswordInput()    { return new String(txtBarberPassword.getPassword()); }
    public String getBarberFNameInput()       { return txtBarberFName.getText().trim(); }
    public String getBarberLNameInput()       { return txtBarberLName.getText().trim(); }
    public String getBarberPhoneInput()       { return txtBarberPhone.getText().trim(); }
    public String getBarberEmailInput()       { return txtBarberEmail.getText().trim(); }
    public String getBarberStartHourInput()   { return txtBarberStartHour.getText().trim(); }
    public String getBarberEndHourInput()     { return txtBarberEndHour.getText().trim(); }
    public String getBarberSpecialtiesInput() { return txtBarberSpecialties.getText().trim(); }
    public boolean getBarberActiveInput()     { return chkBarberActive.isSelected(); }

    // ===== Statistics =====
    public String getStatsStartInput() { return txtStatsStart.getText().trim(); }
    public String getStatsEndInput()   { return txtStatsEnd.getText().trim(); }

    public void setStatsBarbersFilter(List<User> list) {
        cbStatsBarber.removeAllItems();
        User allUser = new User();
        allUser.setId(-1);
        allUser.setFirstName("Όλοι");
        allUser.setLastName("οι Κουρείς");
        cbStatsBarber.addItem(allUser);
        for (User u : list) cbStatsBarber.addItem(u);
    }

    public void setStatsServicesFilter(List<Service> list) {
        cbStatsService.removeAllItems();
        Service allServ = new Service();
        allServ.setId(-1);
        allServ.setName("Όλες οι Υπηρεσίες");
        cbStatsService.addItem(allServ);
        for (Service s : list) cbStatsService.addItem(s);
    }

    public User getStatsSelectedBarber() { return (User) cbStatsBarber.getSelectedItem(); }
    public Service getStatsSelectedService() { return (Service) cbStatsService.getSelectedItem(); }

    /**
     * Ενημερώνει με ασφάλεια όλα τα stat cards. Χειρίζεται null, integers, BigDecimal, Double.
     */
    public void displayStatistics(Map<String, Object> stats) {
        if (stats == null || stats.isEmpty()) {
            lblTotalBookings.setText("0");
            lblCompletedBookings.setText("0");
            lblNoShowBookings.setText("0");
            lblTotalRevenue.setText("€0,00");
            lblAvgRevenue.setText("€0,00");
            lblNoShowRate.setText("0,0%");
            setStatsStatus("Δεν βρέθηκαν δεδομένα για το επιλεγμένο εύρος.", new Color(180, 110, 30));
            return;
        }

        int totalBookings     = toInt(stats.get("totalBookings"));
        int completedBookings = toInt(stats.get("completedBookings"));
        int noShowBookings    = toInt(stats.get("noShowBookings"));
        BigDecimal totalRev   = toBigDecimal(stats.get("totalRevenue"));
        BigDecimal avgRev     = toBigDecimal(stats.get("avgRevenue"));
        double noShowRate     = toDouble(stats.get("noShowRate"));

        NumberFormat money = NumberFormat.getNumberInstance(new Locale("el", "GR"));
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);

        lblTotalBookings.setText(String.valueOf(totalBookings));
        lblCompletedBookings.setText(String.valueOf(completedBookings));
        lblNoShowBookings.setText(String.valueOf(noShowBookings));
        lblTotalRevenue.setText("€" + money.format(totalRev));
        lblAvgRevenue.setText("€" + money.format(avgRev));
        lblNoShowRate.setText(String.format(new Locale("el", "GR"), "%.1f%%", noShowRate));

        if (totalBookings == 0) {
            setStatsStatus("Δεν βρέθηκαν ραντεβού στο επιλεγμένο διάστημα.", new Color(180, 110, 30));
        } else {
            setStatsStatus("Υπολογίστηκαν στατιστικά για " + totalBookings + " ραντεβού.", UIHelper.SUCCESS);
        }
    }

    public void setStatsStatus(String msg, Color color) {
        if (lblStatsStatus != null) {
            lblStatsStatus.setText(msg == null ? " " : msg);
            lblStatsStatus.setForeground(color == null ? UIHelper.TEXT_SECONDARY : color);
        }
    }

    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0.0; }
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    public JTable getServicesTable() { return tblServices; }
    public JTable getBarbersTable()  { return tblBarbers; }

    // ===== Listeners =====
    public void addLogoutListener(ActionListener l) { btnLogout.addActionListener(l); }

    public void addAddServiceListener(ActionListener l)        { btnAddService.addActionListener(l); }
    public void addUpdateServiceListener(ActionListener l)     { btnUpdateService.addActionListener(l); }
    public void addDeactivateServiceListener(ActionListener l) { btnDeactivateService.addActionListener(l); }

    public void addAddBarberListener(ActionListener l)           { btnAddBarber.addActionListener(l); }
    public void addUpdateBarberListener(ActionListener l)        { btnUpdateBarber.addActionListener(l); }
    public void addResetBarberPasswordListener(ActionListener l) { btnResetBarberPassword.addActionListener(l); }

    public void addHardDeleteServiceListener(ActionListener l) { btnHardDeleteService.addActionListener(l); }
    public void addHardDeleteBarberListener(ActionListener l)  { btnHardDeleteBarber.addActionListener(l); }

    public void addGenerateStatsListener(ActionListener l) { btnGenerateStats.addActionListener(l); }
    public void addExportCSVListener(ActionListener l)     { btnExportCSV.addActionListener(l); }
}
