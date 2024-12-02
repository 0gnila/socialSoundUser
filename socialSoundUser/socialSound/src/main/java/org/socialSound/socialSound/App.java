package org.socialSound.socialSound;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;





/**
 * JavaFX App
 */
public class App extends Application 
{
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize MongoDB connection
        String connectionString = "mongodb+srv://swensog:socialSoundT6@socialsound.xbl43.mongodb.net/?retryWrites=true&w=majority&appName=SocialSound";
        
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase("Song_Files"); 

        // Load FXML and set up the primary stage
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/org/socialSound/socialSound/socialSoundMain_resizable.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        controller.setMainWindow(stage);

        stage.setTitle("Social Sound");
        Scene scene = new Scene(root, 640, 400);
        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(400);

        stage.setResizable(true);
        stage.show();

        // Set close request behavior
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    // Static method to get the database for use in other classes
    public static MongoDatabase getDatabase() {
        return database;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (mongoClient != null) {
            mongoClient.close(); // Close MongoDB connection on app exit
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}