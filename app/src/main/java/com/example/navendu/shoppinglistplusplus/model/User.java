package com.example.navendu.shoppinglistplusplus.model;

import java.util.HashMap;

/**
 * Defines the data structure of user objects
 */
public class User {
    private String email;
    private String name;
    private HashMap<String, Object> timestampJoined;
    private boolean hasLoggedInWithPassword;

    public User() {
    }

    public User(String email, String name, HashMap<String, Object> timestampJoined) {
        this.email = email;
        this.name = name;
        this.timestampJoined = timestampJoined;
        this.hasLoggedInWithPassword = false;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }

    public boolean isHasLoggedInWithPassword() {
        return hasLoggedInWithPassword;
    }
}
