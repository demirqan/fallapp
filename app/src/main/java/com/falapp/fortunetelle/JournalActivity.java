package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.falapp.fortunetelle.models.JournalEntry;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.falapp.fortunetelle.utils.JournalDatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JournalActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private JournalDatabaseHelper journalDbHelper;
    private User currentUser;

    private MaterialToolbar toolbar;
    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private EditText etJournalEntry;
    private Button btnSave;

    private long selectedDateMillis;
    private JournalEntry currentEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        // Initialize database helpers
        dbHelper = new DatabaseHelper(this);
        journalDbHelper = new JournalDatabaseHelper(this);

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

        // Set up calendar view
        setupCalendarView();

        // Set up save button
        btnSave.setOnClickListener(v -> saveJournalEntry());

        // Initialize with today's date
        selectedDateMillis = Calendar.getInstance().getTimeInMillis();
        updateSelectedDateText();
        loadJournalEntry();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendar_view);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        etJournalEntry = findViewById(R.id.et_journal_entry);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupToolbar() {
        // setSupportActionBar(toolbar); // Removed to prevent conflict
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Günlük");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDateMillis = calendar.getTimeInMillis();

            updateSelectedDateText();
            loadJournalEntry();
        });
    }

    private void updateSelectedDateText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvSelectedDate.setText(dateFormat.format(new Date(selectedDateMillis)));
    }

    private void loadJournalEntry() {
        currentEntry = journalDbHelper.getJournalEntry(currentUser.getId(), selectedDateMillis);

        if (currentEntry != null) {
            etJournalEntry.setText(currentEntry.getContent());
        } else {
            etJournalEntry.setText("");
        }
    }

    private void saveJournalEntry() {
        String content = etJournalEntry.getText().toString().trim();

        if (content.isEmpty()) {
            // If content is empty, delete entry if it exists
            if (currentEntry != null) {
                journalDbHelper.deleteJournalEntry(currentEntry.getId());
                Toast.makeText(this, "Günlük girişi silindi", Toast.LENGTH_SHORT).show();
                currentEntry = null;
            }
            return;
        }

        if (currentEntry == null) {
            // Create new entry
            currentEntry = new JournalEntry(
                    currentUser.getId(),
                    selectedDateMillis,
                    content,
                    System.currentTimeMillis()
            );
            long entryId = journalDbHelper.addJournalEntry(currentEntry);
            currentEntry.setId((int) entryId);
        } else {
            // Update existing entry
            currentEntry.setContent(content);
            currentEntry.setLastModified(System.currentTimeMillis());
            journalDbHelper.updateJournalEntry(currentEntry);
        }

        Toast.makeText(this, "Günlük kaydedildi", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Auto-save on leaving the activity
        saveJournalEntry();
    }
}