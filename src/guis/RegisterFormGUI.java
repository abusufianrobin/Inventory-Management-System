package guis;

import constants.CommonConstants;
import db.MyJDBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RegisterFormGUI extends Form {
    public RegisterFormGUI() {
        super("Register");
        addGuiComponents();
    }

    private void addGuiComponents() {
        // Project Title
        JLabel titleLabel = new JLabel("📚 EasyTech Bookstore", SwingConstants.CENTER);
        titleLabel.setBounds(0, 10, 520, 40);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel);

        // Register Heading
        JLabel registerLabel = new JLabel("Sign Up", SwingConstants.CENTER);
        registerLabel.setBounds(0, 50, 520, 50);
        registerLabel.setForeground(CommonConstants.TEXT_COLOR);
        registerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        add(registerLabel);

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
        usernameField.setForeground(new Color(50, 50, 50));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
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
        passwordField.setForeground(new Color(50, 50, 50));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        add(passwordField);

        // Re-Password Label
        JLabel rePasswordLabel = new JLabel("Re-enter Password:");
        rePasswordLabel.setBounds(40, 320, 400, 30);
        rePasswordLabel.setForeground(CommonConstants.TEXT_COLOR);
        rePasswordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        add(rePasswordLabel);

        // Re-Password Field
        JPasswordField rePasswordField = new JPasswordField();
        rePasswordField.setBounds(40, 360, 430, 40);
        rePasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        rePasswordField.setBackground(CommonConstants.SECONDARY_COLOR);
        rePasswordField.setForeground(new Color(50, 50, 50));
        rePasswordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        add(rePasswordField);

        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.setBounds(150, 440, 200, 50);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 22));
        registerButton.setBackground(new Color(33, 150, 243)); // Material Blue
        registerButton.setForeground(Color.WHITE);
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.setFocusPainted(false);
        registerButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(68, 180, 255)); // Lighter blue on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(new Color(33, 150, 243)); // Revert to original
            }
        });
        add(registerButton);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String rePassword = new String(rePasswordField.getPassword());

            if (username.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                JOptionPane.showMessageDialog(RegisterFormGUI.this, "Please fill in all fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (validateUserInput(username, password, rePassword)) {
                if (MyJDBC.register(username, password)) {
                    LoginFormGUI loginFormGUI = new LoginFormGUI();
                    dispose();
                    loginFormGUI.setVisible(true);
                    JOptionPane.showMessageDialog(loginFormGUI, "Registered Account Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(RegisterFormGUI.this,
                            "Error: Username already taken or database issue occurred.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(RegisterFormGUI.this,
                        "Error: Username must be at least 6 characters and/or Passwords must match.",
                        "Input Validation", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Switch to LogIn Label
        JLabel loginLabel = new JLabel("Have an account? Login here", SwingConstants.CENTER);
        loginLabel.setBounds(135, 510, 240, 30);
        loginLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLabel.setForeground(Color.LIGHT_GRAY);
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(loginLabel);

        loginLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                RegisterFormGUI.this.dispose();
                new LoginFormGUI().setVisible(true);
            }
        });
    }

    private boolean validateUserInput(String username, String password, String rePassword) {
        return username.length() >= 6 && password.equals(rePassword);
    }
}
