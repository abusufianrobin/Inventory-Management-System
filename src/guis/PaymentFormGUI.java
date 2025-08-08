// Updated: PaymentFormGUI.java with history table, validation, styling, and PDF report

package guis;

import db.MyJDBC;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class PaymentFormGUI extends JFrame {
    private JComboBox<String> paymentTypeBox;
    private JTextField amountField, accountNumberField, cardNumberField, cvvField;
    private JSpinner expiryField;
    private JLabel accountLabel, cardNumberLabel, cvvLabel, expiryLabel;
    private JButton submitBtn;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private String username;

    public PaymentFormGUI(String username) {
        this.username = username;
        setTitle("📄 Make a Payment");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        Color mainBg = new Color(248, 250, 252);
        Color accent = new Color(33, 150, 243);
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);

        JPanel formPanel = new JPanel(null);
        formPanel.setPreferredSize(new Dimension(400, 0));
        formPanel.setBackground(mainBg);

        JLabel titleLabel = new JLabel("💳 Make a Payment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBounds(80, 10, 300, 30);
        formPanel.add(titleLabel);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setBounds(30, 60, 100, 25);
        formPanel.add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(150, 60, 200, 25);
        formPanel.add(amountField);

        JLabel paymentTypeLabel = new JLabel("Payment Type:");
        paymentTypeLabel.setBounds(30, 100, 100, 25);
        formPanel.add(paymentTypeLabel);

        paymentTypeBox = new JComboBox<>(new String[]{"Select", "BKASH", "NAGAD", "CARD"});
        paymentTypeBox.setBounds(150, 100, 200, 25);
        formPanel.add(paymentTypeBox);

        accountLabel = new JLabel("Account No (11 digits):");
        accountLabel.setBounds(30, 140, 150, 25);
        formPanel.add(accountLabel);

        accountNumberField = new JTextField();
        accountNumberField.setBounds(180, 140, 170, 25);
        formPanel.add(accountNumberField);

        cardNumberLabel = new JLabel("Card No (10 digits):");
        cardNumberLabel.setBounds(30, 180, 150, 25);
        cardNumberField = new JTextField();
        cardNumberField.setBounds(180, 180, 170, 25);

        cvvLabel = new JLabel("CVV:");
        cvvLabel.setBounds(30, 220, 150, 25);
        cvvField = new JTextField();
        cvvField.setBounds(180, 220, 170, 25);

        expiryLabel = new JLabel("Expiry Date:");
        expiryLabel.setBounds(30, 260, 150, 25);
        expiryField = new JSpinner(new SpinnerDateModel());
        expiryField.setBounds(180, 260, 170, 25);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(expiryField, "MM/yyyy");
        expiryField.setEditor(dateEditor);

        formPanel.add(cardNumberLabel);
        formPanel.add(cardNumberField);
        formPanel.add(cvvLabel);
        formPanel.add(cvvField);
        formPanel.add(expiryLabel);
        formPanel.add(expiryField);

        submitBtn = new JButton("Submit Payment");
        submitBtn.setBounds(120, 310, 160, 35);
        submitBtn.setBackground(accent);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(submitBtn);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(mainBg);
        historyPanel.setBorder(BorderFactory.createTitledBorder("Payment History"));

        historyModel = new DefaultTableModel(new String[]{"ID", "Amount", "Type", "Time"}, 0);
        historyTable = new JTable(historyModel);
        JScrollPane scroll = new JScrollPane(historyTable);
        historyPanel.add(scroll, BorderLayout.CENTER);

        add(formPanel, BorderLayout.WEST);
        add(historyPanel, BorderLayout.CENTER);

        hideAllDynamicFields();
        paymentTypeBox.addActionListener(e -> updateDynamicFields());
        submitBtn.addActionListener(e -> handlePaymentSubmit());

        loadPaymentHistory();
    }

    private void hideAllDynamicFields() {
        accountLabel.setVisible(false);
        accountNumberField.setVisible(false);
        cardNumberLabel.setVisible(false);
        cardNumberField.setVisible(false);
        cvvLabel.setVisible(false);
        cvvField.setVisible(false);
        expiryLabel.setVisible(false);
        expiryField.setVisible(false);
    }

    private void updateDynamicFields() {
        hideAllDynamicFields();
        String selected = paymentTypeBox.getSelectedItem().toString();
        if (selected.equals("BKASH") || selected.equals("NAGAD")) {
            accountLabel.setVisible(true);
            accountNumberField.setVisible(true);
        } else if (selected.equals("CARD")) {
            cardNumberLabel.setVisible(true);
            cardNumberField.setVisible(true);
            cvvLabel.setVisible(true);
            cvvField.setVisible(true);
            expiryLabel.setVisible(true);
            expiryField.setVisible(true);
        }
    }

    private void loadPaymentHistory() {
        try (Connection conn = MyJDBC.getConnection()) {
            historyModel.setRowCount(0);
            PreparedStatement ps = conn.prepareStatement("SELECT id, amount, payment_type, payment_time FROM payments WHERE username = ? ORDER BY payment_time DESC");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                historyModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_type"),
                        rs.getTimestamp("payment_time")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load payment history: " + e.getMessage());
        }
    }

    private void handlePaymentSubmit() {
        String amountStr = amountField.getText();
        String type = paymentTypeBox.getSelectedItem().toString();

        if (amountStr.isEmpty() || type.equals("Select")) {
            JOptionPane.showMessageDialog(this, "Fill all fields properly.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            String acc = null, card = null, cvv = null, expiry = null;

            if (type.equals("BKASH") || type.equals("NAGAD")) {
                acc = accountNumberField.getText();
                if (acc.length() != 11) throw new Exception("Invalid mobile number (11 digits required)." );
            } else {
                card = cardNumberField.getText();
                if (card.length() != 10) throw new Exception("Invalid card number (10 digits required).");
                cvv = cvvField.getText();
                expiry = new SimpleDateFormat("MM/yyyy").format(((Date) expiryField.getValue()));

                if (new SimpleDateFormat("MM/yyyy").parse(expiry).before(new Date()))
                    throw new Exception("Card expired!");
            }

            try (Connection conn = MyJDBC.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO payments (username, payment_type, amount, account_number, card_number, cvv, expiry_date) VALUES (?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, username);
                ps.setString(2, type);
                ps.setDouble(3, amount);
                ps.setString(4, acc);
                ps.setString(5, card);
                ps.setString(6, cvv);
                ps.setString(7, expiry);
                ps.executeUpdate();
            }

            createPDFReport(amount, type, acc, card);
            JOptionPane.showMessageDialog(this, "Payment Successful! PDF Receipt Generated.");
            loadPaymentHistory();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Payment Failed: " + e.getMessage());
        }
    }

    private void createPDFReport(double amount, String type, String acc, String card) {
        try {
            Document doc = new Document();
            String fileName = "payment_receipt_" + System.currentTimeMillis() + ".pdf";
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();
            doc.addTitle("Payment Receipt");
            doc.add(new Paragraph("EasyTech Bookstore Payment Receipt", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            doc.add(new Paragraph("\nUsername: " + username));
            doc.add(new Paragraph("Amount: BDT " + amount));
            doc.add(new Paragraph("Type: " + type));
            if (acc != null) doc.add(new Paragraph("Account No: " + acc));
            if (card != null) doc.add(new Paragraph("Card No: " + card));
            doc.add(new Paragraph("Time: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date())));
            doc.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to generate PDF: " + e.getMessage());
        }
    }
}
