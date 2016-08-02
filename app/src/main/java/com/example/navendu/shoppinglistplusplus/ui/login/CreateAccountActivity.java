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
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.navendu.shoppinglistplusplus.R;
import com.example.navendu.shoppinglistplusplus.ui.BaseActivity;
import com.example.navendu.shoppinglistplusplus.utils.Constants;
import com.example.navendu.shoppinglistplusplus.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Represents Signup Screen and functionality of the app
 */
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate;
    private String mUserName, mUserEmail, mPassword;
    private SecureRandom mRandom;
    private FirebaseAuth mAuth;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        mRandom = new SecureRandom();

        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
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
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_check_inbox));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {

        mUserName = mEditTextUsernameCreate.getText().toString();
        mUserEmail = mEditTextEmailCreate.getText().toString().toLowerCase();
        mPassword = new BigInteger(130, mRandom).toString(32);

        /* validation on text fields */
        boolean validUserName = isUserNameValid(mUserName);
        boolean validUserEmail = isEmailValid(mUserEmail);

        if (!validUserEmail || !validUserName) {
            return;
        }

        /**
         * If everything was valid show the progress dialog to indicate that
         * account creation has started
         */

        mAuthProgressDialog.show();

        /**
         * Create new user with specified email and password
         */
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(mUserEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            /* Error occurred, log the error and dismiss the progress dialog */
                            Log.d(LOG_TAG, getString(R.string.log_error_occurred) + task.getException().getMessage());
                            mAuthProgressDialog.dismiss();
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mEditTextEmailCreate.setError(getString(R.string.error_invalid_email_not_valid));
                                mEditTextEmailCreate.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                mEditTextEmailCreate.setError(getString(R.string.error_email_taken));
                                mEditTextEmailCreate.requestFocus();
                            } catch (Exception e) {
                                showErrorToast(e.getMessage());
                                Log.e(LOG_TAG, e.getMessage());
                            }

                        } else {
                            uid = task.getResult().getUser().getUid();
                            //Email Reset Start
                            mAuth.sendPasswordResetEmail(mUserEmail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Log.d(LOG_TAG, getString(R.string.log_error_occurred) +
                                                        task.getException().getMessage());
                                                mAuthProgressDialog.dismiss();
                                            } else {
                                                mAuthProgressDialog.dismiss();
                                                Log.i(LOG_TAG, getString(R.string.log_message_auth_successful));
                                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(CreateAccountActivity.this);
                                                SharedPreferences.Editor spe = preferences.edit();
                                                /**
                                                 * Save name and email to sharedPreferences to create User database record
                                                 *  when the registered user will sign in for the first time
                                                 */
                                                spe.putString(Constants.KEY_SIGNUP_EMAIL, mUserEmail).apply();
                                                Utils.createUserInFirebaseHelper(mUserEmail, mUserName, uid);

                                                /**
                                                 * Password reset email sent, open app chooser to pick app
                                                 * for handling inbox email intent
                                                 */
                                                mAuth.signOut();
                                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                                                try {
                                                    startActivity(intent);
                                                    finish();
                                                } catch (android.content.ActivityNotFoundException ex) {
                                                    Log.e(LOG_TAG, "Email Application not found:" + ex.getMessage());
                                                }
                                            }
                                        }
                                    });
                            //Email Reset Ends
                        }
                    }
                });
        // [END create_user_with_email]
    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */


    /**
     * Cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private boolean isEmailValid(String email) {
        boolean isGoodEmail = (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEditTextEmailCreate.setError(String.format(getString(R.string.error_invalid_email_not_valid), email));
            return false;
        }

        return isGoodEmail;
    }

    private boolean isUserNameValid(String userName) {
        if (TextUtils.isEmpty(userName)) {
            mEditTextUsernameCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }

}