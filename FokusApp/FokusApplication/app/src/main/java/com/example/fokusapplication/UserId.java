package com.example.fokusapplication;

public enum UserId {
    instance;
    private String value;
    private UserId() {
        value = "";
    }

    public String get() {
        return value;
    }
    public void set(String id) {
        value = id;
    }
}
