package com.brookes.psntrophies;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TrophiesList extends Activity implements AsyncTaskListener{
	ArrayList<Trophy> trophies;
	ListView trophiesList;
	View gameLayout;
	Boolean showSecretTrophies;
	Boolean showCompletedTrophies;
	Boolean downloadImages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trophies_list);
		//Stores layouts and different editable widgets in variables
		trophiesList = (ListView) findViewById(R.id.trophiesList);
		gameLayout = findViewById(R.id.gameLayout);
		
		TextView gameName = (TextView) findViewById(R.id.gameName);
		TextView trophyTotal = (TextView) findViewById(R.id.trophyTotal);
		ProgressBar completionBar = (ProgressBar) findViewById(R.id.completionProgressBar1);
		TextView completionLabel = (TextView) findViewById(R.id.completionPercentageLabel);
		ImageView gameImage = (ImageView) findViewById(R.id.gamePicture);
		TextView bronzeLabel = (TextView) findViewById(R.id.bronzeLabel);
		TextView silverLabel = (TextView) findViewById(R.id.silverLabel);
		TextView goldLabel = (TextView) findViewById(R.id.goldLabel);
		TextView platinumLabel = (TextView) findViewById(R.id.platinumLabel);
		
		//Retrieves saved preferences
		SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
		String savedName = savedInformation.getString("username", "");
		downloadImages = savedInformation.getBoolean("download_images", true);
		showSecretTrophies = savedInformation.getBoolean("show_secret_trophies", true);
		showCompletedTrophies = savedInformation.getBoolean("show_completed_trophies", true);
		
		//Retrieve Game Object and Background Color from intent
		Intent receivedIntent = getIntent();
		Game game = receivedIntent.getExtras().getParcelable("game");
		String gameId = game.getId();
		String backgroundcolor = receivedIntent.getStringExtra("color");
		
		//Sets information in top layout bases upon recieved Game Object and color
		gameLayout.setBackgroundColor(Color.parseColor(backgroundcolor));
		gameImage.setImageBitmap(game.getBitmap());
		gameName.setText(game.getTitle());
		trophyTotal.setText(Integer.toString(game.getTotalTrophies()));
		completionBar.setProgress(game.getProgress());
		completionLabel.setText(game.getProgress() + "% Complete");
		platinumLabel.setText(""+ Integer.toString(game.getTrophies()[0]));
		goldLabel.setText("" + Integer.toString(game.getTrophies()[1]));
		silverLabel.setText("" + Integer.toString(game.getTrophies()[2]));
		bronzeLabel.setText("" + Integer.toString(game.getTrophies()[3]));
		
		SharedPreferences ffdb = PreferenceManager.getDefaultSharedPreferences(this);
		
		new GetXML(this).execute("http://www.psnapi.com.ar/ps3/api/psn.asmx/getTrophies?sPSNID="+ savedName + "&sGameId=" + gameId); //Downloads trophies xml for this game
	}
	
	@Override
	public void onTrophiesDownloaded(String trophiesXML){
		trophies = new XMLParser().getTrophies(trophiesXML); //Returns ArrayList of Trophy objects
		downloadTrophyImages(trophies);
	}
	
	private void downloadTrophyImages(ArrayList<Trophy> trophies){
		if(downloadImages){
			for(int i=0; i<trophies.size(); i++){ //For each Trophy Object
				new GetImage(this).execute(trophies.get(i).getImage()); //Download the image
			}
		}
		else{
			ArrayList<Trophy> filteredList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies);
			
			trophiesList.setAdapter(new TrophiesAdapter(filteredList, this)); //Draws list based upon new data
		}
	}
	
	@Override
	public void onGameImageDownloaded(String url, Bitmap image) {
		//Saves Bitmap Image to Trophy Object
		for(int i=0; i<trophies.size(); i++){ //Iterates over each Trophy
			if(trophies.get(i).getImage().equals(url)){ //If this image matches this Trophy
				trophies.get(i).setBitmap(image); //Save it
			}
			if(i == (trophies.size() - 1)){ //When all images downloaded
				//Creates a new ArrayList with some trophies hidden depending on settings
				ArrayList<Trophy> filteredList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies);
				
				trophiesList.setAdapter(new TrophiesAdapter(filteredList, this)); //Draws list based upon new data
			}
		}
	}
	
	private ArrayList<Trophy> hideTrophies(ArrayList<Trophy> trophies, boolean showSecretTrophies, boolean showCompletedTrophies){
		//Will return an ArrayList without items which the user doesn't want to see
		ArrayList<Trophy> trophyList = new ArrayList<Trophy>(); //List will hold all trophies or only non-hidden ones
		for(int i = 0;i<trophies.size();i++){ //Iterates over each trophy
			if(showSecretTrophies && showCompletedTrophies){ //If the user wants to see all trophies
				trophyList = trophies; //Store all the trophies in new list
			}
			else if(showSecretTrophies == false && showCompletedTrophies){ //If secret trophies should be hidden but completed trophies shown
				if(trophies.get(i).isHidden() == false || trophies.get(i).getDisplayDate() != null){ //Visible trophies and secret trophies which have been earned
					trophyList.add(trophies.get(i)); //Are added to list
				}
			}
			else if(showCompletedTrophies == false && showSecretTrophies == true){ //If completed trophies should be hidden and secret trophies shown
				if(trophies.get(i).getDisplayDate() == null){ //If Trophy has no completion date
					trophyList.add(trophies.get(i));
				}		
			}
			else if(showCompletedTrophies == false && showSecretTrophies == false){ //If completed trophies and secret trophies should be hidden
				if((trophies.get(i).getDisplayDate() == null) && (trophies.get(i).isHidden() == false)){
					trophyList.add(trophies.get(i));
				}
			}
		}
		return trophyList;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trophies_list, menu);
		if(showSecretTrophies){ //If the user wants to see secret trophies
			menu.getItem(0).setTitle("Hide secret trophies"); //Set the text to this
		} else{
			menu.getItem(0).setTitle("Show secret trophies");
		}
		
		if(showCompletedTrophies){ //If the user wants to see completed trophies
			menu.getItem(1).setTitle("Hide completed trophies"); //Set the text to this
		} else{
			menu.getItem(1).setTitle("Show completed trophies");
		}
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item){
         
        switch (item.getItemId()){
        	case R.id.action_secretTrophies:
        		showSecretTrophies = !showSecretTrophies; //Flip boolean value
        		if(showSecretTrophies){ //If the user can now see secret trophies
        			item.setTitle("Hide secret trophies"); //Give user the option to hide them
        		} else{
        			item.setTitle("Show secret trophies");
        		}
				
				//Saves new setting
        		SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
    	        SharedPreferences.Editor editor = savedInformation.edit();
    	        editor.putBoolean("show_secret_trophies", showSecretTrophies);
    	
    	        // Commit the edits!
    	        editor.commit();
    	        
				//Create new ArrayList based upon new choice
    	        ArrayList<Trophy> filteredTrophies = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies);
    	        //Redraw list with new data
    	        trophiesList.setAdapter(new TrophiesAdapter(filteredTrophies , this));
        		return true;
        	case R.id.action_completedTrophies:
        		this.showCompletedTrophies = !showCompletedTrophies; //Flip boolean value
        		if(showCompletedTrophies){ //If the user can now see completed trophies
        			item.setTitle("Hide completed trophies"); //Give user the option to hide them
        		} else{
        			item.setTitle("Show completed trophies");
        		}
				
				//Saves new setting
        		SharedPreferences savedOptions = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
    	        SharedPreferences.Editor optionsEditor = savedOptions.edit();
    	        optionsEditor.putBoolean("show_completed_trophies", showCompletedTrophies);
    	
    	        // Commit the edits!
    	        optionsEditor.commit();
    	        
				//Create new ArrayList based upon new choice
    	        ArrayList<Trophy> filteredTrophiesList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies);
    	        //Redraw list with new data
    	        trophiesList.setAdapter(new TrophiesAdapter(filteredTrophiesList, this));
        		return true;
			default:
        		return true;       	
        }
	}
	@Override
	public void onProfileDownloaded(String profileXML) {
		//Not used but required due to implementation of AsyncTaskListener		
	}

	@Override
	public void onGamesDownloaded(String gamesXML) {
		//Not used but required due to implementation of AsyncTaskListener		
	}

	@Override
	public void onProfileImageDownloaded(Bitmap image) {
		//Not used but required due to implementation of AsyncTaskListener		
	}

	

}
