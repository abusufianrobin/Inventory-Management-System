package org.example.myjavafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the JavaFX Chat Client.
 * Loads the FXML UI and sets up the primary stage.
 * Multiple instances of this application can be run to simulate multiple users.
 */
public class ChatApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file for the chat interface
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("chat-view.fxml"));
        // Create a scene with the loaded FXML content.
        // The size is set initially, but the UI is designed to be responsive.
        Scene scene = new Scene(fxmlLoader.load(), 600, 450);
        // Link the external stylesheet for custom styling
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        stage.setTitle("JavaFX Client Complain"); // Set the window title
        stage.setScene(scene); // Set the scene to the stage
        stage.show(); // Display the stage

        // Get the controller instance to perform cleanup when the window is closed
        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> {
            // Call the cleanup method in the controller to close the socket
            if (controller != null) {
                controller.disconnectClient();
            }
        });
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch();
    }
}
