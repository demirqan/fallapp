package com.falapp.fortunetelle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.falapp.fortunetelle.adapters.AffirmationsPagerAdapter;
import com.falapp.fortunetelle.models.Affirmation;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AffirmationsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private MaterialToolbar toolbar;
    private ViewPager2 viewPager;
    private AffirmationsPagerAdapter adapter;
    private ImageButton btnPrevious;
    private ImageButton btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affirmations);


        dbHelper = new DatabaseHelper(this);


        currentUser = dbHelper.getUser();
        if (currentUser == null) {

            currentUser = new User("User", 2000);
            dbHelper.addUser(currentUser);
            Toast.makeText(this, "Yeni kullanıcı oluşturuldu!", Toast.LENGTH_SHORT).show();
        }


        initViews();


        setupToolbar();


        setupViewPager();


        setupNavigationButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
    }

    private void setupToolbar() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Olumlamalar");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {

        List<Affirmation> affirmations = getAffirmations();

        // Set up adapter
        adapter = new AffirmationsPagerAdapter(this, affirmations);
        viewPager.setAdapter(adapter);


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationButtons(position);
            }
        });
    }

    private void setupNavigationButtons() {
        btnPrevious.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });


        updateNavigationButtons(0);
    }

    private void updateNavigationButtons(int position) {
        btnPrevious.setVisibility(position > 0 ? View.VISIBLE : View.INVISIBLE);
        btnNext.setVisibility(position < adapter.getItemCount() - 1 ? View.VISIBLE : View.INVISIBLE);
    }

    private List<Affirmation> getAffirmations() {
        List<Affirmation> affirmations = new ArrayList<>();


        affirmations.add(new Affirmation(
                "Her gün her yönden daha iyi ve daha güçlü oluyorum.",
                R.drawable.olum1));

        affirmations.add(new Affirmation(
                "Hayatımdaki tüm zorlukları kolayca aşabiliyorum.",
                R.drawable.olum2));

        affirmations.add(new Affirmation(
                "Ben huzur ve mutlulukla doluyum.",
                R.drawable.olum3));

        affirmations.add(new Affirmation(
                "Hayatım bolluklarla dolu ve bunun için minnettarım.",
                R.drawable.olum1));

        affirmations.add(new Affirmation(
                "Ben değerliyim ve sevilmeyi hak ediyorum.",
                R.drawable.olum3));

        affirmations.add(new Affirmation(
                "Tüm hedeflerime ulaşmak için gereken güce sahibim.",
                R.drawable.olum2));

        affirmations.add(new Affirmation(
                "Her gün yeni fırsatlarla dolu ve ben bunları değerlendirmeye hazırım.",
                R.drawable.olum1));

        affirmations.add(new Affirmation(
                "Kendime güveniyorum ve hayatımın kontrolü bende.",
                R.drawable.olum1));

        affirmations.add(new Affirmation(
                "Ben sağlıklı ve enerji doluyum.",
                R.drawable.olum2));

        affirmations.add(new Affirmation(
                "Her gün daha fazla sevgi ve şükran duygusuyla doluyorum.",
                R.drawable.olum3));

        return affirmations;
    }
}