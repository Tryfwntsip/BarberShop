package com.kourio.views;

import com.kourio.models.Appointment;
import com.kourio.models.Service;
import com.kourio.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CustomerDashboardView – Καθαρή, απλή dashboard οθόνη για πελάτες.
 * Standard Swing μόνο, χωρίς custom painted assets.
 */
public class CustomerDashboardView extends JPanel {

    private final User customer;

    // Header
    private JLabel lblWelcome;
    private JButton btnLogout;

    // Booking
    private JComboBox<String> cbStylePreferences;
    private JLabel lblBarberRecommendation;
    private JButton btnUseRecommendedBarber;
    private JComboBox<User> cbBarbers;
    private JComboBox<Service> cbServices;
    private JTextField txtDate;
    private JButton btnCheckAvailability;
    private JComboBox<LocalTime> cbTimes;
    private JLabel lblPreviewPrice;
    private JLabel lblPreviewDuration;
    private JLabel lblTimesHelper;
    private JButton btnBook;

    // My appointments
    private JTable tblAppointments;
    private DefaultTableModel tblModel;
    private JButton btnCancelAppointment;

    private static final Map<String, String> EN_TO_GR = new HashMap<>();
    private static final Map<String, String> GR_TO_EN = new HashMap<>();
    static {
        EN_TO_GR.put("CONFIRMED",   "Επιβεβαιωμένο");
        EN_TO_GR.put("IN_PROGRESS", "Σε εξέλιξη");
        EN_TO_GR.put("COMPLETED",   "Ολοκληρώθηκε");
        EN_TO_GR.put("NO_SHOW",     "Δεν προσήλθε");
        EN_TO_GR.put("CANCELLED",   "Ακυρώθηκε");
        for (Map.Entry<String, String> e : EN_TO_GR.entrySet()) GR_TO_EN.put(e.getValue(), e.getKey());
    }

    public CustomerDashboardView(User customer) {
        this.customer = customer;
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_APP);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    // ─── HEADER ───────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIHelper.BG_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        lblWelcome = new JLabel("Καλωσήρθατε, " + safeName(customer));
        lblWelcome.setFont(UIHelper.FONT_HEADING);
        lblWelcome.setForeground(Color.WHITE);
        header.add(lblWelcome, BorderLayout.WEST);

