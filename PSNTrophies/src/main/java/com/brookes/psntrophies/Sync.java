package com.brookes.psntrophies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by matt on 09/07/13.
 */
public class Sync extends Service implements AsyncTaskListener{
    Account account = null;
    String username = "";
    ArrayList<Game> changedGames = new ArrayList<Game>();
    ArrayList<Game> oldGames  = new ArrayList<Game>();

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        //Create shared preferences and editor
        SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
        SharedPreferences.Editor savedInformationEditor = savedInformation.edit();

        //Save current time in shared preference
        Date currentDate = Calendar.getInstance().getTime();
        Long currentTime = currentDate.getTime();
        savedInformationEditor.putLong("last_updated", currentTime);
        savedInformationEditor.commit();

        //Create account manager and list of accounts
        AccountManager mAccountManager = AccountManager.get(getBaseContext());
        Account[] accounts = mAccountManager.getAccounts();

        for(int i=0; i<accounts.length;i++){ //Iterate through accounts
            Account tempAccount = accounts[i]; //Create a temporary account variable
            if(tempAccount.type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN Account
                account = tempAccount; //Set this account as one to be used throughout program
                username = account.name;
            }
        }

        if(account != null){ //If account exists
            Log.i("PSN", "Downloading");
            new GetXML(this).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + username); //Downloads profile
            new GetXML(this).execute("http://psntrophies.net16.net/getGames.php?psnid=" + username); //Downloads games
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onProfileDownloaded(String profileXML) {
        //Create shared preference for XML
        SharedPreferences savedXML = getSharedPreferences("com.brookes.psntrophies_xml", 0);
        SharedPreferences.Editor savedXMLEditor = savedXML.edit();

        //Store XML
        savedXMLEditor.putString("profile_xml", profileXML);
        savedXMLEditor.commit();

    }

