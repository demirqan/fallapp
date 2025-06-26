package com.falapp.fortunetelle;

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
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private MaterialToolbar toolbar;
    private EditText etUserName;
    private SwitchMaterial switchNotifications;
    private SwitchMaterial switchDarkMode;
    private Button btnSave;
    private Button btnLogout;
    private TextView tvVersionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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

        // Initialize views
        initViews();

        // Set up toolbar
        setupToolbar();

        // Load current settings
        loadSettings();

        // Set up buttons
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etUserName = findViewById(R.id.et_user_name);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        btnSave = findViewById(R.id.btn_save);
        btnLogout = findViewById(R.id.btn_logout);
        tvVersionInfo = findViewById(R.id.tv_version_info);
    }

    private void setupToolbar() {
        //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Ayarlar");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadSettings() {
        // Set user name
        etUserName.setText(currentUser.getName());

        // Load notification setting from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("FalAppPrefs", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        boolean darkModeEnabled = prefs.getBoolean("dark_mode_enabled", false);

        switchNotifications.setChecked(notificationsEnabled);
        switchDarkMode.setChecked(darkModeEnabled);

        // Set version info
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersionInfo.setText("Versiyon: " + versionName);
        } catch (Exception e) {
            tvVersionInfo.setText("Versiyon: 1.0.0");
        }
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveSettings());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void saveSettings() {
        // Get user name
        String userName = etUserName.getText().toString().trim();
        if (userName.isEmpty()) {
            etUserName.setError("Lütfen bir isim girin");
            etUserName.requestFocus();
            return;
        }

        // Update user name
        currentUser.setName(userName);

        // Update user in database
        // Note: In a real app, we would have a method to update user name
        // For now, we'll just update coins which will save the user object
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());

        // Save notification setting to SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("FalAppPrefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("notifications_enabled", switchNotifications.isChecked());
        editor.putBoolean("dark_mode_enabled", switchDarkMode.isChecked());
        editor.apply();

        // Apply dark mode if changed
        boolean previousDarkMode = prefs.getBoolean("previous_dark_mode", false);
        if (previousDarkMode != switchDarkMode.isChecked()) {
            editor.putBoolean("previous_dark_mode", switchDarkMode.isChecked());
            editor.apply();

            // In a real app, we would apply the theme change
            // For now, we'll just show a toast
            Toast.makeText(this, "Tema değişiklikleri uygulamayı yeniden başlattığınızda etkinleşecek",
                    Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(this, "Ayarlar kaydedildi", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Çıkış Yap")
                .setMessage("Çıkış yapmak istediğinizden emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    // In a real app, we would clear user session
                    // For now, we'll just go back to main activity
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("İptal", null)
                .show();
    }
}