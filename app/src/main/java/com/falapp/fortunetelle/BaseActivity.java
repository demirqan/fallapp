package com.falapp.fortunetelle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;

public class BaseActivity extends AppCompatActivity {

    protected DatabaseHelper dbHelper;
    protected User currentUser;
    protected Handler handler;
    protected Runnable refreshRunnable;

    private static final String READING_AVAILABLE_ACTION = "com.falapp.fortunetelle.READING_AVAILABLE";
    private BroadcastReceiver readingReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        dbHelper = new DatabaseHelper(this);


        FalApplication app = (FalApplication) getApplication();


        currentUser = app.getCurrentUser();
        if (currentUser == null) {
            currentUser = dbHelper.getUser();
            if (currentUser == null) {
                // If no user found, create a new one
                currentUser = new User("User", 20);
                dbHelper.addUser(currentUser);
            }
        }


        handler = new Handler();
        refreshRunnable = this::checkForAvailableReadings;


        readingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int readingId = intent.getIntExtra("reading_id", -1);
                if (readingId != -1) {
                    // Show notification to user
                    FortuneReading reading = dbHelper.getReading(readingId);
                    if (reading != null) {
                        Toast.makeText(BaseActivity.this,
                                reading.getReadableType() + " sonucunuz hazır!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(readingReceiver,
                new IntentFilter(READING_AVAILABLE_ACTION));


        startPeriodicChecks();


        refreshUserData();
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(readingReceiver);


        stopPeriodicChecks();
    }

    protected void refreshUserData() {
        currentUser = dbHelper.getUser();

    }

    protected void startPeriodicChecks() {

        handler.postDelayed(refreshRunnable, 10000);
    }

    protected void stopPeriodicChecks() {
        handler.removeCallbacks(refreshRunnable);
    }

    protected void checkForAvailableReadings() {

        if (currentUser != null) {
            new Thread(() -> {
                java.util.List<FortuneReading> readings = dbHelper.getAllReadingsForUser(currentUser.getId());
                for (FortuneReading reading : readings) {
                    if (!reading.isAvailable() && System.currentTimeMillis() >= reading.getAvailableTime()) {

                        reading.setAvailable(true);
                        dbHelper.updateReadingResult(reading.getId(), reading.getResult(), true);

                        // Notify via broadcast
                        Intent intent = new Intent(READING_AVAILABLE_ACTION);
                        intent.putExtra("reading_id", reading.getId());
                        LocalBroadcastManager.getInstance(BaseActivity.this).sendBroadcast(intent);
                    }
                }


                handler.postDelayed(refreshRunnable, 10000);
            }).start();
        }
    }

    protected boolean checkCoins(int requiredCoins) {
        if (currentUser.getCoins() < requiredCoins) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return false;
        }
        return true;
    }

    protected void deductCoins(int amount) {
        if (currentUser.removeCoins(amount)) {
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
        }
    }
}