    @Override
    public void onPSNGamesDownloaded(String gamesXML) {
        if(gamesXML.isEmpty()){ //If something went wrong during download
            return; //Quit
        }
        Log.i("PSN", "Downloaded");
        //Create shared preference for XML
        SharedPreferences savedXML = getSharedPreferences("com.brookes.psntrophies_xml", 0);
        SharedPreferences.Editor savedXMLEditor = savedXML.edit();

        String oldXML = savedXML.getString("games_xml", "");

        //Lists will hold games and there positions in lists
        ArrayList<Game> newGames = new ArrayList<Game>();
        ArrayList<Integer> changedGamesOldPositions = new ArrayList<Integer>();

        ////Reset lists
        changedGames = new ArrayList<Game>();
        oldGames = new ArrayList<Game>();

        if(!oldXML.isEmpty()){ //If there is currently stored XML data
            Log.i("PSN", "XML Exists");

            newGames = new XMLParser().getPSNAPIGames(gamesXML); //Parse new XML data
            oldGames = new XMLParser().getPSNAPIGames(oldXML); //Parse old XML data

            if(newGames.size() > oldGames.size()){ //If new games have been played
                //Create array of ids in old and new games lists
                String[] oldIds = new String[oldGames.size()];
                String[] newIds = new String[newGames.size()];
                for(int i=0; i<oldGames.size(); i++){ //Iterate over old games
                    oldIds[i] = oldGames.get(i).getId(); //Add id to array
                }

                for(int i=0; i<newGames.size(); i++){ //Iterate over new games
                    newIds[i] = newGames.get(i).getId(); //Add id to array
                }

                for(int i=0; i<newIds.length; i++){ //Iterate over new games
                    for(int j=0; j<oldIds.length; j++){ //Iterate over old games
                        if(newIds[i].equalsIgnoreCase(oldIds[j])){ //If ids match
                            break; //Exit inner loop
                        }
                        else if(j == (oldIds.length - 1)){
                            /*
                            If we reach the end of the loop without having matched
                            the ids then the game was not in the old xml so it must be added
                            as a changed game
                             */
                            changedGames.add(newGames.get(i));
                        }
                    }
                }
            }

            for(int i=0; i<newGames.size(); i++){ //Iterate over downloaded games
                for(int j=0; j<oldGames.size(); j++){ //Iterate over old games
                    if(newGames.get(i).getId().equals(oldGames.get(j).getId())){ //If games match
                        if(newGames.get(i).getTrophiesEarnt() != oldGames.get(j).getTrophiesEarnt()){ //If game information has changed
                            //Add game and position to lists
                            changedGames.add(newGames.get(i));
                            changedGamesOldPositions.add(j);
                        }
                        break;
                    }
                }
            }
        }

        if(changedGames.size() > 0){ //If games have changed
            //Create notification builder and manager
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            //Retrieve shared preferences
            SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
            String ringtone = savedInformation.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
            boolean vibrate = savedInformation.getBoolean("notifications_new_message_vibrate", true);
            boolean showNotifications = savedInformation.getBoolean("notifications_new_message", true);

            if(vibrate){ //If user wants notification to vibrate
                //Create vibrate pattern and apply
                long[] pattern = {300, 200, 300, 200};
                mBuilder.setVibrate(pattern);
            }
            if(!ringtone.isEmpty()){ //If user wants ringtone to play
                mBuilder.setSound(Uri.parse(ringtone)); //Set notification sound
            }

            //Create intents which are launched by notifications
            Intent homeIntent = new Intent(this, Home.class);
            Intent trophiesIntent = new Intent(this, TrophiesList.class);

            if(changedGames.size() == 1){ //If only one game has changed
                int oldTrophiesTotal; //Variable will hold number of trophies previously earned
                try {
                    oldTrophiesTotal = oldGames.get(changedGamesOldPositions.get(0)).getTrophiesEarnt(); //Get old total
                }
                catch (IndexOutOfBoundsException e){ //Exception thrown when new game is added
                    oldTrophiesTotal = 0; //Therefore there were previously no trophies
                }
                int newTrophiesTotal = changedGames.get(0).getTrophiesEarnt(); //Get new total
                int difference = newTrophiesTotal - oldTrophiesTotal; //Calculate difference between totals

                if(difference == 1){ //If only one new trophy has been earned
                    //Download trophy information for this game
                    String id = changedGames.get(0).getId();
                    new GetXML(this).execute("http://psntrophies.net16.net/getTrophies.php?psnid="+ username + "&gameid=" + id);

                    Log.i("PSN", "One new trophy");
                }
                else if(difference > 1){ //If more than one trophy has been earned
                    Log.i("PSN", "Multiple trophies earned");

                    //Download trophy information for this game
                    String id = changedGames.get(0).getId();
                    new GetXML(this).execute("http://psntrophies.net16.net/getTrophies.php?psnid="+ username + "&gameid=" + id);
                }

            }
            else{ //If more than one game played
                Log.i("PSN", "Multiple games played");
                int trophiesEarned = 0; //Running total of how many trophies have been earned
                for(int i=0;i<changedGames.size(); i++){
                    int oldTrophiesTotal; //Variable will hold number of trophies previously earned
                    try {
                        oldTrophiesTotal = oldGames.get(changedGamesOldPositions.get(i)).getTrophiesEarnt(); //Get old total
                    }
                    catch (IndexOutOfBoundsException e){ //Exception thrown when new game is added
                        oldTrophiesTotal = 0; //Therefore there were previously no trophies
                    }

                    int newTrophiesTotal = changedGames.get(i).getTrophiesEarnt(); //Get new total
                    int difference = newTrophiesTotal - oldTrophiesTotal; //Calculate difference
                    trophiesEarned += difference; //Add difference to total

                    //Download trophy information for this game
                    String username = account.name;
                    String id = changedGames.get(0).getId();
                    new GetXML(this).execute("http://psntrophies.net16.net/getTrophies.php?psnid="+ username + "&gameid=" + id);
                }

                // Prepare intent which is triggered if the notification is selected
                PendingIntent pIntent = PendingIntent.getActivity(this, 0, homeIntent, 0);

                //Create notification for multiple games
                mBuilder
                    .setSmallIcon(R.drawable.small_icon)
                    .setContentTitle("You have earned "  + trophiesEarned  +" trophies!")
                    .setContentText("In " + changedGames.size() + " games") //Show how many games have been changed
                    .setContentIntent(pIntent) //Start new activity when pressed
                    .setAutoCancel(true); //Cancel notification when pressed

                mNotificationManager.notify(0, mBuilder.build()); //Display notification
            }
        }
        //Store XML
        savedXMLEditor.putString("games_xml", gamesXML);
        savedXMLEditor.commit();
    }

