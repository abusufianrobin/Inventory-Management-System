package guis;

import constants.CommonConstants;
import db.MyJDBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException; // Import SQLException

public class LoginFormGUI extends Form {
    public LoginFormGUI() {
        super("Login");
        addGuiComponents();
    }

    private void addGuiComponents() {
        // Project Title Label at the top
        JLabel projectTitle = new JLabel("📚 EasyTech Bookstore", SwingConstants.CENTER);
        projectTitle.setBounds(0, 10, 520, 40);
        projectTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        projectTitle.setForeground(Color.WHITE);
        add(projectTitle);

        // Page Heading: Login
        JLabel loginLabel = new JLabel("Login Your Account");
        loginLabel.setBounds(0, 50, 520, 50);
        loginLabel.setForeground(CommonConstants.TEXT_COLOR);
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        loginLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(loginLabel);

        // Username Label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(40, 120, 400, 30);
        usernameLabel.setForeground(CommonConstants.TEXT_COLOR);
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(usernameLabel);

        // Username Field
        JTextField usernameField = new JTextField();
        usernameField.setBounds(40, 160, 430, 40);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        usernameField.setBackground(CommonConstants.SECONDARY_COLOR);
        usernameField.setForeground(new Color(50, 50, 50)); // Darker text for input
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1), // Light gray border
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Padding inside
        ));
        add(usernameField);

        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(40, 220, 400, 30);
        passwordLabel.setForeground(CommonConstants.TEXT_COLOR);
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(passwordLabel);

        // Password Field
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(40, 260, 430, 40);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        passwordField.setBackground(CommonConstants.SECONDARY_COLOR);
        passwordField.setForeground(new Color(50, 50, 50)); // Darker text for input
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        add(passwordField);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setBounds(150, 340, 200, 50);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginButton.setBackground(new Color(76, 175, 80)); // Material Green
        loginButton.setForeground(Color.WHITE);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setFocusPainted(false); // Remove focus border
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(90, 200, 90)); // Darker green on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(76, 175, 80)); // Revert to original
            }
        });
        add(loginButton);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(LoginFormGUI.this, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Using MyJDBC.validateLogin for authentication
            if (MyJDBC.validateLogin(username, password)) {
                JOptionPane.showMessageDialog(LoginFormGUI.this, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new DashboardGUI(username).setVisible(true); // Launch DashboardGUI on successful login
            } else {
                JOptionPane.showMessageDialog(LoginFormGUI.this, "Login Failed. Invalid credentials.", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Register Label
        JLabel registerLabel = new JLabel("Not a user? Register here");
        registerLabel.setBounds(140, 460, 240, 30);
        registerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        registerLabel.setForeground(Color.LIGHT_GRAY);
        registerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        registerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(registerLabel);

        registerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
                new RegisterFormGUI().setVisible(true); // Switch to Register Form
            }
        });
    }

    // This authenticate method is no longer strictly needed if MyJDBC.validateLogin is used directly in the action listener.
    // However, keeping it as a wrapper for MyJDBC.validateLogin does no harm and can be useful for consistency if other parts
    // of your application were expecting a direct 'authenticate' method on the GUI.
    private boolean authenticate(String username, String password) {
        return MyJDBC.validateLogin(username, password);
    }
}
