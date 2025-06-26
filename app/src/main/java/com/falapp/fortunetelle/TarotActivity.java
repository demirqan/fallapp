package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;


import java.util.ArrayList;
import java.util.List;

public class TarotActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private MaterialToolbar toolbar;
    private ImageView[] tarotCards;
    private TextView tvInterpretation;
    private Button btnReset;
    private View progressBar;
;

    private static final int CARD_COUNT = 6;
    private static final int TAROT_COST = 10; // Coins required for tarot reading

    private String[] cardNames = {
            "Büyücü", "Yüksek Rahibe", "İmparatoriçe", "İmparator", "Hierophant", "Aşıklar",
            "Savaş Arabası", "Güç", "Ermiş", "Kader Çarkı", "Adalet", "Asılmış Adam",
            "Ölüm", "Denge", "Şeytan", "Yıkılan Kule", "Yıldız", "Ay",
            "Güneş", "Yargı", "Dünya", "Soytarı"
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
        // If card is already selected, do nothing
        if (selectedCardIndices.contains(cardIndex)) {
            return;
        }

        // Check if user has enough coins for the first card
        if (selectedCardIndices.isEmpty() && currentUser.getCoins() < TAROT_COST) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        // If this is the first card, deduct coins
        if (selectedCardIndices.isEmpty()) {
            currentUser.removeCoins(TAROT_COST);
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
        }

        // Add card to selected cards
        selectedCardIndices.add(cardIndex);

        // Get random card from tarot deck
        int randomCardIndex = (int) (Math.random() * cardNames.length);
        String cardName = cardNames[randomCardIndex];

        // Set card image
        int cardResourceId = getResources().getIdentifier(
                "tarot_" + cardName.toLowerCase().replace(" ", "_"), "drawable", getPackageName());
        if (cardResourceId != 0) {
            tarotCards[cardIndex].setImageResource(cardResourceId);
        } else {
            // If image not found, use a default card
            tarotCards[cardIndex].setImageResource(R.drawable.tarot_default);
        }

        // If at least one card is selected, get interpretation
        if (!selectedCardIndices.isEmpty()) {
            getInterpretation();
        }
    }

    private void getInterpretation() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        tvInterpretation.setVisibility(View.GONE);

        // Get selected card names
        String[] selectedCards = new String[selectedCardIndices.size()];
        for (int i = 0; i < selectedCardIndices.size(); i++) {
            int randomCardIndex = (int) (Math.random() * cardNames.length);
            selectedCards[i] = cardNames[randomCardIndex];
        }

        // Get interpretation in background thread
        new Thread(() -> {
            final String interpretation = chatGPTService.getTarotReading(selectedCards);

            // Update UI on main thread
            runOnUiThread(() -> {
                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                // Show interpretation
                tvInterpretation.setText(interpretation);
                tvInterpretation.setVisibility(View.VISIBLE);

                // Show reset button
                btnReset.setVisibility(View.VISIBLE);
            });
        }).start();
    }

    private void resetCards() {
        // Clear selected cards
        selectedCardIndices.clear();

        // Reset all cards to back side
        for (int i = 0; i < CARD_COUNT; i++) {
            tarotCards[i].setImageResource(R.drawable.cardback);
        }

        // Clear interpretation
        tvInterpretation.setText("");
        tvInterpretation.setVisibility(View.GONE);

        // Hide reset button
        btnReset.setVisibility(View.GONE);
    }
}