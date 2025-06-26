package com.falapp.fortunetelle.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.falapp.fortunetelle.CoinPurchaseActivity;
import com.falapp.fortunetelle.LoveCompatibilityResultActivity;
import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;

import java.util.Calendar;

public class LoveCompatibilityFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private EditText etName;
    private TextView tvBirthDate;
    private Button btnCalculate;
    private ProgressBar progressBar;

    private String selectedBirthDate;
    private static final int COMPATIBILITY_COST = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_love_compatibility, container, false);


        dbHelper = new DatabaseHelper(getContext());


        currentUser = dbHelper.getUser();


        initViews(view);

        // Set click listeners
        btnCalculate.setOnClickListener(v -> validateAndProceed());
        tvBirthDate.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_name);
        tvBirthDate = view.findViewById(R.id.tv_birth_date);
        btnCalculate = view.findViewById(R.id.btn_calculate);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedBirthDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    tvBirthDate.setText(selectedBirthDate);
                }, year, month, day);


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


        if (selectedBirthDate == null || selectedBirthDate.isEmpty()) {
            Toast.makeText(getContext(), "Lütfen doğum tarihi seçin", Toast.LENGTH_SHORT).show();
            return;
        }


        if (currentUser.getCoins() < COMPATIBILITY_COST) {
            Toast.makeText(getContext(), "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), CoinPurchaseActivity.class));
            return;
        }


        currentUser.removeCoins(COMPATIBILITY_COST);
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());


        progressBar.setVisibility(View.VISIBLE);
        btnCalculate.setVisibility(View.GONE);


        new Handler().postDelayed(() -> {

            progressBar.setVisibility(View.GONE);
            btnCalculate.setVisibility(View.VISIBLE);


            Intent intent = new Intent(getActivity(), LoveCompatibilityResultActivity.class);
            intent.putExtra("person_name", name);
            intent.putExtra("person_birth_date", selectedBirthDate);
            startActivity(intent);


            etName.setText("");
            tvBirthDate.setText("Doğum Tarihi Seçin");
            selectedBirthDate = null;
        }, 1500);
    }


    private static class Handler {
        private final android.os.Handler handler = new android.os.Handler();

        public void postDelayed(Runnable r, long delayMillis) {
            handler.postDelayed(r, delayMillis);
        }
    }
}