package com.falapp.fortunetelle.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.FortuneTeller;
import com.falapp.fortunetelle.utils.DatabaseHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadingsAdapter extends RecyclerView.Adapter<ReadingsAdapter.ReadingViewHolder> {

    private Context context;
    private List<FortuneReading> readings;
    private DatabaseHelper dbHelper;
    private ReadingClickListener clickListener;
    private Map<Integer, CountDownTimer> countDownTimers;

    public ReadingsAdapter(Context context, List<FortuneReading> readings,
                           DatabaseHelper dbHelper, ReadingClickListener clickListener) {
        this.context = context;
        this.readings = readings;
        this.dbHelper = dbHelper;
        this.clickListener = clickListener;
        this.countDownTimers = new HashMap<>();
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reading, parent, false);
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        FortuneReading reading = readings.get(position);
        FortuneTeller fortuneTeller = dbHelper.getFortuneTeller(reading.getFortuneTellerId());


        holder.tvReadingType.setText(reading.getReadableType());


        if (fortuneTeller != null) {
            holder.tvFortuneTellerName.setText(fortuneTeller.getName());


            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier(
                    fortuneTeller.getIconResource(), "drawable", context.getPackageName());
            if (resourceId != 0) {
                holder.ivFortuneTellerIcon.setImageResource(resourceId);
            }
        }


        holder.tvUserName.setText(reading.getUserName());


        String readingType = reading.getType();
        int readingIconId = 0;
        switch (readingType) {
            case "coffee":
                readingIconId = R.drawable.kahvefali;
                break;
            case "tarot":
                readingIconId = R.drawable.tarotfali;
                break;
            case "palm":
                readingIconId = R.drawable.elfali;
                break;
            case "face":
                readingIconId = R.drawable.facehal;
                break;
        }

        if (readingIconId != 0) {
            holder.ivReadingIcon.setImageResource(readingIconId);
        }


        if (reading.isReadyToView()) {
            holder.tvCountdown.setText("Hazır");
            holder.tvCountdown.setTextColor(context.getResources().getColor(R.color.green));


            if (countDownTimers.containsKey(reading.getId())) {
                countDownTimers.get(reading.getId()).cancel();
                countDownTimers.remove(reading.getId());
            }
        } else {

            startCountdown(holder.tvCountdown, reading);
        }


        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onReadingClicked(reading);
            }
        });
    }

    private void startCountdown(TextView tvCountdown, FortuneReading reading) {

        if (countDownTimers.containsKey(reading.getId())) {
            countDownTimers.get(reading.getId()).cancel();
        }

        long remainingTime = reading.getRemainingTime();

        CountDownTimer timer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;

                tvCountdown.setText(String.format("%d:%02d", minutes, seconds));
                tvCountdown.setTextColor(context.getResources().getColor(R.color.orange));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Hazır");
                tvCountdown.setTextColor(context.getResources().getColor(R.color.green));


                reading.setAvailable(true);
                dbHelper.updateReadingResult(reading.getId(), reading.getResult(), true);


                countDownTimers.remove(reading.getId());
            }
        };

        timer.start();
        countDownTimers.put(reading.getId(), timer);
    }

    @Override
    public void onViewRecycled(@NonNull ReadingViewHolder holder) {
        super.onViewRecycled(holder);


        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            FortuneReading reading = readings.get(position);


            if (countDownTimers.containsKey(reading.getId())) {
                countDownTimers.get(reading.getId()).cancel();
            }
        }
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    public void updateReadings(List<FortuneReading> newReadings) {
        this.readings = newReadings;
        notifyDataSetChanged();
    }

    public static class ReadingViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivReadingIcon;
        ImageView ivFortuneTellerIcon;
        TextView tvReadingType;
        TextView tvFortuneTellerName;
        TextView tvUserName;
        TextView tvCountdown;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            ivReadingIcon = itemView.findViewById(R.id.iv_reading_icon);
            ivFortuneTellerIcon = itemView.findViewById(R.id.iv_fortune_teller_icon);
            tvReadingType = itemView.findViewById(R.id.tv_reading_type);
            tvFortuneTellerName = itemView.findViewById(R.id.tv_fortune_teller_name);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvCountdown = itemView.findViewById(R.id.tv_countdown);
        }
    }

    public interface ReadingClickListener {
        void onReadingClicked(FortuneReading reading);
    }
}