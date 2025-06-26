package com.falapp.fortunetelle.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.falapp.fortunetelle.CoinPurchaseActivity;
import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.TarotDetailActivity;
import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TarotFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private ImageView[] tarotCards;
    private TextView tvInterpretation;
    private Button btnReset;
    private View progressBar;
    private Button btnFullReading;

    private static final int CARD_COUNT = 6;
    private static final int TAROT_COST = 10; // Coins required for tarot reading

    private List<Integer> selectedCardIndices = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tarot, container, false);


        dbHelper = new DatabaseHelper(getContext());
        chatGPTService = new ChatGPTService();


        currentUser = dbHelper.getUser();


        initViews(view);


        setupTarotCards();


        btnReset.setOnClickListener(v -> resetCards());


        btnFullReading.setOnClickListener(v -> openFullReading());

        return view;
    }

    private void initViews(View view) {
        tvInterpretation = view.findViewById(R.id.tv_interpretation);
        btnReset = view.findViewById(R.id.btn_reset);
        progressBar = view.findViewById(R.id.progress_bar);
        btnFullReading = view.findViewById(R.id.btn_full_reading);


        tarotCards = new ImageView[CARD_COUNT];
        for (int i = 0; i < CARD_COUNT; i++) {
            int resId = getResources().getIdentifier("iv_tarot_card_" + (i + 1), "id", getContext().getPackageName());
            tarotCards[i] = view.findViewById(resId);
        }
    }

    private void setupTarotCards() {

        for (int i = 0; i < CARD_COUNT; i++) {
            tarotCards[i].setImageResource(R.drawable.cardback);
            final int cardIndex = i;

            tarotCards[i].setOnClickListener(v -> onCardClicked(cardIndex));
        }
    }

    private void onCardClicked(int cardIndex) {

        if (selectedCardIndices.contains(cardIndex)) {
            return;
        }


        if (selectedCardIndices.isEmpty() && currentUser.getCoins() < TAROT_COST) {
            Toast.makeText(getContext(), "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), CoinPurchaseActivity.class));
            return;
        }


        if (selectedCardIndices.isEmpty()) {
            currentUser.removeCoins(TAROT_COST);
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
        }

        // Add card to selected cards
        selectedCardIndices.add(cardIndex);

        // Show a random tarot card image
        int[] cardResourceIds = {
                R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician,
                R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician,
                R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician,
                R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician,
                R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician,
                R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician,
                R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician, R.drawable.tarot_magician
        };

        int randomCardIndex = new Random().nextInt(cardResourceIds.length);
        tarotCards[cardIndex].setImageResource(cardResourceIds[randomCardIndex]);


        if (!selectedCardIndices.isEmpty()) {
            getQuickInterpretation();


            if (selectedCardIndices.size() >= 3) {
                btnFullReading.setVisibility(View.VISIBLE);
            }
        }
    }

    private void getQuickInterpretation() {

        progressBar.setVisibility(View.VISIBLE);
        tvInterpretation.setVisibility(View.GONE);


        String interpretation;
        switch (selectedCardIndices.size()) {
            case 1:
                interpretation = "Tek kart seçtiniz. Bu kart şu anki ruh halinizi ve mevcut durumunuzu temsil ediyor. Derinlemesine bir yorum için daha fazla kart seçebilirsiniz.";
                break;
            case 2:
                interpretation = "İki kart seçtiniz. İlk kart mevcut durumunuzu, ikinci kart yakın geleceğinizde karşılaşacağınız etkileri gösteriyor. Tam bir yorum için en az 3 kart seçmelisiniz.";
                break;
            case 3:
                interpretation = "Üç kartlık bir seçim yaptınız. Bu kartlar geçmiş, şimdi ve geleceğinizi temsil eder. Tam bir yorum için 'Detaylı Fal' butonuna tıklayın.";
                break;
            default:
                interpretation = "Birden fazla kart seçtiniz. Bu kartlar hayatınızın farklı yönlerini temsil ediyor. Kapsamlı bir yorum için 'Detaylı Fal' butonuna tıklayın.";
                break;
        }


        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {

                progressBar.setVisibility(View.GONE);


                tvInterpretation.setText(interpretation);
                tvInterpretation.setVisibility(View.VISIBLE);


                btnReset.setVisibility(View.VISIBLE);
            });
        }
    }

    private void resetCards() {

        selectedCardIndices.clear();


        for (int i = 0; i < CARD_COUNT; i++) {
            tarotCards[i].setImageResource(R.drawable.cardback);
        }


        tvInterpretation.setText("");
        tvInterpretation.setVisibility(View.GONE);


        btnReset.setVisibility(View.GONE);


        btnFullReading.setVisibility(View.GONE);
    }

    private void openFullReading() {
        // Navigate to detailed tarot reading activity
        Intent intent = new Intent(getActivity(), TarotDetailActivity.class);
        startActivity(intent);
    }
}