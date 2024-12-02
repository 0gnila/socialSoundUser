package org.socialSound.socialSound;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Login {

    @FXML
    private Button login;

    @FXML
    private TextField Username;

    @FXML
    private PasswordField Password;

    @FXML
    private Button noaccount;

    @FXML
    private ImageView logo;

    @FXML
    private Label wronglogin;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Initialize the controller class.
     * This method is automatically called after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        Image logoImage = new Image(getClass().getResourceAsStream("images/socialSoundLogo.png"));
        logo.setImage(logoImage);
        try {
            // Set up socket connection
            socket = new Socket("localhost", 9811);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            wronglogin.setText("Unable to connect to server");
        }
    }

    @FXML
    public void userLogin(ActionEvent event) {
        checkLogin();
    }

    @FXML
    public void create(ActionEvent event) {
        try {
            Main.changeScene("/org/socialSound/socialSound/SignUp.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkLogin() {
        if (out == null) {
            wronglogin.setText("Server connection is not established.");
            return;
        }

        try {
            // Send login data to the server
            out.println("LOGIN");
            out.println(Username.getText());
            out.println(Password.getText());

            // Read the server response
            String serverResponse = in.readLine();

            if ("SUCCESS".equalsIgnoreCase(serverResponse)) {
                Main.launchApp(); // Call the method to launch App
            } else {
                wronglogin.setText("Invalid username or password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            wronglogin.setText("Error during login. Please try again.");
        }
    }
}