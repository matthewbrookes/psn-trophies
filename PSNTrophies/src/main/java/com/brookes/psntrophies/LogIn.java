package com.brookes.psntrophies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LogIn extends Activity implements AsyncTaskListener {
    //Create variables to be modified/accessed throughout activity
	private EditText psnName;
	private Button loginButton;
	private TextView errorField;
	private String filteredUsername;
    AccountManager accountManager;
    ProgressDialog pDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);

        //Create dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Downloading data");
        pDialog.setIndeterminate(true); //Starts spinning wheel dialog
        pDialog.setCancelable(false);

        accountManager = AccountManager.get(getBaseContext()); //Link variable to account manager
		
		Account[] accounts = accountManager.getAccounts(); //Create list of accounts
        for(int i=0; i<accounts.length; i++){ //Iterate through accounts
            if(accounts[i].type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN account
                //Start Home activity
                Intent intent = new Intent(this, Home.class);
                startActivity(intent);
            }
        }
		//If we reach this point a user must be added

		psnName = (EditText) findViewById(R.id.psnLogin);
		loginButton = (Button) findViewById(R.id.loginButton);
		errorField = (TextView) findViewById(R.id.errorField);
        accountManager = AccountManager.get(getBaseContext());
		
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Hide keyboard
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            inputManager.hideSoftInputFromWindow(psnName.getWindowToken(), 0);
                //Filter username
				String username = psnName.getText().toString();
				filteredUsername = username.replaceAll(" ", "");

				if(filteredUsername.isEmpty()){
					errorField.setText("Username cannot be empty"); //Set error field text
				}
				else{
                    pDialog.show();
					new GetXML(v.getContext()).execute("http://psntrophies.net16.net/getProfile.php?psnid="+filteredUsername); //Attempts to download profile with given name
				}
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
		return true;
	}

	@Override
	public void onProfileDownloaded(String profileXML) {
        pDialog.cancel();
		// TODO Auto-generated method stub
		if(profileXML.contains("<level></level>") || profileXML.contains("<level/>")){ //If invalid user
			errorField.setText("Please enter a valid PSN ID"); //Set error field text
		}
		else{
            //Create new account and add to account manager
            Account account = new Account(filteredUsername, AccountGeneral.ACCOUNT_TYPE);
            accountManager.addAccountExplicitly(account, "", null);

            //Start Home Activity
	        Intent i = new Intent(this, Home.class);
			startActivity(i);
			finish();
		}
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
