package chat;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static final String CHAT_LOG_FILE = "chat_log.txt";

    public static void main(String[] args) {
        System.out.println("Chat server started on port " + PORT);
        try (ServerSocket listener = new ServerSocket(PORT)) {
            while (true) {
                new ServerWorker(listener.accept(), clientWriters, CHAT_LOG_FILE).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void logMessage(String message) {
        try (FileWriter fw = new FileWriter(CHAT_LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + message);
        } catch (IOException e) {
            System.err.println("Error writing to chat log: " + e.getMessage());
        }
    }
}
