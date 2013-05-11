package com.brookes.psntrophies;

import android.app.Activity;
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
	private EditText psnName;
	private Button loginButton;
	private TextView errorField;
	private String filteredUsername;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log_in);
		SharedPreferences savedInformation = getSharedPreferences("savedInformation", 0);
		String savedName = savedInformation.getString("username", "");
		if (!savedName.equals("")){
			Intent i = new Intent(this, Home.class);
			i.putExtra("username", savedName);
			startActivity(i);
			finish();
		}
		
		psnName = (EditText) findViewById(R.id.psnLogin);
		loginButton = (Button) findViewById(R.id.loginButton);
		errorField = (TextView) findViewById(R.id.errorField);
		
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Hide keyboard
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	            inputManager.hideSoftInputFromWindow(psnName.getWindowToken(), 0);
				String username = psnName.getText().toString();
				filteredUsername = username.replaceAll(" ", "");
				if(filteredUsername.equalsIgnoreCase("")){
					errorField.setText("Username cannot be empty");
				}
				else{
					new GetXML(v.getContext()).execute("http://www.psnapi.com.ar/ps3/api/psn.asmx/getPSNID?sPSNID="+filteredUsername); //Attempts to download profile with given name
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
		// TODO Auto-generated method stub
		if(profileXML.contains("xsi:nil=\"true\" xmlns=\"http://www.psnapi.com.ar/ps3/api\"")){
			errorField.setText("Please enter a valid PSN ID");
		}
		else{
			//Save username
			SharedPreferences savedInformation = getSharedPreferences("savedInformation", 0);
	        SharedPreferences.Editor editor = savedInformation.edit();
	        editor.putString("username", filteredUsername);
	
	        // Commit the edits!
	        editor.commit();
	        
	        Intent i = new Intent(this, Home.class);
			i.putExtra("username", filteredUsername);
			startActivity(i);
			finish();
		}
	}

	@Override
	public void onGamesDownloaded(String gamesXML) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProfileImageDownloaded(Bitmap image) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGameImageDownloaded(String url, Bitmap image) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTrophiesDownloaded(String trophiesXML) {
		// TODO Auto-generated method stub
		
	}

}
