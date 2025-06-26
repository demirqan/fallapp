package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

public class FreeCoinActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private MaterialToolbar toolbar;
    private TextView tvCoins;
    private CardView cardWatchAd;
    private CardView cardShareApp;
    private CardView cardDailyBonus;
    private CardView cardCompleteTask;
    private Button btnWatchAd;
    private Button btnShareApp;
    private Button btnDailyBonus;
    private Button btnCompleteTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_coin);

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
        initViews();

        // Set up toolbar
        setupToolbar();

        // Update coin display
        updateCoinsDisplay();

        // Set up free coin options
        setupFreeCoins();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data
        currentUser = dbHelper.getUser();
        updateCoinsDisplay();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCoins = findViewById(R.id.tv_coins);
        cardWatchAd = findViewById(R.id.card_watch_ad);
        cardShareApp = findViewById(R.id.card_share_app);
        cardDailyBonus = findViewById(R.id.card_daily_bonus);
        cardCompleteTask = findViewById(R.id.card_complete_task);
        btnWatchAd = findViewById(R.id.btn_watch_ad);
        btnShareApp = findViewById(R.id.btn_share_app);
        btnDailyBonus = findViewById(R.id.btn_daily_bonus);
        btnCompleteTask = findViewById(R.id.btn_complete_task);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ücretsiz Altın Kazan");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void updateCoinsDisplay() {
        tvCoins.setText(String.valueOf(currentUser.getCoins()));
    }

    private void setupFreeCoins() {
        // Watch Ad option
        btnWatchAd.setOnClickListener(v -> watchAdForCoins());

        // Share App option
        btnShareApp.setOnClickListener(v -> shareApp());

        // Daily Bonus option
        btnDailyBonus.setOnClickListener(v -> claimDailyBonus());

        // Complete Task option
        btnCompleteTask.setOnClickListener(v -> completeTaskForCoins());
    }

    private void watchAdForCoins() {
        // In a real app, this would show a rewarded ad
        // For now, we'll just simulate watching an ad

        // Show ad loading dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Reklam Yükleniyor")
                .setMessage("Lütfen bekleyin...")
                .setCancelable(false)
                .show();

        // Simulate ad loading delay
        new android.os.Handler().postDelayed(() -> {
            // Dismiss dialog and add coins
            if (isFinishing()) return;

            // Add coins
            addCoinsToUser(5);

            // Show success message
            Toast.makeText(FreeCoinActivity.this,
                    "Reklam izlediğiniz için 5 altın kazandınız!", Toast.LENGTH_SHORT).show();

            // Disable button temporarily
            btnWatchAd.setEnabled(false);
            btnWatchAd.setText("Bekleyin (1 saat)");
            new android.os.Handler().postDelayed(() -> {
                if (!isFinishing()) {
                    btnWatchAd.setEnabled(true);
                    btnWatchAd.setText("Reklam İzle");
                }
            }, 10000); // In real app, this would be longer (e.g., 1 hour)

        }, 3000);
    }

    private void shareApp() {
        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Fal Uygulaması");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Fal Uygulaması ile geleceğinizi keşfedin! Hemen indirin: https://play.google.com/store/apps/falapp");

        // Start share activity
        startActivity(Intent.createChooser(shareIntent, "Uygulamamızı Paylaşın"));

        // Add coins after sharing
        new android.os.Handler().postDelayed(() -> {
            if (!isFinishing()) {
                addCoinsToUser(10);
                Toast.makeText(FreeCoinActivity.this,
                        "Paylaşım yaptığınız için 10 altın kazandınız!", Toast.LENGTH_SHORT).show();

                // Disable button temporarily
                btnShareApp.setEnabled(false);
                btnShareApp.setText("Bekleyin (24 saat)");
                new android.os.Handler().postDelayed(() -> {
                    if (!isFinishing()) {
                        btnShareApp.setEnabled(true);
                        btnShareApp.setText("Uygulamayı Paylaş");
                    }
                }, 20000); // In real app, this would be longer (e.g., 24 hours)
            }
        }, 2000);
    }

    private void claimDailyBonus() {
        // Check if daily bonus is available
        // In a real app, this would check the last claim time
        android.content.SharedPreferences prefs = getSharedPreferences("FalAppPrefs", MODE_PRIVATE);
        long lastClaimTime = prefs.getLong("last_daily_bonus_time", 0);
        long currentTime = System.currentTimeMillis();

        // Check if 24 hours have passed since last claim
        if (currentTime - lastClaimTime < 24 * 60 * 60 * 1000) { // 24 hours in milliseconds
            // Not enough time has passed
            long remainingTime = 24 * 60 * 60 * 1000 - (currentTime - lastClaimTime);
            long remainingSeconds = remainingTime / 1000;
            long hours = remainingSeconds / 3600;
            long minutes = (remainingSeconds % 3600) / 60;
            long seconds = remainingSeconds % 60;

            String timeMessage = String.format("Lütfen bekleyin: %02d:%02d:%02d", hours, minutes, seconds);
            Toast.makeText(this, timeMessage, Toast.LENGTH_SHORT).show();
            return;
        }

        // Claim bonus
        int bonus = 15; // Fixed bonus amount
        addCoinsToUser(bonus);

        // Save claim time
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_daily_bonus_time", currentTime);
        editor.apply();

        // Show success message
        Toast.makeText(this, "Günlük bonusunuz: " + bonus + " altın!", Toast.LENGTH_SHORT).show();

        // Update button text
        btnDailyBonus.setText("Alındı (24 saat bekleyin)");
        btnDailyBonus.setEnabled(false);

        // Reset button after delay
        new android.os.Handler().postDelayed(() -> {
            if (!isFinishing()) {
                btnDailyBonus.setEnabled(true);
                btnDailyBonus.setText("Günlük Bonus Al");
            }
        }, 24 * 60 * 60 * 1000); // 24 hours
    }

    private void completeTaskForCoins() {
        // In a real app, this would open an offerwall or similar task system
        // For now, we'll just simulate completing a task

        // Show task options dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Görev Seç")
                .setItems(new String[]{
                        "Uygulamayı Değerlendir (10 Altın)",
                        "Uygulamayı Paylaş (15 Altın)",
                        "Ücretsiz Deneme Başlat (20 Altın)"
                }, (dialog, which) -> {
                    int coinsToAdd;
                    String task;

                    switch (which) {
                        case 0:
                            coinsToAdd = 10;
                            task = "Uygulamayı Değerlendirme";
                            // In a real app, this would open the Play Store rating page
                            break;
                        case 1:
                            coinsToAdd = 15;
                            task = "Uygulamayı Paylaşma";
                            shareApp();
                            return;
                        case 2:
                            coinsToAdd = 20;
                            task = "Ücretsiz Deneme Başlatma";
                            // In a real app, this would open a related app or offer
                            break;
                        default:
                            return;
                    }

                    // Simulate task completion
                    new android.os.Handler().postDelayed(() -> {
                        if (!isFinishing()) {
                            addCoinsToUser(coinsToAdd);

                            // Show success message
                            Toast.makeText(FreeCoinActivity.this,
                                    task + " görevini tamamladığınız için " + coinsToAdd +
                                            " altın kazandınız!", Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                })
                .show();
    }

    private void addCoinsToUser(int amount) {
        // Add coins to user
        currentUser.addCoins(amount);

        // Update user in database
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());

        // Update display
        updateCoinsDisplay();
    }
}