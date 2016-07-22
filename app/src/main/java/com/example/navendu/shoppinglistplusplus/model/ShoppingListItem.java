package com.example.navendu.shoppinglistplusplus.model;

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
    }

    public String getItemName() {
        return itemName;
    }

    public String getOwner() {
        return owner;
    }
}
