package com.kourio.views;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JPanel {
    private static final Color SCREEN_TOP = new Color(11, 18, 32);
    private static final Color SCREEN_BOTTOM = new Color(17, 24, 39);
    private static final Color CARD_BG = new Color(18, 27, 44);
    private static final Color CARD_BORDER = new Color(51, 65, 85);
    private static final Color FIELD_BG = new Color(13, 21, 35);
    private static final Color FIELD_BORDER = new Color(71, 85, 105);
    private static final Color TEXT_MAIN = new Color(248, 250, 252);
    private static final Color TEXT_SOFT = new Color(203, 213, 225);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color ACCENT = new Color(59, 130, 246);
    private static final Color ACCENT_LIGHT = new Color(147, 197, 253);
    private static final Color ERROR_BG = new Color(69, 26, 31);
    private static final Color ERROR_TEXT = new Color(254, 202, 202);
    private static final int CARD_WIDTH = 390;
    private static final int CARD_HEIGHT = 482;
    private static final int FORM_WIDTH = 322;
    private static final int CONTROL_HEIGHT = 42;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnForgotPassword;
    private JLabel lblError;
    private JPanel errorPanel;

    public LoginView() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        JPanel card = new RoundedPanel(CARD_BG, CARD_BORDER, 8, true);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setBorder(BorderFactory.createEmptyBorder(24, 34, 24, 34));

        buildCard(card);
        add(card, new GridBagConstraints());

        txtUsername.addActionListener(e -> txtPassword.requestFocusInWindow());
        txtPassword.addActionListener(e -> btnLogin.doClick());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(
            0, 0, SCREEN_TOP,
            0, getHeight(), SCREEN_BOTTOM
        ));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private void buildCard(JPanel card) {
        LogoBadge logo = new LogoBadge("B");
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logo);
        card.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel("Barber");
        title.setFont(new Font(UIHelper.FONT_TITLE.getFamily(), Font.BOLD, 33));
        title.setForeground(TEXT_MAIN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4));

        JLabel subtitle = new JLabel("Διαχείριση ραντεβού κουρείου");
        subtitle.setFont(new Font(UIHelper.FONT_LABEL.getFamily(), Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(FORM_WIDTH, 284));
        form.setMaximumSize(new Dimension(FORM_WIDTH, 284));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel loginTitle = new JLabel("Σύνδεση λογαριασμού");
        loginTitle.setFont(new Font(UIHelper.FONT_HEADING.getFamily(), Font.BOLD, 18));
        loginTitle.setForeground(TEXT_MAIN);
        loginTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(loginTitle);
        form.add(Box.createVerticalStrut(12));

        form.add(fieldLabel("Όνομα χρήστη"));
        form.add(Box.createVerticalStrut(6));
        txtUsername = new JTextField(22);
        styleLoginField(txtUsername);
        txtUsername.putClientProperty("JTextField.placeholderText", "Εισάγετε το όνομα χρήστη");
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(13));

        JPanel passwordHeader = new JPanel(new BorderLayout());
        passwordHeader.setOpaque(false);
        passwordHeader.setPreferredSize(new Dimension(FORM_WIDTH, 22));
        passwordHeader.setMaximumSize(new Dimension(FORM_WIDTH, 22));
        passwordHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordHeader.add(fieldLabel("Κωδικός πρόσβασης"), BorderLayout.WEST);
        btnForgotPassword = UIHelper.createLinkButton("Ξέχασα τον κωδικό μου");
        btnForgotPassword.setForeground(ACCENT_LIGHT);
        btnForgotPassword.setFont(new Font(UIHelper.FONT_LABEL.getFamily(), Font.BOLD, 12));
        btnForgotPassword.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        btnForgotPassword.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordHeader.add(btnForgotPassword, BorderLayout.EAST);
        form.add(passwordHeader);
        form.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField(22);
        styleLoginField(txtPassword);
        txtPassword.putClientProperty("JTextField.placeholderText", "Εισάγετε τον κωδικό σας");
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(10));

        errorPanel = new RoundedPanel(ERROR_BG, new Color(127, 29, 29), 8, false);
        errorPanel.setLayout(new BorderLayout());
        errorPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        errorPanel.setPreferredSize(new Dimension(FORM_WIDTH, 34));
        errorPanel.setMaximumSize(new Dimension(FORM_WIDTH, 34));
        errorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblError = new JLabel(" ");
        lblError.setFont(new Font(UIHelper.FONT_LABEL.getFamily(), Font.BOLD, 12));
        lblError.setForeground(ERROR_TEXT);
        errorPanel.add(lblError, BorderLayout.CENTER);
        errorPanel.setVisible(false);
        form.add(errorPanel);
        form.add(Box.createVerticalStrut(12));

        btnLogin = UIHelper.createPrimaryButton("Σύνδεση");
        btnLogin.setFont(new Font(UIHelper.FONT_BUTTON.getFamily(), Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(FORM_WIDTH, CONTROL_HEIGHT));
        btnLogin.setMaximumSize(new Dimension(FORM_WIDTH, CONTROL_HEIGHT));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnLogin);

        card.add(form);
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIHelper.FONT_SUBHEAD);
        label.setForeground(TEXT_SOFT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleLoginField(JTextField field) {
        field.setFont(new Font(UIHelper.FONT_LABEL.getFamily(), Font.PLAIN, 14));
        field.setForeground(TEXT_MAIN);
        field.setCaretColor(ACCENT_LIGHT);
        field.setBackground(FIELD_BG);
        field.setSelectionColor(new Color(37, 99, 235));
        field.setSelectedTextColor(Color.WHITE);
        field.setOpaque(true);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(FIELD_BORDER, 8),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        field.setMaximumSize(new Dimension(FORM_WIDTH, CONTROL_HEIGHT));
        field.setPreferredSize(new Dimension(FORM_WIDTH, CONTROL_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public String getUsername() { return txtUsername.getText().trim(); }
    public String getPassword() { return new String(txtPassword.getPassword()); }

    public void setErrorMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            lblError.setText(" ");
            errorPanel.setVisible(false);
        } else {
            lblError.setText(message);
            errorPanel.setVisible(true);
        }
        revalidate();
        repaint();
    }

    public void addLoginListener(ActionListener l) { btnLogin.addActionListener(l); }
    public void addForgotPasswordListener(ActionListener l) { btnForgotPassword.addActionListener(l); }

    public void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        setErrorMessage(null);
    }

    private static class LogoBadge extends JLabel {
        LogoBadge(String text) {
            super(text);
            setPreferredSize(new Dimension(54, 54));
            setMaximumSize(new Dimension(54, 54));
            setMinimumSize(new Dimension(54, 54));
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
            setFont(new Font(UIHelper.FONT_TITLE.getFamily(), Font.BOLD, 27));
            setForeground(Color.WHITE);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), new Color(37, 99, 235)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.setColor(new Color(255, 255, 255, 48));
            g2.fillRoundRect(8, 8, getWidth() - 16, 2, 2, 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color fill;
        private final Color border;
        private final int radius;
        private final boolean shadow;

        RoundedPanel(Color fill, Color border, int radius, boolean shadow) {
            this.fill = fill;
            this.border = border;
            this.radius = radius;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (shadow) {
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillRoundRect(5, 7, getWidth() - 10, getHeight() - 10, radius, radius);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, radius, radius);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 9, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }
}
