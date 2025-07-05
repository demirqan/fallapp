package com.falapp.falciabla;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.falapp.falciabla.models.User;
import com.falapp.falciabla.utils.BillingManager;
import com.falapp.falciabla.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import okhttp3.internal.http2.Http2Reader;

public class CoinPurchaseActivity extends AppCompatActivity implements BillingManager.BillingCallback {

    private RewardedAd rewardedAd;
    private boolean isAdLoading = false;
    private DatabaseHelper dbHelper;
    private User currentUser;
    private BillingManager billingManager;

    private MaterialToolbar toolbar;
    private TextView tvCoins;
    private CardView cardSmallPack;
    private CardView cardMediumPack;
    private CardView cardLargePack;
    private CardView cardFreeCoins;
    private Button btnWatchAd;
    private Button btnCompleteTask;
    private BottomNavigationView bottomNavigationView;
    private TextView tvSmallPackPrice;
    private TextView tvMediumPackPrice;
    private TextView tvLargePackPrice;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_purchase);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);
        MobileAds.initialize(this, initializationStatus -> {});
        loadRewardedAd();

        // Get current user
        currentUser = dbHelper.getUser();
        if (currentUser == null) {
            // If no user found, go back to main activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize billing manager
        billingManager = new BillingManager(this, this, dbHelper, currentUser, this);

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Update coin display
        updateCoinsDisplay();

        // Set up coin packs
        setupCoinPacks();

        // Set up free coins
        setupFreeCoins();

        // Set up navigation
        setupBottomNavigation();
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
        cardSmallPack = findViewById(R.id.card_small_pack);
        cardMediumPack = findViewById(R.id.card_medium_pack);
        cardLargePack = findViewById(R.id.card_large_pack);
        cardFreeCoins = findViewById(R.id.card_free_coins);
        btnWatchAd = findViewById(R.id.btn_watch_ad);
        btnCompleteTask = findViewById(R.id.btn_complete_task);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        tvSmallPackPrice = findViewById(R.id.tv_small_pack_price);
        tvMediumPackPrice = findViewById(R.id.tv_medium_pack_price);
        tvLargePackPrice = findViewById(R.id.tv_large_pack_price);
    }

    private void setupToolbar() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Altın Satın Al");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void updateCoinsDisplay() {
        tvCoins.setText(String.valueOf(currentUser.getCoins()));
    }

    private void setupCoinPacks() {

        // Sabit fiyatları elle yaz
        tvSmallPackPrice.setText("₺11,99");
        tvMediumPackPrice.setText("₺22,99");
        tvLargePackPrice.setText("47,99");


        cardSmallPack.setOnClickListener(v -> billingManager.startPurchaseFlow(BillingManager.PRODUCT_COINS_50));
        cardMediumPack.setOnClickListener(v -> billingManager.startPurchaseFlow(BillingManager.PRODUCT_COINS_120));
        cardLargePack.setOnClickListener(v -> billingManager.startPurchaseFlow(BillingManager.PRODUCT_COINS_300));
    }

    private void setupFreeCoins() {
        cardFreeCoins.setOnClickListener(v -> {

            cardFreeCoins.setVisibility(View.GONE);
            btnWatchAd.setVisibility(View.VISIBLE);
            btnCompleteTask.setVisibility(View.VISIBLE);
        });

        btnWatchAd.setOnClickListener(v -> watchAdForCoins());
        btnCompleteTask.setOnClickListener(v -> completeTaskForCoins());
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
                startActivity(new Intent(CoinPurchaseActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(CoinPurchaseActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                startActivity(new Intent(CoinPurchaseActivity.this, PremiumActivity.class));
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Bağlantıyı sonlandır
        if (billingManager != null) {
            billingManager.endConnection();
        }
    }

    // BillingManager.BillingCallback metodları
    @Override
    public void onBillingSetupFinished(boolean isSuccess) {
        if (!isSuccess) {
            Toast.makeText(this, "Ödeme sistemi başlatılamadı.", Toast.LENGTH_SHORT).show();
        } else {
            // 🔁 Fiyatları dinamik olarak göster

        }
    }

    @Override
    public void onPurchaseSuccess(String productId, int coins, boolean isPremium) {
        if (productId.equals(BillingManager.PRODUCT_PREMIUM_LIFETIME) ||
                productId.equals(BillingManager.SUBSCRIPTION_PREMIUM_MONTHLY) ||
                productId.equals(BillingManager.SUBSCRIPTION_PREMIUM_YEARLY)) {

            Toast.makeText(this, "🎉 Premium üyeliğiniz aktifleştirildi!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PremiumActivity.class);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, coins + " altın başarıyla satın alındı!", Toast.LENGTH_SHORT).show();
            updateCoinsDisplay(); // Yeniden göstermek için
        }
    }

    @Override
    public void onPurchaseFailed(int errorCode, String errorMessage) {
        Toast.makeText(this, "Satın alma işlemi başarısız: " + errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void watchAdForCoins() {
        if (rewardedAd != null) {
            // Reklam hazır → direkt göster
            rewardedAd.show(this, rewardItem -> {
                // Kullanıcı ödülü aldı → dialog varsa kapat, altını ver
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                addCoinsToUser(5);
                Toast.makeText(this, "Reklam izlediğiniz için 5 altın kazandınız!", Toast.LENGTH_SHORT).show();

                // Sonraki reklamı yükle
                loadRewardedAd();
            });
        } else {
            // Reklam hazır değil → yükleniyor dialogunu göster
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Reklam Yükleniyor");
            builder.setMessage("Lütfen bekleyin...");
            builder.setCancelable(false);
            loadingDialog = builder.show();

            // Reklamı yükle
            loadRewardedAd();

            // 3 saniye sonra dialog'u kapat
            new android.os.Handler().postDelayed(() -> {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
                Toast.makeText(CoinPurchaseActivity.this,
                        "Reklam şu anda hazır değil. Lütfen tekrar deneyin.",
                        Toast.LENGTH_SHORT).show();
            }, 3000);
        }
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
                            break;
                        case 1:
                            coinsToAdd = 15;
                            task = "Uygulamayı Paylaşma";
                            break;
                        case 2:
                            coinsToAdd = 20;
                            task = "Ücretsiz Deneme Başlatma";
                            break;
                        default:
                            return;
                    }

                    // Simulate task completion
                    addCoinsToUser(coinsToAdd);

                    // Show success message
                    Toast.makeText(CoinPurchaseActivity.this,
                            task + " görevini tamamladığınız için " + coinsToAdd +
                                    " altın kazandınız!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    private void loadRewardedAd() {
        isAdLoading = true;

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-5042635078560836/8569397728", adRequest, new RewardedAdLoadCallback() {
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
    private void addCoinsToUser(int amount) {
        // Add coins to user
        currentUser.addCoins(amount);

        // Update user in database
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());

        // Update display
        updateCoinsDisplay();
    }
}