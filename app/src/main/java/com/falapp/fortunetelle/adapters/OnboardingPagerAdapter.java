package com.falapp.fortunetelle.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.models.OnboardingPage;

import java.util.List;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingViewHolder> {

    private Context context;
    private List<OnboardingPage> pages;

    public OnboardingPagerAdapter(Context context, List<OnboardingPage> pages) {
        this.context = context;
        this.pages = pages;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_onboarding, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingPage page = pages.get(position);


        holder.ivImage.setImageResource(page.getImageResourceId());
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    public static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
        }
    }
}