package guis;

import db.MyJDBC;
import chat.ChatClient; // Import the ChatClient class

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor; // Import TableCellEditor
import javax.swing.table.TableCellRenderer; // Import TableCellRenderer
import java.awt.*;
import java.awt.event.*;
import java.io.File; // Import File for CSV export
import java.io.FileWriter;
import java.io.IOException; // Import IOException for FileWriter
import java.sql.*;

public class DashboardGUI extends JFrame {
    private final JTable itemTable;
    private final JTable salesTable;
    private final DefaultTableModel itemModel;
    private final DefaultTableModel salesModel;
    private final JTextField searchField;
    // Added chatButton, dashboardBtn, addItemSidebarBtn to declarations for clarity
    private final JButton addItemBtn;
    private final JButton logoutBtn;
    private final JButton searchBtn;
    private final JButton exportBtn;
    private final JButton topPaymentBtn;
    private final JButton paymentSidebarBtn;
    private final JButton chatButton;
    private final JButton dashboardBtn;
    private final JButton addItemSidebarBtn;
    private final JLabel totalQtyLabel;
    private final JLabel totalRevenueLabel;
    private final String username;

    // Define colors once for consistency
    private final Color sidebarBg = new Color(30, 45, 70);
    private final Color sidebarBtnBg = new Color(50, 70, 110);
    private final Color mainBg = new Color(248, 250, 252);
    private final Color accentColor = new Color(33, 150, 243);
    private final Color textColor = Color.WHITE;

