package com.falapp.fortunetelle.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.falapp.fortunetelle.MainActivity;
import com.falapp.fortunetelle.R;
import com.falapp.fortunetelle.ReadingResultActivity;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    private static final String CHANNEL_ID_READINGS = "falapp_readings";
    private static final String CHANNEL_ID_GENERAL = "falapp_general";
    private static final String CHANNEL_ID_PROMO = "falapp_promo";

    private static final String CHANNEL_NAME_READINGS = "Fal Bildirimleri";
    private static final String CHANNEL_NAME_GENERAL = "Genel Bildirimler";
    private static final String CHANNEL_NAME_PROMO = "Kampanya Bildirimleri";

    private static final String CHANNEL_DESC_READINGS = "Fal sonuçları ve yeni fallar hakkında bildirimler";
    private static final String CHANNEL_DESC_GENERAL = "Uygulama güncelleme ve genel bilgilendirme bildirimleri";
    private static final String CHANNEL_DESC_PROMO = "Kampanya ve promosyon bildirimleri";

    private static final int NOTIFICATION_ID_READING_READY = 1001;
    private static final int NOTIFICATION_ID_DAILY_HOROSCOPE = 1002;
    private static final int NOTIFICATION_ID_DAILY_BONUS = 1003;
    private static final int NOTIFICATION_ID_PROMO = 2001;

    private Context context;
    private NotificationManagerCompat notificationManager;
    private Map<String, Boolean> channelCreated;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.channelCreated = new HashMap<>();
    }

    /**
     * Bildirim kanallarını oluştur - Android 8.0+ için gerekli
     */
    public void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Fal sonuçları bildirimleri kanalı
            createChannel(CHANNEL_ID_READINGS, CHANNEL_NAME_READINGS, CHANNEL_DESC_READINGS,
                    NotificationManager.IMPORTANCE_HIGH);

            // Genel bildirimler kanalı
            createChannel(CHANNEL_ID_GENERAL, CHANNEL_NAME_GENERAL, CHANNEL_DESC_GENERAL,
                    NotificationManager.IMPORTANCE_DEFAULT);

            // Promosyon bildirimleri kanalı
            createChannel(CHANNEL_ID_PROMO, CHANNEL_NAME_PROMO, CHANNEL_DESC_PROMO,
                    NotificationManager.IMPORTANCE_LOW);
        }
    }

    /**
     * Tek bir bildirim kanalı oluştur
     */
    private void createChannel(String channelId, String channelName, String channelDesc, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!channelCreated.containsKey(channelId) || !channelCreated.get(channelId)) {
                NotificationChannel channel = new NotificationChannel(
                        channelId, channelName, importance);
                channel.setDescription(channelDesc);

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(channel);

                channelCreated.put(channelId, true);
            }
        }
    }

    /**
     * Fal sonucu hazır bildirimi gönder
     */
    public void showReadingReadyNotification(int readingId, String readingType, String fortuneTellerName) {
        // PendingIntent oluştur - bildirime tıklandığında açılacak ekran
        Intent intent = new Intent(context, ReadingResultActivity.class);
        intent.putExtra("reading_id", readingId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, readingId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Bildirim sesi
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Büyük ikon
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_foreground);

        // Bildirim oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_READINGS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(largeIcon)
                .setContentTitle("Fal Sonucunuz Hazır!")
                .setContentText(fortuneTellerName + " tarafından " + readingType + " sonucunuzu görmek için tıklayın.")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(fortuneTellerName + " tarafından baktırılan " + readingType +
                                " sonucunuz hazır. Hemen görmek için tıklayın."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        // Bildirimi göster
        if (hasNotificationPermission()) {
            notificationManager.notify(NOTIFICATION_ID_READING_READY, builder.build());
        }
    }

    /**
     * Günlük burç bildirimi gönder
     */
    public void showDailyHoroscopeNotification(String zodiacSign) {
        // PendingIntent oluştur - bildirime tıklandığında açılacak ekran
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_horoscope", true);
        intent.putExtra("zodiac_sign", zodiacSign);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Bildirim oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Günlük Burç Yorumunuz")
                .setContentText(zodiacSign + " burcu için günlük yorumunuz hazır!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Bildirimi göster
        if (hasNotificationPermission()) {
            notificationManager.notify(NOTIFICATION_ID_DAILY_HOROSCOPE, builder.build());
        }
    }

    /**
     * Günlük bonus bildirimi gönder
     */
    public void showDailyBonusNotification() {
        // PendingIntent oluştur - bildirime tıklandığında açılacak ekran
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_free_coins", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Bildirim oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Günlük Bonusunuz Hazır!")
                .setContentText("Günlük bonusunuzu almak için tıklayın.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Bildirimi göster
        if (hasNotificationPermission()) {
            notificationManager.notify(NOTIFICATION_ID_DAILY_BONUS, builder.build());
        }
    }

    /**
     * Promosyon bildirimi gönder
     */
    public void showPromotionNotification(String title, String message) {
        // PendingIntent oluştur - bildirime tıklandığında açılacak ekran
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_premium", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Bildirim oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_PROMO)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Bildirimi göster
        if (hasNotificationPermission()) {
            notificationManager.notify(NOTIFICATION_ID_PROMO, builder.build());
        }
    }

    /**
     * Özel bildirim gönder
     */
    public void showCustomNotification(int notificationId, String channelId, String title,
                                       String message, Intent intent, int smallIcon) {
        // PendingIntent oluştur - bildirime tıklandığında açılacak ekran
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(
                    context, notificationId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        // Bildirim oluştur
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(smallIcon > 0 ? smallIcon : R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }

        // Bildirimi göster
        if (hasNotificationPermission()) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    /**
     * Bildirimi iptal et
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    /**
     * Tüm bildirimleri iptal et
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * Bildirim izni kontrolü
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return true; // Android 13'ten önceki sürümlerde izin gerekmez
    }
}