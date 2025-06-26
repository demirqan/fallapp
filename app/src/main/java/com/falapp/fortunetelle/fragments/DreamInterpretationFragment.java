package com.falapp.fortunetelle.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.falapp.fortunetelle.CoinPurchaseActivity;
import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;

public class DreamInterpretationFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private User currentUser;
    private ChatGPTService chatGPTService;

    private EditText etDream;
    private Button btnInterpret;
    private TextView tvInterpretation;
    private ImageView ivDream;
    private View progressBar;

    private static final int INTERPRETATION_COST = 10; // Coins required for dream interpretation

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dream_interpretation, container, false);


        dbHelper = new DatabaseHelper(getContext());
        chatGPTService = new ChatGPTService();


        currentUser = dbHelper.getUser();


        initViews(view);


        btnInterpret.setOnClickListener(v -> validateAndInterpretDream());

        return view;
    }

    private void initViews(View view) {
        etDream = view.findViewById(R.id.et_dream);
        btnInterpret = view.findViewById(R.id.btn_interpret);
        tvInterpretation = view.findViewById(R.id.tv_interpretation);
        ivDream = view.findViewById(R.id.iv_dream);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void validateAndInterpretDream() {

        String dreamText = etDream.getText().toString().trim();


        if (dreamText.isEmpty()) {
            etDream.setError("Lütfen rüyanızı anlatın");
            etDream.requestFocus();
            return;
        }


        if (currentUser.getCoins() < INTERPRETATION_COST) {
            Toast.makeText(getContext(), "Yeterli altınınız yok. Lütfen altın satın alın.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), CoinPurchaseActivity.class));
            return;
        }


        currentUser.removeCoins(INTERPRETATION_COST);
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());


        progressBar.setVisibility(View.VISIBLE);
        btnInterpret.setEnabled(false);
        tvInterpretation.setVisibility(View.GONE);


        new Thread(() -> {
            final String interpretation = chatGPTService.getDreamInterpretation(dreamText);


            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {

                    progressBar.setVisibility(View.GONE);
                    btnInterpret.setEnabled(true);


                    tvInterpretation.setText(interpretation);
                    tvInterpretation.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }
}