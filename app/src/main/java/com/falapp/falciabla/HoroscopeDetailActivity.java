package com.falapp.falciabla;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.falciabla.api.ChatGPTService;
import com.falapp.falciabla.models.User;
import com.falapp.falciabla.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

public class HoroscopeDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private MaterialToolbar toolbar;
    private Button btnShare;
    private TabLayout tabLayout;
    private ImageView ivZodiacSign;
    private TextView tvZodiacSign;
    private TextView tvHoroscope;
    private View progressBar;

    private String[] zodiacSigns = {"Koç", "Boğa", "İkizler", "Yengeç", "Aslan", "Başak",
            "Terazi", "Akrep", "Yay", "Oğlak", "Kova", "Balık"};
    private String[] periods = {"Günlük", "Haftalık", "Aylık"};

    private String currentZodiacSign;
    private String currentPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horoscope_detail);

        // Get data from intent
        currentZodiacSign = getIntent().getStringExtra("zodiac_sign");
        currentPeriod = getIntent().getStringExtra("period");

        if (currentZodiacSign == null || currentPeriod == null) {
            currentZodiacSign = "Koç"; // Default sign
            currentPeriod = "Günlük"; // Default period
        }


        // Initialize database helper and services
        dbHelper = new DatabaseHelper(this);
        chatGPTService = new ChatGPTService();

        // Get current user
        currentUser = dbHelper.getUser();

        // Initialize views
        initViews();

        btnShare.setOnClickListener(v -> {
            String horoscopeText = tvHoroscope.getText().toString().trim();

            if (horoscopeText.isEmpty()) {
                Toast.makeText(HoroscopeDetailActivity.this, "Paylaşılacak yorum yok.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Burç Yorumum (" + currentZodiacSign + ")");
            shareIntent.putExtra(Intent.EXTRA_TEXT, horoscopeText);

            startActivity(Intent.createChooser(shareIntent, "Yorumu Paylaş"));
        });

        // Set up toolbar
        setupToolbar();

        // Set up tab layout for periods
        setupTabLayout();

        // Set up zodiac sign selection
        setupZodiacSignSelection();

        // Get initial horoscope
        getHoroscope();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        ivZodiacSign = findViewById(R.id.iv_zodiac_sign);
        tvZodiacSign = findViewById(R.id.tv_zodiac_sign);
        tvHoroscope = findViewById(R.id.tv_horoscope);
        progressBar = findViewById(R.id.progress_bar);
        btnShare = findViewById(R.id.btn_share);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Burç Yorumu");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTabLayout() {
        // Add tabs for daily, weekly, monthly
        for (String period : periods) {
            tabLayout.addTab(tabLayout.newTab().setText(period));
        }

        // Set selected tab based on current period
        for (int i = 0; i < periods.length; i++) {
            if (periods[i].equals(currentPeriod)) {
                tabLayout.selectTab(tabLayout.getTabAt(i));
                break;
            }
        }

        // Set tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentPeriod = periods[tab.getPosition()];
                getHoroscope();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
    }

    private void setupZodiacSignSelection() {
        tvZodiacSign.setText(currentZodiacSign);
        setZodiacImage(currentZodiacSign);

        // Set click listener to change zodiac sign
        ivZodiacSign.setOnClickListener(v -> showZodiacSignSelectionDialog());
    }

    private void showZodiacSignSelectionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Burç Seçin");
        builder.setItems(zodiacSigns, (dialog, which) -> {
            currentZodiacSign = zodiacSigns[which];
            tvZodiacSign.setText(currentZodiacSign);
            setZodiacImage(currentZodiacSign);
            getHoroscope();
        });
        builder.show();
    }

    private void setZodiacImage(String zodiacSign) {
        int resourceId = 0;
        switch (zodiacSign) {
            case "Koç":
                resourceId = R.drawable.zodiac;
                break;
            case "Boğa":
                resourceId = R.drawable.zodiac;
                break;
            case "İkizler":
                resourceId = R.drawable.zodiac;
                break;
            case "Yengeç":
                resourceId = R.drawable.zodiac;
                break;
            case "Aslan":
                resourceId = R.drawable.zodiac;
                break;
            case "Başak":
                resourceId = R.drawable.zodiac;
                break;
            case "Terazi":
                resourceId = R.drawable.zodiac;
                break;
            case "Akrep":
                resourceId = R.drawable.zodiac;
                break;
            case "Yay":
                resourceId = R.drawable.zodiac;
                break;
            case "Oğlak":
                resourceId = R.drawable.zodiac;
                break;
            case "Kova":
                resourceId = R.drawable.zodiac;
                break;
            case "Balık":
                resourceId = R.drawable.zodiac;
                break;
        }

        if (resourceId != 0) {
            ivZodiacSign.setImageResource(resourceId);
        }
    }

    private void getHoroscope() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        tvHoroscope.setVisibility(View.GONE);

        // Get horoscope in background thread
        new Thread(() -> {
            final String horoscope = chatGPTService.getHoroscopeReading(currentZodiacSign, currentPeriod);

            // Update UI on main thread
            runOnUiThread(() -> {
                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                // Show horoscope
                tvHoroscope.setText(horoscope);
                tvHoroscope.setVisibility(View.VISIBLE);
            });
        }).start();
    }
}