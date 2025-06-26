package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.falapp.fortunetelle.adapters.ReadingsAdapter;
import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MyReadingsActivity extends AppCompatActivity implements ReadingsAdapter.ReadingClickListener {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private RecyclerView recyclerView;
    private ReadingsAdapter adapter;
    private View tvNoReadings;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomNavigationView bottomNavigationView;
    private Handler handler;
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_readings);

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
        recyclerView = findViewById(R.id.recycler_readings);
        tvNoReadings = findViewById(R.id.tv_no_readings);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Fallarım");

        // Set up recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshReadings);

        // Set up navigation
        setupBottomNavigation();

        // Load readings
        loadReadings();

        // Set up auto refresh
        handler = new Handler();
        refreshRunnable = this::refreshReadings;
        startAutoRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh readings when returning to this activity
        refreshReadings();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto refresh when leaving this activity
        stopAutoRefresh();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyReadingsActivity.this, MainActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                // Already on readings, do nothing
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(MyReadingsActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(MyReadingsActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });

        // Set the readings item as checked
        bottomNavigationView.setSelectedItemId(R.id.nav_readings);
    }

    private void loadReadings() {
        List<FortuneReading> readings = dbHelper.getAllReadingsForUser(currentUser.getId());

        if (readings.isEmpty()) {
            tvNoReadings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoReadings.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new ReadingsAdapter(this, readings, dbHelper, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void refreshReadings() {
        List<FortuneReading> readings = dbHelper.getAllReadingsForUser(currentUser.getId());

        if (readings.isEmpty()) {
            tvNoReadings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoReadings.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new ReadingsAdapter(this, readings, dbHelper, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateReadings(readings);
            }
        }

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void startAutoRefresh() {
        // Refresh every 10 seconds to check if readings are available
        handler.postDelayed(refreshRunnable, 10000);
    }

    private void stopAutoRefresh() {
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onReadingClicked(FortuneReading reading) {
        if (reading.isReadyToView()) {
            // If reading is ready to view, show it
            Intent intent = new Intent(this, ReadingResultActivity.class);
            intent.putExtra("reading_id", reading.getId());
            startActivity(intent);
        } else {
            // If reading is not ready, show toast
            long remainingTime = reading.getRemainingTime();
            long remainingSeconds = remainingTime / 1000;
            long minutes = remainingSeconds / 60;
            long seconds = remainingSeconds % 60;

            String timeMessage = String.format("Falınız hazırlanıyor. Kalan süre: %d:%02d", minutes, seconds);
            android.widget.Toast.makeText(this, timeMessage, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}