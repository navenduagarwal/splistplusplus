package com.example.navendu.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.model.ShoppingListItem;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets user add new list item
 */
public class AddListItemDialogFragment extends EditListDialogFragment {

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String listId,
                                                        String encodedEmail) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList,
                R.layout.dialog_add_item, listId, encodedEmail);
        addListItemDialogFragment.setArguments(bundle);
        return addListItemDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    /**
     * Adds new item to the current shopping list and update timestampLastChanged of list
     * updateChildren used to avoid multiple setValue
     */
    @Override
    protected void doListEdit() {
        String mItemName = mEditTextForList.getText().toString();
        /**
         * If EditText input is not empty
         */
        if (!mItemName.equals("")) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL);

            HashMap<String, Object> updatedItemToAddMap = new HashMap<>();

            //Adding new item key
            DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);
            DatabaseReference newListRef = itemsRef.push();
            final String itemId = newListRef.getKey();

            ShoppingListItem itemToAddObject = new ShoppingListItem(mItemName, mEncodedEmail);
            HashMap<String, Object> itemToAdd = (HashMap<String, Object>) new ObjectMapper()
                    .convertValue(itemToAddObject, Map.class);

            /* Add the item to the update map */
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/" + mListId
                    + "/" + itemId, itemToAdd);

            /* Make timestamp for last changed */
            HashMap<String, Object> changedTimestampMap = new HashMap<>();
            changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            /* Add the updated timestamp */
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS + "/" + mListId
                    + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, changedTimestampMap);

            /* Do the update */
            firebaseRef.updateChildren(updatedItemToAddMap);

            /*Close the dialog fragment */
            AddListItemDialogFragment.this.getDialog().cancel();
        }

    }
}
