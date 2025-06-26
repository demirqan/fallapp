package com.falapp.fortunetelle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.falapp.fortunetelle.adapters.OnboardingPagerAdapter;
import com.falapp.fortunetelle.models.OnboardingPage;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "FalAppPrefs";
    private static final String FIRST_RUN = "firstRun";

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button btnNext;
    private Button btnSkip;
    private TextView tvTitle;
    private TextView tvDescription;

    private DatabaseHelper dbHelper;
    private List<OnboardingPage> onboardingPages;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initViews();

        // Create onboarding pages
        createOnboardingPages();

        // Set up ViewPager
        setupViewPager();

        // Set up buttons
        setupButtons();
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
    }

    private void createOnboardingPages() {
        onboardingPages = new ArrayList<>();

        // Add onboarding pages
        onboardingPages.add(new OnboardingPage(
                "Fal Dünyasına Hoş Geldiniz",
                "Kahve, tarot, el ve yüz fallarına bir tıkla ulaşın ve geleceğinizi keşfedin.",
                R.drawable.onboarding_1));

        onboardingPages.add(new OnboardingPage(
                "Uzman Falcılar",
                "Alanında uzman falcılarımız sizin için en doğru yorumları yapıyor.",
                R.drawable.onboarding_2));

        onboardingPages.add(new OnboardingPage(
                "Burçlar ve Astroloji",
                "Günlük, haftalık ve aylık burç yorumlarınıza anında ulaşın.",
                R.drawable.onboarding_3));

        onboardingPages.add(new OnboardingPage(
                "Rüya Yorumları ve Aşk Uyumu",
                "Rüyalarınızı yorumlayın ve aşk uyumunuzu öğrenin.",
                R.drawable.onboarding_4));

        onboardingPages.add(new OnboardingPage(
                "Başlayalım!",
                "Hemen üye olun ve ilk falınızı ücretsiz bakın!",
                R.drawable.onboarding_5));
    }

    private void setupViewPager() {
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this, onboardingPages);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Tab settings can be adjusted here if needed
        }).attach();

        // Set page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateButtons();
                updateTextContent();
            }
        });
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (currentPage < onboardingPages.size() - 1) {
                // Go to next page
                viewPager.setCurrentItem(currentPage + 1);
            } else {
                // Last page, finish onboarding
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());

        // Initialize button states
        updateButtons();
        updateTextContent();
    }

    private void updateButtons() {
        if (currentPage == onboardingPages.size() - 1) {
            // Last page
            btnNext.setText("Başla");
            btnSkip.setVisibility(View.GONE);
        } else {
            btnNext.setText("İleri");
            btnSkip.setVisibility(View.VISIBLE);
        }
    }

    private void updateTextContent() {
        OnboardingPage currentOnboardingPage = onboardingPages.get(currentPage);
        tvTitle.setText(currentOnboardingPage.getTitle());
        tvDescription.setText(currentOnboardingPage.getDescription());
    }

    private void finishOnboarding() {
        // Create new user with default 20 coins
        User user = new User("Kullanıcı", 20);
        dbHelper.addUser(user);

        // Mark first run as completed
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(FIRST_RUN, false);
        editor.apply();

        // Show welcome toast
        Toast.makeText(this, "Hoş geldiniz! 20 altın hediyeniz hesabınıza tanımlandı!", Toast.LENGTH_LONG).show();

        // Go to main activity
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}