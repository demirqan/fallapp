package com.falapp.fortunetelle;

import android.app.Application;
import android.content.SharedPreferences;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;

public class FalApplication extends Application {

    private static final String PREFS_NAME = "FalAppPrefs";
    private static final String FIRST_RUN = "firstRun";

    private DatabaseHelper dbHelper;
    private User currentUser;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Check first run and initialize user if needed
        checkFirstRun();
    }

    private void checkFirstRun() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean(FIRST_RUN, true);

        if (firstRun) {
            // First time running the app
            // Create new user with default 20 coins
            currentUser = new User("User", 20);
            dbHelper.addUser(currentUser);

            // Save that app has been run before
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(FIRST_RUN, false);
            editor.apply();
        } else {
            // Not first run, get user data
            currentUser = dbHelper.getUser();
            if (currentUser == null) {
                // If for some reason user data is lost, recreate it
                currentUser = new User("User", 20);
                dbHelper.addUser(currentUser);
            }
        }
    }

    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void refreshUserData() {
        currentUser = dbHelper.getUser();
    }
}