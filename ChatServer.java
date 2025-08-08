package org.example.myjavafx;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime; // For timestamping messages
import java.time.format.DateTimeFormatter; // For formatting timestamps

/**
 * ChatServer class handles incoming client connections and broadcasts messages.
 * It uses a thread pool to manage multiple client handlers concurrently.
 * This version also logs all chat messages to a text file.
 */
public class ChatServer {

    // Port number for the server to listen on
    private static final int PORT = 12345;
    // List to keep track of all connected client handlers.
    // SynchronizedList is used to ensure thread-safe access from multiple ClientHandler threads.
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    // Thread pool to manage client handler threads efficiently.
    private static ExecutorService pool = Executors.newFixedThreadPool(10); // Max 10 concurrent clients

    // File path for the chat log
    private static final String LOG_FILE = "chat_log.txt";
    // Date formatter for timestamps in the log file
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            // Create a server socket bound to the specified port
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat Server started on port " + PORT);
            logMessage("--- Chat Server Started ---"); // Log server start

            // Continuously listen for new client connections.
            while (true) {
                // Accept a new client connection. This call blocks until a client connects.
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());
                logMessage("New client connected: " + clientSocket.getInetAddress().getHostAddress()); // Log connection

                // Create a new ClientHandler for the connected client.
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                // Add the client handler to the list of active clients.
                clients.add(clientHandler);
                // Submit the client handler to the thread pool for execution.
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            logMessage("Server error: " + e.getMessage()); // Log server error
        } finally {
            // Ensure the server socket is closed when the server shuts down
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                    System.out.println("Server socket closed.");
                    logMessage("--- Chat Server Stopped ---"); // Log server stop
                } catch (IOException e) {
                    System.err.println("Error closing server socket: " + e.getMessage());
                    logMessage("Error closing server socket: " + e.getMessage());
                }
            }
            // Shut down the thread pool gracefully
            pool.shutdown();
        }
    }

    /**
     * Broadcasts a message to all connected clients and logs it to the file.
     * This method is called by any ClientHandler when it receives a message from its client,
     * ensuring all other clients receive the message and it's recorded.
     *
     * @param message The message to be broadcasted and logged.
     */
    public static void broadcastMessage(String message) {
        // Log the message first
        logMessage(message);

        // Iterate over a copy of the clients list to avoid ConcurrentModificationException
        for (ClientHandler client : new ArrayList<>(clients)) {
            if (client != null) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Removes a disconnected client handler from the list.
     *
     * @param clientHandler The client handler to be removed.
     */
    public static void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        String disconnectMessage = "Client disconnected: " + clientHandler.getClientSocket().getInetAddress().getHostAddress();
        System.out.println(disconnectMessage);
        logMessage(disconnectMessage); // Log client disconnection
    }

    /**
     * Logs a message to the chat_log.txt file with a timestamp.
     * This method is synchronized to ensure thread-safe writing to the file,
     * preventing data corruption from concurrent write attempts.
     *
     * @param message The message to log.
     */
    private static void logMessage(String message) {
        // Synchronize on the class lock to ensure only one thread writes to the file at a time
        synchronized (ChatServer.class) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) { // true for append mode
                String timestamp = LocalDateTime.now().format(formatter);
                writer.write("[" + timestamp + "] " + message);
                writer.newLine(); // Add a new line after each message
            } catch (IOException e) {
                System.err.println("Error writing to chat log file: " + e.getMessage());
                // Note: We don't log this error to the file itself to avoid an infinite loop
            }
        }
    }
}

/**
 * ClientHandler class manages communication with a single client.
 * It runs in a separate thread, reading messages from the client and sending messages to it.
 */
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in; // To read messages from the client
    private PrintWriter out;    // To send messages to the client
    private String clientName;  // Name of the client

    /**
     * Constructor for ClientHandler.
     *
     * @param socket The client socket connected to this handler.
     */
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            // Initialize input and output streams for the socket
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true); // true for auto-flush
        } catch (IOException e) {
            System.err.println("Error setting up client handler streams: " + e.getMessage());
            closeResources();
        }
    }

    @Override
    public void run() {
        try {
            // The first message from the client is expected to be their name
            clientName = in.readLine();
            if (clientName == null) {
                // If the client disconnects immediately, handle it
                System.out.println("Client disconnected before sending name.");
                return;
            }
            String joinMessage = clientName + " has joined the chat.";
            System.out.println(joinMessage);
            ChatServer.broadcastMessage(joinMessage); // Broadcast and log join message

            String message;
            // Continuously read messages from the client
            while ((message = in.readLine()) != null) {
                String fullMessage = clientName + ": " + message;
                System.out.println(fullMessage); // Log to server console
                ChatServer.broadcastMessage(fullMessage); // Broadcast and log message
            }
        } catch (IOException e) {
            // Handle client disconnection or other I/O errors
            System.out.println(clientName + " disconnected: " + e.getMessage());
        } finally {
            // Clean up resources when the client disconnects
            String leaveMessage = clientName + " has left the chat.";
            ChatServer.broadcastMessage(leaveMessage); // Broadcast and log leave message
            ChatServer.removeClient(this);
            closeResources();
        }
    }

    /**
     * Sends a message to this specific client.
     *
     * @param message The message to send.
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * Returns the client's socket.
     *
     * @return The client's socket.
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * Closes all resources associated with this client handler.
     */
    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client handler resources: " + e.getMessage());
        }
    }
}
