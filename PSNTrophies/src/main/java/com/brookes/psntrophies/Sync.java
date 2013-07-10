package com.brookes.psntrophies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by matt on 09/07/13.
 */
public class Sync extends Service implements AsyncTaskListener{
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        //Create shared preferences and editor
        SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
        SharedPreferences.Editor savedInformationEditor = savedInformation.edit();

        /*
        // Prepare intent which is triggered if the
        // notification is selected
        Intent newIntent = new Intent(this, Home.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, newIntent, 0);

        // Build notification
        Notification noti = new Notification.Builder(this)
                .setContentTitle("Test notification")
                .setContentText("Sync")
                .setSmallIcon(R.drawable.small_icon)
                .setContentIntent(pIntent)
                .build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

        */

        //Save current time in shared preference
        Date currentDate = Calendar.getInstance().getTime();
        Long currentTime = currentDate.getTime();
        savedInformationEditor.putLong("last_updated", currentTime);
        savedInformationEditor.commit();

        //Create account manager and list of accounts
        AccountManager mAccountManager = AccountManager.get(getBaseContext());
        Account[] accounts = mAccountManager.getAccounts();

        Account account = null;
        for(int i=0; i<accounts.length;i++){ //Iterate through accounts
            Account tempAccount = accounts[i]; //Create a temporary account variable
            if(tempAccount.type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN Account
                account = tempAccount; //Set this account as one to be used throughout program
            }
        }

        if(account != null){ //If account exists
            Log.i("PSN", "Downloading");
            new GetXML(this).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + account.name); //Downloads profile
            new GetXML(this).execute("http://psntrophies.net16.net/getGames.php?psnid=" + account.name); //Downloads games
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
        Log.i("PSN", "Downloaded");
        //Create shared preference for XML
        SharedPreferences savedXML = getSharedPreferences("com.brookes.psntrophies_xml", 0);
        SharedPreferences.Editor savedXMLEditor = savedXML.edit();

        String oldXML = savedXML.getString("games_xml", "");

        //Lists will hold games and there positions in lists
        ArrayList<Game> newGames = new ArrayList<Game>();
        ArrayList<Game> oldGames = new ArrayList<Game>();
        ArrayList<Game> changedGames = new ArrayList<Game>();
        ArrayList<Integer> changedGamesOldPositions = new ArrayList<Integer>();

        if(!oldXML.isEmpty()){ //If there is currently stored XML data
            Log.i("PSN", "XML Exists");

            newGames = new XMLParser().getPSNAPIGames(gamesXML); //Parse new XML data
            oldGames = new XMLParser().getPSNAPIGames(oldXML); //Parse old XML data
            if(newGames.size() > oldGames.size()){ //If new games have been played
                Log.i("PSN", "New Games Added");
                for(int i=0; i<(newGames.size() - oldGames.size()); i++){ //For each new game
                    //Add game to list
                    changedGames.add(newGames.get(i));
                }
            }

            for(int i=0; i<newGames.size(); i++){ //Iterate over downloaded games
                for(int j=0; j<oldGames.size(); j++){ //Iterate over old games
                    if(newGames.get(i).getId().equals(oldGames.get(j).getId())){ //If games match
                        if(!newGames.get(i).getUpdated().equals(oldGames.get(j).getUpdated())){ //If game information has changed
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
                    Log.i("PSN", "One new trophy");

                    //Create notification for one trophy
                    mBuilder
                    .setSmallIcon(R.drawable.small_icon)
                    .setContentTitle("You have earned a trophy!")
                    .setContentText(changedGames.get(0).getTitle()); //Show title of game

                    mNotificationManager.notify(0, mBuilder.build()); //Display notification
                }
                else if(difference > 1){ //If more than one trophy has been earned
                    Log.i("PSN", "Multiple trophies earned");

                    //Create notification for multiple trophies
                    mBuilder
                            .setSmallIcon(R.drawable.small_icon)
                            .setContentTitle("You have earned " + difference + " trophies!")
                            .setContentText(changedGames.get(0).getTitle()); //Show title of game

                    mNotificationManager.notify(0, mBuilder.build()); //Display notification
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
                }

                //Create notification for multiple games
                mBuilder
                        .setSmallIcon(R.drawable.small_icon)
                        .setContentTitle("You have earned "  + trophiesEarned  +" trophies!")
                        .setContentText("In " + changedGames.size() + " games"); //Show how many games have been changed

                mNotificationManager.notify(0, mBuilder.build()); //Display notification
            }
        }

        //Store XML
        savedXMLEditor.putString("games_xml", gamesXML);
        savedXMLEditor.commit();



    }

    @Override
    public void onPSNTrophiesDownloaded(String trophiesXML) {
        //Not used but required due to implementation
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
