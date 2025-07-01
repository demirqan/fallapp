package com.falapp.falciabla;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TarotActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private Random random = new Random();
    private ChatGPTService chatGPTService;

    private MaterialToolbar toolbar;
    private ImageView[] tarotCards;
    private static final int TAROT_COST = 10; // Her yorum başına 5 coin
    private List<String> selectedCardNames = new ArrayList<>(); // 🔥 bunu ekle
    private TextView tvInterpretation;
    private Button btnReset;
    private View progressBar;
;

    private static final int CARD_COUNT = 6;


    private String[] cardNames = {

            "gunes", "yargi_kilici", "aptallik", "yikilan_kule", "kral", "asilmis_adam",
            "bas_rahip", "yildiz","heybetli_kale", "kirmizi_sarap", "dogan_gunes", "tanri_eli"
    };

    private List<Integer> selectedCardIndices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarot);

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

        // Set up tarot cards
        setupTarotCards();

        // Set up navigation
        setupBottomNavigation();

        // Set up reset button
        btnReset.setOnClickListener(v -> resetCards());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvInterpretation = findViewById(R.id.tv_interpretation);
        btnReset = findViewById(R.id.btn_reset);
        progressBar = findViewById(R.id.progress_bar);


        // Initialize tarot card ImageViews
        tarotCards = new ImageView[CARD_COUNT];
        for (int i = 0; i < CARD_COUNT; i++) {
            int resId = getResources().getIdentifier("iv_tarot_card_" + (i + 1), "id", getPackageName());
            tarotCards[i] = findViewById(resId);
        }
    }

    private void setupToolbar() {
       setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tarot Falı");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTarotCards() {
        // Set all cards to back side initially
        for (int i = 0; i < CARD_COUNT; i++) {
            tarotCards[i].setImageResource(R.drawable.cardback);
            final int cardIndex = i;

            tarotCards[i].setOnClickListener(v -> onCardClicked(cardIndex));
        }
    }

    private void setupBottomNavigation() {
        /*
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(TarotActivity.this, MainActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                startActivity(new Intent(TarotActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(TarotActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(TarotActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });

        // Set the home item as checked
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        */
    }



    private void onCardClicked(int cardIndex) {
        if (selectedCardIndices.contains(cardIndex)) {
            return;
        }

        boolean isPremium = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("is_premium", false);

        if (selectedCardIndices.isEmpty()) {
            if (!isPremium) {
                if (currentUser.getCoins() < TAROT_COST) {
                    Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, CoinPurchaseActivity.class));
                    return;
                }

                currentUser.removeCoins(TAROT_COST);
                dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
            }
        }

        if (selectedCardIndices.size() >= 6) {
            Toast.makeText(this, "En fazla 6 kart seçebilirsiniz.", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedCardIndices.add(cardIndex);

        int randomCardIndex = random.nextInt(cardNames.length);
        String cardName = cardNames[randomCardIndex];

        // Logla
        Log.d("TarotActivity", "Seçilen kart: " + cardName);

        tarotCards[cardIndex].setTag(cardName);

        int cardResourceId = getResources().getIdentifier(
                "tarot_" + cardName.toLowerCase().replace(" ", "_"), "drawable", getPackageName());
        if (cardResourceId != 0) {
            tarotCards[cardIndex].setImageResource(cardResourceId);
        } else {
            tarotCards[cardIndex].setImageResource(R.drawable.tarot_default);
        }

        // Burada sınıf seviyesindeki listeyi kullan
        selectedCardNames.add(cardName);

        getInterpretation();
    }


    private void getInterpretation() {
        if (selectedCardNames.isEmpty()) return;

        progressBar.setVisibility(View.VISIBLE);
        tvInterpretation.setVisibility(View.GONE);

        String[] selectedCards = selectedCardNames.toArray(new String[0]);

        new Thread(() -> {
            final String interpretation = chatGPTService.getTarotReading(selectedCards);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvInterpretation.setText(interpretation);
                tvInterpretation.setVisibility(View.VISIBLE);
               // btnReset.setVisibility(View.VISIBLE);
            });
        }).start();
    }


    private void resetCards() {
        selectedCardIndices.clear();
        selectedCardNames.clear();

        for (int i = 0; i < CARD_COUNT; i++) {
            tarotCards[i].setImageResource(R.drawable.cardback);
            tarotCards[i].setTag(null);
        }

        tvInterpretation.setText("");
        tvInterpretation.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
    }
}