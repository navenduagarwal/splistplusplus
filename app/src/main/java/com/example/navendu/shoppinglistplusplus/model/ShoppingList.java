package com.example.navendu.shoppinglistplusplus.model;

/**
 * Created by navendu on 7/21/2016.
 */
public class ShoppingList {
    private String listName;
    private String owner;

    public ShoppingList() {
    }

    public ShoppingList(String listName, String owner) {
        this.listName = listName;
        this.owner = owner;
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }
}
