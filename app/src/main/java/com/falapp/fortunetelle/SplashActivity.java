package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 saniye
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Animasyon ayarları
        ImageView ivLogo = findViewById(R.id.iv_logo);
        TextView tvAppName = findViewById(R.id.tv_app_name);
        TextView tvSlogan = findViewById(R.id.tv_slogan);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivLogo.startAnimation(fadeInAnimation);
        tvAppName.startAnimation(fadeInAnimation);
        tvSlogan.startAnimation(fadeInAnimation);

        // Splash ekranında gerekli kontroller ve yönlendirme
        new Handler().postDelayed(() -> {
            checkUserAndRedirect();
        }, SPLASH_DURATION);
    }

    private void checkUserAndRedirect() {
        // Kullanıcı durumunu kontrol et
        User user = dbHelper.getUser();

        Intent intent;
        if (user == null) {
            // Kullanıcı yoksa onboarding ekranına yönlendir
            intent = new Intent(SplashActivity.this, OnboardingActivity.class);
        } else {
            // Kullanıcı varsa ana ekrana yönlendir
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}