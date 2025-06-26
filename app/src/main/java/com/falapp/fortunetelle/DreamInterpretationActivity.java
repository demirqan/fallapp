package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DreamInterpretationActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

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

        // Initialize views
        initViews();

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
        // Get dream text
        String dreamText = etDream.getText().toString().trim();

        // Validate dream text
        if (dreamText.isEmpty()) {
            etDream.setError("Lütfen rüyanızı anlatın");
            etDream.requestFocus();
            return;
        }

        // Check if user has enough coins
        if (currentUser.getCoins() < INTERPRETATION_COST) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        // Deduct coins from user
        currentUser.removeCoins(INTERPRETATION_COST);
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        btnInterpret.setEnabled(false);
        tvInterpretation.setVisibility(View.GONE);

        // Get dream interpretation in background thread
        new Thread(() -> {
            final String interpretation = chatGPTService.getDreamInterpretation(dreamText);

            // Update UI on main thread
            runOnUiThread(() -> {
                // Hide progress bar
                progressBar.setVisibility(View.GONE);
                btnInterpret.setEnabled(true);

                // Show interpretation
                tvInterpretation.setText(interpretation);
                tvInterpretation.setVisibility(View.VISIBLE);
            });
        }).start();
    }
}