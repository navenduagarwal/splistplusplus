package com.example.navendu.shoppinglistplusplus;


import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

/**
 * Includes one-time initialization of Firebase related code
 */
public class ShoppingListApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance();
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
    }

}