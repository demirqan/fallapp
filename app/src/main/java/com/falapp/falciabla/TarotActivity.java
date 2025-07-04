package com.falapp.falciabla;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.falapp.falciabla.utils.FalLimitManager;
import com.google.android.material.appbar.MaterialToolbar;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TarotActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private Random random = new Random();
    private ChatGPTService chatGPTService;

    private MaterialToolbar toolbar;
    private TextToSpeech tts;
    private Button btnSpeak;
    private ImageView[] tarotCards;
    private static final int TAROT_COST = 10; // Her yorum başına 5 coin
    private List<String> selectedCardNames = new ArrayList<>(); // 🔥 bunu ekle
    private TextView tvInterpretation;
    private Button btnReset;
    private View progressBar;
    private List<String> availableCardNames = new ArrayList<>();
;

    private static final int CARD_COUNT = 6;


    private String[] cardNames = {

            "gunes", "yargi_kilici", "aptallik", "yikilan_kule", "kral", "asilmis_adam",
            "bas_rahip", "yildiz","heybetli_kale", "adalet",
            "kader_carki", "imparator", "deli", "aziz", "olum", "golgelerin_fisiltisi", "denge",
             "savas_arabasi", "buyucu", "imparatorice", "guc", "ermis", "ay", "dunya", "azize",
            "mahkeme"

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

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("tr", "TR"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Türkçe dili desteklenmiyor", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnSpeak.setOnClickListener(v -> {
            boolean isPremium = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .getBoolean("is_premium", false);

            if (!isPremium) {
                Toast.makeText(this, "Sesli okuma sadece Premium üyeler içindir.", Toast.LENGTH_SHORT).show();
                return;
            }

            String text = tvInterpretation.getText().toString();
            if (!text.isEmpty()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Set up toolbar
        setupToolbar();

        // Set up tarot cards
        setupTarotCards();

        // Set up navigation
        setupBottomNavigation();

        // Set up reset button
        btnReset.setOnClickListener(v -> resetCards());
        availableCardNames = new ArrayList<>();
        for (String card : cardNames) {
            availableCardNames.add(card);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvInterpretation = findViewById(R.id.tv_interpretation);
        btnReset = findViewById(R.id.btn_reset);
        progressBar = findViewById(R.id.progress_bar);
        btnSpeak = findViewById(R.id.btn_speak);


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

        // 🔴 İlk kart seçiliyorsa ve premium kullanıcıysa günlük 10 fal hakkı kontrolü yap
        if (selectedCardIndices.isEmpty()) {
            if (isPremium) {
                if (!FalLimitManager.canUsePremiumFal(this)) {
                    Toast.makeText(this, "Bugün 10  fal hakkınızı kullandınız. Yarın tekrar deneyin.", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    FalLimitManager.increasePremiumFalCount(this); // limiti arttır
                }
            } else {
                // 🔸 Premium değilse altın kontrolü yap
                if (currentUser.getCoins() < TAROT_COST) {
                    Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, CoinPurchaseActivity.class));
                    return;
                }

                currentUser.removeCoins(TAROT_COST);
                dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
            }
        }

        // Maksimum 6 kart seçilmesine izin ver
        if (selectedCardIndices.size() >= 6) {
            Toast.makeText(this, "En fazla 6 kart seçebilirsiniz.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (availableCardNames.isEmpty()) {
            Toast.makeText(this, "Tüm kartlar tükendi.", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedCardIndices.add(cardIndex);

        // Rastgele bir kart seç ve liste dışına çıkar
        int randomIndex = random.nextInt(availableCardNames.size());
        String cardName = availableCardNames.remove(randomIndex);

        Log.d("TarotActivity", "Seçilen kart: " + cardName);

        tarotCards[cardIndex].setTag(cardName);

        int cardResourceId = getResources().getIdentifier(
                "tarot_" + cardName.toLowerCase().replace(" ", "_"), "drawable", getPackageName());
        if (cardResourceId != 0) {
            tarotCards[cardIndex].setImageResource(cardResourceId);
        } else {
            tarotCards[cardIndex].setImageResource(R.drawable.tarot_default);
        }

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
                btnSpeak.setVisibility(View.VISIBLE); // herkes görsün
            });
        }).start();
    }


    private void resetCards() {
        availableCardNames.clear();
        availableCardNames.addAll(Arrays.asList(cardNames));
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