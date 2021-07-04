package com.example.fokusapplication;

public enum UserEmail {
    instance;
    private String value;
    private UserEmail() {
        value = "";
    }

    public String get() {
        return value;
    }
    public void set(String email) {
        value = email;
    }
}
