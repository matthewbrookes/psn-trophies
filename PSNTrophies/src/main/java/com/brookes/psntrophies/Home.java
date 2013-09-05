package com.brookes.psntrophies;

import java.io.File;
import java.util.Locale;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.content.Intent;

import android.content.SharedPreferences;

import android.os.Environment;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Home extends ActionBarActivity implements ActionBar.TabListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    Account mAccount = null; //Account on device
    AccountManager mAccountManager = null;

    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String storageState = Environment.getExternalStorageState();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccounts();
        for(int i=0; i<accounts.length; i++){
            if(accounts[i].type.equalsIgnoreCase(AccountGeneral.ACCOUNT_TYPE)){
                mAccount = accounts[i];
                break;
            }
            else if(i == (accounts.length - 1)){ //If no accounts on device
                startActivity(new Intent(this, LogIn.class)); //Start log in activity
                return; //Break out of activity
            }
        }

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
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

        if(mAccount != null){
            setAutoSync(); //Add or delete automatic sync
        }
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
                //Delete account
                mAccountManager.removeAccount(mAccount, null, null);

                SharedPreferences savedXML = getSharedPreferences(mAccount.name + "_xml", 0);
                SharedPreferences.Editor savedXMLEditor = savedXML.edit();
                savedXMLEditor.clear(); //Delete saved XML
                savedXMLEditor.commit(); // Commit the edits!

                if(mExternalStorageWriteable){ //If can write from SD Card
                    File folder  = new File(getExternalFilesDir(null), "/"); //Set folder on sd card
                    //Delete profile picture
                    new DeleteImages(this).deleteFile(new File(folder, mAccount.name + ".png").getPath()); //Path to profile picture
                }
                //Return to login screen
                Intent i = new Intent(this, LogIn.class);
                startActivity(i);
                return true;
            case R.id.action_settings:
                Intent j = new Intent(this, SettingsActivity.class);
                startActivity(j);
            default:
                return false;

        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction fragmentTransaction) {
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //Return either the games or friends fragment to be showed
            Fragment fragment = null;
            switch(position){
                case 0:
                    fragment = new GamesFragment();
                    Bundle args = new Bundle();
                    args.putString("username", mAccount.name); //Set the user's name as the user to be downloaded
                    fragment.setArguments(args);
                    break;
                case 1:
                    fragment = new FriendsFragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    private void setAutoSync(){
        SharedPreferences savedInformation = getSharedPreferences("com.brookes.psntrophies_preferences", 0);
        long syncFrequency = Long.parseLong(savedInformation.getString("sync_frequency", "3600"));
        //Retrieve sync setting from settings
        boolean autoSync = getContentResolver().getSyncAutomatically(mAccount, "com.brookes.psntrophies.provider");

        if(syncFrequency != -1 && autoSync){ //If user wants automatic syncing
            //Update periodic sync
            getContentResolver().addPeriodicSync(mAccount, "com.brookes.psntrophies.provider", new Bundle(), syncFrequency);
        }
        else{
            //Delete periodic sync
            getContentResolver().removePeriodicSync(mAccount, "com.brookes.psntrophies.provider", new Bundle());
        }
    }
}
