package com.falapp.fortunetelle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView tvCoins;
    private DatabaseHelper dbHelper;
    private User currentUser;
    private BottomNavigationView bottomNavigationView;

    private static final String PREFS_NAME = "FalAppPrefs";
    private static final String FIRST_RUN = "firstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Check if it's the first run and initialize user
        checkFirstRun();

        // Find views
        tvCoins = findViewById(R.id.tv_coins);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up navigation
        setupBottomNavigation();

        // Set up fortune type selection
        setupFortuneTypeSelection();

        // Set up other main menu items
        setupMainMenuItems();

        // Update UI with user data
        updateUI();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        User user = dbHelper.getUser();


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data and UI when returning to this activity
        refreshUserData();
        updateUI();
    }

    private void checkFirstRun() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean(FIRST_RUN, true);

        if (firstRun) {
            // First time running the app
            // Create new user with default 20 coins
            currentUser = new User("User", 2000);
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
                currentUser = new User("User", 2000);
                dbHelper.addUser(currentUser);
            }
        }
    }

    private void refreshUserData() {
        currentUser = dbHelper.getUser();
    }

    private void updateUI() {
        // Update coin display
        if (currentUser != null) {
            tvCoins.setText(String.valueOf(currentUser.getCoins()));
        }
        
        // TEST: Uzun basınca altın ekleme
        tvCoins.setOnLongClickListener(v -> {
            addCoinsToUser(1000);
            Toast.makeText(this, "+1000 Altın Eklendi! Toplam: " + currentUser.getCoins(), Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home, do nothing
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                startActivity(new Intent(MainActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(MainActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(MainActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });

        // Set the home item as checked
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void setupFortuneTypeSelection() {
        CardView cardCoffee = findViewById(R.id.card_coffee);
        CardView cardTarot = findViewById(R.id.card_tarot);
        CardView cardPalm = findViewById(R.id.card_palm);
        CardView cardFace = findViewById(R.id.card_face);

        cardCoffee.setOnClickListener(v -> openFortuneReading("coffee"));
        cardTarot.setOnClickListener(v ->   startActivity(new Intent(MainActivity.this, TarotActivity.class)));


        cardPalm.setOnClickListener(v -> openFortuneReading("palm"));
        cardFace.setOnClickListener(v -> openFortuneReading("face"));
    }

    private void setupMainMenuItems() {
        CardView cardMyReadings = findViewById(R.id.card_my_readings);
        CardView cardBuyCoins = findViewById(R.id.card_buy_coins);
        CardView cardFreeCoins = findViewById(R.id.card_free_coins);
        CardView cardPremium = findViewById(R.id.card_premium);
        CardView cardLoveCompatibility = findViewById(R.id.card_love_compatibility);
        CardView cardDreamInterpreter = findViewById(R.id.card_dream_interpreter);

        // Null kontrolü ekle
        if (cardLoveCompatibility == null) {
            Toast.makeText(this, "cardLoveCompatibility bulunamadı!", Toast.LENGTH_SHORT).show();
        }
        if (cardDreamInterpreter == null) {
            Toast.makeText(this, "cardDreamInterpreter bulunamadı!", Toast.LENGTH_SHORT).show();
        }
        CardView cardHoroscope = findViewById(R.id.card_horoscope);
        CardView cardAffirmations = findViewById(R.id.card_affirmations);
        CardView cardJournal = findViewById(R.id.card_journal);
        Button btnContinueReading = findViewById(R.id.btn_continue_reading);
        //   CardView cardRelaxation = findViewById(R.id.card_relaxation);

        cardMyReadings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyReadingsActivity.class)));

        cardBuyCoins.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CoinPurchaseActivity.class)));

        cardFreeCoins.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FreeCoinActivity.class)));

        cardPremium.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PremiumActivity.class)));

        if (cardLoveCompatibility != null) {
            cardLoveCompatibility.setOnClickListener(v -> {
                    Toast.makeText(this, "Aşk Uyumu açılıyor...", Toast.LENGTH_SHORT).show();
                    try {
                        startActivity(new Intent(MainActivity.this, LoveCompatibilityActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
            });
        }

        if (cardDreamInterpreter != null) {
            cardDreamInterpreter.setOnClickListener(v -> {
                    Toast.makeText(this, "Rüya Yorumu açılıyor...", Toast.LENGTH_SHORT).show();
                    try {
                        startActivity(new Intent(MainActivity.this, DreamInterpretationActivity.class));
                    } catch (Exception e) {
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
            });
        }

        cardHoroscope.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HoroscopeActivity.class)));

        cardAffirmations.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AffirmationsActivity.class)));

        cardJournal.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, JournalActivity.class)));

        btnContinueReading.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HoroscopeActivity.class)));

        //    cardRelaxation.setOnClickListener(v ->
        //    startActivity(new Intent(MainActivity.this, RelaxationActivity.class)));
    }

    private void openFortuneReading(String readingType) {
        // Check if user has enough coins (10 coins per reading)
        if (currentUser.getCoins() < 10) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, CoinPurchaseActivity.class));
            return;
        }

        // Open fortune teller selection with the selected reading type
        Intent intent = new Intent(MainActivity.this, FortuneTellerListActivity.class);
        intent.putExtra("reading_type", readingType);
        startActivity(intent);
    }
    
    // Method to add coins to user (for testing)
    private void addCoinsToUser(int amount) {
        if (currentUser != null) {
            // Add coins to user object
            currentUser.addCoins(amount);
            
            // Update database
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
            
            // Update UI
            updateUI();
        }
    }
}