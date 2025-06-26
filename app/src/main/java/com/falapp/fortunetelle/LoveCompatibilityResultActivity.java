package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Random;

public class LoveCompatibilityResultActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private MaterialToolbar toolbar;
    private TextView tvPersonName;
    private TextView tvPersonBirthDate;
    private ProgressBar pbLove;
    private ProgressBar pbDialog;
    private ProgressBar pbPassion;
    private TextView tvLovePercent;
    private TextView tvDialogPercent;
    private TextView tvPassionPercent;
    private TextView tvCompatibilityResult;
    private View progressBar;

    private String personName;
    private String personBirthDate;
    private int lovePercent;
    private int dialogPercent;
    private int passionPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_compatibility_result);

        // Get data from intent
        personName = getIntent().getStringExtra("person_name");
        personBirthDate = getIntent().getStringExtra("person_birth_date");

        if (personName == null || personBirthDate == null) {
            finish();
            return;
        }

        // Initialize database helper and services
        dbHelper = new DatabaseHelper(this);
        chatGPTService = new ChatGPTService();

        // Get current user
        currentUser = dbHelper.getUser();

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Set person info
        tvPersonName.setText(personName);
        tvPersonBirthDate.setText(personBirthDate);

        // Generate random percentages between 60-95%
        generateRandomPercentages();

        // Set percentages
        setPercentages();

        // Get compatibility result
        getCompatibilityResult();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvPersonName = findViewById(R.id.tv_person_name);
        tvPersonBirthDate = findViewById(R.id.tv_person_birth_date);
        pbLove = findViewById(R.id.pb_love);
        pbDialog = findViewById(R.id.pb_dialog);
        pbPassion = findViewById(R.id.pb_passion);
        tvLovePercent = findViewById(R.id.tv_love_percent);
        tvDialogPercent = findViewById(R.id.tv_dialog_percent);
        tvPassionPercent = findViewById(R.id.tv_passion_percent);
        tvCompatibilityResult = findViewById(R.id.tv_compatibility_result);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Aşk Uyumu");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void generateRandomPercentages() {
        Random random = new Random();
        lovePercent = 60 + random.nextInt(36); // 60-95%
        dialogPercent = 60 + random.nextInt(36); // 60-95%
        passionPercent = 60 + random.nextInt(36); // 60-95%
    }

    private void setPercentages() {
        // Set progress bars
        pbLove.setProgress(lovePercent);
        pbDialog.setProgress(dialogPercent);
        pbPassion.setProgress(passionPercent);

        // Set percentage texts
        tvLovePercent.setText("%" + lovePercent);
        tvDialogPercent.setText("%" + dialogPercent);
        tvPassionPercent.setText("%" + passionPercent);
    }

    private void getCompatibilityResult() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        tvCompatibilityResult.setVisibility(View.GONE);

        // Get compatibility result in background thread
        new Thread(() -> {
            // Create a fake user name and birth date for the current user
            String userName = "Ben";
            String userBirthDate = "1/1/1990"; // This is just a placeholder

            final String result = chatGPTService.getLoveCompatibility(
                    userName, userBirthDate, personName, personBirthDate);

            // Update UI on main thread
            runOnUiThread(() -> {
                // Hide progress bar
                progressBar.setVisibility(View.GONE);

                // Show result
                tvCompatibilityResult.setText(result);
                tvCompatibilityResult.setVisibility(View.VISIBLE);
            });
        }).start();
    }
}