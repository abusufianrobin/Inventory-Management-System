package chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class ChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public ChatClient() {
        setTitle("💬 Chat with System");
        setSize(450, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Chat Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(new Color(240, 248, 255)); // Alice Blue
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputPanel.setBackground(new Color(230, 230, 250)); // Lavender

        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.addActionListener(new SendMessageListener());
        inputPanel.add(messageField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(60, 179, 113)); // Medium Sea Green
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener(new SendMessageListener());
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Connect to server
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("127.0.0.1", 12345); // Connect to localhost, port 12345
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            chatArea.append("Connected to chat system.\n");

            // Start a new thread to listen for server messages
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        String finalServerMessage = serverMessage;
                        SwingUtilities.invokeLater(() -> chatArea.append("System: " + finalServerMessage + "\n"));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> chatArea.append("Disconnected from server.\n"));
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not connect to the chat server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            chatArea.append("Could not connect to chat server. Please ensure the server is running.\n");
        }
    }

    private class SendMessageListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = messageField.getText();
            if (message != null && !message.trim().isEmpty()) {
                chatArea.append("You: " + message + "\n");
                if (out != null) {
                    out.println(message); // Send message to server
                }
                messageField.setText(""); // Clear the input field
            }
        }
    }

    // Optional: Override window closing to close socket gracefully
    @Override
    public void dispose() {
        super.dispose();
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
}