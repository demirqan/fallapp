package com.falapp.fortunetelle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.falapp.fortunetelle.adapters.TarotCardsPagerAdapter;
import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.TarotCard;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TarotDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private MaterialToolbar toolbar;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView tvInterpretation;

    private static final int TAROT_COST = 10; // Coins required for tarot reading

    private List<TarotCard> selectedCards = new ArrayList<>();
    private String[] cardNames = {
            "Büyücü", "Yüksek Rahibe", "İmparatoriçe", "İmparator", "Hierophant", "Aşıklar",
            "Savaş Arabası", "Güç", "Ermiş", "Kader Çarkı", "Adalet", "Asılmış Adam",
            "Ölüm", "Denge", "Şeytan", "Yıkılan Kule", "Yıldız", "Ay",
            "Güneş", "Yargı", "Dünya", "Soytarı"
    };

    private String[] cardMeanings = {
            "Yaratıcılık ve güç", "Sezgi ve bilgelik", "Bereket ve şefkat", "Otorite ve istikrar",
            "Gelenek ve manevi bilgi", "Aşk ve uyum", "İrade ve başarı", "Cesaret ve tutku",
            "İç huzur ve rehberlik", "Kader ve döngüler", "Adalet ve denge", "Fedakarlık ve bekleyiş",
            "Dönüşüm ve yeni başlangıçlar", "Sabır ve denge", "Bağımlılık ve yanılsama", "Ani değişim ve yıkım",
            "Umut ve ilham", "Bilinçaltı ve sezgiler", "Başarı ve mutluluk", "Yenilenme ve uyanış",
            "Tamamlanma ve uyum", "Yeni başlangıçlar ve spontanlık"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarot_detail);

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

        // Check if user has enough coins
        if (currentUser.getCoins() < TAROT_COST) {
            android.widget.Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.",
                    android.widget.Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            finish();
            return;
        }

        // Deduct coins from user
        currentUser.removeCoins(TAROT_COST);
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Select random cards
        selectRandomCards(3); // Choose 3 cards for this reading

        // Set up viewpager with selected cards
        setupViewPager();

        // Get tarot reading
        getTarotReading();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        tvInterpretation = findViewById(R.id.tv_interpretation);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tarot Falı");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void selectRandomCards(int count) {
        Random random = new Random();
        List<Integer> selectedIndices = new ArrayList<>();

        while (selectedCards.size() < count) {
            int index = random.nextInt(cardNames.length);

            // Avoid duplicates
            if (!selectedIndices.contains(index)) {
                selectedIndices.add(index);

                // Get card image resource
                int cardResourceId = getResources().getIdentifier(
                        "tarot_" + cardNames[index].toLowerCase().replace(" ", "_"),
                        "drawable", getPackageName());

                // If not found, use a default image
                if (cardResourceId == 0) {
                    cardResourceId = R.drawable.tarot_default;
                }

                // Create card with random orientation (upright or reversed)
                boolean isReversed = random.nextBoolean();
                TarotCard card = new TarotCard(
                        cardNames[index],
                        cardMeanings[index],
                        cardResourceId,
                        isReversed
                );

                selectedCards.add(card);
            }
        }
    }

    private void setupViewPager() {
        // Set adapter
        TarotCardsPagerAdapter adapter = new TarotCardsPagerAdapter(this, selectedCards);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Tab titles can be set here if needed
            tab.setText("Kart " + (position + 1));
        }).attach();
    }

    private void getTarotReading() {
        // Show progress
        tvInterpretation.setText("Tarot yorumu hazırlanıyor...");

        // Get card names for API request
        String[] cardNamesForRequest = new String[selectedCards.size()];
        for (int i = 0; i < selectedCards.size(); i++) {
            TarotCard card = selectedCards.get(i);
            cardNamesForRequest[i] = card.getName() + (card.isReversed() ? " (Ters)" : "");
        }

        // Get interpretation in background thread
        new Thread(() -> {
            final String interpretation = chatGPTService.getTarotReading(cardNamesForRequest);

            // Update UI on main thread
            runOnUiThread(() -> {
                tvInterpretation.setText(interpretation);
            });
        }).start();
    }
}