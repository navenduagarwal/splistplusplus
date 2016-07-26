package com.example.navendu.shoppinglistplusplus.ui.activeListDetails;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.model.ShoppingListItem;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.example.navendu.shoppinglistplusplus.ui.BaseActivity;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.example.navendu.shoppinglistplusplus.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the details screen for the selected shopping list
 */
public class ActiveListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private ListView mListView;
    private ShoppingList mShoppingList;
    private DatabaseReference mCurrentListRef, mCurrentUserRef;
    private String mListId;
    private ActiveListItemAdapter mActiveListItemAdapter;
    private ValueEventListener mCurrentListRefListener, mCurrentUserRefListener;
    private Button mButtonShopping;
    private User mCurrentUser;
    private Boolean mShopping = false;
    private TextView mTextViewPeopleShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);

        /* Get the push ID from the extra passed by ShoppingListFragment */
        Intent intent = this.getIntent();
        mListId = intent.getStringExtra(Constants.KEY_LIST_ID);
        if (mListId == null) {
            /* No point in continuing without a valid ID. */
            finish();
            return;
        }

        /**
         * Firebase reference
         */
        mCurrentListRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_USER_LISTS).child(mEncodedEmail).child(mListId);

        mCurrentListRefListener = mCurrentListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);
                if (shoppingList != null) {
                    mShoppingList = shoppingList;
                    invalidateOptionsMenu();
                    setTitle(shoppingList.getListName());
                    mActiveListItemAdapter.setShoppingList(mShoppingList);
                    HashMap<String, User> usersShopping = shoppingList.getUsersShopping();
                    if (usersShopping != null && usersShopping.size() != 0
                            && usersShopping.containsKey(mEncodedEmail)) {
                        mShopping = true;
                        mButtonShopping.setText(getString(R.string.button_stop_shopping));
                        mButtonShopping.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.dark_grey));
                    } else {
                        mShopping = false;
                        mButtonShopping.setText(getString(R.string.button_start_shopping));
                        mButtonShopping.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));
                    }
                    setWhosShoppingText(mShoppingList.getUsersShopping());
                } else {
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) + databaseError.getMessage());
            }
        });

        /**
         * Keep the most upto date version of current user
         */
        /*Create Firebase references */
        mCurrentUserRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);

        /**
         * Adding ValueListener to control get data and visibiliy of elements on UI
         */
        mCurrentUserRefListener = mCurrentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    mCurrentUser = user;
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, R.string.log_error_occurred + databaseError.getMessage());
            }
        });

        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        /**
         * Create Firebase references to populate items list
         */
        DatabaseReference itemsListRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);


        mActiveListItemAdapter = new ActiveListItemAdapter(this, ShoppingListItem.class,
                R.layout.single_active_list_item, itemsListRef.orderByChild(Constants.FIREBASE_PROPERTY_BOUGHT), mListId, mEncodedEmail);

        /**
         * Set the adapter to the mListView
         */
        mListView.setAdapter(mActiveListItemAdapter);

        /**
         * Perform buy/return action on listView item click event if current user is shopping.
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                /* Check the view is not empty footer */
                if (view.getId() != R.id.list_view_footer_empty) {
                    final ShoppingListItem selectedListItem = mActiveListItemAdapter.getItem(position);
                    String itemId = mActiveListItemAdapter.getRef(position).getKey();

                    if (selectedListItem != null && mShopping) {

                        HashMap<String, Object> updatedItemBoughtData = new HashMap<String, Object>();

                        if (!selectedListItem.isBought()) {
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT, true);
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT_BY, mEncodedEmail);
                        } else if (selectedListItem.getBoughtBy().equals(mEncodedEmail)) {
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT, false);
                            updatedItemBoughtData.put(Constants.FIREBASE_PROPERTY_BOUGHT_BY, null);
                        }

                        /* Get Item Firebase Ref */
                        DatabaseReference itemRef = FirebaseDatabase.getInstance()
                                .getReferenceFromUrl(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS)
                                .child(mListId).child(itemId);
                        /* Update Item */
                        itemRef.updateChildren(updatedItemBoughtData)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Log.d(LOG_TAG, getString(R.string.log_error_updating_data) + task.getException().getMessage());
                                        }
                                    }
                                });
                    }
                }
            }
        });

        /* Show edit list item name dialog on listView item long click event */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            /**
             * If the user is the shopping list's owner or the item's owner,
             * not shopping on the list and the item is not bought,
             * then they can edit the item's name.
             */
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if (view.getId() != R.id.list_view_footer_empty) {
                    ShoppingListItem shoppingListItem = mActiveListItemAdapter.getItem(position);
                    if (shoppingListItem != null) {
                        if ((shoppingListItem.getOwner().equals(mEncodedEmail)
                                || mShoppingList.getOwner().equals(mEncodedEmail))
                                && !mShopping && !shoppingListItem.isBought()) {
                            String itemName = shoppingListItem.getItemName();
                            String itemId = mActiveListItemAdapter.getRef(position).getKey();
                            showEditListItemNameDialog(itemName, itemId);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details, menu);

        /**
         * Get menu items
         */
        MenuItem remove = menu.findItem(R.id.action_remove_list);
        MenuItem edit = menu.findItem(R.id.action_edit_list_name);
        MenuItem share = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /**
         * Set visibility of menu
         */
        if (mShoppingList != null) {
            if (mShoppingList.getOwner().equals(mEncodedEmail)) {

        /* Only the edit and remove options are implemented */
                remove.setVisible(true);
                edit.setVisible(true);
                share.setVisible(false);
                archive.setVisible(false);
            } else {
                remove.setVisible(false);
                edit.setVisible(false);
                share.setVisible(false);
                archive.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Show edit list dialog when the edit action is selected
         */
        if (id == R.id.action_edit_list_name) {
            showEditListNameDialog();
            return true;
        }
        /**
         * removeList() when the remove action is selected
         */
        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

        /**
         * Eventually we'll add this
         */
        if (id == R.id.action_share_list) {
            return true;
        }

        /**
         * archiveList() when the archive action is selected
         */
        if (id == R.id.action_archive) {
            archiveList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mActiveListItemAdapter.cleanup();
        mCurrentListRef.removeEventListener(mCurrentListRefListener);
        mCurrentUserRef.removeEventListener(mCurrentUserRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        mTextViewPeopleShopping = (TextView) findViewById(R.id.text_view_people_shopping);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        /* Common toolbar setup */
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);
        mListView.addFooterView(footer);
        mButtonShopping = (Button) findViewById(R.id.button_shopping);
    }

    private void setWhosShoppingText(HashMap<String, User> usersShopping) {

        if (usersShopping != null) {

            ArrayList<String> usersWhoAreNotYou = new ArrayList<>();
            int numberOfUsersShopping = usersShopping.size();

            /**
             * Creating Array List of shoppers other than current user
             */
            for (User user : usersShopping.values()) {
                if (user != null && !(user.getEmail().equals(mEncodedEmail))) {
                    usersWhoAreNotYou.add(user.getName());
                }
            }
            String currentShoppersText;

            /**
             * If current user is shopping...
             * If current user is the only person shopping, set text to "You are shopping"
             * If current user and one user are shopping, set text "You and userName are shopping"
             * Else set text "You and N others shopping"
             */
            if (mShopping) {
                switch (numberOfUsersShopping) {
                    case 1:
                        currentShoppersText = getString(R.string.text_you_are_shopping);
                        break;
                    case 2:
                        currentShoppersText = String.format(getString(R.string.text_you_and_other_are_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    default:
                        currentShoppersText = String.format(getString(R.string.text_you_and_number_are_shopping),
                                usersWhoAreNotYou.size());
                        break;
                }
            }
            /**
             *If current user is not shopping..
             * If there is only one person shopping, set text to "userName is shopping"
             * If there are two users shopping, set text "userName1 and userName2 are shopping"
             * Else set text "userName and N others shopping"
             */
            else {
                switch (numberOfUsersShopping) {
                    case 1:
                        currentShoppersText = String.format(getString(R.string.text_other_is_shopping),
                                usersWhoAreNotYou.get(0));
                        break;
                    case 2:
                        currentShoppersText = String.format(getString(R.string.text_other_and_other_are_shopping),
                                usersWhoAreNotYou.get(0), usersWhoAreNotYou.get(1));
                        break;
                    default:
                        currentShoppersText = String.format(getString(R.string.text_other_and_number_are_shopping),
                                usersWhoAreNotYou.get(0),
                                usersWhoAreNotYou.size() - 1);
                }
            }
            mTextViewPeopleShopping.setText(currentShoppersText);
        } else {
            mTextViewPeopleShopping.setText("");
        }
    }


    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {
    }


    /**
     * Start AddItemsFromMealActivity to add meal ingredients into the shopping list
     * when the user taps on "add meal" fab
     */
    public void addMeal(View view) {
    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList, mListId, mEncodedEmail);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList, mListId, mEncodedEmail);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String itemName, String itemId) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList, mListId, itemName, itemId,
                mEncodedEmail);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     * If user is already shopping then remove it, else add it to the list
     */
    public void toggleShopping(View view) {

        DatabaseReference firebaseRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL);

        HashMap<String, Object> updatedUserData = new HashMap<String, Object>();
        String propertyToUpdate = Constants.FIREBASE_PROPERTY_USERS_SHOPPING + "/" + mEncodedEmail;

        if (mShopping) {

            /** Remove shopper from user's list **/
            Utils.updateMapForAllWithValue(mListId, mShoppingList.getOwner(), updatedUserData,
                    propertyToUpdate, null);

            /** Update timestamp **/
            Utils.updateMapWithTimestampLastChanged(mListId, mShoppingList.getOwner(), updatedUserData);

            /**Do a deep-path update **/
            firebaseRef.updateChildren(updatedUserData);

        } else {
            /** Add to current list of users **/

            //If current user is not shopping, create map to represent User model add to usersShopping map
            HashMap<String, Object> currentUser = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(mCurrentUser, Map.class);

            /* Add the value to update at the specified property for all lists */
            Utils.updateMapForAllWithValue(mListId, mShoppingList.getOwner(), updatedUserData,
                    propertyToUpdate, currentUser);

            /** Update timestamp **/
            Utils.updateMapWithTimestampLastChanged(mListId, mShoppingList.getOwner(), updatedUserData);

            /**Do a deep-path update **/
            firebaseRef.updateChildren(updatedUserData);

        }
    }


}