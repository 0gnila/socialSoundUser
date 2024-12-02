package org.socialSound.socialSound;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> userCollection;

    public static void main(String[] args) {
        initializeDatabase();

        try (ServerSocket serverSocket = new ServerSocket(9811)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mongoClient != null) {
                mongoClient.close();
            }
        }
    }

    private static void initializeDatabase() {
        String connectionString = "mongodb+srv://swensog:socialSoundT6@socialsound.xbl43.mongodb.net/?retryWrites=true&w=majority&appName=SocialSound";

        try {
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase("User_Files");
            userCollection = database.getCollection("users");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while (true) {
                    String command = in.readLine();
                    if ("LOGIN".equalsIgnoreCase(command)) {
                        handleLogin();
                    } else if ("SIGNUP".equalsIgnoreCase(command)) {
                        handleSignUp();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void handleSignUp() throws IOException {
            String fullname = in.readLine();
            String username = in.readLine();
            String password = in.readLine();
            String email = in.readLine();

            if (userCollection.find(new Document("username", username)).first() != null ||
                userCollection.find(new Document("email", email)).first() != null) {
                out.println("Username or email already exists.");
                return;
            }

            Document newUser = new Document("fullName", fullname)
                .append("username", username)
                .append("password", password)
                .append("email", email)
                .append("inbox", "<Beginning of message history>");
            userCollection.insertOne(newUser);

            out.println("SUCCESS");
        }

        private void handleLogin() throws IOException {
            String username = in.readLine();
            String password = in.readLine();

            Document userDoc = userCollection.find(new Document("username", username)).first();

            if (userDoc != null && userDoc.getString("password").equals(password)) {
                out.println("SUCCESS");
            } else {
                out.println("Invalid username or password.");
            }
        }
    }
}