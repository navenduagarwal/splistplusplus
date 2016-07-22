package com.example.navendu.shoppinglistplusplus.ui.activeListDetails;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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
    EditText mEditTextItemName;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String listId) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();
        Bundle bundle = newInstanceHelper(shoppingList, R.layout.dialog_add_item, listId);
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

    /**
     * Open the keyboard automatically when the dialog fragment is opened
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_add_item, null);
        mEditTextItemName = (EditText) rootView.findViewById(R.id.edit_text_list_dialog);

        /**
         * Call addShoppingList() when user taps "Done" keyboard action
         */
        mEditTextItemName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    doListEdit();
                }
                return true;
            }
        });

        /* Inflate and set the layout for the dialog */
        /* Pass null as the parent view because its going in the dialog layout*/
        builder.setView(rootView)
                /* Add action buttons */
                .setPositiveButton(R.string.positive_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        doListEdit();
                    }
                });

        return builder.create();
    }

    /**
     * Adds new item to the current shopping list and update timestampLastChanged of list
     * updateChildren used to avoid multiple setValue
     */
    @Override
    protected void doListEdit() {
        String userEnteredItemName = mEditTextItemName.getText().toString();
        String owner = "Anonymous Owner";

        /**
         * If EditText input is not empty
         */
        if (!userEnteredItemName.equals("")) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL);

            HashMap<String, Object> updatedItemToAddMap = new HashMap<>();

            //Adding new item key
            DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);
            DatabaseReference newListRef = itemsRef.push();
            final String itemId = newListRef.getKey();

            ShoppingListItem itemToAddObject = new ShoppingListItem(userEnteredItemName, owner);
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
