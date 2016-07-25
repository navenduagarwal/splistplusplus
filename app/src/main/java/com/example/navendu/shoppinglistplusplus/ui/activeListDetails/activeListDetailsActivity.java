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

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.ShoppingList;
import com.example.navendu.shoppinglistplusplus.model.ShoppingListItem;
import com.example.navendu.shoppinglistplusplus.ui.BaseActivity;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Represents the details screen for the selected shopping list
 */
public class ActiveListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    private ListView mListView;
    private ShoppingList mShoppingList;
    private DatabaseReference mActiveListRef;
    private String mListId;
    private ActiveListItemAdapter mActiveListItemAdapter;
    private ValueEventListener mActiveListRefListener;
    private Button mButtonShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);
        /**
         * Firebase reference
         */

        /* Get the push ID from the extra passed by ShoppingListFragment */
        Intent intent = this.getIntent();
        mListId = intent.getStringExtra(Constants.KEY_LIST_ID);
        if (mListId == null) {
            /* No point in continuing without a valid ID. */
            finish();
            return;
        }
        mActiveListRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_ACTIVE_LISTS).child(mListId);

        mActiveListRefListener = mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);
                if (shoppingList != null) {
                    mShoppingList = shoppingList;
                    // detail screen using setTitle and passing in the name of the current
                    // shopping list. You might want to save this shopping list as well.
                    // You can but the invalidateOptionsMenu call inside of the same block of code.
                    // If the shopping list doesn't exist, close the activity using finish()

                    /* Calling invalidateOptionsMenu causes onCreateOptionsMenu to be called */
                    invalidateOptionsMenu();
                    setTitle(shoppingList.getListName());
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
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        mButtonShopping.setText(getString(R.string.button_start_shopping));
        mButtonShopping.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));


        /**
         * Create Firebase references to populate items list
         */
        DatabaseReference itemsListRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);
        mActiveListItemAdapter = new ActiveListItemAdapter(this, ShoppingListItem.class,
                R.layout.single_active_list_item, itemsListRef, mListId, mEncodedEmail);

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

                    if (selectedListItem != null) {

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

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if (view.getId() != R.id.list_view_footer_empty) {
                    ShoppingListItem shoppingListItem = mActiveListItemAdapter.getItem(position);
                    if (shoppingListItem != null) {
                        String itemName = shoppingListItem.getItemName();
                        String itemId = mActiveListItemAdapter.getRef(position).getKey();
                        showEditListItemNameDialog(itemName, itemId);
                        return true;
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
        mActiveListRef.removeEventListener(mActiveListRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
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
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList, mListId);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String itemName, String itemId) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList, mListId, itemName, itemId);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {

    }

}