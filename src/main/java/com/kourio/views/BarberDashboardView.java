package com.kourio.views;

import com.kourio.models.Appointment;
import com.kourio.models.AppointmentStatus;
import com.kourio.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarberDashboardView extends JPanel {
    private static final Map<String, String> GR_TO_EN = new HashMap<>();
    static {
        GR_TO_EN.put("Επιβεβαιωμένο", "CONFIRMED");
        GR_TO_EN.put("Σε εξέλιξη",    "IN_PROGRESS");
        GR_TO_EN.put("Ολοκληρώθηκε",  "COMPLETED");
        GR_TO_EN.put("Δεν προσήλθε",  "NO_SHOW");
        GR_TO_EN.put("Ακυρώθηκε",     "CANCELLED");
    }

    private final User barber;

    // Header
    private JLabel lblWelcome;
    private JButton btnLogout;

    // Tab 1: Daily Schedule
    private JTextField txtScheduleDate;
    private JButton btnLoadSchedule;
    private JTable tblSchedule;
    private DefaultTableModel tblScheduleModel;

    private JComboBox<AppointmentStatus> cbStatuses;
    private JTextField txtNotes;
    private JButton btnUpdateStatus;

    // Tab 2: History Search
    private JTextField txtSearchQuery;
    private JButton btnSearch;
    private JTable tblHistory;
    private DefaultTableModel tblHistoryModel;

    // Tab 3: Monthly Schedule
    private JComboBox<Integer> cbMonth;
    private JComboBox<Integer> cbYear;
    private JButton btnLoadMonth;
    private JTable tblMonth;
    private DefaultTableModel tblMonthModel;

    public BarberDashboardView(User barber) {
        this.barber = barber;
        setLayout(new BorderLayout());
        setBackground(UIHelper.BG_APP);

        createHeaderPanel();
        createTabbedPane();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIHelper.BG_HEADER_BARBER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        lblWelcome = new JLabel("Κουρέας: " + barber.getFullName());
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

        // TAB 1: Schedule Panel
        JPanel schedulePanel = new JPanel(new BorderLayout(15, 15));
        schedulePanel.setBackground(UIHelper.BG_APP);
        schedulePanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setBackground(UIHelper.BG_CARD);
        filterRow.setBorder(UIHelper.compactCardBorder());
        JLabel dateLbl = new JLabel("Ημερομηνία (YYYY-MM-DD):");
        dateLbl.setFont(UIHelper.FONT_SUBHEAD);
        dateLbl.setForeground(UIHelper.TEXT_SECONDARY);
        filterRow.add(dateLbl);
        txtScheduleDate = new JTextField(LocalDate.now().toString(), 10);
        UIHelper.styleTextField(txtScheduleDate);
        filterRow.add(txtScheduleDate);
        btnLoadSchedule = UIHelper.createPrimaryButton("Προβολή Προγράμματος");
        filterRow.add(btnLoadSchedule);
        schedulePanel.add(filterRow, BorderLayout.NORTH);

        tblScheduleModel = new DefaultTableModel(
            new Object[]{"ID", "Ώρα", "Πελάτης", "Τηλέφωνο", "Υπηρεσία", "Διάρκεια", "Κατάσταση", "Σημειώσεις"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblSchedule = new EmptyStateTable(tblScheduleModel, "Δεν υπάρχουν ραντεβού για την επιλεγμένη ημερομηνία.");
        UIHelper.styleTable(tblSchedule);
        tblSchedule.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        installStatusBadge(tblSchedule, 6);
        schedulePanel.add(createTableCard(tblSchedule), BorderLayout.CENTER);

        // Status update panel
        JPanel updatePanel = new JPanel(new GridBagLayout());
        updatePanel.setBackground(UIHelper.BG_CARD);
        updatePanel.setBorder(UIHelper.cardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel updateTitle = UIHelper.createHeadingLabel("Ενημέρωση Κατάστασης Ραντεβού");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 10, 8);
        updatePanel.add(updateTitle, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.gridx = 0; gbc.gridy = 1;
        updatePanel.add(UIHelper.createSectionLabel("Νέα Κατάσταση:"), gbc);
        cbStatuses = new JComboBox<>(new AppointmentStatus[]{
            AppointmentStatus.IN_PROGRESS,
            AppointmentStatus.COMPLETED,
            AppointmentStatus.NO_SHOW,
            AppointmentStatus.CANCELLED
        });
        UIHelper.styleComboBox(cbStatuses);
        gbc.gridx = 1;
        gbc.weightx = 1;
        updatePanel.add(cbStatuses, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0;
        updatePanel.add(UIHelper.createSectionLabel("Σχόλια/Σημειώσεις:"), gbc);
        txtNotes = new JTextField(30);
        UIHelper.styleTextField(txtNotes);
        gbc.gridx = 1;
        gbc.weightx = 1;
        updatePanel.add(txtNotes, gbc);

        btnUpdateStatus = UIHelper.createPrimaryButton("Αποθήκευση Αλλαγής");
        JPanel updateButtonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        updateButtonRow.setOpaque(false);
        updateButtonRow.add(btnUpdateStatus);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 8, 0, 8);
        updatePanel.add(updateButtonRow, gbc);

        schedulePanel.add(updatePanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Ημερήσιο Πρόγραμμα", schedulePanel);

        // TAB 2: Search Customer History
        JPanel historyPanel = new JPanel(new BorderLayout(15, 15));
        historyPanel.setBackground(UIHelper.BG_APP);
        historyPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setBackground(UIHelper.BG_CARD);
        searchRow.setBorder(UIHelper.compactCardBorder());
        JLabel sLbl = new JLabel("Αναζήτηση (Όνομα/Τηλέφωνο):");
        sLbl.setFont(UIHelper.FONT_SUBHEAD);
        sLbl.setForeground(UIHelper.TEXT_SECONDARY);
        searchRow.add(sLbl);
        txtSearchQuery = new JTextField(25);
        UIHelper.styleTextField(txtSearchQuery);
        searchRow.add(txtSearchQuery);
        btnSearch = UIHelper.createPrimaryButton("Αναζήτηση");
        searchRow.add(btnSearch);
        historyPanel.add(searchRow, BorderLayout.NORTH);

        tblHistoryModel = new DefaultTableModel(
            new Object[]{"ID", "Ημερομηνία", "Ώρα", "Πελάτης", "Τηλέφωνο", "Υπηρεσία", "Τιμή", "Κατάσταση", "Σχόλια Κουρέα"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblHistory = new EmptyStateTable(tblHistoryModel, "Δεν βρέθηκαν αποτελέσματα ιστορικού.");
        UIHelper.styleTable(tblHistory);
        installStatusBadge(tblHistory, 7);
        historyPanel.add(createTableCard(tblHistory), BorderLayout.CENTER);

        tabbedPane.addTab("Ιστορικό Πελατών", historyPanel);

        // TAB 3: Monthly Schedule
        JPanel monthPanel = new JPanel(new BorderLayout(15, 15));
        monthPanel.setBackground(UIHelper.BG_APP);
        monthPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 18, 18));

        JPanel monthFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        monthFilterRow.setBackground(UIHelper.BG_CARD);
        monthFilterRow.setBorder(UIHelper.compactCardBorder());
        JLabel mLbl = new JLabel("Μήνας:");
        mLbl.setFont(UIHelper.FONT_SUBHEAD);
        mLbl.setForeground(UIHelper.TEXT_SECONDARY);
        monthFilterRow.add(mLbl);
        cbMonth = new JComboBox<>(new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12});
        UIHelper.styleComboBox(cbMonth);
        cbMonth.setSelectedItem(LocalDate.now().getMonthValue());
        monthFilterRow.add(cbMonth);

        JLabel yLbl = new JLabel("Έτος:");
        yLbl.setFont(UIHelper.FONT_SUBHEAD);
        yLbl.setForeground(UIHelper.TEXT_SECONDARY);
        monthFilterRow.add(yLbl);
        cbYear = new JComboBox<>(new Integer[]{LocalDate.now().getYear(), LocalDate.now().getYear()+1});
        UIHelper.styleComboBox(cbYear);
        monthFilterRow.add(cbYear);

        btnLoadMonth = UIHelper.createPrimaryButton("Προβολή Μήνα");
        monthFilterRow.add(btnLoadMonth);
        monthPanel.add(monthFilterRow, BorderLayout.NORTH);

        tblMonthModel = new DefaultTableModel(
            new Object[]{"Ημερομηνία", "Ώρα", "Πελάτης", "Τηλέφωνο", "Υπηρεσία", "Κατάσταση"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblMonth = new EmptyStateTable(tblMonthModel, "Δεν υπάρχουν ραντεβού για τον επιλεγμένο μήνα.");
        UIHelper.styleTable(tblMonth);
        installStatusBadge(tblMonth, 5);
        monthPanel.add(createTableCard(tblMonth), BorderLayout.CENTER);

        tabbedPane.addTab("Μηνιαίο Πρόγραμμα", monthPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /** Renders a status column with the same colored badges used in the customer view. */
    private void installStatusBadge(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new TableCellRenderer() {
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
        });
    }

    private JComponent createTableCard(JTable table) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIHelper.BG_CARD);
        card.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_SOFT, 1, true));

        JScrollPane sp = new JScrollPane(table);
        card.add(UIHelper.createTableScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    public String getScheduleDateInput() { return txtScheduleDate.getText().trim(); }
    public String getSearchQuery() { return txtSearchQuery.getText().trim(); }
    public AppointmentStatus getSelectedStatus() { return (AppointmentStatus) cbStatuses.getSelectedItem(); }
    public String getNotesInput() { return txtNotes.getText().trim(); }
    public void setNotesInput(String val) { txtNotes.setText(val); }

    public Appointment getSelectedAppointment(List<Appointment> list) {
        int row = tblSchedule.getSelectedRow();
        if (row != -1) {
            int id = (Integer) tblScheduleModel.getValueAt(row, 0);
            for (Appointment app : list) {
                if (app.getId() == id) return app;
            }
        }
        return null;
    }

    public void setScheduleTableData(List<Appointment> list) {
        tblScheduleModel.setRowCount(0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Appointment app : list) {
            tblScheduleModel.addRow(new Object[]{
                app.getId(),
                app.getStartTime().format(timeFormatter),
                app.getCustomer() != null ? app.getCustomer().getFullName() : "Unknown",
                app.getCustomer() != null ? app.getCustomer().getPhone() : "",
                app.getService() != null ? app.getService().getName() : "Unknown",
                app.getDurationAtBooking() + " λ.",
                translateStatus(app.getStatus().name()),
                app.getNotes() != null ? app.getNotes() : ""
            });
        }
    }

    public void setHistoryTableData(List<Appointment> list) {
        tblHistoryModel.setRowCount(0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Appointment app : list) {
            tblHistoryModel.addRow(new Object[]{
                app.getId(),
                app.getAppointmentDate(),
                app.getStartTime().format(timeFormatter),
                app.getCustomer() != null ? app.getCustomer().getFullName() : "Unknown",
                app.getCustomer() != null ? app.getCustomer().getPhone() : "",
                app.getService() != null ? app.getService().getName() : "Unknown",
                "€" + app.getPriceAtBooking(),
                translateStatus(app.getStatus().name()),
                app.getNotes() != null ? app.getNotes() : ""
            });
        }
    }

    public int getSelectedMonth() { return (Integer) cbMonth.getSelectedItem(); }
    public int getSelectedYear() { return (Integer) cbYear.getSelectedItem(); }

    public void setMonthTableData(List<Appointment> list) {
        tblMonthModel.setRowCount(0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Appointment app : list) {
            tblMonthModel.addRow(new Object[]{
                app.getAppointmentDate(),
                app.getStartTime().format(timeFormatter),
                app.getCustomer() != null ? app.getCustomer().getFullName() : "Unknown",
                app.getCustomer() != null ? app.getCustomer().getPhone() : "",
                app.getService() != null ? app.getService().getName() : "Unknown",
                translateStatus(app.getStatus().name())
            });
        }
    }

    private String translateStatus(String status) {
        switch (status) {
            case "CONFIRMED":   return "Επιβεβαιωμένο";
            case "IN_PROGRESS": return "Σε εξέλιξη";
            case "COMPLETED":   return "Ολοκληρώθηκε";
            case "NO_SHOW":     return "Δεν προσήλθε";
            case "CANCELLED":   return "Ακυρώθηκε";
            default: return status;
        }
    }

    private static class EmptyStateTable extends JTable {
        private final String emptyMessage;

        EmptyStateTable(DefaultTableModel model, String emptyMessage) {
            super(model);
            this.emptyMessage = emptyMessage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() > 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(UIHelper.FONT_LABEL);
            g2.setColor(UIHelper.TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int x = Math.max(16, (getWidth() - fm.stringWidth(emptyMessage)) / 2);
            int y = Math.max(48, getHeight() / 2);
            g2.drawString(emptyMessage, x, y);
            g2.dispose();
        }
    }

    public void addLogoutListener(ActionListener l) { btnLogout.addActionListener(l); }
    public void addLoadScheduleListener(ActionListener l) { btnLoadSchedule.addActionListener(l); }
    public void addUpdateStatusListener(ActionListener l) { btnUpdateStatus.addActionListener(l); }
    public void addSearchListener(ActionListener l) { btnSearch.addActionListener(l); }
    public void addLoadMonthListener(ActionListener l) { btnLoadMonth.addActionListener(l); }
}
