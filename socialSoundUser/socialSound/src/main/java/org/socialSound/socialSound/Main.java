package org.socialSound.socialSound;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static Stage stg;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Save the stage reference
            stg = primaryStage;

            // Start the server in a separate thread
            startServer();

            // Load the initial FXML file
            Parent root = FXMLLoader.load(getClass().getResource("/org/socialSound/socialSound/Sample.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Social Sound");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeScene(String fxmlPath) throws IOException {
        Parent pane = FXMLLoader.load(Main.class.getResource(fxmlPath));
        Scene scene = new Scene(pane);
        stg.setScene(scene);
        stg.show();
    }

    private void startServer() {
        Thread serverThread = new Thread(() -> {
            try {
                Server.main(new String[]{}); // Start the server
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.setDaemon(true); // Ensure the thread stops when the application exits
        serverThread.start();
    }

    public static void launchApp() {
        Platform.runLater(() -> {
            try {
                App app = new App();
                app.start(new Stage()); // Start the App stage
                if (stg != null) {
                    stg.close(); // Close the login window
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}