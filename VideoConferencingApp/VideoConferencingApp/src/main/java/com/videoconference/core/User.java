package com.videoconference.core;

public class User {
    private final String userId;
    private final String name;      // 添加 name 字段
    private final String email;
    private final String password;

    public User(String userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean login(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    public static User register(String userId, String name, String email, String password) {
        System.out.println("User registered: " + email);
        return new User(userId, name, email, password);
    }

    public boolean login() {
        System.out.println("User logged in: " + email);
        return true;
    }
}