    public DashboardGUI(String username) {
        this.username = username;
        setTitle("EasyTech Bookstore - Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Sidebar =====
        JPanel sidebar = new JPanel();
        sidebar.setBackground(sidebarBg);
        sidebar.setPreferredSize(new Dimension(180, 0));
        // Using GridLayout for 6 rows: Dashboard, Add Item, Payment, Chat, Logout, and a vertical glue
        sidebar.setLayout(new GridLayout(6, 1, 0, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // Initialize all sidebar buttons using the helper method for consistent styling
        dashboardBtn = createSidebarButton("Dashboard");
        addItemSidebarBtn = createSidebarButton("Add Item");
        paymentSidebarBtn = createSidebarButton("Payment");
        chatButton = createSidebarButton("💬 Chat with System"); // New chat button
        logoutBtn = createSidebarButton("Logout");

        // Add buttons to the sidebar panel in the desired order
        sidebar.add(dashboardBtn);
        sidebar.add(addItemSidebarBtn);
        sidebar.add(paymentSidebarBtn);
        sidebar.add(chatButton); // Add the chat button
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalGlue()); // This glue will push other components to the top within GridLayout

        add(sidebar, BorderLayout.WEST);

        // ===== Top bar =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(mainBg);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel titleLabel = new JLabel("📚 EasyTech Bookstore - Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(accentColor);
        topBar.add(titleLabel, BorderLayout.WEST);

        JLabel welcomeLabel = new JLabel("Welcome, " + username.toUpperCase());
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(Color.DARK_GRAY);
        topBar.add(welcomeLabel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // ===== Main content =====
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(mainBg);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainPanel, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlsPanel.setBackground(mainBg);
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        controlsPanel.add(searchField);

        searchBtn = new JButton("Search");
        // Style common buttons (Search, Add Item, Payment)
        styleMainButtons(searchBtn);
        controlsPanel.add(searchBtn);

        addItemBtn = new JButton("Add Item");
        styleMainButtons(addItemBtn);
        controlsPanel.add(addItemBtn);

        topPaymentBtn = new JButton("Payment");
        styleMainButtons(topPaymentBtn);
        controlsPanel.add(topPaymentBtn);

        mainPanel.add(controlsPanel, BorderLayout.NORTH);

        JSplitPane tablesSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tablesSplitPane.setResizeWeight(0.5);

        // Item Table
        itemModel = new DefaultTableModel(new String[]{"ID", "Item Name", "Qty", "Price", "Sell", "Edit", "Delete"}, 0);
        itemTable = new JTable(itemModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing for Sell, Edit, Delete columns (indices 4, 5, 6)
                return column >= 4;
            }
        };
        // Add custom cell renderer for buttons (Sell, Edit, Delete)
        itemTable.getColumn("Sell").setCellRenderer(new ButtonRenderer());
        itemTable.getColumn("Sell").setCellEditor(new ButtonEditor(new JTextField(), "Sell"));
        itemTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        itemTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JTextField(), "Edit"));
        itemTable.getColumn("Delete").setCellRenderer(new ButtonRenderer());
        itemTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JTextField(), "Delete"));

        itemTable.setRowHeight(30); // Make rows taller for better button appearance
        itemTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        itemTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemTable.setSelectionBackground(new Color(200, 220, 255)); // Light blue selection

        JScrollPane itemScroll = new JScrollPane(itemTable);
        itemScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor, 1), "Inventory Items",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), accentColor));
        tablesSplitPane.setTopComponent(itemScroll);

        // Sales Table
        salesModel = new DefaultTableModel(new String[]{"Sale ID", "Item Name", "Qty Sold", "Time"}, 0);
        salesTable = new JTable(salesModel);
        salesTable.setRowHeight(30);
        salesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        salesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        salesTable.setSelectionBackground(new Color(200, 220, 255));

        JScrollPane salesScroll = new JScrollPane(salesTable);
        salesScroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(accentColor, 1), "Sales Records",
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16), accentColor));
        tablesSplitPane.setBottomComponent(salesScroll);

        mainPanel.add(tablesSplitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomPanel.setBackground(mainBg);

        exportBtn = new JButton("Export to CSV");
        styleMainButtons(exportBtn);
        bottomPanel.add(exportBtn);

        totalQtyLabel = new JLabel("Total Sold: 0");
        totalQtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalQtyLabel.setForeground(Color.DARK_GRAY);
        bottomPanel.add(totalQtyLabel);

        totalRevenueLabel = new JLabel("Total Revenue: $0.00");
        totalRevenueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        totalRevenueLabel.setForeground(Color.DARK_GRAY);
        bottomPanel.add(totalRevenueLabel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // === Button Actions ===
        searchBtn.addActionListener(e -> loadItems(searchField.getText().trim()));
        addItemBtn.addActionListener(e -> new AddItemGUI(this).setVisible(true));
        addItemSidebarBtn.addActionListener(e -> new AddItemGUI(this).setVisible(true));
        topPaymentBtn.addActionListener(e -> new PaymentFormGUI(username).setVisible(true));
        paymentSidebarBtn.addActionListener(e -> new PaymentFormGUI(username).setVisible(true));
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFormGUI().setVisible(true);
            }
        });
        dashboardBtn.addActionListener(e -> {
            loadItems(""); // Refresh all items
            loadSales(); // Refresh sales data
            searchField.setText(""); // Clear search field
        });
        exportBtn.addActionListener(e -> exportSalesToCSV());
        // Action Listener for the new Chat button
        chatButton.addActionListener(e -> {
            // Check if server is running before attempting to connect
            // This is a simple check; a more robust solution might involve polling or a dedicated server status check
            new ChatClient().setVisible(true);
        });


        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = itemTable.rowAtPoint(e.getPoint());
                int col = itemTable.columnAtPoint(e.getPoint());
                if (row < 0) return; // Clicked outside any row
                if (itemModel.getValueAt(row, 0) == null) return; // Ensure row is not empty

                int itemId = Integer.parseInt(itemModel.getValueAt(row, 0).toString());

                if (col == 4) { // Sell column
                    String qtyStr = JOptionPane.showInputDialog(DashboardGUI.this, "Enter quantity to sell:", "Sell Item", JOptionPane.PLAIN_MESSAGE);
                    if (qtyStr != null) { // User didn't cancel
                        try {
                            int qtyToSell = Integer.parseInt(qtyStr);
                            if (qtyToSell > 0) {
                                sellItem(itemId, qtyToSell);
                                loadItems(""); // Refresh items after selling
                                loadSales();   // Refresh sales after selling
                            } else {
                                JOptionPane.showMessageDialog(DashboardGUI.this, "Quantity must be positive.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(DashboardGUI.this, "Invalid quantity. Please enter a number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if (col == 5) { // Edit column
                    String currentName = itemModel.getValueAt(row, 1).toString();
                    String currentQty = itemModel.getValueAt(row, 2).toString();
                    String currentPrice = itemModel.getValueAt(row, 3).toString();

                    JTextField nameField = new JTextField(currentName);
                    JTextField qtyField = new JTextField(currentQty);
                    JTextField priceField = new JTextField(currentPrice);

                    JPanel panel = new JPanel(new GridLayout(0, 2));
                    panel.add(new JLabel("New Name:"));
                    panel.add(nameField);
                    panel.add(new JLabel("New Quantity:"));
                    panel.add(qtyField);
                    panel.add(new JLabel("New Price:"));
                    panel.add(priceField);

                    int result = JOptionPane.showConfirmDialog(DashboardGUI.this, panel, "Edit Item",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            String newName = nameField.getText();
                            int newQty = Integer.parseInt(qtyField.getText());
                            double newPrice = Double.parseDouble(priceField.getText());

                            if (newName.isEmpty() || newQty <= 0 || newPrice <= 0) {
                                JOptionPane.showMessageDialog(DashboardGUI.this, "Please enter valid item details.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            updateItem(itemId, newName, newQty, newPrice);
                            loadItems(""); // Refresh items after update
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(DashboardGUI.this, "Quantity and Price must be numeric.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if (col == 6) { // Delete column
                    int confirm = JOptionPane.showConfirmDialog(DashboardGUI.this, "Are you sure you want to delete this item?", "Delete Item", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteItem(itemId);
                        loadItems(""); // Refresh items after delete
                    }
                }
            }
        });

        // Initial data loading
        loadItems("");
        loadSales();
    }

    // --- Helper method to create and style sidebar buttons ---
    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(textColor);
        button.setBackground(sidebarBtnBg);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10)); // Top, Left, Bottom, Right padding
        button.setHorizontalAlignment(SwingConstants.LEFT); // Align text to the left
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(accentColor); // Highlight on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(sidebarBtnBg); // Revert on exit
            }
        });
        return button;
    }

    // --- Helper method to style main content panel buttons ---
    private void styleMainButtons(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(accentColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Padding
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(68, 180, 255)); // Lighter blue on hover
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(accentColor); // Revert on exit
            }
        });
    }

    // --- Table Button Renderer and Editor Classes (for Sell, Edit, Delete buttons in table) ---
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton button;

        public ButtonRenderer() {
            setLayout(new GridBagLayout()); // Use GridBagLayout for centering
            button = new JButton();
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Smaller padding for table buttons
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(button); // Add button to the panel
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof String) {
                button.setText((String) value);
            } else {
                button.setText(""); // Clear text if not a String
            }

            // Set button color based on action
            if ("Sell".equals(button.getText())) {
                button.setBackground(new Color(76, 175, 80)); // Green
                button.setForeground(Color.WHITE);
            } else if ("Edit".equals(button.getText())) {
                button.setBackground(new Color(255, 152, 0)); // Orange
                button.setForeground(Color.WHITE);
            } else if ("Delete".equals(button.getText())) {
                button.setBackground(new Color(244, 67, 54)); // Red
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(new Color(180, 180, 180)); // Default gray
                button.setForeground(Color.BLACK);
            }

            return this; // Return the panel containing the button
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final String label;
        private boolean isPushed;

        public ButtonEditor(JTextField checkBox, String buttonLabel) {
            super(checkBox);
            this.label = buttonLabel;
            button = new JButton();
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 12));
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Smaller padding
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Set button color based on action for editor
            if ("Sell".equals(label)) {
                button.setBackground(new Color(76, 175, 80)); // Green
                button.setForeground(Color.WHITE);
            } else if ("Edit".equals(label)) {
                button.setBackground(new Color(255, 152, 0)); // Orange
                button.setForeground(Color.WHITE);
            } else if ("Delete".equals(label)) {
                button.setBackground(new Color(244, 67, 54)); // Red
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(new Color(180, 180, 180)); // Default gray
                button.setForeground(Color.BLACK);
            }

            button.addActionListener(e -> fireEditingStopped()); // Stop editing when button is clicked
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Return the label itself, which will be caught by the mouse listener for the actual action
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }


    // --- Data Loading and Operations ---
    public void loadItems(String searchKeyword) {
        itemModel.setRowCount(0); // Clear existing data
        // Use 'item_name' as per your SQL schema for the 'items' table
        String query = "SELECT id, item_name, quantity, price FROM items";
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            query += " WHERE item_name LIKE ?"; // Search by item_name
        }

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                ps.setString(1, "%" + searchKeyword + "%");
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                itemModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("item_name"), // Use item_name
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        "Sell", // Placeholder for button
                        "Edit", // Placeholder for button
                        "Delete" // Placeholder for button
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading items: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSales() {
        salesModel.setRowCount(0); // Clear existing data
        double totalRevenue = 0.0;
        int totalQtySold = 0;

        // Join with 'items' table to get item_name and price directly for revenue calculation
        String query = "SELECT s.sale_id, i.item_name, s.quantity_sold, s.sale_time, i.price " +
                "FROM sales s JOIN items i ON s.item_id = i.id ORDER BY s.sale_time DESC";

        try (Connection conn = MyJDBC.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                salesModel.addRow(new Object[]{
                        rs.getInt("sale_id"),
                        rs.getString("item_name"), // Use item_name from the join
                        rs.getInt("quantity_sold"),
                        rs.getTimestamp("sale_time")
                });
                // Calculate total revenue and quantity sold using the price directly from the joined table
                int qty = rs.getInt("quantity_sold");
                double price = rs.getDouble("price"); // Get price directly from joined result set
                totalRevenue += qty * price;
                totalQtySold += qty;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sales: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        totalQtyLabel.setText("Total Sold: " + totalQtySold);
        totalRevenueLabel.setText(String.format("Total Revenue: BDT %.2f", totalRevenue));
    }


    private void updateItem(int itemId, String newName, int newQty, double newPrice) {
        try (Connection conn = MyJDBC.getConnection()) {
            // Use 'item_name' as per your SQL schema for the 'items' table
            String query = "UPDATE items SET item_name=?, quantity=?, price=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, newName);
            ps.setInt(2, newQty);
            ps.setDouble(3, newPrice);
            ps.setInt(4, itemId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Item updated successfully!", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Item not found or no changes made.", "Update Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating item: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItem(int itemId) {
        try (Connection conn = MyJDBC.getConnection()) {
            String query = "DELETE FROM items WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, itemId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Item deleted successfully!", "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Item not found.", "Delete Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sellItem(int itemId, int qtyToSell) {
        Connection conn = null; // Declare connection outside try-with-resources for rollback
        try {
            conn = MyJDBC.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Check current quantity
            PreparedStatement checkQtyPs = conn.prepareStatement("SELECT quantity FROM items WHERE id = ?");
            checkQtyPs.setInt(1, itemId);
            ResultSet rs = checkQtyPs.executeQuery();

            if (rs.next()) {
                int currentQty = rs.getInt("quantity");

                if (currentQty < qtyToSell) {
                    JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + currentQty, "Stock Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Update item quantity
                PreparedStatement updateQtyPs = conn.prepareStatement("UPDATE items SET quantity = ? WHERE id = ?");
                updateQtyPs.setInt(1, currentQty - qtyToSell);
                updateQtyPs.setInt(2, itemId);
                updateQtyPs.executeUpdate();

                // Log the sale
                // Ensure column names match your 'sales' table schema ('item_id', 'quantity_sold')
                PreparedStatement logSalePs = conn.prepareStatement("INSERT INTO sales (item_id, quantity_sold) VALUES (?, ?)");
                logSalePs.setInt(1, itemId);
                logSalePs.setInt(2, qtyToSell);
                logSalePs.executeUpdate();

                conn.commit(); // Commit transaction
                JOptionPane.showMessageDialog(this, "Item sold successfully!", "Sale Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Item not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            // Rollback on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            JOptionPane.showMessageDialog(this, "Sell failed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred during sell: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit state
                    conn.close(); // Close the connection
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    private void exportSalesToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Sales to CSV");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure .csv extension
            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (FileWriter fw = new FileWriter(fileToSave)) {
                // Write header
                for (int i = 0; i < salesModel.getColumnCount(); i++) {
                    fw.append(salesModel.getColumnName(i));
                    if (i < salesModel.getColumnCount() - 1) {
                        fw.append(",");
                    }
                }
                fw.append("\n");

                // Write data
                for (int i = 0; i < salesModel.getRowCount(); i++) {
                    for (int j = 0; j < salesModel.getColumnCount(); j++) {
                        // Handle null values to avoid NullPointerExceptions
                        Object value = salesModel.getValueAt(i, j);
                        fw.append(value != null ? value.toString() : "");
                        if (j < salesModel.getColumnCount() - 1) {
                            fw.append(",");
                        }
                    }
                    fw.append("\n");
                }
                JOptionPane.showMessageDialog(this, "Sales data exported to " + fileToSave.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
