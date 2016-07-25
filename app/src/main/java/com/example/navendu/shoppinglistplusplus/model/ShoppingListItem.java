package com.example.navendu.shoppinglistplusplus.model;

/**
 * Defines the data structure of ShoppingListItem objects
 */
public class ShoppingListItem {
    private String itemName;
    private String owner;
    private String boughtBy;
    private boolean bought;

    public ShoppingListItem() {
    }

    public ShoppingListItem(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
        this.boughtBy = null;
        this.bought = false;
    }

    public String getItemName() {
        return itemName;
    }

    public String getOwner() {
        return owner;
    }

    public String getBoughtBy() {
        return boughtBy;
    }

    public boolean isBought() {
        return bought;
    }
}
