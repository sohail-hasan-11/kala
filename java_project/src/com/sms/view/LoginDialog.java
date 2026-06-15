package com.sms.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Login Dialog for the Student Management System.
 */
public class LoginDialog extends JDialog {

    private final JComboBox<String> roleCombo;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginBtn;
    private final JButton cancelBtn;

    private boolean loginSuccessful = false;
    private String selectedRole = "";
    private String username = "";

    public LoginDialog(Frame parent) {
        super(parent, "System Authentication", true);
        setSize(380, 440);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // Header Section
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("🔒 Portal Login");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(title);

        JLabel subtitle = new JLabel("Please enter your database system credentials");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(130, 140, 155));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(subtitle);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Section
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Role
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel rLbl = new JLabel("Select Role:");
        rLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(rLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        roleCombo = new JComboBox<>(new String[]{"Admin", "User (Viewer)"});
        roleCombo.putClientProperty("JComponent.roundRect", true);
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(roleCombo, gbc);
        row++;

        // Username
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel uLbl = new JLabel("Username:");
        uLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(uLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        usernameField = new JTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Enter username");
        usernameField.putClientProperty("JComponent.roundRect", true);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usernameField.setMargin(new Insets(4, 8, 4, 8));
        formPanel.add(usernameField, gbc);
        row++;

        // Password
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel pLbl = new JLabel("Password:");
        pLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(pLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Enter password");
        passwordField.putClientProperty("JComponent.roundRect", true);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setMargin(new Insets(4, 8, 4, 8));
        formPanel.add(passwordField, gbc);
        row++;

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel buttonsRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonsRow.setOpaque(false);

        loginBtn = new JButton("Login Securely");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(new Color(41, 128, 185));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.putClientProperty("JComponent.roundRect", true);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(e -> validateLogin());

        cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.putClientProperty("JComponent.roundRect", true);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());

        buttonsRow.add(loginBtn);
        buttonsRow.add(cancelBtn);
        actionPanel.add(buttonsRow);

        // Help Tips Area (Displays defaults)
        JLabel tipLabel = new JLabel("<html><center><font color='#8c96a5'><b>Test Credentials:</b><br>" +
                "Admin: <b>admin</b> / <b>admin123</b><br>" +
                "User (Viewer): <b>user</b> / <b>user123</b></font></center></html>");
        tipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        actionPanel.add(tipLabel);

        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        // Connect pressing Enter key to validateLogin
        getRootPane().setDefaultButton(loginBtn);
    }

    private void validateLogin() {
        String role = (String) roleCombo.getSelectedItem();
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Username and Password.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean verified = false;

        if ("Admin".equals(role)) {
            if ("admin".equals(user) && "admin123".equals(pass)) {
                verified = true;
                selectedRole = "ADMIN";
            }
        } else {
            if ("user".equals(user) && "user123".equals(pass)) {
                verified = true;
                selectedRole = "USER";
            }
        }

        if (verified) {
            loginSuccessful = true;
            username = user;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password for the selected Role.", "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public String getUsername() {
        return username;
    }
}