    @Override
    public void onPSNTrophiesDownloaded(String trophiesXML) {
        //Retrieve shared preferences
        SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
        String ringtone = savedInformation.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound");
        boolean vibrate = savedInformation.getBoolean("notifications_new_message_vibrate", true);
        boolean showNotifications = savedInformation.getBoolean("notifications_new_message", true);

        //Create notification builder and manager
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(vibrate){ //If user wants notification to vibrate
            //Create vibrate pattern and apply
            long[] pattern = {300, 200, 300, 200};
            mBuilder.setVibrate(pattern);
        }
        if(!ringtone.isEmpty()){ //If user wants ringtone to play
            mBuilder.setSound(Uri.parse(ringtone)); //Set notification sound
        }

        //Create shared preference for XML and updates
        SharedPreferences savedXML = getSharedPreferences("com.brookes.psntrophies_xml", 0);
        SharedPreferences.Editor savedXMLEditor = savedXML.edit();
        SharedPreferences savedUpdateTimes = getSharedPreferences("com.brookes.psntrophies_updates", 0);
        SharedPreferences.Editor savedUpdateEditor = savedUpdateTimes.edit();

        //Calculate the current time
        Date currentDate = Calendar.getInstance().getTime();
        Long currentTime = currentDate.getTime();

        if(changedGames.size() == 1){ //If only one game has changed
            Game game = changedGames.get(0);
            if(showNotifications){
                //Create list of old and new trophies
                ArrayList<Trophy> newTrophies = new XMLParser().getPSNAPITrophies(trophiesXML); //Create list from new XML
                ArrayList<Trophy> oldTrophies = new ArrayList<Trophy>();

                //Create list of trophies which have been earned since last sync
                ArrayList<Trophy> earnedTrophies = new ArrayList<Trophy>();

                String oldXML = savedXML.getString(game.getId(), ""); //Attempt to retrieve saved xml
                if(!oldXML.isEmpty()){ //If saved xml exists
                    oldTrophies = new XMLParser().getPSNAPITrophies(oldXML); //Parse old XML
                    for(int i=0; i<newTrophies.size(); i++){ //Iterate over new trophies
                        for(int j=0; j<oldTrophies.size(); j++){ //Iterate over old trophies
                            if(newTrophies.get(i).getId() == oldTrophies.get(j).getId()){ //If trophies match
                                //If this is the trophy which has been earned
                                if((!newTrophies.get(i).getDateEarned().isEmpty() && oldTrophies.get(j).getDateEarned().isEmpty())){
                                    earnedTrophies.add(newTrophies.get(i));
                                }
                            }
                        }
                    }
                }
                else{
                    for(int i=0; i<oldGames.size(); i++){ //Iterate over trophies
                        if(changedGames.get(0).getId().equals(oldGames.get(i).getId())){ //If ids match
                            //Calculate number of trophies which have been earned
                            int trophiesEarned = changedGames.get(0).getTrophiesEarnt() - oldGames.get(i).getTrophiesEarnt();

                            Trophy trophy = new Trophy(); //Create a blank trophy
                            for(int j=0; j<trophiesEarned; j++){ //For each trophy earned
                                earnedTrophies.add(trophy); //Add blank trophy to list
                            }
                        }
                    }
                }

                if(earnedTrophies.size() == 1){ //If one trophy has been earned
                    Intent trophiesIntent = new Intent(this, TrophiesList.class);
                    //Put extras in intent
                    trophiesIntent.putExtra("game", changedGames.get(0));
                    /*
                    The stack builder object will contain an artificial back stack for the
                    started Activity.
                    This ensures that navigating backward from the Activity leads out of
                    the application to the Home screen.
                    */
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    // Adds the back stack for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(TrophiesList.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(trophiesIntent);
                    //Create the pending intent
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent); //Add pending intent to notification

                    if(!oldXML.isEmpty()){ //If old xml exists
                        //Create notification for one trophy using information
                        mBuilder
                            .setContentTitle("You have earned a trophy!")
                            .setContentText(earnedTrophies.get(0).getTitle()) //Show title of trophy
                            .setAutoCancel(true); //Cancel notification when pressed

                        switch (earnedTrophies.get(0).getType()) {
                            case PLATINUM:
                                mBuilder.setSmallIcon(R.drawable.platinum100);
                                break;
                            case GOLD:
                                mBuilder.setSmallIcon(R.drawable.gold100);
                                break;
                            case SILVER:
                                mBuilder.setSmallIcon(R.drawable.silver100);
                                break;
                            case BRONZE:
                                mBuilder.setSmallIcon(R.drawable.bronze100);
                                break;
                        }
                    }
                    else{ //If there was no xml so we don't know which trophy was earned
                        //Create a stock notification
                        mBuilder
                            .setContentTitle("You have earned a trophy!")
                            .setContentText("Playing " + changedGames.get(0).getTitle()) //Show title of game
                            .setSmallIcon(R.drawable.small_icon) //Show app icon
                            .setAutoCancel(true); //Cancel notification when pressed
                    }

                    mNotificationManager.notify(0, mBuilder.build()); //Display notification
                }
                else if(earnedTrophies.size() > 1){ //If more than one trophy has been earned
                    Intent trophiesIntent = new Intent(this, TrophiesList.class);
                    //Put extras in intent
                    trophiesIntent.putExtra("game", changedGames.get(0));

                    /*
                    The stack builder object will contain an artificial back stack for the
                    started Activity.
                    This ensures that navigating backward from the Activity leads out of
                    the application to the Home screen.
                    */
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    // Adds the back stack for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(TrophiesList.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(trophiesIntent);
                    //Create the pending intent
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent); //Add pending intent to notification

                    if(!oldXML.isEmpty()){ //If old xml exists
                        //Create notification for multiple trophies using information
                        mBuilder
                            .setContentTitle("You have earned " + earnedTrophies.size() + " trophies!")
                            .setContentText("Playing " + changedGames.get(0).getTitle()) //Show title of game
                            .setContentIntent(resultPendingIntent) //Start new activity when pressed
                            .setSmallIcon(R.drawable.small_icon)
                            .setAutoCancel(true); //Cancel notification when pressed

                        //Create big view style
                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                        inboxStyle.setBigContentTitle("You have earned " + earnedTrophies.size() + " trophies!");
                        for (int i=0; i<earnedTrophies.size(); i++) { //Iterate over earned trophies
                            inboxStyle.addLine(earnedTrophies.get(i).getTitle()); //Add trophy titles to big view
                        }
                        mBuilder.setStyle(inboxStyle);
                    }
                    else{ //If there was no xml so we don't know which trophy was earned
                        //Create a stock notification
                        mBuilder
                            .setContentTitle("You have earned " + earnedTrophies.size() + " trophies!")
                            .setContentText("Playing " + changedGames.get(0).getTitle()) //Show title of game
                            .setContentIntent(resultPendingIntent) //Start new activity when pressed
                            .setSmallIcon(R.drawable.small_icon)
                            .setAutoCancel(true); //Cancel notification when pressed
                    }


                    mNotificationManager.notify(0, mBuilder.build()); //Display notification
                }
            }
            //Save XML
            savedXMLEditor.putString(game.getId(), trophiesXML);
            savedUpdateEditor.putLong(game.getId(), currentTime);

            //Commit the edits
            savedXMLEditor.commit();
            savedUpdateEditor.commit();
        }
        else{ //If more than one game has changed
            for(int i=0; i<changedGames.size(); i++){ //Iterate over changed game
                if(trophiesXML.contains(changedGames.get(i).getId())){ //If game matches
                    Game game = changedGames.get(i); //Create game object

                    //Save XML
                    savedXMLEditor.putString(game.getId(), trophiesXML);
                    savedUpdateEditor.putLong(game.getId(), currentTime);

                    //Commit the edits
                    savedXMLEditor.commit();
                    savedUpdateEditor.commit();
                }
            }
        }

    }

    @Override
    public void onProfileImageDownloaded(Bitmap image) {
        //Not used but required due to implementation
    }

    @Override
    public void onGameImageDownloaded(String url, Bitmap image) {
        //Not used but required due to implementation
    }
}
