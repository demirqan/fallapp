package com.falapp.falciabla.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FalLimitManager {

    private static final String PREFS_NAME = "app_prefs";
    private static final String DATE_KEY = "premium_fal_tarihi";
    private static final String COUNT_KEY = "premium_fal_sayisi";
    private static final int MAX_DAILY_LIMIT = 10;

    private static String getToday() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return formatter.format(new Date());
    }

    public static boolean canUsePremiumFal(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String today = getToday();
        String savedDay = prefs.getString(DATE_KEY, "");
        int count = prefs.getInt(COUNT_KEY, 0);

        if (!today.equals(savedDay)) {
            prefs.edit()
                    .putString(DATE_KEY, today)
                    .putInt(COUNT_KEY, 0)
                    .apply();
            return true;
        }

        return count < MAX_DAILY_LIMIT;
    }

    public static void increasePremiumFalCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(COUNT_KEY, 0);
        prefs.edit().putInt(COUNT_KEY, count + 1).apply();
    }

    public static int getRemainingFalCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = getToday();
        String savedDay = prefs.getString(DATE_KEY, "");
        int count = prefs.getInt(COUNT_KEY, 0);

        if (!today.equals(savedDay)) {
            return MAX_DAILY_LIMIT;
        }

        return Math.max(0, MAX_DAILY_LIMIT - count);
    }

    public static int getUsedFalCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = getToday();
        String savedDay = prefs.getString(DATE_KEY, "");

        if (!today.equals(savedDay)) {
            return 0;
        }

        return prefs.getInt(COUNT_KEY, 0);
    }
}
