package com.kourio.views;


import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public final class UIHelper {

    public static final Color BG_APP           = new Color(10, 15, 28);
    public static final Color BG_CARD          = new Color(17, 24, 39);
    public static final Color BG_CARD_SOFT     = new Color(24, 32, 48);
    public static final Color BG_FIELD         = new Color(12, 20, 34);
    public static final Color BG_HEADER        = new Color(13, 20, 34);
    public static final Color BG_HEADER_BARBER = new Color(13, 25, 43);
    public static final Color BG_HEADER_OWNER  = new Color(20, 22, 45);

    public static final Color PRIMARY          = new Color(59, 130, 246);
    public static final Color PRIMARY_HOVER    = new Color(37, 99, 235);
    public static final Color PRIMARY_SOFT     = new Color(30, 58, 138);

    public static final Color SUCCESS          = new Color(34, 197, 94);
    public static final Color SUCCESS_HOVER    = new Color(22, 163, 74);
    public static final Color SUCCESS_SOFT     = new Color(20, 83, 45);

    public static final Color WARNING          = new Color(245, 158, 11);
    public static final Color WARNING_HOVER    = new Color(217, 119, 6);
    public static final Color WARNING_SOFT     = new Color(92, 60, 16);

    public static final Color DANGER           = new Color(239, 68, 68);
    public static final Color DANGER_HOVER     = new Color(220, 38, 38);
    public static final Color DANGER_SOFT      = new Color(69, 26, 31);

    public static final Color INFO             = new Color(56, 189, 248);
    public static final Color INFO_SOFT        = new Color(12, 74, 110);

    public static final Color NEUTRAL          = new Color(71, 85, 105);
    public static final Color NEUTRAL_HOVER    = new Color(51, 65, 85);
    public static final Color NEUTRAL_SOFT     = new Color(30, 41, 59);

    public static final Color TEXT_PRIMARY     = new Color(248, 250, 252);
    public static final Color TEXT_SECONDARY   = new Color(203, 213, 225);
    public static final Color TEXT_MUTED       = new Color(148, 163, 184);
    public static final Color BORDER_SOFT      = new Color(51, 65, 85);
    public static final Color SELECTION_BG     = new Color(30, 64, 124);

    private static final String FONT_FAMILY = pickFontFamily();

    public static final Font FONT_TITLE    = new Font(FONT_FAMILY, Font.BOLD,  24);
    public static final Font FONT_HEADING  = new Font(FONT_FAMILY, Font.BOLD,  18);
    public static final Font FONT_SUBHEAD  = new Font(FONT_FAMILY, Font.BOLD,  13);
    public static final Font FONT_LABEL    = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BUTTON   = new Font(FONT_FAMILY, Font.BOLD,  13);
    public static final Font FONT_SMALL    = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BADGE    = new Font(FONT_FAMILY, Font.BOLD,  11);
    public static final Font FONT_STAT_BIG = new Font(FONT_FAMILY, Font.BOLD,  26);

    private UIHelper() {}

    private static String pickFontFamily() {
        String[] preferred = {"Inter", "SF Pro Text", "Helvetica Neue", "Segoe UI", "Arial"};
        try {
            String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            java.util.Set<String> set = new java.util.HashSet<>(java.util.Arrays.asList(available));
            for (String p : preferred) if (set.contains(p)) return p;
        } catch (Throwable ignored) {}
        return Font.SANS_SERIF;
    }

    public static void applyGlobalLookAndFeel() {
        // The entire UI is built around FlatLaf-specific client properties and UIManager keys
        // (Component.arc, Button.arc, FlatLaf.style, TabbedPane.*, …). Use FlatLaf as the
        // rendering engine so all of that actually takes effect (it was running on Nimbus,
        // which silently ignored every one of those hints).
        try {
            FlatDarkLaf.setup();
        } catch (Exception ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        UIManager.put("defaultFont", FONT_LABEL);
        UIManager.put("Panel.background", BG_APP);
        UIManager.put("Viewport.background", BG_CARD);
        UIManager.put("OptionPane.background", BG_CARD);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.focusColor", PRIMARY);
        UIManager.put("Component.borderColor", BORDER_SOFT);
        UIManager.put("Component.innerFocusWidth", 0);

        UIManager.put("TextComponent.arc", 10);
        UIManager.put("TextField.background", BG_FIELD);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", PRIMARY);
        UIManager.put("PasswordField.background", BG_FIELD);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", PRIMARY);

        UIManager.put("ComboBox.background", BG_FIELD);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.buttonBackground", BG_FIELD);
        UIManager.put("ComboBox.buttonArrowColor", TEXT_SECONDARY);
        UIManager.put("ComboBox.popupBackground", BG_CARD);
        UIManager.put("ComboBox.selectionBackground", SELECTION_BG);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);

        UIManager.put("TabbedPane.background", BG_APP);
        UIManager.put("TabbedPane.foreground", TEXT_SECONDARY);
        UIManager.put("TabbedPane.selectedBackground", BG_CARD_SOFT);
        UIManager.put("TabbedPane.selectedForeground", TEXT_PRIMARY);
        UIManager.put("TabbedPane.hoverColor", BG_CARD_SOFT);
        UIManager.put("TabbedPane.focusColor", BG_CARD_SOFT);
        UIManager.put("TabbedPane.underlineColor", PRIMARY);
        UIManager.put("TabbedPane.tabSeparatorsFullHeight", false);
        UIManager.put("TabbedPane.showTabSeparators", false);

        UIManager.put("Table.background", BG_CARD);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", SELECTION_BG);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", BORDER_SOFT);
        UIManager.put("TableHeader.background", BG_CARD_SOFT);
        UIManager.put("TableHeader.foreground", TEXT_PRIMARY);
        UIManager.put("TableHeader.separatorColor", BORDER_SOFT);

        UIManager.put("CheckBox.background", BG_CARD);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
        UIManager.put("TitledBorder.titleColor", TEXT_PRIMARY);
        UIManager.put("Button.disabledText", TEXT_MUTED);
        UIManager.put("Label.foreground", TEXT_PRIMARY);

        // Slim, rounded scrollbars (FlatLaf) for a more polished, modern feel
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.track", BG_APP);
        UIManager.put("ScrollBar.thumb", NEUTRAL_SOFT);
        UIManager.put("ScrollBar.hoverThumbColor", NEUTRAL);
        UIManager.put("ScrollBar.showButtons", false);

        // Consistent rounded corners and softer popups/tooltips across the app
        UIManager.put("ScrollPane.arc", 10);
        UIManager.put("PopupMenu.background", BG_CARD);
        UIManager.put("ToolTip.background", BG_CARD_SOFT);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        UIManager.put("TextComponent.selectionBackground", PRIMARY_SOFT);

        // Accent color used by FlatLaf for focus/selection affordances
        UIManager.put("Component.accentColor", PRIMARY);
    }

    public static JButton createPrimaryButton(String text) {
        return styleButton(new JButton(text), PRIMARY, PRIMARY_HOVER, Color.WHITE);
    }

    public static JButton createSuccessButton(String text) {
        return styleButton(new JButton(text), SUCCESS, SUCCESS_HOVER, Color.WHITE);
    }

    public static JButton createWarningButton(String text) {
        return styleButton(new JButton(text), WARNING, WARNING_HOVER, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return styleButton(new JButton(text), DANGER, DANGER_HOVER, Color.WHITE);
    }

    public static JButton createNeutralButton(String text) {
        return styleButton(new JButton(text), NEUTRAL, NEUTRAL_HOVER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return styleButton(new JButton(text), BG_CARD_SOFT, NEUTRAL_HOVER, TEXT_PRIMARY);
    }

    public static JButton createLinkButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(new Color(147, 197, 253));
        b.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton styleButton(JButton b, Color bg, Color hover, Color fg) {
        b.setFont(FONT_BUTTON);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.putClientProperty("FlatLaf.style",
            "background:" + hex(bg) + ";hoverBackground:" + hex(hover) + ";pressedBackground:" + hex(hover.darker()) + ";"
                + "foreground:" + hex(fg));
        return b;
    }

    public static Border roundedFieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(8, 11, 8, 11)
        );
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        );
    }

    public static Border compactCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );
    }

    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BG_CARD);
        panel.setBorder(cardBorder());
        return panel;
    }

    public static void styleTextField(JTextField field) {
        field.setFont(FONT_LABEL);
        field.setBackground(BG_FIELD);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(PRIMARY);
        field.setSelectionColor(PRIMARY_HOVER);
        field.setSelectedTextColor(Color.WHITE);
        field.setBorder(roundedFieldBorder());
        field.putClientProperty("JTextField.showClearButton", false);
        field.putClientProperty("JComponent.roundRect", true);
    }

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(FONT_LABEL);
        combo.setBackground(BG_FIELD);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_SOFT, 1, true));
        combo.putClientProperty("JComponent.roundRect", true);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBackground(isSelected ? SELECTION_BG : BG_CARD);
                label.setForeground(TEXT_PRIMARY);
                label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
                return label;
            }
        });
    }

    public static void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(FONT_LABEL);
        checkBox.setForeground(TEXT_SECONDARY);
        checkBox.setBackground(BG_CARD);
        checkBox.setOpaque(false);
        checkBox.putClientProperty("JComponent.focusWidth", 1);
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_LABEL);
        table.setRowHeight(34);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG_CARD);
        table.setGridColor(new Color(31, 41, 59));
        table.setFillsViewportHeight(true);
        table.putClientProperty("Table.showHorizontalLines", true);
        table.putClientProperty("Table.showVerticalLines", false);
        table.putClientProperty("Table.selectionArc", 8);

        table.getTableHeader().setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
        table.getTableHeader().setBackground(BG_CARD_SOFT);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(BG_CARD_SOFT);
                label.setForeground(TEXT_PRIMARY);
                label.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SOFT),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return label;
            }
        });
    }

    public static void styleTabbedPane(JTabbedPane tabs) {
        tabs.setBackground(BG_APP);
        tabs.setForeground(TEXT_SECONDARY);
        tabs.setFont(FONT_LABEL);
        tabs.setBorder(BorderFactory.createEmptyBorder(14, 16, 16, 16));
        tabs.putClientProperty("JTabbedPane.tabType", "card");
        tabs.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
        tabs.putClientProperty("JTabbedPane.showTabSeparators", false);
        tabs.putClientProperty("JTabbedPane.hasFullBorder", false);
        tabs.putClientProperty("FlatLaf.style",
            "tabArc:12;tabInsets:9,18,9,18;selectedBackground:" + hex(BG_CARD_SOFT) + ";"
                + "hoverColor:" + hex(BG_CARD_SOFT) + ";focusColor:" + hex(BG_CARD_SOFT) + ";"
                + "underlineColor:" + hex(PRIMARY));
    }

    public static JLabel createSectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SUBHEAD);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    public static JLabel createHeadingLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel createMutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private static class BadgeLabel extends JLabel {
        private final Color bg;

        BadgeLabel(String text, Color fg, Color bg) {
            super(text == null ? "" : text);
            this.bg = bg;
            setForeground(fg);
            setFont(FONT_BADGE);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JComponent createBadge(String text, Color fg, Color bg) { return new BadgeLabel(text, fg, bg); }
    public static JComponent createSuccessBadge(String t) { return createBadge(t, new Color(187, 247, 208), SUCCESS_SOFT); }
    public static JComponent createWarningBadge(String t) { return createBadge(t, new Color(253, 230, 138), WARNING_SOFT); }
    public static JComponent createDangerBadge(String t)  { return createBadge(t, new Color(254, 202, 202), DANGER_SOFT);  }
    public static JComponent createInfoBadge(String t)    { return createBadge(t, new Color(186, 230, 253), INFO_SOFT);    }
    public static JComponent createNeutralBadge(String t) { return createBadge(t, TEXT_SECONDARY, NEUTRAL_SOFT); }

    public static JComponent createStatusBadge(String statusName, String label) {
        if (statusName == null) return createNeutralBadge(label == null ? "-" : label);
        switch (statusName) {
            case "CONFIRMED":   return createInfoBadge(label);
            case "IN_PROGRESS": return createWarningBadge(label);
            case "COMPLETED":   return createSuccessBadge(label);
            case "NO_SHOW":     return createDangerBadge(label);
            case "CANCELLED":   return createNeutralBadge(label);
            default:            return createNeutralBadge(label);
        }
    }

    public static JScrollPane createTableScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_CARD);
        sp.setBackground(BG_CARD);
        sp.putClientProperty("JScrollBar.showButtons", false);
        return sp;
    }

    private static String hex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
