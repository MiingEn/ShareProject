package com.videoconference.core;

public class User {
    private final String userId;
    private final String email;
    private final String password;

    public User(String userId, String email, String password) {
        this.userId = userId;
        this.email = email;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public boolean login(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // As per class diagram: register() stub
    public static User register(String userId, String email, String password) {
        System.out.println("User registered: " + email);
        return new User(userId, email, password);
    }

    // As per class diagram: login() stub
    public boolean login() {
        System.out.println("User logged in: " + email);
        return true;
    }
}
