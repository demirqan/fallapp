package com.falapp.fortunetelle.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.models.JournalEntry;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.falapp.fortunetelle.utils.JournalDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JournalFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private JournalDatabaseHelper journalDbHelper;
    private User currentUser;

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private EditText etJournalEntry;
    private Button btnSave;

    private long selectedDateMillis;
    private JournalEntry currentEntry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal, container, false);


        dbHelper = new DatabaseHelper(getContext());
        journalDbHelper = new JournalDatabaseHelper(getContext());


        currentUser = dbHelper.getUser();


        initViews(view);


        setupCalendarView();


        btnSave.setOnClickListener(v -> saveJournalEntry());


        selectedDateMillis = Calendar.getInstance().getTimeInMillis();
        updateSelectedDateText();
        loadJournalEntry();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        saveJournalEntry();
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        etJournalEntry = view.findViewById(R.id.et_journal_entry);
        btnSave = view.findViewById(R.id.btn_save);
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
        if (currentUser == null) return;

        currentEntry = journalDbHelper.getJournalEntry(currentUser.getId(), selectedDateMillis);

        if (currentEntry != null) {
            etJournalEntry.setText(currentEntry.getContent());
        } else {
            etJournalEntry.setText("");
        }
    }

    private void saveJournalEntry() {
        if (currentUser == null) return;

        String content = etJournalEntry.getText().toString().trim();

        if (content.isEmpty()) {
            // If content is empty, delete entry if it exists
            if (currentEntry != null) {
                journalDbHelper.deleteJournalEntry(currentEntry.getId());
                Toast.makeText(getContext(), "Günlük girişi silindi", Toast.LENGTH_SHORT).show();
                currentEntry = null;
            }
            return;
        }

        if (currentEntry == null) {

            currentEntry = new JournalEntry(
                    currentUser.getId(),
                    selectedDateMillis,
                    content,
                    System.currentTimeMillis()
            );
            long entryId = journalDbHelper.addJournalEntry(currentEntry);
            currentEntry.setId((int) entryId);
        } else {

            currentEntry.setContent(content);
            currentEntry.setLastModified(System.currentTimeMillis());
            journalDbHelper.updateJournalEntry(currentEntry);
        }

        Toast.makeText(getContext(), "Günlük kaydedildi", Toast.LENGTH_SHORT).show();
    }
}