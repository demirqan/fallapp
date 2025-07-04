package com.falapp.falciabla;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.falciabla.api.ChatGPTService;
import com.falapp.falciabla.models.User;
import com.falapp.falciabla.utils.DatabaseHelper;
import com.falapp.falciabla.utils.FalLimitManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DreamInterpretationActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private TextToSpeech tts;
    private Button btnSpeak;

    private MaterialToolbar toolbar;
    private EditText etDream;
    private Button btnInterpret;
    private TextView tvInterpretation;
    private ImageView ivDream;
    private View progressBar;
    private BottomNavigationView bottomNavigationView;

    private static final int INTERPRETATION_COST = 10; // Coins required for dream interpretation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dream_interpretation);

        // Initialize database helper and services
        dbHelper = new DatabaseHelper(this);
        chatGPTService = new ChatGPTService();

        // Get current user
        currentUser = dbHelper.getUser();
        if (currentUser == null) {
            // Create new user if not exists
            currentUser = new User("User", 2000);
            dbHelper.addUser(currentUser);
            Toast.makeText(this, "Yeni kullanıcı oluşturuldu!", Toast.LENGTH_SHORT).show();
        }
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
            boolean isPremium = getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("is_premium", false);
            if (!isPremium) {
                Toast.makeText(this, "🔒 Sesli okuma sadece Premium üyeler içindir.", Toast.LENGTH_SHORT).show();
                return;
            }

            String text = tvInterpretation.getText().toString();
            if (!text.isEmpty()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Initialize views


        // Set up toolbar
        setupToolbar();

        // Set up navigation
        setupBottomNavigation();

        // Set up button click listener
        btnInterpret.setOnClickListener(v -> validateAndInterpretDream());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etDream = findViewById(R.id.et_dream);
        btnInterpret = findViewById(R.id.btn_interpret);
        tvInterpretation = findViewById(R.id.tv_interpretation);
        ivDream = findViewById(R.id.iv_dream);
        progressBar = findViewById(R.id.progress_bar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        btnSpeak = findViewById(R.id.btn_speak);
    }

    private void setupToolbar() {
        // Mevcut ActionBar'ı kullan, setSupportActionBar çağırma
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Rüya Yorumla");
        }

        // Toolbar navigation tıklamasını handle et
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupBottomNavigation() {
        // Önce item'i seç, sonra listener'ı setup et
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Ana sayfaya dönmek için mevcut Activity'yi kapat
                finish();
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                startActivity(new Intent(DreamInterpretationActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(DreamInterpretationActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(DreamInterpretationActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });
    }

    private void validateAndInterpretDream() {
        String dreamText = etDream.getText().toString().trim();

        if (dreamText.isEmpty()) {
            etDream.setError("Lütfen rüyanızı anlatın");
            etDream.requestFocus();
            return;
        }

        boolean isPremium = getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean("is_premium", false);

        // 💎 Premium kullanıcılar için günlük 10 fal limiti kontrolü
        if (isPremium) {
            if (!FalLimitManager.canUsePremiumFal(this)) {
                Toast.makeText(this, "Bugün 10 fal yorumu hakkınızı kullandınız. Yarın tekrar deneyin.", Toast.LENGTH_LONG).show();
                return;
            } else {
                FalLimitManager.increasePremiumFalCount(this); // limiti arttır
            }
        }

        // 🪙 Premium olmayan kullanıcılar için coin kontrolü
        if (!isPremium && currentUser.getCoins() < INTERPRETATION_COST) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        // ✅ Coin sadece premium olmayanlardan düşülür
        if (!isPremium) {
            currentUser.removeCoins(INTERPRETATION_COST);
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnInterpret.setEnabled(false);
        tvInterpretation.setVisibility(View.GONE);

        // Get dream interpretation
        new Thread(() -> {
            final String interpretation = chatGPTService.getDreamInterpretation(dreamText);

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnInterpret.setEnabled(true);

                tvInterpretation.setText(interpretation);
                tvInterpretation.setVisibility(View.VISIBLE);
                btnSpeak.setVisibility(View.VISIBLE);


            });
        }).start();
    }
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}