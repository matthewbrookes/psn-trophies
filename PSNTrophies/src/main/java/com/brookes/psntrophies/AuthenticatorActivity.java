package com.brookes.psntrophies;

import android.accounts.Account;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
public class AuthenticatorActivity extends Activity implements AsyncTaskListener {
    //Create constants
    public final static String ARG_ACCOUNT_TYPE = AccountGeneral.ACCOUNT_TYPE;
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = AccountGeneral.ACCOUNT_NAME;
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    private final int REQ_SIGNUP = 1;

    //Create variables to be modified/accessed throughout activity
    private AccountManager mAccountManager;
    private String mAuthTokenType;

    private String filteredUsername = "";
    private String userPass = "";
    private TextView errorField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mAccountManager = AccountManager.get(getBaseContext()); //Link variable to account manager

        errorField = (TextView) findViewById(R.id.errorField);

        //Retrieve extras from intent
        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);

        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

        if (accountName != null) { //If a username is already saved
            ((TextView)findViewById(R.id.psnLogin)).setText(accountName); //Set the username
        }

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        }); //When button pressed submit form
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void submit() {

        //Retrieve username and default password
        final String userName = ((TextView) findViewById(R.id.psnLogin)).getText().toString();
        userPass = "";

        //Hide keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(findViewById(R.id.psnLogin).getWindowToken(), 0);


        filteredUsername = userName.replaceAll(" ", ""); //Filter username
        if(filteredUsername.isEmpty()){ //If username is empty
            errorField.setText("Username cannot be empty"); //Set error field text
        }
        else{
            new GetXML(this).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + filteredUsername); //Attempts to download profile with given name
        }


    }

    @Override
    public void onProfileDownloaded(String profileXML) {
        if(profileXML.contains("<level></level>") || profileXML.contains("<level/>")){ //If invalid profile
            errorField.setText("Please enter a valid PSN ID"); //Set error field text
        }
        else{
            //Save username
            final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

            new AsyncTask<String, Void, Intent>() {

                @Override
                protected Intent doInBackground(String... params) {


                    String authtoken = null;
                    Bundle data = new Bundle();
                    try {
                        authtoken = "user:pass";

                        data.putString(AccountManager.KEY_ACCOUNT_NAME, filteredUsername);
                        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                        data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                        data.putString(PARAM_USER_PASS, userPass);

                    } catch (Exception e) {
                        data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                    }

                    final Intent res = new Intent();
                    res.putExtras(data);
                    return res;
                }

                @Override
                protected void onPostExecute(Intent intent) {
                    if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                        Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    } else {
                        finishLogin(intent);
                    }
                }
            }.execute();
        }
    }

    private void finishLogin(Intent intent) {
        //After login return account

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }
        setResult(RESULT_OK, intent);
        finish();
    }




    @Override
    public void onProfileImageDownloaded(Bitmap image) {
        // Not used but is required due to implementations
    }

    @Override
    public void onGameImageDownloaded(String url, Bitmap image) {
        // Not used but is required due to implementations
    }

    @Override
    public void onPSNGamesDownloaded(String gamesXML) {
        // Not used but is required due to implementations
    }

    @Override
    public void onPSNTrophiesDownloaded(String trophiesXML) {
        // Not used but is required due to implementations
    }
}