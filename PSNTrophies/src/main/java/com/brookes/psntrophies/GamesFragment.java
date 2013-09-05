package com.brookes.psntrophies;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class GamesFragment extends Fragment implements AsyncTaskListener {
    //Variables are modified throughout fragment so are defined here
    SharedPreferences savedInformation;
    SharedPreferences savedXML;
    SharedPreferences.Editor savedInformationEditor;
    SharedPreferences.Editor savedXMLEditor;
    String username = "";
    String gamesFilter;
    String gamesSort;
    View profileLayout = null;
    Profile profile;
    ArrayList<Game> games;
    ArrayList<Game> filteredGamesList;
    ImageView profileImage = null;
    TextView updateText = null;
    TextView psnName = null;
    TextView psnPlus = null;
    TextView psnAboutMe = null;
    TextView psnTrophyLevel = null;
    TextView psnTrophyProgress = null;
    String backgroundColor = "";
    String gamesXML = "";
    String profileXML = "";
    View profileTable = null;
    ListView gamesList = null;
    TextView bronzeLabel, silverLabel, goldLabel, platinumLabel = null;
    boolean gamesDownloaded = false;
    boolean downloadImages;
    boolean saveImages;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String storageState = Environment.getExternalStorageState();
    int imagesDownloadedCounter = 0;
    long lastUpdated = 0L;
    long deleteFrequency;
    long syncFrequency;
    ProgressDialog imagesDialog;
    ProgressDialog profileDialog;
    ProgressDialog gamesDialog;

    ArrayList<AsyncTask<String, Void, Bitmap>> imageProcesses = new ArrayList<AsyncTask <String, Void, Bitmap>>();

    Fragment currentFragment;

    Context context;
    File folder;

    public GamesFragment() {

    }

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        currentFragment = this;
        context = getActivity();
        folder = new File(context.getExternalFilesDir(null), "/"); //Set folder on sd card
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_games, container, false);

        username = getArguments().getString("username");
        if(username.isEmpty()){ //If didn't retrieve username
            startActivity(new Intent(context, Home.class)); //Start Home activity
            return rootView; //Return default view
        }

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

        if(mExternalStorageWriteable){ //If can write to SD card
            //Create the folder where the images for games will be stored
            File gameImagesFolder = new File(folder, "/gameImages");
            gameImagesFolder.mkdir();
        }
        //Create shared preferences and editor
        savedInformation = context.getSharedPreferences("com.brookes.psntrophies_preferences", 0);
        savedInformationEditor = savedInformation.edit();

        savedXML = context.getSharedPreferences(username + "_xml", 0);
        savedXMLEditor = savedXML.edit();

        changeSettings(); //Retrieves saved settings

        //Retrieves saved XML
        gamesXML = savedXML.getString("games_xml", "");
        profileXML = savedXML.getString("profile_xml", "");

        //Assigns variables to widgets
        profileLayout = rootView.findViewById(R.id.profileLayout);
        profileLayout.setVisibility(View.INVISIBLE);
        updateText = (TextView) rootView.findViewById(R.id.updateText);
        profileImage = (ImageView) rootView.findViewById(R.id.profilePicture);
        psnName = (TextView) rootView.findViewById(R.id.psnName);
        psnAboutMe = (TextView) rootView.findViewById(R.id.psnAboutMe);

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL){
            //On smaller screens the user is given information about PS Plus membership
            psnPlus = (TextView) rootView.findViewById(R.id.psnPlus);
        }
        psnTrophyLevel = (TextView) rootView.findViewById(R.id.psnTrophyLevel);
        psnTrophyProgress = (TextView) rootView.findViewById(R.id.psnTrophyProgress);
        bronzeLabel = (TextView) rootView.findViewById(R.id.bronzeLabel);
        silverLabel = (TextView) rootView.findViewById(R.id.silverLabel);
        goldLabel = (TextView) rootView.findViewById(R.id.goldLabel);
        platinumLabel = (TextView) rootView.findViewById(R.id.platinumLabel);
        profileTable = rootView.findViewById(R.id.profileInformationTable);
        gamesList = (ListView) rootView.findViewById(R.id.gamesList);

        //Create progress dialogs
        gamesDialog = createDialog(DownloadType.PSNAPIGAMES);
        profileDialog = createDialog(DownloadType.PROFILE);

        //Calculate time now
        Date currentDate = Calendar.getInstance().getTime();
        Long currentTime = currentDate.getTime();

        if ((savedInstanceState != null) && (savedInstanceState.getParcelableArrayList("games") != null)) { //If games have been saved
            games = savedInstanceState.getParcelableArrayList("games"); //Retrieve games
            if(savedInstanceState.getParcelable("profile") != null){ //If profile saved
                profile = savedInstanceState.getParcelable("profile"); //Retrieve profile

                //Draw profile
                setProfileColor();
                setProfileInformation();
                setProfilePicture();
            }
            else if(profile != null){ //If profile object exists
                //Draw profile
                setProfileColor();
                setProfileInformation();
                setProfilePicture();
            }
            else{ //Download profile
                profileDialog.show();
                new GetXML(currentFragment).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + username); //Downloads profile
            }
            gamesList.setAdapter(new GamesAdapter(games, context)); //Draw list
            setGamesListener();

            //Show update time
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            Date d = new Date(lastUpdated);
            String displayDate = f.format(d);
            updateText.setText(displayDate);

            return rootView; //Break out of function
        }

        if (games != null) { //If games already exists
            gamesDownloaded = true; //Change flag

            //Change the update label on home screen
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            Date d = new Date(lastUpdated);
            String displayDate = f.format(d);
            updateText.setText(displayDate);

            XMLParser parser = new XMLParser();
            profile = parser.getProfile(profileXML); //Parses XML into Profile Object

            //Draw profile
            setProfileColor();
            setProfileInformation();
            setProfilePicture();

            //Filter and draw list
            filteredGamesList = filterGames(games);
            gamesList.setAdapter(new GamesAdapter(filteredGamesList, context));
            setGamesListener();

            return rootView; //Break out of function
        }

        long timeBetweenSyncs = (syncFrequency * 100); //Amount of time app should wait before downloading data again

        if(syncFrequency == -1){ //If user doesn't want app to sync automatically
            timeBetweenSyncs = 3600000; //Set sync period to one hour to stop data being re-downloaded all the time
        }
        if(deleteFrequency != -1){ //If the user wants images to be deleted automatically
            automaticDelete(currentTime);
        }

        if(lastUpdated == 0L || gamesXML.isEmpty() || profileXML.isEmpty()){ //If information hasn't been synced or there is no saved XML
            sync(); //Starts the process
        }
        else{
            //Calculate amount of time since last sync
            Long timeSinceSync = currentTime - lastUpdated;
            if(timeSinceSync > timeBetweenSyncs){ //If information is old
                sync(); //Starts the process
            }
            else{
                //Change the update label on home screen
                DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                Date d = new Date(lastUpdated);
                String displayDate = f.format(d);
                updateText.setText(displayDate);

                parseGames(gamesXML); //Parse games and download images

                XMLParser parser = new XMLParser();
                profile = parser.getProfile(profileXML); //Parses XML into Profile Object
                //Draw profile
                setProfileColor();
                setProfileInformation();
                setProfilePicture();
            }
        }

        return rootView;
    }

    @Override
    public void onResume(){ //When the activity regains focus
        super.onResume();
        if(gamesDownloaded){ //If the games have been downloaded
            long previousUpdate = lastUpdated;
            changeSettings(); //Retrieves saved settings

            if(previousUpdate != lastUpdated){
                String newGamesXML = savedXML.getString("games_xml", "");
                if(!newGamesXML.isEmpty()){
                    parseGames(newGamesXML);
                    //Change the update label on home screen
                    DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    Date d = new Date(lastUpdated);
                    String displayDate = f.format(d);
                    updateText.setText(displayDate);
                }
            }

            //The list is filtered then drawn
            filteredGamesList = filterGames(games);
            gamesList.setAdapter(new GamesAdapter(filteredGamesList, context));
            setGamesListener();
        }
    }

    private void sync(){
        //Save new update time
        Date currentDate = Calendar.getInstance().getTime();
        Long currentTime = currentDate.getTime();
        savedInformationEditor.putLong("last_updated", currentTime);
        savedInformationEditor.commit();
        lastUpdated = currentTime;
        //Change label on home screen
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        Date d = new Date(lastUpdated);
        String displayDate = f.format(d);
        updateText.setText(displayDate);

        new GetXML(currentFragment).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + username); //Downloads profile
        profileDialog.show();
        new GetXML(currentFragment).execute("http://psntrophies.net16.net/getGames.php?psnid=" + username); //Downloads games
        gamesDialog.show();
    }

    @Override
    public void onProfileDownloaded(String profileXML) {
        profileDialog.cancel();
        XMLParser parser = new XMLParser();
        profile = parser.getProfile(profileXML); //Parses XML into Profile Object
        //Store XML
        savedXMLEditor.putString("profile_xml", profileXML);
        savedXMLEditor.commit();
        //Draw profile
        setProfileColor();
        setProfileInformation();
        setProfilePicture();
    }

    @Override
    public void onPSNGamesDownloaded(String gamesXML) {
        gamesDialog.cancel();
        //Store XML
        savedXMLEditor.putString("games_xml", gamesXML);
        savedXMLEditor.commit();

        parseGames(gamesXML); //Parse the games and download images
    }

    private void downloadGameImages(ArrayList<Game> games){ //This function downloads images if required
        gamesDownloaded = true;
        if(downloadImages){ //If the user wants to download images
            //Resets the progress dialog, the counter and the list of processes
            imagesDownloadedCounter = 0;
            imageProcesses.clear();
            imagesDialog = createDialog(DownloadType.GAMESTROPHIES);
            int newMax = 0;
            for(int i=0; i<games.size(); i++){
                if(games.get(i).getBitmap() == null){ //Only download images which haven't been downloaded
                    newMax++;
                    imagesDialog.setMax(newMax);
                    imagesDialog.show();
                    imageProcesses.add(new GetImage(currentFragment).execute(games.get(i).getImage())); //Download game image and add it to list
                }
                //If no images need downloaded
                else if(i == (games.size() - 1)){
                    //Filter and draw games list
                    filteredGamesList = filterGames(games);
                    gamesList.setAdapter(new GamesAdapter(filteredGamesList, context));
                    setGamesListener();
                }
            }
        }
        else{
            //Draw list without images
            filteredGamesList = filterGames(games);
            gamesList.setAdapter(new GamesAdapter(filteredGamesList, context));
            setGamesListener();
        }

    }

    @Override
    public void onGameImageDownloaded(String url, Bitmap image) {
        imagesDownloadedCounter++; //Increment counter
        imagesDialog.setProgress(imagesDownloadedCounter); //Set the new progress level
        // Attaches image to Object
        for(int i=0; i<games.size(); i++){
            if(games.get(i).getImage().equals(url)){
                games.get(i).setBitmap(image);

                if(mExternalStorageWriteable && saveImages){ //If can write to SD Card & user wants to save images
                    //Save image as 'gameid'.png
                    File gameImage = new File(folder, "/gameImages/" + games.get(i).getId() + ".png");
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
                    gamesDownloaded = true;
                    //List filtered and drawn
                    filteredGamesList = filterGames(games);
                    gamesList.setAdapter(new GamesAdapter(filteredGamesList, context));
                    setGamesListener();
                }
            }
        }
    }
    private void parseGames(String gamesXML){
        if(!gamesDownloaded){ //If games being downloaded for first time
            games = new XMLParser().getPSNAPIGames(gamesXML); //Parses XML into Game Object
            //This loop reads the images from SD card and applies them
            for(int i=0;i<games.size();i++){
                if(mExternalStorageAvailable){ //If can read from SD Card
                    File savedImageFile = new File(folder, "/gameImages/" + games.get(i).getId() + ".png");
                    if(savedImageFile.exists()){
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(savedImageFile.toString(), options);
                        games.get(i).setBitmap(bitmap);
                    }
                }
            }
            downloadGameImages(games);
        }
        else{
            ArrayList<Game> newGames = new XMLParser().getPSNAPIGames(gamesXML); //Parses XML into Game Object
            //This loop iterate through games which have just been downloaded
            for(int i=0;i<newGames.size();i++){
                //This loop iterates through old list to match game images and assign them to new ones
                for(int j =0; j<games.size(); j++){
                    if(games.get(j).getId().equals(newGames.get(i).getId())){
                        newGames.get(i).setBitmap(games.get(j).getBitmap());
                        break;
                    }
                }
            }
            games = newGames;
            downloadGameImages(games);
        }
    }

    private void automaticDelete(Long now){
        //Retrieve all information from saved updates
        SharedPreferences savedUpdateTimes = context.getSharedPreferences(username + "_updates", 0);
        Map<String, Long> allUpdates = (Map<String, Long>) savedUpdateTimes.getAll();

        //Create array of keys in update
        Set<String> savedKeys = allUpdates.keySet();
        Object[] savedKeysArray = savedKeys.toArray();

        for(int i=0; i<savedKeysArray.length; i++){ //Iterates over each key
            Long lastUpdateTime = savedUpdateTimes.getLong((String) savedKeysArray[i], 0L); //Retrieves update time from saved preferences
            Long timeDifference = now - lastUpdateTime; //Difference between time now and time last updated
            if(timeDifference > deleteFrequency){ //If user wants the images to be deleted at this point
                File imageFolder = new File(folder, "/" + savedKeysArray[i]); //Create path to image folder
                new DeleteImages(context).deleteImages(imageFolder.getPath()); //Delete the images
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

        savedInformationEditor.putString("bg_color", backgroundColor).commit(); //Save background color
        profileLayout.setBackgroundColor(Color.parseColor(color)); //Set background to this color
    }
    private void setProfilePicture(){
        profileImage.setImageResource(R.drawable.defaultavatar); //Set default picture
        if(mExternalStorageAvailable){ //If can read from SD Card
            File savedImageFile = new File(folder, username + ".png"); //Path to profile picture
            if(savedImageFile.exists()){
                //Retrieve image
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(savedImageFile.toString(), options);
                //Set image
                profileImage.setImageBitmap(bitmap);
            }
        }
        if(downloadImages){ //If the user wants to download images
            String uri = profile.getAvatar(); //From downloaded profile
            new GetImage(currentFragment).execute(uri); //Download the image
        }
    }
    private void setProfileInformation(){
        setProfileColor();
        //Sets the information in widget to reflect downloaded data
        psnName.setText(profile.getName());
        psnAboutMe.setText(profile.getAboutMe());
        if(psnPlus != null){
            if(profile.isPlus()){
                psnPlus.setText("Yes!");
            }
            else{
                psnPlus.setText("No");
            }
        }
        psnTrophyLevel.setText(Integer.toString(profile.getLevel()));
        psnTrophyProgress.setText(Integer.toString(profile.getProgress()) + "%");
        platinumLabel.setText(Integer.toString(profile.getTrophies()[0]));
        goldLabel.setText(Integer.toString(profile.getTrophies()[1]));
        silverLabel.setText(Integer.toString(profile.getTrophies()[2]));
        bronzeLabel.setText(Integer.toString(profile.getTrophies()[3]));

        //Shows the top layout
        profileLayout.setVisibility(View.VISIBLE);
    }
    @Override
    public void onProfileImageDownloaded(String url, Bitmap image) { //When image downloaded
        profileImage.setImageBitmap(image);
        if(mExternalStorageWriteable && saveImages){ //If can write to SD Card & user wants to save images
            //Save image as profile.png
            File imageFile = new File(folder, username + ".png");
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(imageFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if(fOut != null){
                image.compress(Bitmap.CompressFormat.PNG, 0, fOut);
            }
            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList<Game> filterGames(ArrayList<Game> oldGamesList){
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

    private ProgressDialog createDialog(DownloadType type){
        ProgressDialog dialog = new ProgressDialog(context);
        switch (type) {
            case GAMESTROPHIES:
                //Creates a horizontal percentage progress dialog
                dialog.setMessage("Downloading images...");
                dialog.setIndeterminate(false);
                dialog.setMax(0);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() { //Draw cancel button
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //When cancel button clicked
                        for (int i = 0; i < imageProcesses.size(); i++) {
                            imageProcesses.get(i).cancel(true); //Cancel all processes
                        }
                        //Filter list and draw it
                        filteredGamesList = filterGames(games);
                        gamesList.setAdapter(new GamesAdapter(filteredGamesList, context));
                        setGamesListener();
                        //Hide dialog and change flag
                        gamesDownloaded = true;
                        dialog.dismiss();
                    }
                });
                return dialog;
            case PROFILE:
                dialog.setMessage("Downloading profile");
                dialog.setIndeterminate(true); //Starts spinning wheel dialog
                dialog.setCancelable(false);
                return dialog;
            case PSNAPIGAMES:
                dialog.setMessage("Downloading games");
                dialog.setIndeterminate(true); //Starts spinning wheel dialog
                dialog.setCancelable(false);
                return dialog;
            case PSNAPITROPHIES:
                return dialog;
        }
        return dialog;
    }

    public void changeSettings(){
        gamesFilter = savedInformation.getString("filter_games", "all");
        gamesSort = savedInformation.getString("sort_games", "recent");
        downloadImages = savedInformation.getBoolean("download_images", true);
        saveImages = savedInformation.getBoolean("save_images", true);
        lastUpdated = savedInformation.getLong("last_updated", 0L);
        deleteFrequency = Long.parseLong(savedInformation.getString("delete_frequency", "-1"));
        syncFrequency = Long.parseLong(savedInformation.getString("sync_frequency", "3600"));
    }
    private void setGamesListener(){
        gamesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressWarnings("rawtypes")
            public void onItemClick(AdapterView a, View v, int position, long id) {
                //When item pressed trophy page is opened
                Intent trophiesIntent = new Intent(v.getContext(), TrophiesList.class);
                trophiesIntent.putExtra("game", filteredGamesList.get(position));
                trophiesIntent.putExtra("username", username);
                //trophiesIntent.putExtra("bg_color", backgroundColor);
                startActivity(trophiesIntent);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.games, menu);
        MenuItem sync = menu.findItem(R.id.action_sync);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            sync.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_sync:
                sync();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        //Save profile and games
        outState.putParcelableArrayList("games", games);
        outState.putParcelable("profile", profile);
    }

    @Override
    public void onPSNTrophiesDownloaded(String trophiesXML) {
        // Not used but is required due to implementations

    }

    @Override
    public void onFriendsDownloaded(String friendsXML) {
        // Not used but is required due to implementations
    }
}
