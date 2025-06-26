package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.FortuneTeller;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

public class ReadingResultActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private FortuneReading reading;
    private FortuneTeller fortuneTeller;

    private MaterialToolbar toolbar;
    private ImageView ivReadingIcon;
    private TextView tvFortuneTellerName;
    private TextView tvUserName;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_result);

        // Get reading id from intent
        int readingId = getIntent().getIntExtra("reading_id", -1);
        if (readingId == -1) {
            finish();
            return;
        }

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get reading from database
        reading = dbHelper.getReading(readingId);
        if (reading == null) {
            finish();
            return;
        }

        // Get fortune teller
        fortuneTeller = dbHelper.getFortuneTeller(reading.getFortuneTellerId());
        if (fortuneTeller == null) {
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Set reading data
        setReadingData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivReadingIcon = findViewById(R.id.iv_reading_icon);
        tvFortuneTellerName = findViewById(R.id.tv_fortune_teller_name);
        tvUserName = findViewById(R.id.tv_user_name);
        tvResult = findViewById(R.id.tv_result);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(reading.getReadableType() + " Sonucu");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setReadingData() {
        // Set reading icon based on type
        String readingType = reading.getType();
        int readingIconId = 0;
        switch (readingType) {
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

        // Set fortune teller name
        tvFortuneTellerName.setText(fortuneTeller.getName());

        // Set user name
        tvUserName.setText(reading.getUserName());

        // Set reading result
        tvResult.setText(reading.getResult());
    }
}