package com.example.navendu.shoppinglistplusplus.model;

import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Defines the data structure of ShoppingListItem objects
 */
public class ShoppingListItem {
    private String itemName;
    private String owner;

    public ShoppingListItem() {
    }

    public ShoppingListItem(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
        HashMap<String, Object> timestampNowObj = new HashMap<>();
        timestampNowObj.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
    }

    public String getItemName() {
        return itemName;
    }

    public String getOwner() {
        return owner;
    }

}
