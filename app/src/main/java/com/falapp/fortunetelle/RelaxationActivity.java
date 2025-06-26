package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RelaxationActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private MaterialToolbar toolbar;
    private CardView cardAffirmations;
    private CardView cardJournal;
    private TextView tvTitle;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relaxation);

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

        // Set up card click listeners
        setupCardClickListeners();

        // Set up navigation
        setupBottomNavigation();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardAffirmations = findViewById(R.id.card_affirmations);
        cardJournal = findViewById(R.id.card_journal);
        tvTitle = findViewById(R.id.tv_title);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Rahatla");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCardClickListeners() {
        cardAffirmations.setOnClickListener(v -> {
            // Open affirmations activity
            startActivity(new Intent(RelaxationActivity.this, AffirmationsActivity.class));
        });

        cardJournal.setOnClickListener(v -> {
            // Open journal activity
            startActivity(new Intent(RelaxationActivity.this, JournalActivity.class));
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(RelaxationActivity.this, MainActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                startActivity(new Intent(RelaxationActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(RelaxationActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(RelaxationActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });

        // Set the home item as checked
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }
}