package com.falapp.fortunetelle.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.falapp.fortunetelle.CoinPurchaseActivity;
import com.falapp.fortunetelle.HoroscopeDetailActivity;
import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.google.android.material.tabs.TabLayout;

public class HoroscopeFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private TabLayout tabLayout;
    private ImageView ivZodiacSign;
    private TextView tvZodiacSign;
    private TextView tvHoroscope;
    private View btnContinueReading;
    private View cardZodiacSign;
    private View progressBar;

    private String[] zodiacSigns = {"Koç", "Boğa", "İkizler", "Yengeç", "Aslan", "Başak",
            "Terazi", "Akrep", "Yay", "Oğlak", "Kova", "Balık"};
    private String[] periods = {"Günlük", "Haftalık", "Aylık"};

    private String currentZodiacSign = "Koç"; // Default sign
    private String currentPeriod = "Günlük"; // Default period
    private static final int HOROSCOPE_COST = 10; // Coins required for horoscope reading

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horoscope, container, false);


        dbHelper = new DatabaseHelper(getContext());
        chatGPTService = new ChatGPTService();


        currentUser = dbHelper.getUser();


        initViews(view);


        setupTabLayout();


        setupZodiacSignSelection();


        btnContinueReading.setOnClickListener(v -> openDetailedHoroscope());


        getHoroscope();

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        ivZodiacSign = view.findViewById(R.id.iv_zodiac_sign);
        tvZodiacSign = view.findViewById(R.id.tv_zodiac_sign);
        tvHoroscope = view.findViewById(R.id.tv_horoscope);
        btnContinueReading = view.findViewById(R.id.btn_continue_reading);
        cardZodiacSign = view.findViewById(R.id.card_zodiac_sign);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupTabLayout() {

        for (String period : periods) {
            tabLayout.addTab(tabLayout.newTab().setText(period));
        }


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentPeriod = periods[tab.getPosition()];
                getHoroscope();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
    }

    private void setupZodiacSignSelection() {
        tvZodiacSign.setText(currentZodiacSign);
        setZodiacImage(currentZodiacSign);

        cardZodiacSign.setOnClickListener(v -> showZodiacSignSelectionDialog());
    }

    private void showZodiacSignSelectionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Burç Seçin");
        builder.setItems(zodiacSigns, (dialog, which) -> {
            currentZodiacSign = zodiacSigns[which];
            tvZodiacSign.setText(currentZodiacSign);
            setZodiacImage(currentZodiacSign);
            getHoroscope();
        });
        builder.show();
    }

    private void setZodiacImage(String zodiacSign) {
        int resourceId = 0;
        switch (zodiacSign) {
            case "Koç":
                resourceId = R.drawable.zodiac_aries;
                break;
            case "Boğa":
                resourceId = R.drawable.zodiac_taurus;
                break;
            case "İkizler":
                resourceId = R.drawable.zodiac_gemini;
                break;
            case "Yengeç":
                resourceId = R.drawable.zodiac_cancer;
                break;
            case "Aslan":
                resourceId = R.drawable.zodiac_leo;
                break;
            case "Başak":
                resourceId = R.drawable.zodiac_virgo;
                break;
            case "Terazi":
                resourceId = R.drawable.zodiac_libra;
                break;
            case "Akrep":
                resourceId = R.drawable.zodiac_scorpio;
                break;
            case "Yay":
                resourceId = R.drawable.zodiac_sagittarius;
                break;
            case "Oğlak":
                resourceId = R.drawable.zodiac_capricorn;
                break;
            case "Kova":
                resourceId = R.drawable.zodiac_aquarius;
                break;
            case "Balık":
                resourceId = R.drawable.zodiac_pisces;
                break;
        }

        if (resourceId != 0) {
            ivZodiacSign.setImageResource(resourceId);
        }
    }

    private void getHoroscope() {

        if (currentUser.getCoins() < HOROSCOPE_COST) {
            Toast.makeText(getContext(), "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), CoinPurchaseActivity.class));
            return;
        }


        currentUser.removeCoins(HOROSCOPE_COST);
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());


        progressBar.setVisibility(View.VISIBLE);
        tvHoroscope.setVisibility(View.GONE);
        btnContinueReading.setVisibility(View.GONE);


        new Thread(() -> {
            final String horoscope = chatGPTService.getHoroscopeReading(currentZodiacSign, currentPeriod);


            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Hide progress bar
                    progressBar.setVisibility(View.GONE);

                    // Show horoscope and button
                    String shortHoroscope = getShortHoroscope(horoscope);
                    tvHoroscope.setText(shortHoroscope);
                    tvHoroscope.setVisibility(View.VISIBLE);
                    btnContinueReading.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private String getShortHoroscope(String fullHoroscope) {

        int endIndex = fullHoroscope.indexOf("\n\n");
        if (endIndex != -1) {
            return fullHoroscope.substring(0, endIndex) + "...";
        } else if (fullHoroscope.length() > 200) {
            return fullHoroscope.substring(0, 200) + "...";
        } else {
            return fullHoroscope;
        }
    }

    private void openDetailedHoroscope() {
        Intent intent = new Intent(getActivity(), HoroscopeDetailActivity.class);
        intent.putExtra("zodiac_sign", currentZodiacSign);
        intent.putExtra("period", currentPeriod);
        startActivity(intent);
    }
}