package com.example.navendu.shoppinglistplusplus.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.model.User;
import com.example.navendu.shoppinglistplusplus.ui.BaseActivity;
import com.example.navendu.shoppinglistplusplus.ui.MainActivity;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.example.navendu.shoppinglistplusplus.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Represents Sign in screen and functionality of the app
 */
public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_SIGN_IN = 9001;
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;
    private String mUserEmail, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mSharedPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefEditor = mSharedPref.edit();
        mAuth = FirebaseAuth.getInstance();
        initializeScreen();
        /**
         * Call signInPassword() when user taps "Done" keyboard action
         */
        mEditTextPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    signInPassword();
                }
                return true;
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mAuthProgressDialog.dismiss();
                FirebaseUser user = firebaseAuth.getCurrentUser();
                /**
                 * If there is a valid session to be restored, start MainActivity.
                 * No need to pass data via SharedPreferences because app
                 * already holds userName/provider data from the latest session
                 */

                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        mAuth.addAuthStateListener(mAuthStateListener);
        /**
         * Get the newly registered user email if present, use null as default value
         */
        String signupEmail = mSharedPref.getString(Constants.KEY_SIGNUP_EMAIL, null);
        /**
         * fill in the email editText and remove value from SharedPreferences if email is present
         */

        if (signupEmail != null) {
            mEditTextEmailInput.setText(signupEmail);

            /**
             * Clear signupEmail sharedPreferences to make sure that they are used just once
             */
            mSharedPrefEditor.putString(Constants.KEY_SIGNUP_EMAIL, null).apply();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    /**
     * Sign in with Password provider when user clicks sign in button
     */
    public void onSignInPressed(View view) {
        signInPassword();
    }

    /**
     * Open CreateAccountActivity when user taps on "Sign up" TextView
     */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = (EditText) findViewById(R.id.edit_text_password);
        LinearLayout linearLayoutLoginActivity = (LinearLayout) findViewById(R.id.linear_layout_login_activity);
        initializeBackground(linearLayoutLoginActivity);
        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
        mAuthProgressDialog.setCancelable(false);
        /* Setup Google Sign In */
        setupGoogleSignIn();
    }

    /**
     * Sign in with Password provider (used when user taps "Done" action on keyboard)
     */
    public void signInPassword() {

        mUserEmail = mEditTextEmailInput.getText().toString();
        mPassword = mEditTextPasswordInput.getText().toString();

        /* validation on text fields */
        boolean validUserEmail = isEmailValid(mUserEmail);
        boolean validPassword = isPasswordValid(mPassword);

        if (!validPassword || !validUserEmail) {
            return;
        }

        /**
         * If everything was valid show the progress dialog to indicate that
         * account creation has started
         */
        mAuthProgressDialog.show();
        mAuth.signInWithEmailAndPassword(mUserEmail, mPassword)
                .addOnCompleteListener(this, new MyAuthResultHandler(Constants.PASSWORD_PROVIDER));
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's email/password provider.
     */
    private void setAuthenticatedUserPasswordProvider(UserInfo user) {
        final String unprocessedEmail = user.getEmail().toLowerCase();
        mEncodedEmail = Utils.encodeEmail(unprocessedEmail);
        final DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);
        /**
         * Check if current user has logged in at least once
         */
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    /**
                     * If recently registered user has hasLoggedInWithPassword = "false"
                     * (never logged in using password provider)
                     */
                    if (!user.isHasLoggedInWithPassword()) {
                        userRef.child(Constants.FIREBASE_PROPERTY_USER_HAS_LOGGED_IN_WITH_PASSWORD).setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's Google login provider.
     */
    private void setAuthenticatedUserGoogle(UserInfo user) {
        final String unprocessedEmail = user.getEmail().toLowerCase();
        mEncodedEmail = Utils.encodeEmail(unprocessedEmail);
        final String userName = user.getDisplayName();
        Utils.createUserInFirebaseHelper(mEncodedEmail, userName, user.getUid());
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * GOOGLE SIGN IN CODE
     */

    /* Sets up the Google Sign In Button : https://developers.google.com/android/reference/com/google/android/gms/common/SignInButton */
    private void setupGoogleSignIn() {
        SignInButton signInButton = (SignInButton) findViewById(R.id.login_with_google);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignInGooglePressed(v);
            }
        });
    }

    /**
     * Sign in with Google plus when user clicks "Sign in with Google" textView (button)
     */
    public void onSignInGooglePressed(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
        mAuthProgressDialog.show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        /**
         * An unresolvable error has occurred and Google APIs (including Sign-In) will not
         * be available.
         */
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + result);
        mAuthProgressDialog.dismiss();
        showErrorToast("Google Play Services error." + result.toString());
    }

    /**
     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                showErrorToast("Login Failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + acct.getId());
        mAuthProgressDialog.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new MyAuthResultHandler(Constants.GOOGLE_PROVIDER));
    }

    private boolean isEmailValid(String email) {
        if (TextUtils.isEmpty(email)) {
            mEditTextEmailInput.setError(getString(R.string.error_cannot_be_empty));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEditTextEmailInput.setError(String.format(getString(R.string.error_invalid_email_not_valid), email));
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            mEditTextPasswordInput.setError(getString(R.string.error_cannot_be_empty));
            return false;
        } else if (password.length() < 6) {
            mEditTextPasswordInput.setError(getResources().getString(R.string.error_invalid_password_not_valid));
            return false;
        }
        return true;
    }

    private class MyAuthResultHandler implements OnCompleteListener<AuthResult> {

        private final String provider;

        public MyAuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            Log.i(LOG_TAG, provider + " " + getString(R.string.log_message_auth_successful));

            if (!task.isSuccessful()) {
                mAuthProgressDialog.dismiss();
                            /* Error occurred, log the error and dismiss the progress dialog */
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidUserException e) {
                    mEditTextEmailInput.setError(getString(R.string.error_message_email_issue));
                    mEditTextEmailInput.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    mEditTextPasswordInput.setError(e.getMessage());
                    mEditTextPasswordInput.requestFocus();
                } catch (FirebaseAuthUserCollisionException e) {
                    showErrorToast("User already exists and collison - need to handle this case");
                } catch (FirebaseNetworkException e) {
                    showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
                } catch (Exception e) {
                    showErrorToast(e.getMessage());
                    Log.e(LOG_TAG, e.getMessage());
                }
            } else {
                mAuthProgressDialog.dismiss();
                Log.i(LOG_TAG, " " + getString(R.string.log_message_auth_successful));

                UserInfo user = task.getResult().getUser().getProviderData().get(0);
                String userProvider = task.getResult().getUser().getProviderData().get(1).getProviderId();
                if (user != null) {
                    if (userProvider.equals(Constants.PASSWORD_PROVIDER)) {
                        setAuthenticatedUserPasswordProvider(user);
                    } else if (userProvider.equals(Constants.GOOGLE_PROVIDER)) {
                        setAuthenticatedUserGoogle(user);
                    } else {
                        Log.e(LOG_TAG, getString(R.string.log_error_invalid_provider) + userProvider);
                    }
                     /* Save provider name and encodedEmail for later use and start MainActivity */
                    mSharedPrefEditor.putString(Constants.KEY_PROVIDER_ID, userProvider).apply();
                    mSharedPrefEditor.putString(Constants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();

                /* Go to main activity */
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(LOG_TAG, "user not found");
                }
            }
        }
    }
}