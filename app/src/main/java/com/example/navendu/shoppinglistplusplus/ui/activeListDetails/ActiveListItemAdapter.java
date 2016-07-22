package com.example.navendu.shoppinglistplusplus.ui.activeListDetails;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingListItem;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.Query;

/**
 * Populates list_view_shopping_list_items inside ActiveListDetailsActivity
 */
public class ActiveListItemAdapter extends FirebaseListAdapter<ShoppingListItem> {

    /**
     * Public constructor that initializes private instance variables when adapter is created
     *
     * @param activity    current activity
     * @param modelClass  model class we are using for layout
     * @param modelLayout model of data we get in reply
     * @param ref         query URL string
     */
    public ActiveListItemAdapter(Activity activity, Class<ShoppingListItem> modelClass, int modelLayout, Query ref) {
        super(activity, modelClass, modelLayout, ref);
    }


    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_active_list_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View v, ShoppingListItem item, int position) {
        TextView mTextViewItemName = (TextView) v.findViewById(R.id.text_view_active_list_item_name);
        mTextViewItemName.setText(item.getItemName());
        ImageButton trashCanButton = (ImageButton) v.findViewById(R.id.button_remove_item);
        trashCanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                        .setTitle(mActivity.getString(R.string.remove_item_option))
                        .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeItem();
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

    private void removeItem() {
    }
}
