package com.falapp.falciabla;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.falapp.falciabla.models.FortuneReading;
import com.falapp.falciabla.models.FortuneTeller;
import com.falapp.falciabla.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReadingResultActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private FortuneReading reading;
    private FortuneTeller fortuneTeller;

    private MaterialToolbar toolbar;
    private ImageView ivReadingIcon;
    private TextView tvFortuneTellerName;
    private TextView tvUserName;
    private TextView tvResult;

    private Button btnShare, btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_result);

        int readingId = getIntent().getIntExtra("reading_id", -1);
        if (readingId == -1) {
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        reading = dbHelper.getReading(readingId);
        if (reading == null) {
            finish();
            return;
        }

        fortuneTeller = dbHelper.getFortuneTeller(reading.getFortuneTellerId());
        if (fortuneTeller == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setReadingData();
        setupButtonListeners(); // ✅ Paylaş & Kaydet
        checkForUpdatedResultPeriodically();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivReadingIcon = findViewById(R.id.iv_reading_icon);
        tvFortuneTellerName = findViewById(R.id.tv_fortune_teller_name);
        tvUserName = findViewById(R.id.tv_user_name);
        tvResult = findViewById(R.id.tv_result);

        // Bu şekilde, sınıf değişkenlerine atama yapılmalı:
        btnShare = findViewById(R.id.btn_share);
        btnSave = findViewById(R.id.btn_save);
        Button btnNewReading = findViewById(R.id.btn_new_reading); // Bu local olabilir

        // btnNewReading tıklama
        btnNewReading.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(reading.getReadableType() + " Sonucu");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setReadingData() {
        int readingIconId = 0;
        switch (reading.getType()) {
            case "coffee":
                readingIconId = R.drawable.ic_coffee;
                break;
            case "tarot":
                readingIconId = R.drawable.ic_tarot;
                break;
            case "palm":
                readingIconId = R.drawable.ic_palm;
                break;
            case "face":
                readingIconId = R.drawable.ic_face;
                break;
        }
        if (readingIconId != 0) {
            ivReadingIcon.setImageResource(readingIconId);
        }

        tvFortuneTellerName.setText(fortuneTeller.getName());
        tvUserName.setText(reading.getUserName());
        if (reading.getResult() != null && !reading.getResult().trim().isEmpty()) {
            tvResult.setText(reading.getResult());
        } else {
            tvResult.setText("Fal sonucu hazırlanıyor, lütfen bekleyin...");
        }
    }

    private void checkForUpdatedResultPeriodically() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Veritabanından güncel veriyi tekrar çek
                FortuneReading updatedReading = dbHelper.getReading(reading.getId());

                // Eğer sonuç geldiyse ekranda güncelle
                if (updatedReading.getResult() != null && !updatedReading.getResult().trim().isEmpty()) {
                    reading = updatedReading;
                    tvResult.setText(updatedReading.getResult());
                } else {
                    // Hala boşsa kullanıcıya bilgi ver ve tekrar dene
                    tvResult.setText("Fal sonucu hazırlanıyor, lütfen bekleyin...");
                    handler.postDelayed(this, 5000); // 5 saniye sonra tekrar dene
                }
            }
        }, 3000); // İlk kontrol 3 saniye sonra
    }


    private void setupButtonListeners() {
        btnShare.setOnClickListener(v -> {
            String text = tvResult.getText().toString();
            if (!text.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(shareIntent, "Falını paylaş"));
            } else {
                Toast.makeText(this, "Paylaşılacak içerik yok.", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            String text = tvResult.getText().toString();
            if (!text.isEmpty()) {
                try {
                    File file = new File(getExternalFilesDir(null), "fal_" + System.currentTimeMillis() + ".txt");
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(text.getBytes());
                    fos.close();
                    Toast.makeText(this, "Fal kaydedildi:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Kaydetme sırasında hata oluştu.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Kaydedilecek içerik yok.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
