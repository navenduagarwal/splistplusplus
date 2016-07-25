package com.example.navendu.shoppinglistplusplus.ui.activeLists;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Firebase adapter for populating shopping list
 */
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {
    private String mEncodedEmail;

    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout, Query ref,
                             String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mEncodedEmail = encodedEmail;
        this.mActivity = activity;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_active_lists)
     * with items inflated from single_active_list.xml
     * populateView also handles data changes and updates the listView accordingly
     */

    @Override
    protected void populateView(View v, ShoppingList model, int position) {
        TextView mTextViewListName = (TextView) v.findViewById(R.id.text_view_list_name);
        final TextView mTextViewListCreatedBy = (TextView) v.findViewById(R.id.text_view_created_by_user);
        final TextView mTextViewPeopleShopping = (TextView) v.findViewById(R.id.text_view_people_shopping_count);
        String ownerEmail = model.getOwner();
        mTextViewListName.setText(model.getListName());
        mTextViewListCreatedBy.setText(model.getOwner());

        /**
         * Updating Created By Text
         */
        if (ownerEmail != null) {
            if (mEncodedEmail.equals(ownerEmail)) {
                mTextViewListCreatedBy.setText(mActivity.getString(R.string.text_you));
            } else {
                DatabaseReference ownersRef = FirebaseDatabase.getInstance()
                        .getReferenceFromUrl(Constants.FIREBASE_URL_USERS).child(ownerEmail);
                /* Get Owners name */
                ownersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User owner = dataSnapshot.getValue(User.class);
                        if (owner != null) {
                            mTextViewListCreatedBy.setText(owner.getName());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(mActivity.getClass().getSimpleName(), mActivity.getString(R.string.log_error_the_read_failed) + databaseError.getMessage());
                    }
                });
            }
        }

        /**
         * Updating Number of Current Shopper's List
         * Show "1 person is shopping" if one person is shopping
         * Show "N people shopping" if two or more users are shopping
         * Show nothing if nobody is shopping
         */
        if (model.getUsersShopping() != null) {
            int shoppers = model.getUsersShopping().size();
            switch (shoppers) {
                case 1:
                    mTextViewPeopleShopping.setText(
                            String.format(mActivity.getString(R.string.person_shopping), shoppers)
                    );
                    break;
                default:
                    mTextViewPeopleShopping.setText(
                            String.format(mActivity.getString(R.string.people_shopping), shoppers)
                    );
                    break;
            }
        } else {
            /* otherwise show nothing */
            mTextViewPeopleShopping.setText("");
        }
    }
}
