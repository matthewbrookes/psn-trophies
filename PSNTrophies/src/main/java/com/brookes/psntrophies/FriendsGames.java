package com.brookes.psntrophies;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import java.util.Locale;

import static com.brookes.psntrophies.Home.*;

public class FriendsGames extends ActionBarActivity {
    SectionsPagerAdapter mSectionsPagerAdapter;
    String username = "";

    /**
     * The {@link android.support.v4.view.ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendsgames);

        // Create the adapter that will return the games fragment
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        username = getIntent().getExtras().getString("username"); //Retrieve desired username

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.friends_games, menu);
        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //Return the fragment to be showed
            Fragment fragment = null;
            switch(position){
                case 0:
                    fragment = new GamesFragment();
                    Bundle args = new Bundle();
                    args.putString("username", username); //Set the friend's name as the user to be downloaded
                    fragment.setArguments(args);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show only games fragment
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
            }
            return null;
        }
    }
}
