
package org.socialSound.socialSound;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Signup {

    @FXML
    private Button signup;
    @FXML
    private TextField Fullname;
    @FXML
    private TextField Username;
    @FXML
    private PasswordField Password;
    @FXML
    private TextField Email;
    @FXML
    private Label teller;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        try {
            socket = new Socket("localhost", 9811);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            teller.setText("Welcome to SocialSound");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to server");
        }
    }

    @FXML
    public void Signsup(ActionEvent event) {
        if (validateInput()) {
            sendSignupRequest();
        }
    }

    private boolean validateInput() {
        if (Fullname.getText().isEmpty() || Username.getText().isEmpty() ||
            Password.getText().isEmpty() || Email.getText().isEmpty()) {
            teller.setText("Please fill in all fields.");
            return false;
        }
        if (!Email.getText().contains("@") || !Email.getText().contains(".")) {
            teller.setText("Please enter a valid email address.");
            return false;
        }
        if (Password.getText().length() < 6) {
            teller.setText("Password must be at least 6 characters long.");
            return false;
        }
        return true;
    }

    private void sendSignupRequest() {
        try {
            out.println("SIGNUP");
            out.println(Fullname.getText());
            out.println(Username.getText());
            out.println(Password.getText());
            out.println(Email.getText());

            String serverResponse = in.readLine();
            if ("SUCCESS".equalsIgnoreCase(serverResponse)) {
                teller.setText("Account created successfully!");
                Main.changeScene("/org/socialSound/socialSound/Sample.fxml"); // Navigate back to login screen
            } else {
                teller.setText(serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error communicating with the server");
        }
    }
}
