package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.falapp.fortunetelle.adapters.FortuneTellerAdapter;
import com.falapp.fortunetelle.models.FortuneTeller;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class FortuneTellerListActivity extends AppCompatActivity implements FortuneTellerAdapter.FortuneTellerClickListener {

    private String readingType;
    private RecyclerView recyclerView;
    private FortuneTellerAdapter adapter;
    private DatabaseHelper dbHelper;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortune_teller_list);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Get current user
        currentUser = dbHelper.getUser();

        // Get reading type from intent
        readingType = getIntent().getStringExtra("reading_type");
        if (readingType == null) {
            Toast.makeText(this, "Fal türü belirlenmedi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
      //  //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getReadingTypeTitle(readingType));

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set up recycler view
        recyclerView = findViewById(R.id.recycler_fortune_tellers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load fortune tellers
        loadFortuneTellers();
    }

    private void loadFortuneTellers() {
        List<FortuneTeller> fortuneTellers = dbHelper.getAllFortuneTellers();
        adapter = new FortuneTellerAdapter(this, fortuneTellers, this);
        recyclerView.setAdapter(adapter);
    }

    private String getReadingTypeTitle(String type) {
        switch (type) {
            case "coffee":
                return "Kahve Falı";
            case "tarot":
                return "Tarot Falı";
            case "palm":
                return "El Falı";
            case "face":
                return "Yüz Falı";
            default:
                return "Fal";
        }
    }

    @Override
    public void onFortuneTellerClicked(FortuneTeller fortuneTeller) {
        // Check if user has enough coins
        if (currentUser.getCoins() < fortuneTeller.getPrice()) {
            Toast.makeText(this, "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        // Open reading detail activity
        Intent intent = new Intent(this, ReadingDetailActivity.class);
        intent.putExtra("reading_type", readingType);
        intent.putExtra("fortune_teller_id", fortuneTeller.getId());
        startActivity(intent);
    }
}