package com.example.navendu.shoppinglistplusplus.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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


    /**
     * Return true if currentUserEmail equals to shoppingList.owner()
     * Return false otherwise
     */
    public static boolean checkIfOwner(ShoppingList shoppingList, String currentUserEmail) {
        return (shoppingList.getOwner() != null &&
                shoppingList.getOwner().equals(currentUserEmail));
    }

    /**
     * Encode user email to use it as a Firebase key (Firebase does not allow "." in the key name)
     * Encoded email is also used as "userEmail", list and item "owner" value
     */
    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    public static String decodeEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    /**
     * Adds values to a pre-existing HashMap for updating a property for all of the ShoppingList copies.
     * The HashMap can then be used with updateChildren to update the property
     * for all ShoppingList copies.
     *
     * @param sharedWith       The list of users the shopping list that has been updated is shared with.
     * @param listId           The id of the shopping list.
     * @param owner            The owner of the shopping list.
     * @param mapToUpdate      The map containing the key, value pairs which will be used
     *                         to update the Firebase database. This MUST be a Hashmap of key
     *                         value pairs who's urls are absolute (i.e. from the root node)
     * @param propertyToUpdate The property to update
     * @param valueToUpdate    The value to update
     * @return The updated HashMap with the new value inserted in all lists
     */
    public static HashMap<String, Object> updateMapForAllWithValue(
            final HashMap<String, User> sharedWith,
            final String listId,
            final String owner,
            HashMap<String, Object> mapToUpdate,
            String propertyToUpdate,
            Object valueToUpdate) {
        mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_LISTS + "/" + owner + "/"
                + listId + "/" + propertyToUpdate, valueToUpdate);
        if (sharedWith != null) {
            for (User user : sharedWith.values()) {
                mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USER_LISTS + "/" + user.getEmail() + "/"
                        + listId + "/" + propertyToUpdate, valueToUpdate);
            }
        }
        return mapToUpdate;
    }

    /**
     * Adds values to a pre-existing HashMap for updating all Last Changed Timestamps for all of
     * the ShoppingList copies. This method uses {@link #updateMapForAllWithValue} to update the
     * last changed timestamp for all ShoppingList copies.
     *
     * @param sharedWith           The list of users the shopping list that has been updated is shared with.
     * @param listId               The id of the shopping list.
     * @param owner                The owner of the shopping list.
     * @param mapToAddDateToUpdate The map containing the key, value pairs which will be used
     *                             to update the Firebase database. This MUST be a Hashmap of key
     *                             value pairs who's urls are absolute (i.e. from the root node)
     * @return
     */
    public static HashMap<String, Object> updateMapWithTimestampLastChanged(
            final HashMap<String, User> sharedWith, final String listId, final String owner,
            HashMap<String, Object> mapToAddDateToUpdate) {

        HashMap<String, Object> timestampNowHash = new HashMap<>();
        timestampNowHash.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

        updateMapForAllWithValue(
                sharedWith,
                listId, owner, mapToAddDateToUpdate,
                Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, timestampNowHash);
        return mapToAddDateToUpdate;
    }

    public static void createUserInFirebaseHelper(final String mUserEmail, final String mUserName, final String uid) {
        final String encodedEmail = Utils.encodeEmail(mUserEmail);
        final DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL);
        /**
         * See if there is already a user (for example, if they already logged in with an associated
         * Google account.
         */
                    HashMap<String, Object> userAndUidMapping = new HashMap<String, Object>();
                     /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(encodedEmail, mUserName, timestampJoined);
                    HashMap<String, Object> newUserMap = (HashMap<String, Object>)
                            new ObjectMapper().convertValue(newUser, Map.class);
                     /* Add the user and UID to the update map */
                    userAndUidMapping.put("/" + Constants.FIREBASE_LOCATION_USERS + "/" + encodedEmail,
                            newUserMap);
                    userAndUidMapping.put("/" + Constants.FIREBASE_LOCATION_UID_MAPPINGS + "/"
                            + uid, encodedEmail);
        /* Try to update the database; if there is already a user, this will fail */
        firebaseRef.updateChildren(userAndUidMapping).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                             /* Try just making a uid mapping */
                    firebaseRef.child(Constants.FIREBASE_LOCATION_UID_MAPPINGS)
                            .child(uid).setValue(encodedEmail);
                }
            }
        });
    }

    /**
     * Once an update is made to a ShoppingList, this method is responsible for updating the
     * reversed timestamp to be equal to the negation of the current timestamp. This comes after
     * the updateMapWithTimestampChanged because ServerValue.TIMESTAMP must be resolved to a long
     * value.
     *
     * @param exception          The Firebase error, if there was one, from the original update. This
     *                           method should only run if the shopping list's timestamp last changed
     *                           was successfully updated.
     * @param logTagFromActivity The log tag from the activity calling this method
     * @param listId             The updated shopping list push ID
     * @param sharedWith         The list of users that this updated shopping list is shared with
     * @param owner              The owner of the updated shopping list
     */
    public static void updateTimestampReversed(Exception exception, final String logTagFromActivity,
                                               final String listId, final HashMap<String, User> sharedWith,
                                               final String owner) {
        if (exception != null) {
            Log.d(logTagFromActivity, "Error updating timestamp: " + exception.getMessage());
        } else {
            final DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL);
            firebaseRef.child(Constants.FIREBASE_LOCATION_USER_LISTS).child(owner)
                    .child(listId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ShoppingList list = dataSnapshot.getValue(ShoppingList.class);
                    if (list != null) {
                        long timeReverse = -(list.getTimestampLastChangedLong());
                        String timeReverseLocation = Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED_REVERSE
                                + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP;

                        /**
                         * Create map and fill it in with deep path multi write operations list
                         */
                        HashMap<String, Object> updatedShoppingListData = new HashMap<String, Object>();

                        updateMapForAllWithValue(sharedWith, listId, owner, updatedShoppingListData,
                                timeReverseLocation, timeReverse);
                        firebaseRef.updateChildren(updatedShoppingListData);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(logTagFromActivity, "Error updating data: " + databaseError.getMessage());
                }
            });
        }
    }

}