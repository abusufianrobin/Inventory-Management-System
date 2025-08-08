package guis;

import db.MyJDBC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddItemGUI extends JFrame {
    private JTextField nameField, qtyField, priceField;
    private JButton addBtn;
    private DashboardGUI dashboard;

    public AddItemGUI(DashboardGUI dashboard) {
        this.dashboard = dashboard;

        setTitle("📦 Add New Item - EasyTech Bookstore");
        setSize(520, 400);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        getContentPane().setBackground(new Color(245, 248, 255)); // Lightest tone

        // --- Menu Bar (Navbar Simulation) ---
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(33, 150, 243)); // Material Blue
        menuBar.setPreferredSize(new Dimension(520, 40));

        JButton homeBtn = createNavButton("Home");
        JButton inventoryBtn = createNavButton("Inventory");
        JButton logoutBtn = createNavButton("Logout");

        menuBar.add(Box.createHorizontalStrut(10));
        menuBar.add(homeBtn);
        menuBar.add(Box.createHorizontalStrut(10));
        menuBar.add(inventoryBtn);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(logoutBtn);

        setJMenuBar(menuBar);

        // --- Title Label ---
        JLabel title = new JLabel("Add Inventory Item");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBounds(0, 30, 520, 40);
        title.setForeground(new Color(33, 33, 33));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title);

        // --- Input Labels & Fields ---
        nameField = createInputField("Item Name:", 90);
        qtyField = createInputField("Quantity:", 150);
        priceField = createInputField("Price:", 210);

        // --- Add Button ---
        addBtn = new JButton("➕ Add Item");
        addBtn.setBounds(180, 280, 160, 45);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addBtn.setBackground(new Color(76, 175, 80)); // Green
        addBtn.setForeground(Color.WHITE);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.setFocusPainted(false);
        addBtn.setBorder(BorderFactory.createLineBorder(new Color(56, 142, 60), 1, true));
        add(addBtn);

        // --- Button Action ---
        addBtn.addActionListener(this::addItemToDatabase);
    }

    private void addItemToDatabase(ActionEvent e) {
        String name = nameField.getText().trim();
        String qtyText = qtyField.getText().trim();
        String priceText = priceField.getText().trim();

        if (name.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        try {
            int quantity = Integer.parseInt(qtyText);
            double price = Double.parseDouble(priceText);

            if (quantity < 0 || price < 0) {
                JOptionPane.showMessageDialog(this, "Quantity and Price must be non-negative.");
                return;
            }

            try (Connection conn = MyJDBC.getConnection()) {
                String sql = "INSERT INTO items (item_name, quantity, price) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setInt(2, quantity);
                ps.setDouble(3, price);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Item added successfully!");
                    nameField.setText("");
                    qtyField.setText("");
                    priceField.setText("");

                    if (dashboard != null) {
                        dashboard.loadItems("");
                    }

                    dispose();
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity and Price must be numeric.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    // --- Reusable Nav Button ---
    private JButton createNavButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(33, 150, 243));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // --- Reusable Input Field with Label ---
    private JTextField createInputField(String label, int y) {
        JLabel lbl = new JLabel(label);
        lbl.setBounds(50, y, 100, 25);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lbl.setForeground(new Color(33, 33, 33));
        add(lbl);

        JTextField field = new JTextField();
        field.setBounds(160, y, 280, 30);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(255, 255, 255));
        field.setForeground(new Color(33, 33, 33));
        field.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        add(field);
        return field;
    }
}
