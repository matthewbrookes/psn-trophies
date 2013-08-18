package com.brookes.psntrophies;

import android.accounts.Account;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class AuthenticatorActivity extends Activity implements AuthenticatorListener{
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

    private String filteredEmail = "";
    private String userPass = "";
    private TextView errorField = null;
    private EditText psnEmail = null;
    private EditText psnPassword = null;
    private ProgressDialog pDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);
        mAccountManager = AccountManager.get(getBaseContext()); //Link variable to account manager

        errorField = (TextView) findViewById(R.id.errorField);
        psnEmail = (EditText) findViewById(R.id.psnEmail);
        psnPassword = (EditText) findViewById(R.id.psnPassword);

        //Retrieve extras from intent
        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);

        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

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
        final String email = psnEmail.getText().toString();
        userPass = psnPassword.getText().toString();

        //Hide keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(psnEmail.getWindowToken(), 0);


        filteredEmail = email.replaceAll(" ", "").toLowerCase(Locale.ENGLISH); //Filter username
        if(filteredEmail.isEmpty()){ //If username is empty
            errorField.setText("Username cannot be empty"); //Set error field text
        }
        else if(userPass.isEmpty()){ //If password is empty
            errorField.setText("Password cannot be empty"); //Set error field text
        }
        else{
            pDialog = new ProgressDialog(this);
            pDialog.setIndeterminate(true);
            pDialog.setMessage("Authenticating " + filteredEmail);
            pDialog.setCancelable(false);
            pDialog.show();
            new ServerAuthenticate(this).authenticateUser(filteredEmail, userPass);
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

            //Make account sync automatically
            getContentResolver().setSyncAutomatically(account, "com.brookes.psntrophies.provider", true);
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
            mAccountManager.setUserData(account, "email", authtoken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onAccountAuthenticated(String username) {
        pDialog.cancel();
        if(username.isEmpty()){
            errorField.setText("Username/Password is incorrect");
        }
        else{
            //Save username
            final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);
            final String finalUsername = username;
            new AsyncTask<String, Void, Intent>() {

                @Override
                protected Intent doInBackground(String... params) {
                    Bundle data = new Bundle();
                    try {
                        data.putString(AccountManager.KEY_ACCOUNT_NAME, finalUsername);
                        data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                        data.putString(AccountManager.KEY_AUTHTOKEN, filteredEmail);
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
}