        btnLogout = UIHelper.createDangerButton("Αποσύνδεση");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnLogout);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private String safeName(User u) {
        if (u == null) return "";
        String full = u.getFullName();
        return full == null ? "" : full;
    }

    // ─── BODY ─────────────────────────────────────────────────
    private JComponent buildBody() {
        JTabbedPane tabs = new JTabbedPane();
        UIHelper.styleTabbedPane(tabs);

        tabs.addTab("Νέο Ραντεβού",       wrapInScroll(buildBookingTab()));
        tabs.addTab("Τα Ραντεβού μου",    buildAppointmentsTab());
        return tabs;
    }

    private JScrollPane wrapInScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(UIHelper.BG_APP);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    // ─── TAB 1: Booking ───────────────────────────────────────
    private JComponent buildBookingTab() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UIHelper.BG_CARD);
        card.setBorder(UIHelper.cardBorder());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 6, 8, 12);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        // Heading
        JLabel h = UIHelper.createHeadingLabel("Κράτηση Νέου Ραντεβού");
        g.gridx = 0; g.gridy = row++; g.gridwidth = 2; g.insets = new Insets(0, 6, 4, 6);
        card.add(h, g);

        JLabel sub = UIHelper.createMutedLabel("Συμπληρώστε τα στοιχεία και επιβεβαιώστε.");
        g.gridy = row++; g.insets = new Insets(0, 6, 16, 6);
        card.add(sub, g);

        g.gridwidth = 1; g.insets = new Insets(8, 6, 8, 12);

        // Στυλ
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        card.add(UIHelper.createSectionLabel("Στυλ προτίμησης"), g);
        cbStylePreferences = new JComboBox<>();
        UIHelper.styleComboBox(cbStylePreferences);
        cbStylePreferences.setPreferredSize(new Dimension(320, 30));
        g.gridx = 1; g.weightx = 1;
        card.add(cbStylePreferences, g);
        row++;

        // Recommendation row
        g.gridx = 0; g.gridy = row++; g.gridwidth = 2; g.insets = new Insets(2, 6, 10, 6);
        card.add(buildRecommendationRow(), g);
        g.gridwidth = 1; g.insets = new Insets(8, 6, 8, 12);

        // Κουρέας
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        card.add(UIHelper.createSectionLabel("Κουρέας"), g);
        cbBarbers = new JComboBox<>();
        UIHelper.styleComboBox(cbBarbers);
        cbBarbers.setPreferredSize(new Dimension(320, 30));
        cbBarbers.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) setText(((User) value).getFullName());
                setBackground(isSelected ? UIHelper.SELECTION_BG : UIHelper.BG_CARD);
                setForeground(UIHelper.TEXT_PRIMARY);
                return this;
            }
        });
        g.gridx = 1; g.weightx = 1;
        card.add(cbBarbers, g);
        row++;

        // Υπηρεσία
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        card.add(UIHelper.createSectionLabel("Υπηρεσία"), g);
        cbServices = new JComboBox<>();
        UIHelper.styleComboBox(cbServices);
        cbServices.setPreferredSize(new Dimension(320, 30));
        g.gridx = 1; g.weightx = 1;
        card.add(cbServices, g);
        row++;

        // Ημερομηνία + έλεγχος
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        card.add(UIHelper.createSectionLabel("Ημερομηνία"), g);

        JPanel dateRow = new JPanel(new BorderLayout(10, 0));
        dateRow.setOpaque(false);
        txtDate = new JTextField(LocalDate.now().plusDays(1).toString(), 10);
        UIHelper.styleTextField(txtDate);
        txtDate.setPreferredSize(new Dimension(140, 30));
        dateRow.add(txtDate, BorderLayout.WEST);

        btnCheckAvailability = UIHelper.createPrimaryButton("Έλεγχος Διαθεσιμότητας");
        dateRow.add(btnCheckAvailability, BorderLayout.EAST);
        g.gridx = 1; g.weightx = 1;
        card.add(dateRow, g);
        row++;

        // Ώρα + helper
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        card.add(UIHelper.createSectionLabel("Ώρα"), g);
        JPanel timeRow = new JPanel(new BorderLayout(10, 0));
        timeRow.setOpaque(false);
        cbTimes = new JComboBox<>();
        UIHelper.styleComboBox(cbTimes);
        cbTimes.setPreferredSize(new Dimension(140, 30));
        timeRow.add(cbTimes, BorderLayout.WEST);

        lblTimesHelper = new JLabel("Πατήστε «Έλεγχος Διαθεσιμότητας» για να δείτε ώρες.");
        lblTimesHelper.setFont(UIHelper.FONT_SMALL);
        lblTimesHelper.setForeground(UIHelper.TEXT_MUTED);
        timeRow.add(lblTimesHelper, BorderLayout.CENTER);
        g.gridx = 1; g.weightx = 1;
        card.add(timeRow, g);
        row++;

        // Σύνοψη
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        card.add(UIHelper.createSectionLabel("Σύνοψη"), g);

        JPanel summary = new JPanel(new GridLayout(1, 2, 12, 0));
        summary.setOpaque(false);
        lblPreviewPrice    = new JLabel("—");
        lblPreviewDuration = new JLabel("—");
        summary.add(buildSummaryCard("Τιμή",     lblPreviewPrice,    UIHelper.PRIMARY));
        summary.add(buildSummaryCard("Διάρκεια", lblPreviewDuration, UIHelper.SUCCESS));
        g.gridx = 1; g.weightx = 1;
        card.add(summary, g);
        row++;

        // Confirm button
        btnBook = UIHelper.createSuccessButton("Επιβεβαίωση Κράτησης");
        btnBook.setEnabled(false);
        btnBook.setPreferredSize(new Dimension(220, 40));
        JPanel bookRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bookRow.setOpaque(false);
        bookRow.add(btnBook);

        g.gridx = 0; g.gridy = row; g.gridwidth = 2; g.weightx = 1; g.insets = new Insets(16, 6, 6, 6);
        card.add(bookRow, g);

        return card;
    }

    private JPanel buildRecommendationRow() {
        JPanel rec = new JPanel(new BorderLayout(12, 0));
        rec.setBackground(UIHelper.PRIMARY_SOFT);
        rec.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIHelper.BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        lblBarberRecommendation = new JLabel("Πρόταση κουρέα: -");
        lblBarberRecommendation.setFont(UIHelper.FONT_LABEL);
        lblBarberRecommendation.setForeground(UIHelper.TEXT_SECONDARY);
        rec.add(lblBarberRecommendation, BorderLayout.CENTER);

        btnUseRecommendedBarber = UIHelper.createSecondaryButton("Επιλογή Πρότασης");
        btnUseRecommendedBarber.setEnabled(false);
        rec.add(btnUseRecommendedBarber, BorderLayout.EAST);
        return rec;
    }

    private JPanel buildSummaryCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(UIHelper.BG_CARD_SOFT);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIHelper.BORDER_SOFT, 1, true),
                BorderFactory.createEmptyBorder(8, 14, 10, 14)
            )
        ));
        JLabel t = new JLabel(title);
        t.setFont(UIHelper.FONT_SMALL);
        t.setForeground(UIHelper.TEXT_SECONDARY);
        card.add(t, BorderLayout.NORTH);

        valueLabel.setFont(new Font(UIHelper.FONT_LABEL.getFamily(), Font.BOLD, 18));
        valueLabel.setForeground(accent);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ─── TAB 2: My appointments ───────────────────────────────
    private JComponent buildAppointmentsTab() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UIHelper.BG_APP);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Heading
        JPanel hb = new JPanel();
        hb.setLayout(new BoxLayout(hb, BoxLayout.Y_AXIS));
        hb.setOpaque(false);
        JLabel h = UIHelper.createHeadingLabel("Τα Ραντεβού μου");
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        hb.add(h);
        JLabel s = UIHelper.createMutedLabel("Δείτε το ιστορικό και ακυρώστε ενεργά ραντεβού.");
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        hb.add(s);
        root.add(hb, BorderLayout.NORTH);

        // Table card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(UIHelper.BG_CARD);
        tableCard.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_SOFT, 1, true));

        tblModel = new DefaultTableModel(
            new Object[]{"ID", "Ημερομηνία", "Ώρα", "Κουρέας", "Υπηρεσία", "Τιμή", "Διάρκεια", "Κατάσταση"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblAppointments = new JTable(tblModel);
        UIHelper.styleTable(tblAppointments);
        tblAppointments.setRowHeight(32);
        tblAppointments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status badges στη στήλη "Κατάσταση"
        TableCellRenderer badgeRenderer = new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                String greek = value == null ? "" : value.toString();
                String key = GR_TO_EN.getOrDefault(greek, greek);
                JComponent badge = UIHelper.createStatusBadge(key, greek);
                JPanel cell = new JPanel(new GridBagLayout());
                cell.setOpaque(true);
                cell.setBackground(isSelected ? UIHelper.SELECTION_BG : UIHelper.BG_CARD);
                cell.add(badge);
                return cell;
            }
        };
        tblAppointments.getColumnModel().getColumn(7).setCellRenderer(badgeRenderer);

        tableCard.add(UIHelper.createTableScrollPane(tblAppointments), BorderLayout.CENTER);
        root.add(tableCard, BorderLayout.CENTER);

        // Action row
        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        JLabel note = new JLabel("Επιτρέπεται ακύρωση έως 2 ώρες πριν το ραντεβού.");
        note.setFont(UIHelper.FONT_SMALL);
        note.setForeground(UIHelper.TEXT_SECONDARY);
        actionRow.add(note, BorderLayout.WEST);

        btnCancelAppointment = UIHelper.createDangerButton("Ακύρωση Επιλεγμένου Ραντεβού");
        JPanel rightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightRow.setOpaque(false);
        rightRow.add(btnCancelAppointment);
        actionRow.add(rightRow, BorderLayout.EAST);
        root.add(actionRow, BorderLayout.SOUTH);

        return root;
    }

    // ─── Public API (δεν αλλάζει — χρησιμοποιείται από CustomerController) ──
    public void setBarbers(List<User> barbers) {
        cbBarbers.removeAllItems();
        if (barbers == null) return;
        for (User b : barbers) cbBarbers.addItem(b);
    }

    public void setStylePreferences(String[] preferences) {
        cbStylePreferences.removeAllItems();
        if (preferences == null) return;
        for (String preference : preferences) cbStylePreferences.addItem(preference);
    }

    public void setServices(List<Service> services) {
        cbServices.removeAllItems();
        if (services == null) return;
        for (Service s : services) cbServices.addItem(s);
    }

    public void setAvailableTimes(List<LocalTime> times) {
        cbTimes.removeAllItems();
        if (times == null || times.isEmpty()) {
            btnBook.setEnabled(false);
            lblTimesHelper.setText("Δεν υπάρχουν διαθέσιμες ώρες – δοκιμάστε άλλη ημερομηνία.");
            lblTimesHelper.setForeground(UIHelper.DANGER_HOVER);
        } else {
            for (LocalTime t : times) cbTimes.addItem(t);
            btnBook.setEnabled(true);
            lblTimesHelper.setText("Βρέθηκαν " + times.size() + " διαθέσιμες ώρες.");
            lblTimesHelper.setForeground(UIHelper.SUCCESS);
        }
    }

    public String getSelectedStylePreference() {
        Object selected = cbStylePreferences.getSelectedItem();
        return selected == null ? "" : selected.toString();
    }

    public User getSelectedBarber()       { return (User) cbBarbers.getSelectedItem(); }
    public Service getSelectedService()   { return (Service) cbServices.getSelectedItem(); }
    public String getDateInput()          { return txtDate.getText().trim(); }
    public LocalTime getSelectedTime()    { return (LocalTime) cbTimes.getSelectedItem(); }

    public void setSelectedBarber(User barber) {
        if (barber == null) return;
        for (int i = 0; i < cbBarbers.getItemCount(); i++) {
            User item = cbBarbers.getItemAt(i);
            if (item != null && item.getId() == barber.getId()) {
                cbBarbers.setSelectedIndex(i);
                return;
            }
        }
    }

    public void setBarberRecommendation(User barber, String reason, boolean canApply) {
        String text;
        if (barber == null) {
            text = "Πρόταση κουρέα: " + (reason == null ? "-" : reason);
            lblBarberRecommendation.setForeground(UIHelper.TEXT_SECONDARY);
        } else {
            text = "Πρόταση κουρέα: " + barber.getFullName() + " – " + (reason == null ? "" : reason);
            lblBarberRecommendation.setForeground(UIHelper.TEXT_PRIMARY);
        }
        lblBarberRecommendation.setText(text);
        lblBarberRecommendation.setToolTipText(text);
        btnUseRecommendedBarber.setEnabled(canApply && barber != null);
    }

    public void updatePriceAndDurationPreview(BigDecimal price, int duration) {
        NumberFormat money = NumberFormat.getNumberInstance(new Locale("el", "GR"));
        money.setMinimumFractionDigits(2);
        money.setMaximumFractionDigits(2);
        lblPreviewPrice.setText(price == null ? "—" : "€" + money.format(price));
        lblPreviewDuration.setText(duration + " λεπτά");
    }

    public void setAppointmentsTableData(List<Appointment> list) {
        tblModel.setRowCount(0);
        if (list == null) return;
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        for (Appointment app : list) {
            tblModel.addRow(new Object[]{
                app.getId(),
                app.getAppointmentDate(),
                app.getStartTime().format(tf),
                app.getBarber() != null ? app.getBarber().getFullName() : "Unknown",
                app.getService() != null ? app.getService().getName() : "Unknown",
                "€" + app.getPriceAtBooking(),
                app.getDurationAtBooking() + " λ.",
                translateStatus(app.getStatus().name())
            });
        }
    }

    public Appointment getSelectedAppointment(List<Appointment> list) {
        if (list == null) return null;
        int row = tblAppointments.getSelectedRow();
        if (row != -1) {
            int id = (Integer) tblModel.getValueAt(row, 0);
            for (Appointment app : list) if (app.getId() == id) return app;
        }
        return null;
    }

    private static String translateStatus(String status) {
        String gr = EN_TO_GR.get(status);
        return gr == null ? status : gr;
    }

    // Listeners
    public void addLogoutListener(ActionListener l)              { btnLogout.addActionListener(l); }
    public void addStylePreferenceListener(ActionListener l)     { cbStylePreferences.addActionListener(l); }
    public void addUseRecommendedBarberListener(ActionListener l){ btnUseRecommendedBarber.addActionListener(l); }
    public void addCheckAvailabilityListener(ActionListener l)   { btnCheckAvailability.addActionListener(l); }
    public void addServiceSelectionListener(ActionListener l)    { cbServices.addActionListener(l); }
    public void addBookListener(ActionListener l)                { btnBook.addActionListener(l); }
    public void addCancelAppointmentListener(ActionListener l)   { btnCancelAppointment.addActionListener(l); }
}
