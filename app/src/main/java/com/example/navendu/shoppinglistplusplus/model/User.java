package com.example.navendu.shoppinglistplusplus.model;

import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

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

    public User(String email, String name) {
        this.email = email;
        this.name = name;
        HashMap<String, Object> timestampNowObj = new HashMap<>();
        timestampNowObj.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampJoined = timestampNowObj;
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

    @Exclude
    public long getTimestampJoinedLong() {
        return (long) timestampJoined.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }

}
