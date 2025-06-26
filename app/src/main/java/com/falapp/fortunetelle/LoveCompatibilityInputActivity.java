package com.falapp.fortunetelle;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;

public class LoveCompatibilityInputActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private MaterialToolbar toolbar;
    private EditText etName;
    private TextView tvBirthDate;
    private Button btnNext;

    private String selectedBirthDate;
    private static final int COMPATIBILITY_COST = 10; // Coins required for compatibility check

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_compatibility_input);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get current user
        currentUser = dbHelper.getUser();
        if (currentUser == null) {
            // Create new user if not exists
            currentUser = new User("User", 2000);
            dbHelper.addUser(currentUser);
            Toast.makeText(this, "Yeni kullanıcı oluşturuldu!", Toast.LENGTH_SHORT).show();
        }

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Set click listeners
        btnNext.setOnClickListener(v -> validateAndProceed());
        tvBirthDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.et_name);
        tvBirthDate = findViewById(R.id.tv_birth_date);
        btnNext = findViewById(R.id.btn_next);
    }

    private void setupToolbar() {
        // setSupportActionBar(toolbar); // Removed to prevent conflict
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Yeni Aday");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedBirthDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    tvBirthDate.setText(selectedBirthDate);
                }, year, month, day);

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void validateAndProceed() {
        // Validate name
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Lütfen isim girin");
            etName.requestFocus();
            return;
        }

        // Validate birth date
        if (selectedBirthDate == null || selectedBirthDate.isEmpty()) {
            Toast.makeText(this, "Lütfen doğum tarihi seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user has enough coins
        if (currentUser.getCoins() < COMPATIBILITY_COST) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        // Deduct coins from user
        currentUser.removeCoins(COMPATIBILITY_COST);
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());

        // Open compatibility result activity
        Intent intent = new Intent(this, LoveCompatibilityResultActivity.class);
        intent.putExtra("person_name", name);
        intent.putExtra("person_birth_date", selectedBirthDate);
        startActivity(intent);
    }
}