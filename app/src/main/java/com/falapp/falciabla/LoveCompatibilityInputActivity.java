package com.falapp.falciabla;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.falapp.falciabla.models.User;
import com.falapp.falciabla.utils.DatabaseHelper;
import com.falapp.falciabla.utils.FalLimitManager;
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // Klasik spinner görünüm
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedBirthDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    tvBirthDate.setText(selectedBirthDate);
                },
                year, month, day
        );

        // Spinner görünümünü aktif et
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);

        // Bugünden ileri tarih seçilmesin
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void validateAndProceed() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Lütfen isim girin");
            etName.requestFocus();
            return;
        }

        if (selectedBirthDate == null || selectedBirthDate.isEmpty()) {
            Toast.makeText(this, "Lütfen doğum tarihi seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isPremium = currentUser.isPremium();

        // Premium kullanıcı ise günlük fal hakkını kontrol et
        if (isPremium) {
            if (!FalLimitManager.canUsePremiumFal(this)) {
                Toast.makeText(this, "Bugün 10 fal yorumu hakkınızı kullandınız. Yarın tekrar deneyin.", Toast.LENGTH_LONG).show();
                return;
            } else {
                FalLimitManager.increasePremiumFalCount(this); // limiti arttır
            }
        }

        // Premium değilse altın kontrolü yap
        if (!isPremium && currentUser.getCoins() < COMPATIBILITY_COST) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        if (!isPremium) {
            currentUser.removeCoins(COMPATIBILITY_COST);
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
        }

        // Devam et
        Intent intent = new Intent(this, LoveCompatibilityResultActivity.class);
        intent.putExtra("person_name", name);
        intent.putExtra("person_birth_date", selectedBirthDate);
        startActivity(intent);
    }
}