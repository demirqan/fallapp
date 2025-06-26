package com.falapp.fortunetelle.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.models.FortuneTeller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FortuneTellerAdapter extends RecyclerView.Adapter<FortuneTellerAdapter.FortuneTellerViewHolder> {

    private Context context;
    private List<FortuneTeller> fortuneTellers;
    private FortuneTellerClickListener clickListener;


    private Map<String, String> specialtyEmojiMap;

    public FortuneTellerAdapter(Context context, List<FortuneTeller> fortuneTellers, FortuneTellerClickListener clickListener) {
        this.context = context;
        this.fortuneTellers = fortuneTellers;
        this.clickListener = clickListener;


        initSpecialtyEmojiMap();
    }

    private void initSpecialtyEmojiMap() {
        specialtyEmojiMap = new HashMap<>();
        specialtyEmojiMap.put("aşk", "❤️"); // Love
        specialtyEmojiMap.put("para", "💰"); // Money
        specialtyEmojiMap.put("sağlık", "🏥"); // Health
        specialtyEmojiMap.put("arkadaş", "👥"); // Friendship
        specialtyEmojiMap.put("aile", "👨‍👩‍👧‍👦"); // Family
        specialtyEmojiMap.put("kariyer", "💼"); // Career
    }

    @NonNull
    @Override
    public FortuneTellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fortune_teller, parent, false);
        return new FortuneTellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FortuneTellerViewHolder holder, int position) {
        FortuneTeller fortuneTeller = fortuneTellers.get(position);


        holder.tvName.setText(fortuneTeller.getName());


        holder.tvOnlineStatus.setText(fortuneTeller.isOnline() ? "Online" : "Offline");
        holder.tvOnlineStatus.setTextColor(context.getResources().getColor(
                fortuneTeller.isOnline() ? R.color.online_color : R.color.offline_color));


        holder.tvPrice.setText(String.valueOf(fortuneTeller.getPrice()));


        holder.rbRating.setRating(fortuneTeller.getRating());


        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier(
                fortuneTeller.getIconResource(), "drawable", context.getPackageName());
        if (resourceId != 0) {
            holder.ivProfile.setImageResource(resourceId);
        }


        StringBuilder specialtiesBuilder = new StringBuilder();
        String[] specialties = fortuneTeller.getSpecialtiesArray();
        for (String specialty : specialties) {
            if (specialtiesBuilder.length() > 0) {
                specialtiesBuilder.append("  ");
            }
            String emoji = specialtyEmojiMap.get(specialty);
            specialtiesBuilder.append(emoji != null ? emoji : "");
            specialtiesBuilder.append(" ").append(specialty);
        }
        holder.tvSpecialties.setText(specialtiesBuilder.toString());


        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFortuneTellerClicked(fortuneTeller);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fortuneTellers.size();
    }

    public static class FortuneTellerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;
        TextView tvOnlineStatus;
        TextView tvPrice;
        TextView tvSpecialties;
        RatingBar rbRating;

        public FortuneTellerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvOnlineStatus = itemView.findViewById(R.id.tv_online_status);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvSpecialties = itemView.findViewById(R.id.tv_specialties);
            rbRating = itemView.findViewById(R.id.rb_rating);
        }
    }

    public interface FortuneTellerClickListener {
        void onFortuneTellerClicked(FortuneTeller fortuneTeller);
    }
}