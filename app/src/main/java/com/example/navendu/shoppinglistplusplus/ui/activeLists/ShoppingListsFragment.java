package com.example.navendu.shoppinglistplusplus.ui.activeLists;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.ui.activeListDetails.ActiveListDetailsActivity;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.firebase.client.Firebase;

/**
 * Created by navendu on 7/21/2016.
 */
public class ShoppingListsFragment extends Fragment {
    private static String LOG_TAG = ShoppingListsFragment.class.getSimpleName();
    private ListView mListView;
    private TextView mTextViewListName;
    private TextView mTextViewOwnerName;
    private TextView mTextViewEditTime;

    public ShoppingListsFragment() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static ShoppingListsFragment newInstance() {
        ShoppingListsFragment fragment = new ShoppingListsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Initalize UI elements
         */
        View rootView = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
        initializeScreen(rootView);

        /**
         * Create Firebase references
         */
        Firebase refListName = new Firebase(Constants.FIREBASE_URL).child(Constants.FIREBASE_LOCATION_ACTIVE_LIST);

        /**
         * Add valueEventListener to Firebase references
         * to control get data and control behaviour and visibility of elements
         */
//        refListName.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d(LOG_TAG, "The data has changed");
//
//                // You can get the text using getValue. Since the DataSnapshot is of the exact
//                // data you asked for (the node listName), when you use getValue you know it
//                // will return a String.
//                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);
//                //Now take the TextView for the list Name
//                //set it's value to shopping list
//                if (shoppingList != null) {
//                    mTextViewListName.setText(shoppingList.getListName());
//                    mTextViewOwnerName.setText(shoppingList.getOwner());
//                    if (shoppingList.getTimestampLastChanged() != null) {
//                        mTextViewEditTime.setText(
//
//                                Utils.SIMPLE_DATE_FORMAT.format(shoppingList.getTimestampLastChangedLong()));
//                    } else {
//                        mTextViewEditTime.setText("Not Available");
//                    }
//                }
//            }

//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//                Log.e(LOG_TAG,getString(R.string.log_error_the_read_failed)+firebaseError.getMessage());
//            }
//        });

        /**
         * Set interactive bits, such as click events and adapters
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        // TODO Add an OnClick listener here so that when the user clicks on the
        mTextViewListName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addIntent = new Intent(getActivity(), ActiveListDetailsActivity.class);
                startActivity(addIntent);
            }
        });

        // mTextViewListName it opens up an instance of ActiveListDetailsActivity

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list_view_active_lists);
        mTextViewListName = (TextView) rootView.findViewById(R.id.text_view_list_name);
        mTextViewOwnerName = (TextView) rootView.findViewById(R.id.text_view_created_by_user);
        mTextViewEditTime = (TextView) rootView.findViewById(R.id.text_view_edit_time);
    }
}
