package chat;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ServerWorker extends Thread {
    private Socket clientSocket;
    private Set<PrintWriter> clientWriters;
    private BufferedReader in;
    private PrintWriter out;
    private String chatLogFile;

    public ServerWorker(Socket clientSocket, Set<PrintWriter> clientWriters, String chatLogFile) {
        this.clientSocket = clientSocket;
        this.clientWriters = clientWriters;
        this.chatLogFile = chatLogFile;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Add new client's writer to the set
            synchronized (clientWriters) {
                clientWriters.add(out);
            }

            String clientMessage;
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("Received: " + clientMessage);
                ChatServer.logMessage("Client: " + clientMessage);
                // For a simple system-chat, the server can "respond"
                // In a multi-user chat, you would broadcast to all clientWriters
                String serverResponse = "System received your message: \"" + clientMessage + "\"";
                out.println(serverResponse); // Send response back to the client
                ChatServer.logMessage("System: " + serverResponse);
            }

        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            if (out != null) {
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}