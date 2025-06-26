package com.falapp.fortunetelle.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.adapters.AffirmationsPagerAdapter;
import com.falapp.fortunetelle.models.Affirmation;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AffirmationsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private User currentUser;

    private ViewPager2 viewPager;
    private AffirmationsPagerAdapter adapter;
    private ImageButton btnPrevious;
    private ImageButton btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_affirmations, container, false);


        dbHelper = new DatabaseHelper(getContext());


        currentUser = dbHelper.getUser();


        initViews(view);


        setupViewPager();


        setupNavigationButtons();

        return view;
    }

    private void initViews(View view) {
        viewPager = view.findViewById(R.id.view_pager);
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnNext = view.findViewById(R.id.btn_next);
    }

    private void setupViewPager() {

        List<Affirmation> affirmations = getAffirmations();


        adapter = new AffirmationsPagerAdapter(getContext(), affirmations);
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
                R.drawable.affirmation_1));

        affirmations.add(new Affirmation(
                "Hayatımdaki tüm zorlukları kolayca aşabiliyorum.",
                R.drawable.affirmation_2));

        affirmations.add(new Affirmation(
                "Ben huzur ve mutlulukla doluyum.",
                R.drawable.affirmation_3));

        affirmations.add(new Affirmation(
                "Hayatım bolluklarla dolu ve bunun için minnettarım.",
                R.drawable.affirmation_4));

        affirmations.add(new Affirmation(
                "Ben değerliyim ve sevilmeyi hak ediyorum.",
                R.drawable.affirmation_5));

        affirmations.add(new Affirmation(
                "Tüm hedeflerime ulaşmak için gereken güce sahibim.",
                R.drawable.affirmation_6));

        affirmations.add(new Affirmation(
                "Her gün yeni fırsatlarla dolu ve ben bunları değerlendirmeye hazırım.",
                R.drawable.affirmation_7));

        affirmations.add(new Affirmation(
                "Kendime güveniyorum ve hayatımın kontrolü bende.",
                R.drawable.affirmation_8));

        affirmations.add(new Affirmation(
                "Ben sağlıklı ve enerji doluyum.",
                R.drawable.affirmation_9));

        affirmations.add(new Affirmation(
                "Her gün daha fazla sevgi ve şükran duygusuyla doluyorum.",
                R.drawable.affirmation_10));

        return affirmations;
    }
}