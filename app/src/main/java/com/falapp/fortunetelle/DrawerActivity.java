package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.navigation.NavigationView;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    protected ActionBarDrawerToggle toggle;

    protected DatabaseHelper dbHelper;
    protected User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new DatabaseHelper(this);

        // Get current user
        currentUser = dbHelper.getUser();
        if (currentUser == null) {
            // If no user found, go back to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Set up drawer
        setupDrawer();

        // Update navigation header with user info
        updateNavigationHeader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data
        currentUser = dbHelper.getUser();
        updateNavigationHeader();
    }

    protected void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // Set up toolbar
        //setSupportActionBar(toolbar);

        // Set up drawer toggle
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void updateNavigationHeader() {
        if (navigationView != null && currentUser != null) {
            // Get header view
            android.view.View headerView = navigationView.getHeaderView(0);

            // Update user name
            android.widget.TextView tvUserName = headerView.findViewById(R.id.tv_user_name);
            if (tvUserName != null) {
                tvUserName.setText(currentUser.getName());
            }

            // Update coins
            android.widget.TextView tvCoins = headerView.findViewById(R.id.tv_coins);
            if (tvCoins != null) {
                tvCoins.setText(String.valueOf(currentUser.getCoins()) + " Altın");
            }

            // Update premium status
            android.widget.TextView tvPremiumStatus = headerView.findViewById(R.id.tv_premium_status);
            if (tvPremiumStatus != null) {
                tvPremiumStatus.setText(currentUser.isPremium() ? "Premium Üye" : "Standart Üye");
                tvPremiumStatus.setTextColor(getResources().getColor(
                        currentUser.isPremium() ? R.color.premium_color : R.color.standard_color));
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item clicks
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        }
        else if (itemId == R.id.nav_readings) {
            startActivity(new Intent(this, MyReadingsActivity.class));
        }
        else if (itemId == R.id.nav_horoscope) {
            startActivity(new Intent(this, HoroscopeActivity.class));
        }
        else if (itemId == R.id.nav_tarot) {
            startActivity(new Intent(this, TarotActivity.class));
        }
        else if (itemId == R.id.nav_dream) {
            startActivity(new Intent(this, DreamInterpretationActivity.class));
        }
        else if (itemId == R.id.nav_love) {
            startActivity(new Intent(this, LoveCompatibilityInputActivity.class));
        }
        else if (itemId == R.id.nav_relaxation) {
            startActivity(new Intent(this, RelaxationActivity.class));
        }
        else if (itemId == R.id.nav_coins) {
            startActivity(new Intent(this, CoinPurchaseActivity.class));
        }
        else if (itemId == R.id.nav_premium) {
            startActivity(new Intent(this, PremiumActivity.class));
        }
        else if (itemId == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        // Close drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Close drawer if open, otherwise handle normal back press
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}