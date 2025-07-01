package com.falapp.falciabla;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.falapp.falciabla.models.User;
import com.falapp.falciabla.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class FreeCoinActivity extends AppCompatActivity {

    private RewardedAd rewardedAd;
    private boolean isAdLoading = false;
    private AlertDialog loadingDialog; // reklam bekleme ekranı
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

        MobileAds.initialize(this, initializationStatus -> {});
        loadRewardedAd();  // reklamı başta yükle

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
        loadRewardedAd();  // Reklamı her dönüşte tekrar dene
        SharedPreferences prefs = getSharedPreferences("FalAppPrefs", MODE_PRIVATE);
        long lastShareTime = prefs.getLong("last_share_time", 0);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastShareTime < 7 * 24 * 60 * 60 * 1000) {
            btnShareApp.setEnabled(false);
            btnShareApp.setText("Paylaşıldı (7 gün sonra)");
        } else {
            btnShareApp.setEnabled(true);
            btnShareApp.setText("Uygulamayı Paylaş");
        }

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
        // Butonlara tıklama dinleyicileri (görünmese bile ekli kalsın)
        btnWatchAd.setOnClickListener(v -> watchAdForCoins());
        btnShareApp.setOnClickListener(v -> shareApp());
        btnDailyBonus.setOnClickListener(v -> claimDailyBonus());
        btnCompleteTask.setOnClickListener(v -> completeTaskForCoins());

        // Kartlara tıklama dinleyicileri (görünen asıl öğeler)
        cardWatchAd.setOnClickListener(v -> watchAdForCoins());
        cardShareApp.setOnClickListener(v -> shareApp());
        cardDailyBonus.setOnClickListener(v -> claimDailyBonus());
        cardCompleteTask.setOnClickListener(v -> completeTaskForCoins());
    }

    private void watchAdForCoins() {
        if (rewardedAd != null) {
            rewardedAd.show(this, rewardItem -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                addCoinsToUser(5);
                Toast.makeText(this, "Reklam izlediğiniz için 5 altın kazandınız!", Toast.LENGTH_SHORT).show();
                loadRewardedAd();  // tekrar reklam yükle
            });
        } else {
            // Reklam henüz hazır değilse kullanıcıya göster
            loadingDialog = new AlertDialog.Builder(this)
                    .setTitle("Reklam Yükleniyor")
                    .setMessage("Lütfen bekleyin...")
                    .setCancelable(false)
                    .show();

            loadRewardedAd();

            // 3 saniye sonra dialog'u otomatik kapat ve bilgi ver
            new android.os.Handler().postDelayed(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                Toast.makeText(this, "Reklam şu an hazır değil. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
            }, 3000);
        }
    }

    private void shareApp() {
        SharedPreferences prefs = getSharedPreferences("FalAppPrefs", MODE_PRIVATE);
        long lastShareTime = prefs.getLong("last_share_time", 0);
        long currentTime = System.currentTimeMillis();

        // 7 gün = 7 * 24 * 60 * 60 * 1000 ms
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L;

        if (currentTime - lastShareTime < sevenDaysInMillis) {
            long remainingTime = sevenDaysInMillis - (currentTime - lastShareTime);
            long remainingSeconds = remainingTime / 1000;
            long days = remainingSeconds / (24 * 3600);
            long hours = (remainingSeconds % (24 * 3600)) / 3600;
            long minutes = (remainingSeconds % 3600) / 60;
            long seconds = remainingSeconds % 60;

            String timeMessage = String.format("Paylaşmak için bekleyin: %d gün %02d:%02d:%02d", days, hours, minutes, seconds);
            Toast.makeText(this, timeMessage, Toast.LENGTH_LONG).show();
            return;
        }

        // Paylaşım intent'i başlat
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Fal Uygulaması");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Fal Uygulaması ile geleceğinizi keşfedin! Hemen indirin: https://play.google.com/store/apps/details?id=com.falapp.falciabla");

        startActivity(Intent.createChooser(shareIntent, "Uygulamamızı Paylaşın"));

        // 2 saniye sonra altın ver ve zamanı kaydet
        new Handler().postDelayed(() -> {
            if (!isFinishing()) {
                addCoinsToUser(10);
                Toast.makeText(FreeCoinActivity.this,
                        "Paylaşım yaptığınız için 10 altın kazandınız!", Toast.LENGTH_SHORT).show();

                // Paylaşım zamanını kaydet
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("last_share_time", System.currentTimeMillis());
                editor.apply();

                // Butonu devre dışı bırak
                btnShareApp.setEnabled(false);
                btnShareApp.setText("Paylaşıldı (7 gün sonra tekrar)");
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
    private void loadRewardedAd() {
        isAdLoading = true;

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
                isAdLoading = false;
            }

            @Override
            public void onAdFailedToLoad(com.google.android.gms.ads.LoadAdError adError) {
                rewardedAd = null;
                isAdLoading = false;
            }
        });
    }
}