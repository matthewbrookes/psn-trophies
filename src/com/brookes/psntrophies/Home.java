package com.brookes.psntrophies;



import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Home extends Activity implements AsyncTaskListener{
	String username;
	View profileLayout = null;
	Profile profile;
	ArrayList<Game> games;
	ImageView profileImage = null;
	TextView psnName = null;
	TextView psnAboutMe = null;
	TextView psnTrophyLevel = null;
	TextView psnTrophyProgress = null;
	String textColor = "#FFFFFF";
	String backgroundColor = "";
	View profileTable = null;
	ListView gamesList = null;
	TextView bronzeLabel, silverLabel, goldLabel, platinumLabel = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		Intent i = getIntent();
		username = i.getStringExtra("username");
		profileLayout = findViewById(R.id.profileLayout);
		profileLayout.setVisibility(View.INVISIBLE);
		profileImage = (ImageView) findViewById(R.id.profilePicture);
		psnName = (TextView) findViewById(R.id.psnName);
		psnAboutMe = (TextView) findViewById(R.id.psnAboutMe);
		psnTrophyLevel = (TextView) findViewById(R.id.psnTrophyLevel);
		psnTrophyProgress = (TextView) findViewById(R.id.psnTrophyProgress);
		bronzeLabel = (TextView) findViewById(R.id.bronzeLabel);
		silverLabel = (TextView) findViewById(R.id.silverLabel);
		goldLabel = (TextView) findViewById(R.id.goldLabel);
		platinumLabel = (TextView) findViewById(R.id.platinumLabel);
		profileTable = findViewById(R.id.profileInformationTable);
		gamesList = (ListView) findViewById(R.id.gamesList);
		sync();		
	}
	
	private void sync(){
		new GetXML(this).execute("http://www.psnapi.com.ar/ps3/api/psn.asmx/getPSNID?sPSNID="+username); //Downloads profile
		new GetXML(this).execute("http://www.psnapi.com.ar/ps3/api/psn.asmx/getGames?sPSNID=" + username); //Downloads games
	}
	
	@Override
	public void onProfileDownloaded(String profileXML) {
		XMLParser parser = new XMLParser();
		profile = parser.getProfile(profileXML);
		setProfilePicture(); //Starts chain of drawing profile
	}
	
	@Override
	public void onGamesDownloaded(String gamesXML) {
		XMLParser parser = new XMLParser();
		games = parser.getGames(gamesXML);
		downloadGameImages(games);
		
	}
	
	private void downloadGameImages(ArrayList<Game> games){
		for(int i=0; i<games.size(); i++){
			new GetImage(this).execute(games.get(i).getImage());
		}
	}
	
	@Override
	public void onGameImageDownloaded(String url, Bitmap image) {
		// TODO Auto-generated method stub
		for(int i=0; i<games.size(); i++){
			if(games.get(i).getImage().equals(url)){
				games.get(i).setBitmap(image);
			}
			if(i == (games.size() - 1)){ //When all images downloaded
				gamesList.setAdapter(new GamesAdapter(games , this));
			}
		}
		gamesList.setOnItemClickListener(new OnItemClickListener() {
			 @SuppressWarnings("rawtypes")
			public void onItemClick(AdapterView a, View v, int position, long id) {
			                
			         Intent trophiesIntent = new Intent(v.getContext(), TrophiesList.class);
			         trophiesIntent.putExtra("game", games.get(position));
			         trophiesIntent.putExtra("color",backgroundColor);
			         startActivity(trophiesIntent);
             }
	     });
	}
	
	private void setProfileColor(){
		String color = "#"; //Creates hex string
		String red = Integer.toHexString(profile.getBackgroundColor()[0]); //Stores the red component as a hex value in String form
		String green = Integer.toHexString(profile.getBackgroundColor()[1]);
		String blue = Integer.toHexString(profile.getBackgroundColor()[2]);
		color = color.concat(red).concat(green).concat(blue); //Creates hex string
		if(color.equals("#000")){ //If there is no custom profile color
			color = "#181AB5"; //Set blue background
		}
		if(color.length() < 7){ //If the HEX string is is not complete
			int numbersNeeded = 7 - color.length();
			for(int i=0;i<numbersNeeded;i++){
				color = color.concat("0"); //Adds a 0 until the HEX string contains 6 numbers following the #
			}
		}
		backgroundColor = color;
		profileLayout.setBackgroundColor(Color.parseColor(color)); //Set background to this color
	}
	private void setProfilePicture(){
		String uri = profile.getAvatar(); //From downloaded profile
		new GetImage(this).execute(uri); //Download the image
	}
	private void setProfileInformation(){
		setProfileColor();
		
		psnName.setText(profile.getName());
		psnAboutMe.setText(profile.getAboutMe());
		psnTrophyLevel.setText(Integer.toString(profile.getLevel()));
		psnTrophyProgress.setText(Integer.toString(profile.getProgess()) + "%");
		platinumLabel.setText(Integer.toString(profile.getTrophies()[0]));
		goldLabel.setText(Integer.toString(profile.getTrophies()[1]));
		silverLabel.setText(Integer.toString(profile.getTrophies()[2]));
		bronzeLabel.setText(Integer.toString(profile.getTrophies()[3]));
		profileLayout.setVisibility(View.VISIBLE);
		
	}
	@Override
	public void onProfileImageDownloaded(Bitmap image) { //When image downloaded
		Bitmap scaledImage; //Will hold a scaled image on smaller screens
		int screenSize = getResources().getConfiguration().screenLayout &
		        Configuration.SCREENLAYOUT_SIZE_MASK;
		if(screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL || 
				screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL){
			//On smaller screens the image is scaled down
			scaledImage = Bitmap.createScaledBitmap(image, 180, 180, false);
		}
		else{
			//On tablets it stays as the original
			scaledImage = image;
		}
		profileImage.setImageBitmap(scaledImage);
		setProfileColor();
		setProfileInformation();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item){
         
        switch (item.getItemId()){
        	case R.id.action_logout:
        		SharedPreferences savedInformation = getSharedPreferences("savedInformation", 0);
    	        SharedPreferences.Editor editor = savedInformation.edit();
    	        editor.putString("username", "");
    	
    	        // Commit the edits!
    	        editor.commit();
    	        Intent i = new Intent(this, LogIn.class);
    	        startActivity(i);
    	        finish();
        		return true;
        	case R.id.action_sync:
        		sync();
        		return true;
        	default:
        		return true;       	
        }
	}

	@Override
	public void onTrophiesDownloaded(String trophiesXML) {
		// TODO Auto-generated method stub
		
	}

	
	


	

}
