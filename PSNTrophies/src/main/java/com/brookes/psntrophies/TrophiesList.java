package com.brookes.psntrophies;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TrophiesList extends Activity implements AsyncTaskListener{
    SharedPreferences savedInformation;
    SharedPreferences savedUpdateTimes;
    SharedPreferences savedXML;
    SharedPreferences.Editor savedInformationEditor;
    SharedPreferences.Editor savedUpdateEditor;
    SharedPreferences.Editor savedXMLEditor;
	ArrayList<Trophy> trophies;
	ListView trophiesList;
	View gameLayout;
    TextView updateText = null;
	boolean showSecretTrophies;
	boolean showCompletedTrophies;
	boolean downloadImages;
    boolean saveImages;
	boolean showUnearnedTrophies;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String storageState = Environment.getExternalStorageState();
	String gameId;
	String username;
	int imagesDownloadedCounter = 0;
	ProgressDialog imagesDialog;
    ProgressDialog trophiesDialog;
	ArrayList<AsyncTask <String, Void, Bitmap>> imageProcesses = new ArrayList<AsyncTask <String, Void, Bitmap>>();
    ImageView gameImage;
    Game game;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trophies_list);

        //Checks if external storage is mounted and what access rights the app has
        if (Environment.MEDIA_MOUNTED.equals(storageState)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
		//Stores layouts and different editable widgets in variables
		trophiesList = (ListView) findViewById(R.id.trophiesList);
		gameLayout = findViewById(R.id.gameLayout);
        updateText = (TextView) findViewById(R.id.updateText);
		TextView gameName = (TextView) findViewById(R.id.gameName);
		TextView trophyTotal = (TextView) findViewById(R.id.trophyTotal);
		ProgressBar completionBar = (ProgressBar) findViewById(R.id.completionProgressBar1);
		TextView completionLabel = (TextView) findViewById(R.id.completionPercentageLabel);
        gameImage = (ImageView) findViewById(R.id.gamePicture);
        TextView bronzeLabel = (TextView) findViewById(R.id.bronzeLabel);
		TextView silverLabel = (TextView) findViewById(R.id.silverLabel);
		TextView goldLabel = (TextView) findViewById(R.id.goldLabel);
		TextView platinumLabel = (TextView) findViewById(R.id.platinumLabel);

        trophiesDialog = createDialog(DownloadType.PSNAPITROPHIES); //Create dialog

        //Retrieve Game, username and background color from intent
        Intent receivedIntent = getIntent();
        game = receivedIntent.getExtras().getParcelable("game");
        gameId = game.getId();
        username = receivedIntent.getExtras().getString("username");
        String backgroundcolor = receivedIntent.getExtras().getString("bg_color");

        //Create shared preferences and editor
        savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
        savedInformationEditor = savedInformation.edit();

        savedXML = getSharedPreferences(username + "_xml", 0);
        savedXMLEditor = savedXML.edit();

        savedUpdateTimes = getSharedPreferences(username + "_updates", 0);
        savedUpdateEditor = savedUpdateTimes.edit();

        //Retrieves saved settings
        String trophiesXML = savedXML.getString(gameId, "");
        Long lastUpdated = savedUpdateTimes.getLong(gameId, 0L);
		downloadImages = savedInformation.getBoolean("download_images", true);
        saveImages = savedInformation.getBoolean("save_images", true);
		showSecretTrophies = savedInformation.getBoolean("show_secret_trophies", true);
		showCompletedTrophies = savedInformation.getBoolean("show_completed_trophies", true);
		showUnearnedTrophies = savedInformation.getBoolean("show_unearned_trophies", true);

        if(backgroundcolor == null){ //If background color was not retrieved
            //Get it from shared preferences
            backgroundcolor = savedInformation.getString("bg_color", "#989898");
        }

		//Sets information in top layout bases upon received Game Object and color
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

        if(mExternalStorageWriteable){ //If can write to SD card
            //Create the folder where the images for the trophies will be stored
            File trophyImagesFolder = new File(getExternalFilesDir(null), "/" + gameId);
            trophyImagesFolder.mkdir();
        }

        //If there is no game image and can read from SD Card
        if(game.getBitmap() == null && mExternalStorageAvailable){
            File savedImageFile = new File(getExternalFilesDir(null), "/gameImages/" + game.getId() + ".png");
            if(savedImageFile.exists()){ //If an image for this game exists
                //Retrieve it
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(savedImageFile.toString(), options);

                game.setBitmap(bitmap); //Apply to game object
                gameImage.setImageBitmap(bitmap); //Draw on screen
            }
            else if(downloadImages){ //If no image exists and user wants to download images
                new GetImage(this).execute(game.getImage());
            }
        }

        //Calculate amount of time since last sync
        Date currentDate = Calendar.getInstance().getTime();
        Long currentTime = currentDate.getTime();

        //Change the update label on home screen
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        Date d = new Date(lastUpdated);
        String displayDate = f.format(d);
        updateText.setText(displayDate);

        trophies = (ArrayList<Trophy>) getLastNonConfigurationInstance(); //Attempt to retrieve trophies if screen was rotated
        if (trophies != null) { //If trophies were retrieved

            //List filtered and drawn
            ArrayList<Trophy> filteredList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);
            trophiesList.setAdapter(new TrophiesAdapter(filteredList, this));

            return; //Break out of function
        }

        if(lastUpdated == 0L || trophiesXML.isEmpty()){ //If information hasn't been synced or there is no saved XML
            //Save the new update time
            savedUpdateEditor.putLong(gameId, currentTime);
            savedUpdateEditor.commit();

            //Change the update label on home screen
            d = new Date(currentTime);
            displayDate = f.format(d);
            updateText.setText(displayDate);

            trophiesDialog.show();

            new GetXML(this).execute("http://psntrophies.net16.net/getTrophies.php?psnid="+ username + "&gameid=" + gameId); //Downloads trophies xml for this game
        }
        else{
            //Create trophies from saved xml
            trophies = new XMLParser().getPSNAPITrophies(trophiesXML);
            //Iterates through list creating dates in local format without "seconds" information
            DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            for(int j=0;j<trophies.size();j++){
                if(!trophies.get(j).getDateEarned().isEmpty()){ //If the trophy has been earned
                    //Seconds since epoch converted to milliseconds and formatted to a date
                    Date date = new Date(Long.parseLong(trophies.get(j).getDateEarned()) * 1000L);
                    String trophyDisplayDate = format.format(date);
                    trophies.get(j).setDisplayDate(trophyDisplayDate);
                }
                if(mExternalStorageAvailable){ //If can read from SD Card
                    File savedImageFile = new File(getExternalFilesDir(null), "/" + gameId + "/" + trophies.get(j).getId() + ".png");
                    if(savedImageFile.exists()){
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(savedImageFile.toString(), options);
                        trophies.get(j).setBitmap(bitmap);
                    }
                }
            }
            downloadTrophyImages(trophies);
        }
	}

    @Override
    public Object onRetainNonConfigurationInstance() { //Save trophies when screen rotates
        final ArrayList<Trophy> listToSave = trophies;
        return listToSave;
    }
	
	@Override
	public void onPSNTrophiesDownloaded(String trophiesXML) {
        trophiesDialog.cancel();
        savedXMLEditor.putString(gameId, trophiesXML);
        savedXMLEditor.commit();
		trophies = new XMLParser().getPSNAPITrophies(trophiesXML);
        //Iterates through list creating dates in local format without "seconds" information
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		for(int j=0;j<trophies.size();j++){
			if(!trophies.get(j).getDateEarned().isEmpty()){ //If the trophy has been earned
                //Seconds since epoch converted to milliseconds and formatted to a date
				Date d = new Date(Long.parseLong(trophies.get(j).getDateEarned()) * 1000L);
				String displayDate = f.format(d); 
				trophies.get(j).setDisplayDate(displayDate);
			}
            if(mExternalStorageAvailable){ //If can read from SD Card
                File savedImageFile = new File(getExternalFilesDir(null), "/" + gameId + "/" + trophies.get(j).getId() + ".png");
                if(savedImageFile.exists()){
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(savedImageFile.toString(), options);
                    trophies.get(j).setBitmap(bitmap);
                }
            }
		}
		downloadTrophyImages(trophies);
	}

    private void downloadTrophyImages(ArrayList<Trophy> trophies){
		if(downloadImages){
            //Resets the progress dialog, the counter and the list of processes
            imagesDownloadedCounter = 0;
            imageProcesses.clear();
            imagesDialog = createDialog(DownloadType.GAMESTROPHIES);
            int newMax = 0;
			for(int i=0; i<trophies.size(); i++){ //For each Trophy Object
				if(trophies.get(i).getBitmap() == null){ //If there is no image
                    newMax++;
                    imagesDialog.setMax(newMax);
                    imagesDialog.show();
					imageProcesses.add(new GetImage(this).execute(trophies.get(i).getImage())); //Download the image
				}
                //If no images need downloaded
                else if(i == (trophies.size() - 1)){
                    //Filter and draw games list
                    ArrayList<Trophy> filteredList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);
                    trophiesList.setAdapter(new TrophiesAdapter(filteredList, this)); //Draws list based upon new data
                }
			}
		}
		else{
			//Filter list and draw it
			ArrayList<Trophy> filteredList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);
			trophiesList.setAdapter(new TrophiesAdapter(filteredList, this)); //Draws list based upon new data
		}
	}
	
	@Override
	public void onGameImageDownloaded(String url, Bitmap image) {
		//Saves Bitmap Image to Trophy Object
        if(trophies != null){ //If trophies list has been created
            for(int i=0; i<trophies.size(); i++){ //Iterates over each Trophy
                if(trophies.get(i).getImage().equals(url)){ //If this image matches this Trophy
                    imagesDownloadedCounter++; //Increment counter
                    imagesDialog.setProgress(imagesDownloadedCounter); //Update the progress dialog
                    trophies.get(i).setBitmap(image); //Save it
                    if(mExternalStorageWriteable && saveImages){ //If can write to SD Card & user wants to save images
                        //Save image as 'id'.png
                        File gameImage = new File(getExternalFilesDir(null), "/" + gameId + "/" + trophies.get(i).getId() + ".png");
                        FileOutputStream fOut = null;
                        try {
                            fOut = new FileOutputStream(gameImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        image.compress(Bitmap.CompressFormat.PNG, 0, fOut);
                        try {
                            fOut.flush();
                            fOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(imagesDownloadedCounter == imageProcesses.size()){ //If all images downloaded
                        //Hide progress dialog and change flag
                        imagesDialog.dismiss();
                        //List filtered and drawn
                        ArrayList<Trophy> filteredList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);
                        trophiesList.setAdapter(new TrophiesAdapter(filteredList, this));
                    }
                    break;
                }
                else if(i == (trophies.size() - 1)){ //The image must be the game image as there are no matches
                    gameImage.setImageBitmap(image); //So the image is applied
                    game.setBitmap(image); //The image is added to object

                    if(mExternalStorageWriteable && saveImages){ //If can write to SD Card & user wants to save images
                        //Save image as 'gameid'.png
                        File gameImage = new File(getExternalFilesDir(null), "/gameImages/" + game.getId() + ".png");
                        FileOutputStream fOut = null;
                        try {
                            fOut = new FileOutputStream(gameImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        image.compress(Bitmap.CompressFormat.PNG, 0, fOut);
                        try {
                            fOut.flush();
                            fOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else{ //If it hasn't then this must be the game image
            gameImage.setImageBitmap(image); //So the image is applied
            game.setBitmap(image); //The image is added to object

            if(mExternalStorageWriteable && saveImages){ //If can write to SD Card & user wants to save images
                //Save image as 'gameid'.png
                File gameImage = new File(getExternalFilesDir(null), "/gameImages/" + game.getId() + ".png");
                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(gameImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                image.compress(Bitmap.CompressFormat.PNG, 0, fOut);
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	private ArrayList<Trophy> hideTrophies(ArrayList<Trophy> trophies, boolean showSecretTrophies, boolean showCompletedTrophies, boolean showUnearnedTrophies){
		//Will return an ArrayList without items which the user doesn't want to see
		ArrayList<Trophy> trophyList = new ArrayList<Trophy>(); //List will hold final trophies to be shown
		ArrayList<Trophy> completedTrophies = new ArrayList<Trophy>(); //List will hold trophies after first stage
		ArrayList<Trophy> unearnedTrophies = new ArrayList<Trophy>(); //List will hold trophies after second stage
		
		if(showSecretTrophies && showCompletedTrophies && showUnearnedTrophies){ //If the user wants to see all trophies
			trophyList = trophies; //Store all the trophies in new list
		}
		else{
			if(!showCompletedTrophies){ //If completed trophies should be hidden
				for(int i = 0;i<trophies.size();i++){ //Iterates over each trophy
					if(trophies.get(i).getDateEarned().isEmpty()){ //Trophies which haven't been completed
						completedTrophies.add(trophies.get(i)); //Are added to list
					}
				}	
			}
			else if(showCompletedTrophies){
				completedTrophies = trophies; //All trophies added
			}
			
			if(!showUnearnedTrophies){ //If unearned trophies should be hidden
				for(int i = 0;i<completedTrophies.size();i++){ //Iterates over each trophy
					if(!completedTrophies.get(i).getDateEarned().isEmpty()){ //Trophies which have been completed
						unearnedTrophies.add(completedTrophies.get(i)); //Are added to list
					}
				}
			}
			else if(showUnearnedTrophies){
				unearnedTrophies = completedTrophies;	//All trophies added
			}
			
			if(!showSecretTrophies){ //If secret trophies should be hidden
				for(int i = 0;i<unearnedTrophies.size();i++){ //Iterates over each trophy
					if(unearnedTrophies.get(i).isHidden() == false || !unearnedTrophies.get(i).getDateEarned().isEmpty()){ //Visible trophies and secret trophies which have been earned
						trophyList.add(unearnedTrophies.get(i)); //Are added to list
					}
				}
			}
			else if(showSecretTrophies){
				trophyList = unearnedTrophies; //All trophies added
			}
		}
		
		return trophyList;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.trophies_list, menu);
		if(showSecretTrophies){ //If the user wants to see secret trophies
			menu.getItem(1).setTitle("Hide secret trophies"); //Set the text to this
		} else{
			menu.getItem(1).setTitle("Show secret trophies");
		}
		
		if(showCompletedTrophies){ //If the user wants to see completed trophies
			menu.getItem(2).setTitle("Hide completed trophies"); //Set the text to this
		} else{
			menu.getItem(2).setTitle("Show completed trophies");
		}
		
		if(showUnearnedTrophies){ //If the user wants to see completed trophies
			menu.getItem(3).setTitle("Hide unearned trophies"); //Set the text to this
		} else{
			menu.getItem(3).setTitle("Show unearned trophies");
		}
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item){
		//Saves new setting
		ArrayList<Trophy> filteredTrophies;
        switch (item.getItemId()){
            case R.id.action_sync:
                //Calculate current time
                Date currentDate = Calendar.getInstance().getTime();
                Long currentTime = currentDate.getTime();

                //Save current time
                savedUpdateEditor.putLong(gameId, currentTime);
                savedUpdateEditor.commit();

                //Change the update label on home screen
                DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                Date d = new Date(currentTime);
                String displayDate = f.format(d);
                updateText.setText(displayDate);

                new GetXML(this).execute("http://psntrophies.net16.net/getTrophies.php?psnid="+ username + "&gameid=" + gameId); //Downloads trophies xml for this game
                return true;
        	case R.id.action_secretTrophies:
        		showSecretTrophies = !showSecretTrophies; //Flip boolean value
        		if(showSecretTrophies){ //If the user can now see secret trophies
        			item.setTitle("Hide secret trophies"); //Give user the option to hide them
        		} else{
        			item.setTitle("Show secret trophies");
        		}
				
    	        savedInformationEditor.putBoolean("show_secret_trophies", showSecretTrophies);
    	
    	        // Commit the edits!
    	        savedInformationEditor.commit();
    	        
				//Create new ArrayList based upon new choice
    	        filteredTrophies = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);
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
        		savedInformationEditor.putBoolean("show_completed_trophies", showCompletedTrophies);
    	
    	        // Commit the edits!
    	        savedInformationEditor.commit();
    	        
				//Create new ArrayList based upon new choice
    	        filteredTrophies = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);
    	        //Redraw list with new data
    	        trophiesList.setAdapter(new TrophiesAdapter(filteredTrophies, this));
        		return true;
        	case R.id.action_unearnedTrophies:
        		this.showUnearnedTrophies = !showUnearnedTrophies; //Flip boolean value
        		if(showUnearnedTrophies){ //If the user can now see completed trophies
        			item.setTitle("Hide unearned trophies"); //Give user the option to hide them
        		} else{
        			item.setTitle("Show unearned trophies");
        		}
				
				//Saves new setting
        		savedInformationEditor.putBoolean("show_unearned_trophies", showCompletedTrophies);
    	
    	        // Commit the edits!
    	        savedInformationEditor.commit();
    	        
				//Create new ArrayList based upon new choice
    	        filteredTrophies = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);
    	        //Redraw list with new data
    	        trophiesList.setAdapter(new TrophiesAdapter(filteredTrophies, this));
        		return true;
            case android.R.id.home:
                finish();
                return true;
			default:
        		return true;       	
        }
	}
    private ProgressDialog createDialog(DownloadType type){
        ProgressDialog dialog = new ProgressDialog(this);
        switch (type) {
            case GAMESTROPHIES:
                //Creates a horizontal percentage progress dialog
                dialog.setMessage("Downloading images...");
                dialog.setIndeterminate(false);
                dialog.setMax(0);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); //Displays a progress bar
                dialog.setCancelable(false); //The back button will not cancel image download
                //Creates a cancel button
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i=0; i<imageProcesses.size();i++){
                            imageProcesses.get(i).cancel(true); //Cancel each outstanding image process
                        }
                        ArrayList<Trophy> filteredList = hideTrophies(trophies, showSecretTrophies, showCompletedTrophies, showUnearnedTrophies);

                        trophiesList.setAdapter(new TrophiesAdapter(filteredList, getApplicationContext())); //Draws list based upon new data
                        dialog.dismiss();
                    }
                });
                return dialog;
            case PROFILE:
                return dialog;
            case PSNAPIGAMES:
                return dialog;
            case PSNAPITROPHIES:
                dialog.setMessage("Downloading trophies");
                dialog.setIndeterminate(true); //Starts spinning wheel dialog
                dialog.setCancelable(false);
                return dialog;
        }
        return dialog;
    }
	
	@Override
	public void onProfileDownloaded(String profileXML) {
		// Not used but is required due to implementations
	}

	@Override
	public void onProfileImageDownloaded(String url, Bitmap image) {
		// Not used but is required due to implementations
	}

	@Override
	public void onPSNGamesDownloaded(String gamesXML) {
		// Not used but is required due to implementations
	}

    @Override
    public void onFriendsDownloaded(String friendsXML) {
        // Not used but is required due to implementations
    }

}
