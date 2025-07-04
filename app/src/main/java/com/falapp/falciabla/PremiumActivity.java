package com.falapp.falciabla;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class PremiumActivity extends AppCompatActivity implements BillingManager.BillingCallback {

    private static final String PREFS_NAME = "app_prefs";
    private static final String PREMIUM_BONUS_GIVEN = "premium_bonus_given";



    private DatabaseHelper dbHelper;
    private User currentUser;
    private BillingManager billingManager;

    private MaterialToolbar toolbar;
    private TextView tvStatus;
    private CardView cardMonthly;
    private CardView cardYearly;
    private CardView cardLifetime;
    private Button btnSubscribe;
    private TextView tvPremiumBenefits;
    private BottomNavigationView bottomNavigationView;
    private TextView tvMonthlyPrice;
    private TextView tvYearlyPrice;
    private TextView tvLifetimePrice;

    private int selectedPlanId = -1;
    private static final int PLAN_MONTHLY = 0;
    private static final int PLAN_YEARLY = 1;
    private static final int PLAN_LIFETIME = 2;

    private boolean hasShownPremiumToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get current user
        currentUser = dbHelper.getUser();
      //  if (currentUser == null) {
       //     // If no user found, go back to main activity
      //      startActivity(new Intent(this, MainActivity.class));
      //      finish();
      //      return;
     //   }


        // Initialize billing manager
        billingManager = new BillingManager(this, this, dbHelper, currentUser, this);

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Update status display
        updateStatusDisplay();

        // Set up subscription plans
        setupSubscriptionPlans();

        // Set up subscribe button
        btnSubscribe.setOnClickListener(v -> {

            subscribeToPremium();
        });

        // Set up navigation
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Önce güncelle
        currentUser = dbHelper.getUser();
        billingManager.queryPurchases(); // <--- EKLE

        Log.d("PremiumCheck", "Premium flag from DB: " + currentUser.isPremium());

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Log.d("PremiumCheck", "Premium flag from SharedPreferences: " + prefs.getBoolean("is_premium", false));

        updateStatusDisplay();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStatus = findViewById(R.id.tv_status);
        cardMonthly = findViewById(R.id.card_monthly);
        cardYearly = findViewById(R.id.card_yearly);
        cardLifetime = findViewById(R.id.card_lifetime);
        btnSubscribe = findViewById(R.id.btn_subscribe);
        tvPremiumBenefits = findViewById(R.id.tv_premium_benefits);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        tvMonthlyPrice = findViewById(R.id.tv_monthly_price);
        tvYearlyPrice = findViewById(R.id.tv_yearly_price);
        tvLifetimePrice = findViewById(R.id.tv_lifetime_price);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Premium Üyelik");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void updateStatusDisplay() {
        // isPremium sadece bir kere tanımlanmalı
        boolean isPremium = (currentUser != null) ? currentUser.isPremium() : false;
        Log.d("PremiumCheck", "updateStatusDisplay - currentUser.isPremium(): " + isPremium);

        if (isPremium) {
            tvStatus.setText("Premium Üyelik Aktif");
            tvStatus.setTextColor(getResources().getColor(R.color.premium_color));

            // Premium kullanıcı için UI ayarları
            cardMonthly.setVisibility(View.GONE);
            cardYearly.setVisibility(View.GONE);
            cardLifetime.setVisibility(View.GONE);
            btnSubscribe.setVisibility(View.GONE);

            tvPremiumBenefits.setText("Premium üyelik avantajlarından yararlanıyorsunuz:\n\n" +
                    "• Günlük 10 Adet Fal Bakma\n" +
                    "• Sesli Fal Okuma\n" +
                    "• Gelişmiş Cevaplar\n" +
                    "• Gelişmiş özellikler\n" +
                    "• Öncelikli destek");
        } else {
            tvStatus.setText("Standart Üyelik");
            tvStatus.setTextColor(getResources().getColor(R.color.standard_color));

            // Standart kullanıcı için UI ayarları
            cardMonthly.setVisibility(View.VISIBLE);
            cardYearly.setVisibility(View.VISIBLE);
            cardLifetime.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);

            tvPremiumBenefits.setText("Premium üyelik avantajları:\n\n" +
                    "• Günlük 10 Adet Fal Bakma\n" +
                    "• Sesli Fal Okuma\n" +
                    "• Gelişmiş Cevaplar\n" +
                    "• Gelişmiş özellikler\n" +
                    "• Öncelikli destek");
        }
    }

    private void setupSubscriptionPlans() {
        // Fiyatları BillingManager'dan al
        String monthlyPrice = "34.99 TL";
        String yearlyPrice = "144.99 TL";
        String lifetimePrice = "839.99 TL";

        tvMonthlyPrice.setText(monthlyPrice);
        tvYearlyPrice.setText(yearlyPrice);
        tvLifetimePrice.setText(lifetimePrice);

        // Set click listeners for plan selection
        cardMonthly.setOnClickListener(v -> selectPlan(PLAN_MONTHLY));
        cardYearly.setOnClickListener(v -> selectPlan(PLAN_YEARLY));
        cardLifetime.setOnClickListener(v -> selectPlan(PLAN_LIFETIME));

        // Set initial selection (default to monthly)
        selectPlan(PLAN_MONTHLY);
    }

    private void selectPlan(int planId) {
        // Reset all plan cards
        cardMonthly.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        cardYearly.setCardBackgroundColor(getResources().getColor(R.color.card_background));
        cardLifetime.setCardBackgroundColor(getResources().getColor(R.color.card_background));

        // Highlight selected plan
        switch (planId) {
            case PLAN_MONTHLY:
                cardMonthly.setCardBackgroundColor(getResources().getColor(R.color.selected_card_background));
                btnSubscribe.setText("Aylık Plana Abone Ol (34.99 TL)");
                break;
            case PLAN_YEARLY:
                cardYearly.setCardBackgroundColor(getResources().getColor(R.color.selected_card_background));
                btnSubscribe.setText("Yıllık Plana Abone Ol (144.99 TL)");
                break;
            case PLAN_LIFETIME:
                cardLifetime.setCardBackgroundColor(getResources().getColor(R.color.selected_card_background));
                btnSubscribe.setText("Ömür Boyu Erişim Satın Al (839.99 TL)");
                break;
        }

        selectedPlanId = planId;
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(PremiumActivity.this, MainActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_readings) {
                startActivity(new Intent(PremiumActivity.this, MyReadingsActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_horoscope) {
                startActivity(new Intent(PremiumActivity.this, HoroscopeActivity.class));
                return true;
            }
            else if (itemId == R.id.nav_premium) {
                // Already on premium, do nothing
                return true;
            }

            return false;
        });

        // Set the premium item as checked
        bottomNavigationView.setSelectedItemId(R.id.nav_premium);
    }

    private void subscribeToPremium() {
        // Ödeme işlemini başlat
        switch (selectedPlanId) {
            case PLAN_MONTHLY:
                billingManager.startPurchaseFlow(BillingManager.SUBSCRIPTION_PREMIUM_MONTHLY);
                break;
            case PLAN_YEARLY:
                billingManager.startPurchaseFlow(BillingManager.SUBSCRIPTION_PREMIUM_YEARLY);
                break;
            case PLAN_LIFETIME:
                billingManager.startPurchaseFlow(BillingManager.PRODUCT_PREMIUM_LIFETIME);
                break;
        }
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
        }
    }


    @Override
    public void onPurchaseSuccess(String productId, int coins, boolean isPremium) {
        Log.d("PremiumCheck", "🎯 onPurchaseSuccess: " + productId + " | premium=" + isPremium + " | coins=" + coins);

        runOnUiThread(() -> {
            if (isPremium && !hasShownPremiumToast) {
                hasShownPremiumToast = true;
                Toast.makeText(this, "🎉 Premium üyeliğiniz başarıyla aktifleştirildi!", Toast.LENGTH_LONG).show();

                if (currentUser != null) {
                    currentUser.setPremium(true);
                    dbHelper.updateUserPremium(currentUser.getId(), true);
                }

                SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                prefs.edit().putBoolean("is_premium", true).apply();
            }

            currentUser = dbHelper.getUser();


        });
        updateStatusDisplay();
    }



    @Override
    public void onPurchaseFailed(int errorCode, String errorMessage) {
        Toast.makeText(this, "Satın alma işlemi başarısız: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
    private boolean isPremiumBonusGiven() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(PREMIUM_BONUS_GIVEN, false);
    }

    private void setPremiumBonusGiven() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(PREMIUM_BONUS_GIVEN, true).apply();
    }
}