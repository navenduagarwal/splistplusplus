package com.example.navendu.shoppinglistplusplus.ui.sharing;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.example.navendu.shoppinglistplusplus.utils.Utils;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Populates the list_view_friends_autocomplete inside AddFriendActivity
 */
public class AutocompleteFriendAdapter extends FirebaseListAdapter<User> {
    private String mEncodedEmail;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public AutocompleteFriendAdapter(Activity activity, Class<User> modelClass, int modelLayout,
                                     Query ref, String encodedEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mEncodedEmail = encodedEmail;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_autocomplete_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, final User user, int position) {
        TextView textViewFriendEmail = (TextView) view.findViewById(R.id.text_view_autocomplete_item);
        textViewFriendEmail.setText(Utils.decodeEmail(user.getEmail()));

        textViewFriendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNotCurrentUser(user)) {

                    DatabaseReference currentUserFriendsRef = FirebaseDatabase.getInstance()
                            .getReferenceFromUrl(Constants.FIREBASE_URL_USER_FRIENDS).child(mEncodedEmail);

                    final DatabaseReference friendRef = currentUserFriendsRef.child(user.getEmail());

                    /**
                     * Add listener for single value event to perform a one time operation
                     */
                    friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            /**
                             * Add selected user to current user's friends if not in friends yet
                             */

                            if (isNotAlreadyAdded(dataSnapshot, user)) {
                                friendRef.setValue(user);
                                mActivity.finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(mActivity.getClass().getSimpleName()
                                    , mActivity.getString(R.string.log_error_the_read_failed) + databaseError.getMessage());
                        }
                    });
                }

            }
        });
    }

    /**
     * Checks if the friend you try to add is the current user
     **/
    private boolean isNotCurrentUser(User user) {
        if (user.getEmail().equals(mEncodedEmail)) {
            /* Toast appropriate error message if the user is trying to add themselves  */
            Toast.makeText(mActivity,
                    mActivity.getResources().getString(R.string.toast_you_cant_add_yourself),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    /**
     * Checks if the friend you try to add is already added, given a dataSnapshot of a user
     **/
    private boolean isNotAlreadyAdded(DataSnapshot dataSnapshot, User user) {
        if (dataSnapshot.getValue(User.class) != null) {
             /* Toast appropriate error message if the user is already a friend of the user */
            String friendError = String.format(mActivity.getResources().
                            getString(R.string.toast_is_already_your_friend),
                    user.getName());

            Toast.makeText(mActivity,
                    friendError,
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
