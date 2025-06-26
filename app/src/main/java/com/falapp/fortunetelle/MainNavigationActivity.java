package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.fragments.AffirmationsFragment;
import com.falapp.fortunetelle.fragments.DreamInterpretationFragment;
import com.falapp.fortunetelle.fragments.HoroscopeFragment;
import com.falapp.fortunetelle.fragments.JournalFragment;
import com.falapp.fortunetelle.fragments.LoveCompatibilityFragment;
import com.falapp.fortunetelle.fragments.ReadingsFragment;
import com.falapp.fortunetelle.fragments.TarotFragment;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainNavigationActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private BottomNavigationView bottomNavigationView;
    private BottomNavigationView featureNavigationView;
    private FrameLayout fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get current user
        currentUser = dbHelper.getUser();
        if (currentUser == null) {
            // If no user found, go back to main activity to create one
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        featureNavigationView = findViewById(R.id.feature_navigation);
        fragmentContainer = findViewById(R.id.fragment_container);

        // Set up navigation
        setupBottomNavigation();
        setupFeatureNavigation();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new ReadingsFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Go to main activity
                startActivity(new Intent(MainNavigationActivity.this, MainActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                loadFragment(new ReadingsFragment());
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                loadFragment(new HoroscopeFragment());
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(MainNavigationActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });
    }

    private void setupFeatureNavigation() {
        featureNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.feature_tarot) {
                loadFragment(new TarotFragment());
                return true;
            }
            else if (itemId == R.id.feature_dream) {
                loadFragment(new DreamInterpretationFragment());
                return true;
            }
            else if (itemId == R.id.feature_love) {
                loadFragment(new LoveCompatibilityFragment());
                return true;
            }
            else if (itemId == R.id.feature_journal) {
                loadFragment(new JournalFragment());
                return true;
            }
            else if (itemId == R.id.feature_affirmations) {
                loadFragment(new AffirmationsFragment());
                return true;
            }

            return false;
        });
    }

    private boolean loadFragment(androidx.fragment.app.Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    public void checkCoinsAndProceed(int requiredCoins, Runnable action) {
        // Refresh user data
        currentUser = dbHelper.getUser();

        // Check if user has enough coins
        if (currentUser.getCoins() < requiredCoins) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
        } else {
            // Deduct coins and perform action
            currentUser.removeCoins(requiredCoins);
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
            action.run();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public DatabaseHelper getDatabaseHelper() {
        return dbHelper;
    }
}