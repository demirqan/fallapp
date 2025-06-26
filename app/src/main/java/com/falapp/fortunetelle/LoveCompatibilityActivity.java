package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LoveCompatibilityActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private MaterialToolbar toolbar;
    private CardView cardNewCompatibility;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_compatibility);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get current user
        currentUser = dbHelper.getUser();
        if (currentUser == null) {
            // If no user found, go back to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Set up compatibility option
        setupCompatibilityOption();

        // Set up navigation
        setupBottomNavigation();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardNewCompatibility = findViewById(R.id.card_new_compatibility);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        // Mevcut ActionBar'ı kullan, setSupportActionBar çağırma
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Aşk Uyumu");
        }

        // Toolbar navigation tıklamasını handle et
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupCompatibilityOption() {
        cardNewCompatibility.setOnClickListener(v -> {
            // Navigate to new compatibility input
            Intent intent = new Intent(LoveCompatibilityActivity.this, LoveCompatibilityInputActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        // Önce item'i seç, sonra listener'ı setup et
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Ana sayfaya dönmek için mevcut Activity'yi kapat
                finish();
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                startActivity(new Intent(LoveCompatibilityActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(LoveCompatibilityActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(LoveCompatibilityActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });
    }
}