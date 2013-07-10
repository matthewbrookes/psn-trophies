package com.brookes.psntrophies;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by matt on 09/07/13.
 */
public class Sync extends Service implements AsyncTaskListener{
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
        SharedPreferences.Editor savedInformationEditor = savedInformation.edit();


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
            new GetXML(this).execute("http://psntrophies.net16.net/getProfile.php?psnid=" + account.name); //Downloads profile
            new GetXML(this).execute("http://psntrophies.net16.net/getGames.php?psnid=" + account.name); //Downloads games
        }
        else{
            return; //Exit sync
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onProfileDownloaded(String profileXML) {
        Toast.makeText(this, "Profile downloaded", Toast.LENGTH_SHORT).show();
        SharedPreferences savedXML = getSharedPreferences("com.brookes.psntrophies_xml", 0);
        SharedPreferences.Editor savedXMLEditor = savedXML.edit();
        //Store XML
        savedXMLEditor.putString("profile_xml", profileXML);
        savedXMLEditor.commit();

    }

    @Override
    public void onPSNGamesDownloaded(String gamesXML) {
        Toast.makeText(this, "Games downloaded", Toast.LENGTH_SHORT).show();
        SharedPreferences savedXML = getSharedPreferences("com.brookes.psntrophies_xml", 0);
        SharedPreferences.Editor savedXMLEditor = savedXML.edit();
        //StoreXML
        savedXMLEditor.putString("games_xml", gamesXML);
        savedXMLEditor.commit();

    }

    @Override
    public void onPSNTrophiesDownloaded(String trophiesXML) {

    }

    @Override
    public void onProfileImageDownloaded(Bitmap image) {

    }

    @Override
    public void onGameImageDownloaded(String url, Bitmap image) {

    }
}
