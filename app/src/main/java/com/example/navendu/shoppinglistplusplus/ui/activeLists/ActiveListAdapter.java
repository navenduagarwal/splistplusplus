package com.example.navendu.shoppinglistplusplus.ui.activeLists;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.utils.Utils;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.Query;

/**
 * Firebase adapter for populating shopping list
 */
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {
    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout, Query ref) {
        super(activity, modelClass, modelLayout, ref);
    }


    /**
     * Protected method that populates the view attached to the adapter (list_view_active_lists)
     * with items inflated from single_active_list.xml
     * populateView also handles data changes and updates the listView accordingly
     */

    @Override
    protected void populateView(View v, ShoppingList model, int position) {
        TextView mTextViewListName = (TextView) v.findViewById(R.id.text_view_list_name);
        TextView mTextViewListCreatedBy = (TextView) v.findViewById(R.id.text_view_created_by_user);
        TextView mTextViewEditTime = (TextView) v.findViewById(R.id.text_view_edit_time);

        mTextViewListName.setText(model.getListName());
        mTextViewListCreatedBy.setText(model.getOwner());
        if (model.getTimestampLastChanged() != null) {
            mTextViewEditTime.setText(
                    Utils.SIMPLE_DATE_FORMAT.format(model.getTimestampLastChangedLong()));
        } else {
            mTextViewEditTime.setText("Not Available");
        }
    }
}
