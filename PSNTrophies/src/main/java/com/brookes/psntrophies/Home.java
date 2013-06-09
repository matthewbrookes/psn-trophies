package com.brookes.psntrophies;



import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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
	//Variables are modified throughout activity so are defined here
	String username;
	String gamesFilter;
	String gamesSort;
	View profileLayout = null;
	Profile profile;
	ArrayList<Game> games;
	ArrayList<Game> filteredGamesList;
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
	Boolean gamesDownloaded = false;
	Boolean downloadImages;
	int imagesDownloadedCounter = 0;
	ProgressDialog pDialog;
	ArrayList<AsyncTask <String, Void, Bitmap>> imageProcesses = new ArrayList<AsyncTask <String, Void, Bitmap>>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		//Retrieves saved settings
		SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
		username = savedInformation.getString("username", "");
		gamesFilter = savedInformation.getString("filter_games", "all");
		gamesSort = savedInformation.getString("sort_games", "recent");
		downloadImages = savedInformation.getBoolean("download_images", true);
		//Assigns variables to widgets
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
		
		sync(); //Starts the process
	}
		@Override
		protected void onResume(){ //When the activity regains focus
			super.onResume();
			if(gamesDownloaded){ //If the games have been downloaded
				//The filter setting is retrieved
				SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
				gamesFilter = savedInformation.getString("filter_games", "all");
				gamesSort = savedInformation.getString("sort_games", "recent");
				downloadImages = savedInformation.getBoolean("download_images", true);
				//The list is filtered then drawn
				filteredGamesList = filterGames(games);
				
				gamesList.setAdapter(new GamesAdapter(filteredGamesList, getApplicationContext()));
			}
		}
	private void sync(){
		new GetXML(this).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + username); //Downloads profile
		new GetXML(this).execute("http://psntrophies.net16.net/getGames.php?psnid=" + username); //Downloads games
	}
	
	@Override
	public void onProfileDownloaded(String profileXML) {
		XMLParser parser = new XMLParser();
		profile = parser.getProfile(profileXML); //Parses XML into Profile Object
		setProfilePicture(); //Starts chain of drawing profile
	}
	
	@Override
	public void onPSNGamesDownloaded(String gamesXML) {
		if(!gamesDownloaded){ //If games being downloaded for first time
			games = new XMLParser().getPSNAPIGames(gamesXML); //Parses XML into Game Object	
			//This loop generates the percentage completion and assigns it to game
			for(int i=0;i<games.size();i++){
				float progress = 0;
				for(int j=0;j<games.get(i).getTrophies().length;j++){
					progress += games.get(i).getTrophies()[j];
				}
				int total = games.get(i).getTotalTrophies();
				float progressPercent = (progress / total) * 100;
				games.get(i).setProgress((int)progressPercent);
			}
			downloadGameImages(games);
		}
		else{
			ArrayList<Game> newGames = new XMLParser().getPSNAPIGames(gamesXML); //Parses XML into Game Object	
			int difference = newGames.size() - games.size(); //How many new games have been played
			//This loop generates the percentage completion and assigns it to game
			for(int i=0;i<newGames.size();i++){
				float progress = 0;
				progress += (newGames.get(i).getTrophies()[1] * 90);
				progress += (newGames.get(i).getTrophies()[2] * 30);
				progress += (newGames.get(i).getTrophies()[3] * 15);
				
				int totalPoints = newGames.get(i).getTotalPoints();
				float progressPercent = (progress / totalPoints) * 100;
				newGames.get(i).setProgress((int)progressPercent);
				if(difference > 0){
					newGames.get(i).setBitmap(games.get(i + difference).getBitmap()); //Assign previously downloaded bitmap to game
				}
				else if(games.get(0).getId().equals(newGames.get(0).getId())){
					newGames.get(i).setBitmap(games.get(i).getBitmap());
				}
				else{
					for(int j=0;j<games.size();j++){
						if(newGames.get(i).equals(games.get(j).getId())){
							newGames.get(i).setBitmap(games.get(j).getBitmap());
							break;
						}
					}
				}
			}
			games = newGames;
			downloadGameImages(games);
		}
	}
	
	private void downloadGameImages(ArrayList<Game> games){ //This function downloads images if required
		if(downloadImages){ //If the user wants to download images
			//Resets the progress dialog, the counter and the list of processes
			imagesDownloadedCounter = 0;
			imageProcesses.clear();
			pDialog = createDialog();
			int newMax = 0;
			for(int i=0; i<games.size(); i++){
				if(games.get(i).getBitmap() == null){ //Only download images which haven't been downloaded
					newMax++;
					pDialog.setMax(newMax);
					pDialog.show();
					imageProcesses.add(new GetImage(this).execute(games.get(i).getImage())); //Download game image and add it to list
				}
			}
		}
		else{
			//Does the same thing as onGameImageDownloaded when they've all been downloaded
			gamesDownloaded = true;
			//Draw list without images
			filteredGamesList = filterGames(games);
			gamesList.setAdapter(new GamesAdapter(filteredGamesList, this));
			setGamesListener();
		}
		
	}
	
	@Override
	public void onGameImageDownloaded(String url, Bitmap image) {
		imagesDownloadedCounter++; //Increment counter
		pDialog.setProgress(imagesDownloadedCounter); //Set the new progress level
		// Attaches image to Object
		for(int i=0; i<games.size(); i++){
			if(games.get(i).getImage().equals(url)){
				games.get(i).setBitmap(image);
				if(i == (games.size() - 1)){ //When all images downloaded
					//Hide progress dialog and change flag
					pDialog.dismiss();
					gamesDownloaded = true;
					//List filtered and drawn
					filteredGamesList = filterGames(games);
					gamesList.setAdapter(new GamesAdapter(filteredGamesList, this));
					setGamesListener();
				}
			}
		}
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
		if(downloadImages){ //If the user wants to download images
			String uri = profile.getAvatar(); //From downloaded profile
			new GetImage(this).execute(uri); //Download the image
		}
		else{
			//Bypass downloading of image
			setProfileColor();
			setProfileInformation();
			profileImage.setImageBitmap(drawBlankProfileImage());
		}
	}
	private void setProfileInformation(){
		setProfileColor();
		//Sets the information in widget to reflect downloaded data
		psnName.setText(profile.getName());
		psnAboutMe.setText(profile.getAboutMe());
		psnTrophyLevel.setText(Integer.toString(profile.getLevel()));
		psnTrophyProgress.setText(Integer.toString(profile.getProgess()) + "%");
		platinumLabel.setText(Integer.toString(profile.getTrophies()[0]));
		goldLabel.setText(Integer.toString(profile.getTrophies()[1]));
		silverLabel.setText(Integer.toString(profile.getTrophies()[2]));
		bronzeLabel.setText(Integer.toString(profile.getTrophies()[3]));
		
		//Shows the top layout
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
	
	private ArrayList<Game> filterGames(ArrayList<Game> oldGamesList){
		pDialog.dismiss(); //Dismiss dialog if it remains
		//This function filters the list of games as per the settings
		ArrayList<Game> newGamesList = new ArrayList<Game>();
		if (gamesFilter.equals("all")){
			newGamesList = oldGamesList;
		} else if (gamesFilter.equals("ps_vita")){
			for (int i=0;i<oldGamesList.size();i++){
				if(oldGamesList.get(i).getPlatform().equals("psp2")){
					//If it's a PS Vita game
					newGamesList.add(oldGamesList.get(i));
				}
			}
		} else if (gamesFilter.equals("ps3")){
			for (int i=0;i<oldGamesList.size();i++){
				if(oldGamesList.get(i).getPlatform().equals("ps3")){
					//If it's a PS3 game
					newGamesList.add(oldGamesList.get(i));
				}
			}
		}
		return sortGames(newGamesList);
	}
	
	private ArrayList<Game> sortGames(ArrayList<Game> oldGamesList){
		//Sorts list of games depending on saved setting using SortList class
		SortList sortList = new SortList();
		if (gamesSort.equals("recent")){
			return sortList.sortRecent(oldGamesList);
		}
		else if(gamesSort.equals("alphabetically")){
			return sortList.sortAlphabetical(oldGamesList);
		}
		else{
			return sortList.sortPlatform(oldGamesList);
		}
	}
	
	private ProgressDialog createDialog(){
		//Creates a horizontal percentage progress dialog
		ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Downloading images...");
        dialog.setIndeterminate(false);
        dialog.setMax(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() { //Draw cancel button
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	//When cancel button clicked
            	for(int i=0; i<imageProcesses.size();i++){
            		imageProcesses.get(i).cancel(true); //Cancel all processes
            	}
            	//Filter list and draw it
            	filteredGamesList = filterGames(games);
				gamesList.setAdapter(new GamesAdapter(filteredGamesList, getApplicationContext()));
				setGamesListener();
				//Hide dialog and change flag
				gamesDownloaded = true;
                dialog.dismiss();
            }
        });
        return dialog;
	}
	
	private void setGamesListener(){
		gamesList.setOnItemClickListener(new OnItemClickListener() {
			 @SuppressWarnings("rawtypes")
			public void onItemClick(AdapterView a, View v, int position, long id) {
	                 //When item pressed trophy page is opened
			         Intent trophiesIntent = new Intent(v.getContext(), TrophiesList.class);
			         trophiesIntent.putExtra("game", filteredGamesList.get(position));
			         trophiesIntent.putExtra("color",backgroundColor);
			         startActivity(trophiesIntent);
            }
	     });
	}
	
	public Bitmap drawBlankProfileImage(){
		Bitmap image = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888);
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
		return scaledImage;
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
        		SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
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
        	case R.id.action_settings:
        		Intent j = new Intent(this, SettingsActivity.class);
    	        startActivity(j);
        	default:
        		return true;       	
        }
	}

	@Override
	public void onPSNTrophiesDownloaded(String trophiesXML) {
		// Not used but is required due to implementations
		
	}
}
