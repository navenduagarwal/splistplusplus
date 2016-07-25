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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

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


    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /**
         * Link layout elements from XML and setup progress dialog
         */
        initializeScreen();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    String firebaseEmail = user.getEmail();
                    UserInfo userInfo = user.getProviderData().get(0);
                    String firebaseUserName = userInfo.getDisplayName();
                       /* Make a hashmap for the specific properties we are changing */
                    String encodedEmail = Utils.encodeEmail(firebaseEmail);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor spe = sharedPreferences.edit();
                    spe.putString(Constants.KEY_ENCODED_EMAIL, encodedEmail);
                    spe.putString(Constants.KEY_PROVIDER_ID, user.getProviderId());
                    spe.apply();

                    Utils.createUserInFirebaseHelper(firebaseEmail, firebaseUserName);
                    /* Go to main activity */
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
        // [END auth_state_listener]

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

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    // [END on_stop_remove_listener]


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spe = preferences.edit();

        /**
         * Get the newly registered user email if present, use null as default value
         */

        String signupEmail = preferences.getString(Constants.KEY_SIGNUP_EMAIL, null);

        /**
         * fill in the email editText and remove value from SharedPreferences if email is present
         */

        if (signupEmail != null) {
            mEditTextEmailInput.setText(signupEmail);

            /**
             * Clear signupEmail sharedPreferences to make sure that they are used just once
             */
            spe.putString(Constants.KEY_SIGNUP_EMAIL, null).apply();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(mUserEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        mAuthProgressDialog.dismiss();
                        if (!task.isSuccessful()) {
                            /* Error occurred, log the error and dismiss the progress dialog */
                            String errorMessage = task.getException().getMessage();
                            Log.d(LOG_TAG, getString(R.string.log_error_occurred) + errorMessage);
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                mEditTextEmailInput.setError(getString(R.string.error_message_email_issue));
                                mEditTextEmailInput.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mEditTextPasswordInput.setError(errorMessage);
                                mEditTextPasswordInput.requestFocus();
                            } catch (Exception e) {
                                showErrorToast(e.getMessage());
                                Log.e(LOG_TAG, e.getMessage());
                            }

                        } else {
                            Log.i(LOG_TAG, " " + getString(R.string.log_message_auth_successful));
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's email/password provider.
     */
    private void setAuthenticatedUserPasswordProvider(@NonNull final FirebaseAuth firebaseAuth) {
    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's Google login provider.
     */
    private void setAuthenticatedUserGoogle(@NonNull final FirebaseAuth firebaseAuth) {

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

    /**
     * This callback is triggered when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    // [START onactivityresult]
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
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                showErrorToast("Login Failed");
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(LOG_TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        mAuthProgressDialog.show();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(LOG_TAG, "signInWithCredential", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                showErrorToast(getString(R.string.error_message_email_issue));
                            } catch (FirebaseAuthUserCollisionException e) {
                                showErrorToast("User already exists and collison - need to handle this case");
                            } catch (Exception e) {
                                showErrorToast(e.getMessage());
                                Log.e(LOG_TAG, e.getMessage());
                            }
                        }
                        // [START_EXCLUDE]
                        mAuthProgressDialog.dismiss();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

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
}