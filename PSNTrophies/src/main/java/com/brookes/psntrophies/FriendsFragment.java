package com.brookes.psntrophies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class FriendsFragment extends Fragment implements AsyncTaskListener, AuthenticatorListener{
    //Variables are modified throughout fragment so are defined here
    SharedPreferences savedInformation;
    SharedPreferences savedXML;
    SharedPreferences.Editor savedInformationEditor;
    SharedPreferences.Editor savedXMLEditor;
    View profileLayout = null;
    Profile profile;
    ArrayList<Friend> friends;
    ImageView profileImage = null;
    TextView updateText = null;
    TextView psnName = null;
    TextView psnPlus = null;
    TextView psnAboutMe = null;
    TextView psnTrophyLevel = null;
    TextView psnTrophyProgress = null;
    String backgroundColor = "";
    String friendsXML = "";
    String profileXML = "";
    String email = "";
    String password = "";
    View profileTable = null;
    ListView friendsList = null;
    TextView bronzeLabel, silverLabel, goldLabel, platinumLabel = null;
    boolean downloadImages;
    boolean saveImages;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String storageState = Environment.getExternalStorageState();
    int imagesDownloadedCounter = 0;
    long lastUpdated = 0L;
    long deleteFrequency;
    long syncFrequency;
    ProgressDialog profileDialog;
    ProgressDialog friendsDialog;

    ArrayList<AsyncTask<String, Void, Bitmap>> imageProcesses = new ArrayList<AsyncTask <String, Void, Bitmap>>();
    Account account = null;

    Fragment currentFragment;
    Context context;
    File folder;

    public FriendsFragment() {

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
        setHasOptionsMenu(true); //Show menu

        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        //Create account manager and list of accounts
        final AccountManager mAccountManager = AccountManager.get(context);
        Account[] accounts = mAccountManager.getAccounts();

        for(int i=0; i<accounts.length;i++){ //Iterate through accounts
            Account tempAccount = accounts[i]; //Create a temporary account variable
            if(tempAccount.type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN Account
                account = tempAccount; //Set this account as one to be used throughout program
            }
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

        savedXML = context.getSharedPreferences(account.name + "_xml", 0);
        savedXMLEditor = savedXML.edit();

        profileXML = savedXML.getString("profile_xml", "");
        friendsXML = savedXML.getString("friends_xml", "");

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
        friendsList = (ListView) rootView.findViewById(R.id.friendsList);

        changeSettings(); //Get latest settings

        profileDialog = createDialog(DownloadType.PROFILE);

        //Calculate time now
        Date currentDate = Calendar.getInstance().getTime();
        long currentTime = currentDate.getTime();


        //Retrieve password and email
        password = mAccountManager.getPassword(account);
        email = mAccountManager.getUserData(account, "email");

        friendsDialog = createDialog(DownloadType.FRIENDS);


        if(!profileXML.isEmpty()){ //If profile xml has been downloaded
            XMLParser parser = new XMLParser();
            profile = parser.getProfile(profileXML); //Parses XML into Profile Object
            //Draw profile
            setProfileColor();
            setProfileInformation();
            setProfilePicture();
        }
        else{
            profileDialog.show();
            new GetXML(currentFragment).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + account.name); //Downloads profile
        }

        if ((savedInstanceState != null) && (savedInstanceState.getParcelableArrayList("friends") != null)) { //If friends have been saved
            friends = savedInstanceState.getParcelableArrayList("friends"); //Retrieve friends
            if(savedInstanceState.getParcelable("profile") != null){
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
                new GetXML(currentFragment).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + account.name); //Downloads profile
            }
            friendsList.setAdapter(new FriendsAdapter(friends, context)); //Draw list

            //Show update time
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            Date d = new Date(lastUpdated);
            String displayDate = f.format(d);
            updateText.setText(displayDate);

            return rootView; //Break out of function
        }

        if(friendsXML.isEmpty()){
            friendsDialog.show(); //Show progress dialog
            //Authenticate user and download friends
            new ServerAuthenticate(currentFragment).authenticateUser(email, password);
        }
        else{ //Show friends and sync
            friends = new XMLParser().getFriends(friendsXML); //Parse friends
            if(mExternalStorageAvailable){ //If can read from SD Card
                for(int i=0; i<friends.size(); i++){ //Iterate over friends
                    File avatar = new File(folder, friends.get(i).getUsername() + ".png"); //Location of avatar
                    if(avatar.exists()){
                        //Convert to bitmap
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(avatar.toString(), options);

                        friends.get(i).setImage(bitmap); //Set bitmap for friend
                    }
                }
            }
            //Show update time
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            Date d = new Date(lastUpdated);
            String displayDate = f.format(d);
            updateText.setText(displayDate);

            downloadFriendsImages(friends);

            new ServerAuthenticate(currentFragment).authenticateUser(email, password); //Authenticate user and download friends
        }
        return rootView;
    }

    @Override
    public void onResume(){ //When the activity regains focus
        super.onResume();
        if(savedInformation != null){
            changeSettings(); //Retrieve latest settings
        }
        if(account != null){ //If account has been assigned
            //Store original email and password in variables
            String oldEmail = email;
            String oldPass = password;

            AccountManager accountManager = AccountManager.get(context); //Get instance of AM

            //Get new email and password
            String newEmail = accountManager.getUserData(account, "email");
            String newPass = accountManager.getPassword(account);

            if((!newEmail.equalsIgnoreCase(oldEmail)) || (!newPass.equalsIgnoreCase(oldPass))){
                //If passwords don't match
                //Assign member variables to new data
                email = newEmail;
                password = newPass;

                friendsDialog.show(); //Show progress dialog
                //Authenticate with new data
                new ServerAuthenticate(currentFragment).authenticateUser(email, password);
            }
        }
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
    public void onFriendsDownloaded(String friendsXML) {
        friendsDialog.cancel();
        //Show update time
        lastUpdated = Calendar.getInstance().getTime().getTime();
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        Date d = new Date(lastUpdated);
        String displayDate = f.format(d);
        updateText.setText(displayDate);

        if(friendsXML.contains("<Friends/>")){ //Means something went wrong server side
            if(this.friendsXML.isEmpty()){
                friendsDialog.show(); //Only show dialog if member variable is empty
            }
            new ServerAuthenticate(currentFragment).authenticateUser(email, password);
        }
        else if(!friendsXML.contains("<Friends></Friends>")){ //If friends have been downloaded
            friends = new XMLParser().getFriends(friendsXML); //Parse XML
            this.friendsXML = friendsXML; //Save in member variable

            if(mExternalStorageAvailable){ //If can read from SD Card
                for(int i=0; i<friends.size(); i++){ //Iterate over friends
                    File avatar = new File(folder, friends.get(i).getUsername() + ".png"); //Location of avatar
                    if(avatar.exists()){
                        //Convert to bitmap
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(avatar.toString(), options);

                        friends.get(i).setImage(bitmap); //Set bitmap for friend
                    }
                }
            }
            downloadFriendsImages(friends);


            //Save time and XML
            savedInformationEditor.putLong("friends_updated", lastUpdated).commit();
            savedXMLEditor.putString("friends_xml", friendsXML).commit();
        }
    }

    @Override
    public void onProfileImageDownloaded(String url, Bitmap image) {
        if(url.equalsIgnoreCase(profile.getAvatar())){ //If downloading profile image
            profileImage.setImageBitmap(image);
        }
        else{
            // Attaches image to Friend
            for(int i=0; i<friends.size(); i++){
                if(friends.get(i).getAvatar().equals(url)){
                    friends.get(i).setImage(image);

                    if(mExternalStorageWriteable && saveImages){ //If can write to SD Card & user wants to save images
                        //Save image as 'name'.png
                        File gameImage = new File(folder, friends.get(i).getUsername() + ".png");
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
                        //Draw list
                        friendsList.setAdapter(new FriendsAdapter(friends, context));
                    }
                }
            }
        }
    }

    private void downloadFriendsImages(ArrayList<Friend> friends){ //This function downloads images if required
        if(downloadImages){ //If the user wants to download images
            //Resets the progress dialog, the counter and the list of processes
            for(int i=0; i<friends.size(); i++){
                new GetImage(currentFragment).execute(friends.get(i).getAvatar()); //Download game image and add it to list
                //At end of list
                if(i == (friends.size() - 1)){
                    //Draw list
                    friendsList.setAdapter(new FriendsAdapter(friends, context));
                }
            }
        }
        else{
            //Draw list without images
            friendsList.setAdapter(new FriendsAdapter(friends, context));
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
            File savedImageFile = new File(folder, account.name + ".png"); //Path to profile picture
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

    public void changeSettings(){
        downloadImages = savedInformation.getBoolean("download_images", true);
        saveImages = savedInformation.getBoolean("save_images", true);
        lastUpdated = savedInformation.getLong("last_updated", 0L);
        deleteFrequency = Long.parseLong(savedInformation.getString("delete_frequency", "-1"));
        syncFrequency = Long.parseLong(savedInformation.getString("sync_frequency", "3600"));
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

                        //Draw list
                        friendsList.setAdapter(new FriendsAdapter(friends, context));

                        //Hide dialog
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
            case FRIENDS:
                dialog.setMessage("Downloading friends");
                dialog.setIndeterminate(true); //Starts spinning wheel dialog
                dialog.setCancelable(true); //Can hide this dialog as it takes a long time to download
                return dialog;
        }
        return dialog;
    }

    @Override
    public void onAccountAuthenticated(String name) {
        if(name.isEmpty()){
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Please change information in the Settings menu")
                    .setTitle("Saved Email/Password is incorrect")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and show it
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        else{
            if(password.isEmpty()){
                AccountManager am = AccountManager.get(context);
                password = am.getPassword(account);
            }
            new GetXML(currentFragment).execute("http://psntrophies.net16.net/getFriends.php?email=" + email + "&pass=" + password);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.friends, menu);
        MenuItem sync = menu.findItem(R.id.action_sync);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            sync.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_sync:
                friendsDialog.show(); //Show progress dialog
                new ServerAuthenticate(currentFragment).authenticateUser(email, password);
                new GetImage(currentFragment).execute(profile.getAvatar()); //Download profile
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save profile and friends
        outState.putParcelableArrayList("friends", friends); //Save array when screen rotates
        outState.putParcelable("profile", profile);
    }

    @Override
    public void onPSNGamesDownloaded(String gamesXML) {
        //Not used but required due to implementations
    }

    @Override
    public void onPSNTrophiesDownloaded(String trophiesXML) {
        //Not used but required due to implementations
    }

    @Override
    public void onGameImageDownloaded(String url, Bitmap image) {
        //Not used but required due to implementations
    }

}
