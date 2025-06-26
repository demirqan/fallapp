package com.falapp.fortunetelle.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.models.Affirmation;

import java.util.List;

public class AffirmationsPagerAdapter extends RecyclerView.Adapter<AffirmationsPagerAdapter.AffirmationViewHolder> {

    private Context context;
    private List<Affirmation> affirmations;

    public AffirmationsPagerAdapter(Context context, List<Affirmation> affirmations) {
        this.context = context;
        this.affirmations = affirmations;
    }

    @NonNull
    @Override
    public AffirmationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_affirmation, parent, false);
        return new AffirmationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AffirmationViewHolder holder, int position) {
        Affirmation affirmation = affirmations.get(position);


        holder.tvAffirmation.setText(affirmation.getText());


        holder.ivBackground.setImageResource(affirmation.getImageResourceId());
    }

    @Override
    public int getItemCount() {
        return affirmations.size();
    }

    public static class AffirmationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackground;
        TextView tvAffirmation;

        public AffirmationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackground = itemView.findViewById(R.id.iv_background);
            tvAffirmation = itemView.findViewById(R.id.tv_affirmation);
        }
    }
}