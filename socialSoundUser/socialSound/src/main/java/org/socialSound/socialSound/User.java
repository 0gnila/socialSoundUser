
package org.socialSound.socialSound;

import org.bson.Document;

public class User {
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String inbox;

    public User(String fullName, String username, String password, String email, String inbox) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.inbox = inbox;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getInbox() {
        return inbox;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setInbox(String inbox) {
        this.inbox = inbox;
    }

    public Document toDocument() {
        return new Document("fullName", fullName)
                .append("username", username)
                .append("password", password)
                .append("email", email)
                .append("inbox", inbox);
    }

    public static User fromDocument(Document doc) {
        return new User(
                doc.getString("fullName"),
                doc.getString("username"),
                doc.getString("password"),
                doc.getString("email"),
                doc.getString("inbox")
        );
    }
}
