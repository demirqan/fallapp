package com.falapp.fortunetelle.adapters;

import android.content.Context;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.models.TarotCard;

import java.util.List;

public class TarotCardsPagerAdapter extends RecyclerView.Adapter<TarotCardsPagerAdapter.TarotCardViewHolder> {

    private Context context;
    private List<TarotCard> cards;

    public TarotCardsPagerAdapter(Context context, List<TarotCard> cards) {
        this.context = context;
        this.cards = cards;
    }

    @NonNull
    @Override
    public TarotCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarot_card, parent, false);
        return new TarotCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarotCardViewHolder holder, int position) {
        TarotCard card = cards.get(position);


        holder.tvCardName.setText(card.getName() + (card.isReversed() ? " (Ters)" : ""));


        holder.tvCardMeaning.setText(card.getAppropriateMeaning());


        holder.ivCardImage.setImageResource(card.getImageResourceId());


        if (card.isReversed()) {

            Matrix matrix = new Matrix();
            matrix.postRotate(180);


            holder.ivCardImage.setScaleType(ImageView.ScaleType.MATRIX);
            holder.ivCardImage.setImageMatrix(matrix);
        } else {

            holder.ivCardImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public static class TarotCardViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCardImage;
        TextView tvCardName;
        TextView tvCardMeaning;

        public TarotCardViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCardImage = itemView.findViewById(R.id.iv_card_image);
            tvCardName = itemView.findViewById(R.id.tv_card_name);
            tvCardMeaning = itemView.findViewById(R.id.tv_card_meaning);
        }
    }
}