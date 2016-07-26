package com.example.navendu.shoppinglistplusplus.utils;

import android.content.Context;
import android.util.Log;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Utility Class
 */
public class Utils {
    /**
     * Format the timestamp with SimpleDateFormat
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final String LOG_TAG = Utils.class.getSimpleName();
    private Context mContext = null;


    /**
     * Public constructor that takes mContext for later use
     */
    public Utils(Context con) {
        mContext = con;
    }

    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public static void createUserInFirebaseHelper(final String mUserEmail, final String mUserName) {
        final String encodedEmail = Utils.encodeEmail(mUserEmail);
        final DatabaseReference userLocation = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_USERS).child(encodedEmail);
        /**
         * See if there is already a user (for example, if they already logged in with an associated
         * Google account.
         */
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                     /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(mUserEmail, mUserName, timestampJoined);
                    userLocation.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, R.string.log_error_occurred + databaseError.getMessage());
            }
        });
    }

    /**
     * Adds values to a pre-existing HashMap for updating a property for all of the ShoppingList copies.
     * The HashMap can then be used with updateChildren to update the property
     * for all ShoppingList copies.
     *
     * @param listId           The id of the shopping list.
     * @param owner            The owner of the shopping list.
     * @param mapToUpdate      The map containing the key, value pairs which will be used
     *                         to update the Firebase database. This MUST be a Hashmap of key
     *                         value pairs who's urls are absolute (i.e. from the root node)
     * @param propertyToUpdate The property to update
     * @param valueToUpdate    The value to update
     * @return The updated HashMap with the new value inserted in all lists
     */
    public static HashMap<String, Object> updateMapForAllWithValue(final String listId,
                                                                   final String owner,
                                                                   HashMap<String, Object> mapToUpdate,
                                                                   String propertyToUpdate,
                                                                   Object valueToUpdate) {
        mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_LISTS + "/" + owner + "/"
                + listId + "/" + propertyToUpdate, valueToUpdate);

        return mapToUpdate;
    }

    /**
     * Adds values to a pre-existing HashMap for updating all Last Changed Timestamps for all of
     * the ShoppingList copies. This method uses {@link #updateMapForAllWithValue} to update the
     * last changed timestamp for all ShoppingList copies.
     *
     * @param listId               The id of the shopping list.
     * @param owner                The owner of the shopping list.
     * @param mapToAddDateToUpdate The map containing the key, value pairs which will be used
     *                             to update the Firebase database. This MUST be a Hashmap of key
     *                             value pairs who's urls are absolute (i.e. from the root node)
     * @return
     */
    public static HashMap<String, Object> updateMapWithTimestampLastChanged(String listId, String owner,
                                                                            HashMap<String, Object> mapToAddDateToUpdate) {

        HashMap<String, Object> timeStampNowHash = new HashMap<>();
        timeStampNowHash.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        updateMapForAllWithValue(listId, owner, mapToAddDateToUpdate,
                Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, timeStampNowHash);
        return mapToAddDateToUpdate;
    }
}
