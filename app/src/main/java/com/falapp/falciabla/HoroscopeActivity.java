package com.falapp.falciabla;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.falapp.falciabla.api.ChatGPTService;
import com.falapp.falciabla.models.User;
import com.falapp.falciabla.utils.DatabaseHelper;
import com.falapp.falciabla.utils.FalLimitManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

public class HoroscopeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ImageView ivZodiacSign;
    private TextView tvZodiacSign;
    private TextView tvHoroscope;
    private Button btnContinueReading;
    private CardView cardZodiacSign;
    private View progressBar;
    private BottomNavigationView bottomNavigationView;

    private String[] zodiacSigns = {"Koç", "Boğa", "İkizler", "Yengeç", "Aslan", "Başak",
            "Terazi", "Akrep", "Yay", "Oğlak", "Kova", "Balık"};
    private String[] periods = {"Günlük", "Haftalık", "Aylık"};

    private String currentZodiacSign = null; //"Koç"; // Default sign
    private String currentPeriod = "Günlük"; // Default period
    private static final int HOROSCOPE_COST = 10; // Coins required for horoscope reading

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horoscope);

        // Initialize database helper and services
        dbHelper = new DatabaseHelper(this);
        chatGPTService = new ChatGPTService();

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

        // Set up tab layout for periods
        setupTabLayout();

        // Set up zodiac sign selection
        setupZodiacSignSelection();

        // Set up navigation
        setupBottomNavigation();

        // Set up continue reading button
        btnContinueReading.setOnClickListener(v -> openDetailedHoroscope());

        // Get initial horoscope
       // getHoroscope();
        tvHoroscope.setText("Burç yorumunu görmek için burcunuzu seçin.");
        tvHoroscope.setVisibility(View.VISIBLE);
        btnContinueReading.setVisibility(View.GONE);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        ivZodiacSign = findViewById(R.id.iv_zodiac_sign);
        tvZodiacSign = findViewById(R.id.tv_zodiac_sign);
        tvHoroscope = findViewById(R.id.tv_horoscope);
        btnContinueReading = findViewById(R.id.btn_continue_reading);
        cardZodiacSign = findViewById(R.id.card_zodiac_sign);
        progressBar = findViewById(R.id.progress_bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Burçlar ve Astroloji");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTabLayout() {
        // Add tabs for daily, weekly, monthly
        for (String period : periods) {
            tabLayout.addTab(tabLayout.newTab().setText(period));
        }

        // Set tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentPeriod = periods[tab.getPosition()];
                if (currentZodiacSign != null) {
                    getHoroscope(); // ✅ Sadece burç seçilmişse yorum al
                }
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

        cardZodiacSign.setOnClickListener(v -> showZodiacSignSelectionDialog());
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(HoroscopeActivity.this, MainActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                startActivity(new Intent(HoroscopeActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                // Already on horoscope, do nothing
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(HoroscopeActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });

        // Set the horoscope item as checked
        bottomNavigationView.setSelectedItemId(R.id.nav_horoscope);
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
        if (zodiacSign == null) {
            // Null durumunda varsayılan görseli yükle
            ivZodiacSign.setImageResource(R.drawable.zodiac);
            return;
        }

        int resourceId = 0;
        switch (zodiacSign) {
            case "Koç":
            case "Boğa":
            case "İkizler":
            case "Yengeç":
            case "Aslan":
            case "Başak":
            case "Terazi":
            case "Akrep":
            case "Yay":
            case "Oğlak":
            case "Kova":
            case "Balık":
                resourceId = R.drawable.zodiac;
                break;
        }

        if (resourceId != 0) {
            ivZodiacSign.setImageResource(resourceId);
        } else {
            // Güvenlik amacıyla yine varsayılanı yükle
            ivZodiacSign.setImageResource(R.drawable.zodiac);
        }
    }

    private void getHoroscope() {
        boolean isPremium = getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("is_premium", false);

        // 🔒 Premium ise fal hakkı kontrolü
        if (isPremium) {
            if (!FalLimitManager.canUsePremiumFal(this)) {
                Toast.makeText(this, "Bugünlük tüm fal hakkınızı kullandınız. Yarın tekrar deneyin.", Toast.LENGTH_LONG).show();
                return;
            } else {
                FalLimitManager.increasePremiumFalCount(this);
            }
        }

        // 🔒 Yeterli coin kontrolü sadece premium olmayanlar için
        if (!isPremium && currentUser.getCoins() < HOROSCOPE_COST) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        // ✅ Coin sadece premium olmayanlardan düşülür
        if (!isPremium) {
            currentUser.removeCoins(HOROSCOPE_COST);
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        tvHoroscope.setVisibility(View.GONE);
        btnContinueReading.setVisibility(View.GONE);

        // Get horoscope in background thread
        new Thread(() -> {
            final String horoscope = chatGPTService.getHoroscopeReading(currentZodiacSign, currentPeriod);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                String shortHoroscope = getShortHoroscope(horoscope);
                tvHoroscope.setText(shortHoroscope);
                tvHoroscope.setVisibility(View.VISIBLE);
                btnContinueReading.setVisibility(View.VISIBLE);
            });
        }).start();
    }

    private String getShortHoroscope(String fullHoroscope) {
        // Get first paragraph or first 200 characters
        int endIndex = fullHoroscope.indexOf("\n\n");
        if (endIndex != -1) {
            return fullHoroscope.substring(0, endIndex) + "...";
        } else if (fullHoroscope.length() > 200) {
            return fullHoroscope.substring(0, 200) + "...";
        } else {
            return fullHoroscope;
        }
    }

    private void openDetailedHoroscope() {
        Intent intent = new Intent(this, HoroscopeDetailActivity.class);
        intent.putExtra("zodiac_sign", currentZodiacSign);
        intent.putExtra("period", currentPeriod);
        startActivity(intent);
    }
}