package com.example.navendu.shoppinglistplusplus.ui.activeListDetails;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.model.ShoppingListItem;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Populates list_view_shopping_list_items inside ActiveListDetailsActivity
 */
public class ActiveListItemAdapter extends FirebaseListAdapter<ShoppingListItem> {
    private String mListId;
    private String mEncodedEmail;
    private ShoppingList mShoppingList;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     *
     * @param activity    current activity
     * @param modelClass  model class we are using for layout
     * @param modelLayout model of data we get in reply
     * @param ref         query URL string
     */
    public ActiveListItemAdapter(Activity activity, Class<ShoppingListItem> modelClass,
                                 int modelLayout, Query ref, String listId, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mListId = listId;
        this.mEncodedEmail = encodedEmail;
    }

    /**
     * Public method that is used to pass shoppingList object when it is loaded in ValueEventListener
     */
    public void setShoppingList(ShoppingList shoppingList) {
        this.mShoppingList = shoppingList;
        this.notifyDataSetChanged();
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_active_list_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View v, ShoppingListItem item, int position) {
        TextView textViewItemName = (TextView) v.findViewById(R.id.text_view_active_list_item_name);
        textViewItemName.setText(item.getItemName());
        
        final TextView textViewBoughtByUser = (TextView) v.findViewById(R.id.text_view_bought_by_user);
        TextView textViewBoughtBy = (TextView) v.findViewById(R.id.text_view_bought_by);

        String owner = item.getOwner();

        ImageButton trashCanButton = (ImageButton) v.findViewById(R.id.button_remove_item);
        final String itemToRemoveId = this.getRef(position).getKey();

        setItemAppearanceBasedOnBoughtStatus(owner, textViewBoughtByUser, textViewBoughtBy,
                trashCanButton, textViewItemName, item);

        trashCanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                        .setTitle(mActivity.getString(R.string.remove_item_option))
                        .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (itemToRemoveId != null) {
                                    removeItem(itemToRemoveId);
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                /* Dismiss the dialog */
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    private void removeItem(String itemId) {
        if (mListId != null) {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL);

            HashMap<String, Object> updatedItemToAddMap = new HashMap<>();

            //Get key of current item
            DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                    .getReferenceFromUrl(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);

            /* Add the item to the update map */
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS + "/" + mListId
                    + "/" + itemId, null);

            /* Make timestamp for last changed */
            HashMap<String, Object> changedTimestampMap = new HashMap<>();
            changedTimestampMap.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            /* Add the updated timestamp */
            updatedItemToAddMap.put("/" + Constants.FIREBASE_LOCATION_ACTIVE_LISTS + "/" + mListId
                    + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, changedTimestampMap);

            /* Do the update */
            firebaseRef.updateChildren(updatedItemToAddMap);
        }
    }

    /**
     * If selected item is bought
     * Set "Bought by" text to "You" if current user is owner of the list
     * Set "Bought by" text to userName if current user is NOT owner of the list
     * Set the remove item button invisible if current user is NOT list or item owner
     */
    public void setItemAppearanceBasedOnBoughtStatus(String owner, final TextView textViewBoughtByUser, TextView textViewBoughtBy,
                                                     ImageButton trashCanButton, TextView textViewItemName, ShoppingListItem item) {

        if (item.isBought() && item.getBoughtBy() != null) {
            textViewBoughtBy.setVisibility(View.VISIBLE);
            textViewBoughtByUser.setVisibility(View.VISIBLE);
            trashCanButton.setVisibility(View.INVISIBLE); //Invisible instead of gone, so that layout still take the space

            /* Add a Strike through item name */
            textViewItemName.setPaintFlags(textViewItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            if (item.getBoughtBy().equals(mEncodedEmail)) {
                textViewBoughtByUser.setText(mActivity.getString(R.string.text_you));
            } else {
                //Get key of current item
                DatabaseReference itemsRef = FirebaseDatabase.getInstance()
                        .getReferenceFromUrl(Constants.FIREBASE_URL_USERS).child(item.getBoughtBy());

                itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            textViewBoughtByUser.setText(user.getName());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(mActivity.getClass().getSimpleName(), mActivity.getString(R.string.log_error_the_read_failed) + databaseError.getMessage());
                    }
                });
            }
        } else {

            /* Remove the strike-through */
            textViewItemName.setPaintFlags(textViewItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textViewBoughtBy.setVisibility(View.INVISIBLE);
            textViewBoughtByUser.setVisibility(View.INVISIBLE);
            textViewBoughtByUser.setText("");
            trashCanButton.setVisibility(View.VISIBLE); //Invisible instead of gone, so that layout still take the spac
        }
    }
}
