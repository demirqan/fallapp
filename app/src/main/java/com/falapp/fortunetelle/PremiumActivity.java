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
import com.falapp.fortunetelle.utils.BillingManager;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PremiumActivity extends AppCompatActivity implements BillingManager.BillingCallback {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium);

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
        btnSubscribe.setOnClickListener(v -> subscribeToPremium());

        // Set up navigation
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data
        currentUser = dbHelper.getUser();
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
        if (currentUser.isPremium()) {
            tvStatus.setText("Premium Üyelik Aktif");
            tvStatus.setTextColor(getResources().getColor(R.color.premium_color));

            // Hide subscription options for premium users
            cardMonthly.setVisibility(View.GONE);
            cardYearly.setVisibility(View.GONE);
            cardLifetime.setVisibility(View.GONE);
            btnSubscribe.setVisibility(View.GONE);

            // Show benefits text
            tvPremiumBenefits.setText("Premium üyelik avantajlarından yararlanıyorsunuz:\n\n" +
                    "• Sınırsız fal bakma\n" +
                    "• Reklamsız deneyim\n" +
                    "• Özel falcı erişimi\n" +
                    "• Gelişmiş özellikler\n" +
                    "• Öncelikli destek");
        } else {
            tvStatus.setText("Standart Üyelik");
            tvStatus.setTextColor(getResources().getColor(R.color.standard_color));

            // Show subscription options for standard users
            cardMonthly.setVisibility(View.VISIBLE);
            cardYearly.setVisibility(View.VISIBLE);
            cardLifetime.setVisibility(View.VISIBLE);
            btnSubscribe.setVisibility(View.VISIBLE);

            // Show benefits text
            tvPremiumBenefits.setText("Premium üyelik avantajları:\n\n" +
                    "• Sınırsız fal bakma\n" +
                    "• Reklamsız deneyim\n" +
                    "• Özel falcı erişimi\n" +
                    "• Gelişmiş özellikler\n" +
                    "• Öncelikli destek");
        }
    }

    private void setupSubscriptionPlans() {
        // Fiyatları BillingManager'dan al
        String monthlyPrice = "29 TL";
        String yearlyPrice = "119 TL";
        String lifetimePrice = "499 TL";

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
                btnSubscribe.setText("Aylık Plana Abone Ol (29.99 TL)");
                break;
            case PLAN_YEARLY:
                cardYearly.setCardBackgroundColor(getResources().getColor(R.color.selected_card_background));
                btnSubscribe.setText("Yıllık Plana Abone Ol (199.99 TL)");
                break;
            case PLAN_LIFETIME:
                cardLifetime.setCardBackgroundColor(getResources().getColor(R.color.selected_card_background));
                btnSubscribe.setText("Ömür Boyu Erişim Satın Al (499.99 TL)");
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
        // Kullanıcı bilgilerini güncelle


        // Başarı mesajını göster
        Toast.makeText(this,
                "Premium üyeliğiniz başarıyla aktifleştirildi!", Toast.LENGTH_SHORT).show();

        // Bonus altınları göster
        if (coins > 0) {
            Toast.makeText(this, "Bonus olarak " + coins + " altın kazandınız!", Toast.LENGTH_LONG).show();
        }

        // Ekranı güncelle
        updateStatusDisplay();
    }

    @Override
    public void onPurchaseFailed(int errorCode, String errorMessage) {
        Toast.makeText(this, "Satın alma işlemi başarısız: " + errorMessage, Toast.LENGTH_SHORT).show();
    }
}