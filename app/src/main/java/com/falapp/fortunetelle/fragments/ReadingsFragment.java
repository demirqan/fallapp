package com.falapp.fortunetelle.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.ReadingResultActivity;
import com.falapp.fortunetelle.adapters.ReadingsAdapter;
import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ReadingsFragment extends Fragment implements ReadingsAdapter.ReadingClickListener {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private RecyclerView recyclerView;
    private ReadingsAdapter adapter;
    private TextView tvNoReadings;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabNewReading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_readings, container, false);


        dbHelper = new DatabaseHelper(getContext());


        currentUser = dbHelper.getUser();


        recyclerView = view.findViewById(R.id.recycler_readings);
        tvNoReadings = view.findViewById(R.id.tv_no_readings);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        fabNewReading = view.findViewById(R.id.fab_new_reading);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        swipeRefreshLayout.setOnRefreshListener(this::refreshReadings);


        fabNewReading.setOnClickListener(v -> {
            // Navigate to MainActivity to select reading type
            Intent intent = new Intent(getActivity(), com.falapp.fortunetelle.MainActivity.class);
            intent.putExtra("openNewReading", true);
            startActivity(intent);
        });


        loadReadings();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshReadings();
    }

    private void loadReadings() {
        if (currentUser == null) return;

        List<FortuneReading> readings = dbHelper.getAllReadingsForUser(currentUser.getId());

        if (readings.isEmpty()) {
            tvNoReadings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoReadings.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            adapter = new ReadingsAdapter(getContext(), readings, dbHelper, this);
            recyclerView.setAdapter(adapter);
        }
    }

    private void refreshReadings() {
        if (currentUser == null) return;

        List<FortuneReading> readings = dbHelper.getAllReadingsForUser(currentUser.getId());

        if (readings.isEmpty()) {
            tvNoReadings.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoReadings.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new ReadingsAdapter(getContext(), readings, dbHelper, this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateReadings(readings);
            }
        }

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onReadingClicked(FortuneReading reading) {
        if (reading.isReadyToView()) {

            Intent intent = new Intent(getActivity(), ReadingResultActivity.class);
            intent.putExtra("reading_id", reading.getId());
            startActivity(intent);
        } else {
            // If reading is not ready, show toast
            long remainingTime = reading.getRemainingTime();
            long remainingSeconds = remainingTime / 1000;
            long minutes = remainingSeconds / 60;
            long seconds = remainingSeconds % 60;

            String timeMessage = String.format("Falınız hazırlanıyor. Kalan süre: %d:%02d", minutes, seconds);
            android.widget.Toast.makeText(getContext(), timeMessage, android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}