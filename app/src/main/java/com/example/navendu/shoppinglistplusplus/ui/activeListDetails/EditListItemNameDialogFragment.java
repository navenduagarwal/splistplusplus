package com.example.navendu.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.os.Bundle;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.example.navendu.shoppinglistplusplus.utils.Utils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Lets user edit list item name for all copies of the current list
 */
public class EditListItemNameDialogFragment extends EditListDialogFragment {
    String mItemName;
    String mItemId;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListItemNameDialogFragment newInstance(ShoppingList shoppingList, String listId,
                                                             String itemName, String itemId
            , String encodedEmail, HashMap<String, User> sharedWithUsers) {
        EditListItemNameDialogFragment editListItemNameDialogFragment = new EditListItemNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_item,
                listId, encodedEmail, sharedWithUsers);
        bundle.putString(Constants.KEY_LIST_ITEM_NAME, itemName);
        bundle.putString(Constants.KEY_LIST_ITEM_ID, itemId);
        editListItemNameDialogFragment.setArguments(bundle);

        return editListItemNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItemName = getArguments().getString(Constants.KEY_LIST_ITEM_NAME);
        mItemId = getArguments().getString(Constants.KEY_LIST_ITEM_ID);
    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         */
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        //set default name in edit dialog box
        super.helpSetDefaultValueEditText(mItemName);
        return dialog;
    }

    /**
     * Change selected list item name to the editText input if it is not empty
     */
    protected void doListEdit() {
        final String inputItemName = mEditTextForList.getText().toString();
        /**
         *Set input text to the current list if it is not empty
         */
        if (!inputItemName.equals("") && !inputItemName.equals(mItemName)) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL);

            /* Make a map for the item you are changing the name of */
            HashMap<String, Object> updatedDataItemToEditMap = new HashMap<>();

            /* Add the item to the update map */
            updatedDataItemToEditMap.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/" + mListId
                    + "/" + mItemId + "/" + Constants.FIREBASE_PROPERTY_ITEM_NAME, inputItemName);

            /* updated timestamp in all lists*/
            Utils.updateMapWithTimestampLastChanged(mSharedWith, mListId, mOwner, updatedDataItemToEditMap);

            /* Do the update */
            firebaseRef.updateChildren(updatedDataItemToEditMap);
        }
    }
}