package com.brookes.psntrophies;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class SettingsActivity extends PreferenceActivity implements AuthenticatorListener{
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static Context context;
    private static Account account = null;
    private static File folder;
    private static String newEmail = "";
    private static String newPass = "";
    private static String oldEmail = "";
    private static String oldPass = "";
    private static ProgressDialog authenticationDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
        context = this;
        folder = new File(getExternalFilesDir(null), "/");
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // Add blank list so we can display General header
        addPreferencesFromResource(R.xml.pref_blank);

        // Add 'general' preferences and header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_general);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_general);

        Preference deleteButton = findPreference("delete_button");
        if (deleteButton != null) {
            deleteButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { //When user clicks button to delete images
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    // Dialog checks user wants to delete all images
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Are you sure you want to delete all saved images?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    boolean deletedImages = deleteImages(); //Try to delete images from SD Card
                                    //Create a successful and error message
                                    Toast errorMsg = Toast.makeText(context, "Unable to access SD Card", Toast.LENGTH_SHORT);
                                    Toast successMsg = Toast.makeText(context, "Images have been deleted", Toast.LENGTH_SHORT);
                                    if(deletedImages){ //If images successfully deleted
                                        successMsg.show(); //Show a success message
                                    }
                                    else{
                                        errorMsg.show(); //Show an error message
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                    // Create the AlertDialog object and show it
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });
        }

        //Create account manager and list of accounts
        final AccountManager mAccountManager = AccountManager.get(getBaseContext());
        Account[] accounts = mAccountManager.getAccounts();

        Preference emailButton = findPreference("email");
        if (emailButton != null) {
            for(int i=0; i<accounts.length;i++){ //Iterate through accounts
                Account tempAccount = accounts[i]; //Create a temporary account variable
                if(tempAccount.type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN Account
                    account = tempAccount;
                }
            }
            oldEmail = mAccountManager.getUserData(account, "email");
            emailButton.setSummary(oldEmail);

            emailButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createLogInDialog().show();
                    return false;
                }
            });
        }

        Preference passwordButton = findPreference("password");
        if (passwordButton != null) {
            for(int i=0; i<accounts.length;i++){ //Iterate through accounts
                Account tempAccount = accounts[i]; //Create a temporary account variable
                if(tempAccount.type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN Account
                    account = tempAccount;
                }
            }

            String password = mAccountManager.getPassword(account); //Retrieve password
            oldPass = password;

            //Create a masked password same length as proper password
            int passwordLength = password.length();
            String maskedPassword = "";
            for(int j=0; j<passwordLength; j++){
                maskedPassword += "*";
            }

            passwordButton.setSummary(maskedPassword); //Set masked password as summary
            passwordButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    createLogInDialog().show();
                    return false;
                }
            });
        }

        // Add 'games' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_games);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_games);

        // Add 'trophies' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_trophies);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_trophies);

        // Add 'notifications' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("filter_games"));
        bindPreferenceSummaryToValue(findPreference("sort_games"));
        bindPreferenceSummaryToValue(findPreference("delete_frequency"));
        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        bindPreferenceSummaryToValue(findPreference("sync_frequency"));
    }

    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isLargeTablet(this) && !isSimplePreferences(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isLargeTablet(context);
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference
                        .setSummary(index >= 0 ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone
                                .getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference
                .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(),
                        ""));
    }

    private AlertDialog createLogInDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View layout = inflater.inflate(R.layout.dialog_signin, null);
        final EditText emailWidget = (EditText)layout.findViewById(R.id.email);
        final EditText passwordWidget = (EditText)layout.findViewById(R.id.password);

        if(account != null){
            emailWidget.setText(AccountManager.get(context).getUserData(account, "email"));
            passwordWidget.setText(AccountManager.get(context).getPassword(account));
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(layout)
                // Add action buttons
                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String email = emailWidget.getText().toString().toLowerCase();
                        String password = passwordWidget.getText().toString();
                        newEmail = email;
                        newPass = password;
                        if((!newEmail.equalsIgnoreCase(oldEmail)) || (!newPass.equalsIgnoreCase(oldPass))){ //If data changed
                            authenticationDialog = new ProgressDialog(context);
                            authenticationDialog.setIndeterminate(true);
                            authenticationDialog.setMessage("Authenticating " + email);
                            authenticationDialog.setCancelable(false);
                            authenticationDialog.show();
                            //Authenticate with new data
                            new ServerAuthenticate(context).authenticateUser(email, password);
                        }
                        else{
                            dialog.cancel();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onAccountAuthenticated(String username) {
        authenticationDialog.cancel();
        if(username.isEmpty()){
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Email/Password is incorrect")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and show it
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        else{
            AccountManager am = AccountManager.get(getBaseContext());
            am.setPassword(account, newPass);
            if(!oldEmail.equalsIgnoreCase(newEmail)){ //If email has changed
                am.setUserData(account, "email", newEmail);

            }

            //Recreate preferences with new data
            Preference emailButton = findPreference("email");
            if (emailButton != null) {
                emailButton.setSummary(AccountManager.get(context).getUserData(account, "email"));
                oldEmail = emailButton.getSummary().toString();
                emailButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        createLogInDialog().show();
                        return false;
                    }
                });
            }

            Preference passwordButton = findPreference("password");
            if (passwordButton != null) {
                String password = newPass; //Retrieve password
                oldPass = password;

                //Create a masked password same length as proper password
                int passwordLength = password.length();
                String maskedPassword = "";
                for(int j=0; j<passwordLength; j++){
                    maskedPassword += "*";
                }

                passwordButton.setSummary(maskedPassword); //Set masked password as summary
                passwordButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        createLogInDialog().show();
                        return false;
                    }
                });
            }
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("delete_frequency"));
            Preference deleteButton = (Preference)findPreference("delete_button");
            if (deleteButton != null) {
                deleteButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { //When user clicks button to delete images
                    @Override
                    public boolean onPreferenceClick(Preference arg0) {
                        // Dialog checks user wants to delete all images
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Are you sure you want to delete all saved images?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        boolean deletedImages = deleteImages(); //Try to delete images from SD Card
                                        //Create a successful and error message
                                        Toast errorMsg = Toast.makeText(context, "Unable to access SD Card", Toast.LENGTH_SHORT);
                                        Toast successMsg = Toast.makeText(context, "Images have been deleted", Toast.LENGTH_SHORT);
                                        if(deletedImages){ //If images successfully deleted
                                            successMsg.show(); //Show a success message
                                        }
                                        else{
                                            errorMsg.show(); //Show an error message
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                        // Create the AlertDialog object and show it
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return true;
                    }
                });
            }

            //Create account manager and list of accounts
            final AccountManager mAccountManager = AccountManager.get(context);
            Account[] accounts = mAccountManager.getAccounts();

            Preference emailButton = findPreference("email");
            if (emailButton != null) {
                for(int i=0; i<accounts.length;i++){ //Iterate through accounts
                    Account tempAccount = accounts[i]; //Create a temporary account variable
                    if(tempAccount.type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN Account
                        account = tempAccount;
                    }
                }

                emailButton.setSummary(mAccountManager.getUserData(account, "email"));
                oldEmail = emailButton.getSummary().toString();
                emailButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //createLogInDialog().show();
                        return false;
                    }
                });
            }

            Preference passwordButton = findPreference("password");
            if (passwordButton != null) {
                for(int i=0; i<accounts.length;i++){ //Iterate through accounts
                    Account tempAccount = accounts[i]; //Create a temporary account variable
                    if(tempAccount.type.equals(AccountGeneral.ACCOUNT_TYPE)){ //If it is a PSN Account
                        account = tempAccount;
                    }
                }

                String password = mAccountManager.getPassword(account); //Retrieve password
                oldPass = password;

                //Create a masked password same length as proper password
                int passwordLength = password.length();
                String maskedPassword = "";
                for(int j=0; j<passwordLength; j++){
                    maskedPassword += "*";
                }

                passwordButton.setSummary(maskedPassword); //Set masked password as summary
                passwordButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //createLogInDialog().show();
                        return false;
                    }
                });
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GamesPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_games);
            bindPreferenceSummaryToValue(findPreference("filter_games"));
            bindPreferenceSummaryToValue(findPreference("sort_games"));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class TrophiesPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_trophies);
        }
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends
            PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }

    private static boolean deleteImages(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String storageState = Environment.getExternalStorageState();
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

        boolean error = false; //Flag is changed if an error occurs

        if(mExternalStorageAvailable && mExternalStorageWriteable){ //If can read and write to SD Card
            boolean success = new DeleteImages(context).deleteImages(folder.getPath());
            if(success){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false; //Report failure
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return true;
        }
    }
}