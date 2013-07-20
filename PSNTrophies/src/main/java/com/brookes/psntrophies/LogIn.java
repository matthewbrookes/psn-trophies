package com.brookes.psntrophies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class LogIn extends Activity{
    private AccountManager accountManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);

        accountManager = AccountManager.get(getBaseContext()); //Link variable to account manager

        Account[] accounts = accountManager.getAccounts(); //Create list of accounts
        for(int i=0; i<accounts.length; i++){ //Iterate through accounts
            if(accounts[i].type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN account
                //Start Home activity
                Intent intent = new Intent(this, Home.class);
                startActivity(intent);
                break;
            }
            if(i == (accounts.length - 1)){ //If no accounts are PSN Accounts
                //Create a new one
                accountManager.addAccount(AccountGeneral.ACCOUNT_TYPE, "", null, null, this, null,null);
            }
        }
	}

    @Override
    protected void onResume(){
        super.onResume();
        Account[] accounts = accountManager.getAccounts(); //Create list of accounts
        for(int i=0; i<accounts.length; i++){ //Iterate through accounts
            if(accounts[i].type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN account
                //Start Home activity
                Intent intent = new Intent(this, Home.class);
                startActivity(intent);
            }
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log_in, menu);
		return true;
	}
}
