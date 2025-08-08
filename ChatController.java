package org.example.myjavafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;

import java.util.Optional;

/**
 * ChatController manages the JavaFX UI for the chat client.
 * It handles user input, displays messages, and interacts with the ChatClient for network operations.
 */
public class ChatController {

    @FXML
    private ListView<String> messageListView; // Displays chat messages
    @FXML
    private TextField messageInput;         // Input field for new messages
    @FXML
    private Button sendButton;              // Button to send messages
    @FXML
    private Button connectButton;           // Button to connect/disconnect

    private ChatClient chatClient;
    private ObservableList<String> messages; // Data model for the ListView
    private String username;

    // Server configuration
    private static final String SERVER_ADDRESS = "localhost"; // Change to your server IP if not local
    private static final int SERVER_PORT = 12345;

    /**
     * Initializes the controller. This method is called automatically after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Initialize the ObservableList for messages
        messages = FXCollections.observableArrayList();
        messageListView.setItems(messages); // Bind the ListView to the ObservableList

        // Disable input fields until connected
        messageInput.setDisable(true);
        sendButton.setDisable(true);

        // Add an event listener to the message input field for sending messages on ENTER key press
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onSendMessage();
            }
        });

        // Prompt for username on startup
        promptForUsername();
    }

    /**
     * Prompts the user to enter a username using a TextInputDialog.
     */
    private void promptForUsername() {
        TextInputDialog dialog = new TextInputDialog("Guest");
        dialog.setTitle("Username");
        dialog.setHeaderText("Please enter your username:");
        dialog.setContentText("Username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                username = "Guest"; // Default if empty
            } else {
                username = name.trim();
            }
            messages.add("Welcome, " + username + "! Click 'Connect' to Submit the Complain..");
        });

        // If the user closes the dialog without entering a name, still set a default
        if (!result.isPresent()) {
            username = "Guest";
            messages.add("Welcome, " + username + "! Click 'Connect' to Submit the Complain.");
        }
    }

    /**
     * Handles the action when the "Connect" or "Disconnect" button is clicked.
     */
    @FXML
    protected void onConnectDisconnect() {
        if (chatClient == null || !chatClient.isConnected()) {
            // Connect to the server
            connectButton.setText("Connecting...");
            connectButton.setDisable(true); // Prevent multiple clicks during connection attempt

            // Connect in a new thread to avoid freezing the UI.
            // This ensures the UI remains responsive during the potentially blocking connection process.
            new Thread(() -> {
                chatClient = new ChatClient(messages);
                boolean success = chatClient.connect(SERVER_ADDRESS, SERVER_PORT, username);
                Platform.runLater(() -> {
                    // Update UI elements on the JavaFX Application Thread after connection attempt.
                    if (success) {
                        messages.add("Connected to server as " + username + ".");
                        messageInput.setDisable(false);
                        sendButton.setDisable(false);
                        connectButton.setText("Disconnect");
                        connectButton.setDisable(false);
                        messageInput.requestFocus(); // Focus on input field
                    } else {
                        messages.add("Failed to connect to server.");
                        connectButton.setText("Connect");
                        connectButton.setDisable(false);
                    }
                });
            }).start();
        } else {
            // Disconnect from the server
            disconnectClient();
            messages.add("Disconnected from server.");
            messageInput.setDisable(true);
            sendButton.setDisable(true);
            connectButton.setText("Connect");
            connectButton.setDisable(false);
        }
    }

    /**
     * Handles the action when the "Send" button is clicked or ENTER is pressed in the input field.
     */
    @FXML
    protected void onSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            if (chatClient != null && chatClient.isConnected()) {
                chatClient.sendMessage(message);
                messageInput.clear(); // Clear the input field after sending
            } else {
                messages.add("You are not connected to the Complain server.");
            }
        }
    }

    /**
     * Disconnects the client from the server. Called when the application closes.
     */
    public void disconnectClient() {
        if (chatClient != null && chatClient.isConnected()) {
            chatClient.disconnect();
        }
    }
}
