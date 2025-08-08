package org.example.myjavafx;

import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ChatClient handles the network communication for the JavaFX chat application.
 * It connects to the server, sends messages, and receives messages in a separate thread.
 */
public class ChatClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ObservableList<String> messageList; // Reference to the ListView's items
    private String username;
    private boolean connected = false;

    /**
     * Constructor for ChatClient.
     *
     * @param messageList The ObservableList that backs the ListView in the UI.
     * Messages received from the server will be added here.
     */
    public ChatClient(ObservableList<String> messageList) {
        this.messageList = messageList;
    }

    /**
     * Connects to the chat server. This method should be called in a separate thread
     * to avoid blocking the JavaFX Application Thread.
     *
     * @param serverAddress The IP address or hostname of the server.
     * @param serverPort The port number of the server.
     * @param username The username for this client.
     * @return true if connection is successful, false otherwise.
     */
    public boolean connect(String serverAddress, int serverPort, String username) {
        this.username = username;
        try {
            // Establish socket connection to the server
            socket = new Socket(serverAddress, serverPort);
            // Initialize input and output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true); // true for auto-flush

            // Send the username to the server immediately after connecting
            out.println(username);
            connected = true;

            // Start a new thread to listen for incoming messages from the server.
            // This thread runs continuously in the background, allowing the UI thread
            // to remain responsive.
            new Thread(this::listenForMessages).start();
            return true;
        } catch (IOException e) {
            Platform.runLater(() -> messageList.add("Error: Could not connect to server: " + e.getMessage()));
            System.err.println("Error connecting to server: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    /**
     * Listens for messages from the server. This method runs in a dedicated thread.
     * Messages are added to the ObservableList, which automatically updates the UI.
     */
    private void listenForMessages() {
        try {
            String message;
            // Continuously read messages from the server. This is a blocking call,
            // so it must be on a separate thread to avoid freezing the UI.
            while (connected && (message = in.readLine()) != null) {
                // Update the UI on the JavaFX Application Thread.
                // Platform.runLater ensures thread-safe UI updates.
                final String receivedMessage = message;
                Platform.runLater(() -> messageList.add(receivedMessage));
            }
        } catch (IOException e) {
            if (connected) { // Only show error if disconnection was not intentional
                Platform.runLater(() -> messageList.add("Disconnected from server: " + e.getMessage()));
                System.err.println("Error listening for messages: " + e.getMessage());
            }
        } finally {
            disconnect(); // Ensure resources are closed on disconnection
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message The message to send.
     */
    public void sendMessage(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
        } else {
            Platform.runLater(() -> messageList.add("Error: Not connected to server."));
        }
    }

    /**
     * Disconnects the client from the server and closes all resources.
     */
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            Platform.runLater(() -> messageList.add("You have disconnected from the chat."));
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
    }

    /**
     * Checks if the client is currently connected to the server.
     * @return true if connected, false otherwise.
     */
    public boolean isConnected() {
        return connected;
    }
}
