package com.falapp.fortunetelle;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.FortuneTeller;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.falapp.fortunetelle.utils.NotificationHelper;

public class ReadingNotificationService extends Service {

    private static final String READING_AVAILABLE_ACTION = "com.falapp.fortunetelle.READING_AVAILABLE";

    private DatabaseHelper dbHelper;
    private ChatGPTService chatGPTService;
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
        chatGPTService = new ChatGPTService();
        notificationHelper = new NotificationHelper(this);
        notificationHelper.createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int readingId = intent.getIntExtra("reading_id", -1);
            if (readingId != -1) {
                // Process reading in background thread
                new Thread(() -> processReading(readingId)).start();
            }
        }
        return START_NOT_STICKY;
    }

    private void processReading(int readingId) {
        // Get reading from database
        FortuneReading reading = dbHelper.getReading(readingId);
        if (reading == null) {
            stopSelf();
            return;
        }

        // Get fortune teller
        FortuneTeller fortuneTeller = dbHelper.getFortuneTeller(reading.getFortuneTellerId());
        if (fortuneTeller == null) {
            stopSelf();
            return;
        }

        // Wait until available time
        long currentTime = System.currentTimeMillis();
        long waitTime = reading.getAvailableTime() - currentTime;

        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Create prompt for ChatGPT
        String prompt = createPrompt(reading, fortuneTeller);

        // Get result from ChatGPT
        String result = chatGPTService.getResponse(prompt);

        // Update reading in database
        reading.setResult(result);
        reading.setAvailable(true);
        dbHelper.updateReadingResult(readingId, result, true);

        // Show notification
        showReadingReadyNotification(reading);

        // Broadcast that reading is available
        Intent broadcastIntent = new Intent(READING_AVAILABLE_ACTION);
        broadcastIntent.putExtra("reading_id", readingId);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        // Stop service
        stopSelf();
    }

    private String createPrompt(FortuneReading reading, FortuneTeller fortuneTeller) {
        StringBuilder promptBuilder = new StringBuilder();

        // Add role
        promptBuilder.append("Sen ").append(fortuneTeller.getName())
                .append(" adında profesyonel bir falcısın ve ");

        // Add reading type
        switch (reading.getType()) {
            case "coffee":
                promptBuilder.append("kahve falı");
                break;
            case "tarot":
                promptBuilder.append("tarot falı");
                break;
            case "palm":
                promptBuilder.append("el falı");
                break;
            case "face":
                promptBuilder.append("yüz falı");
                break;
        }

        promptBuilder.append(" bakıyorsun.\n\n");

        // Add user details
        promptBuilder.append("Kişi hakkında bilgiler:\n");
        promptBuilder.append("- İsim: ").append(reading.getUserName()).append("\n");
        promptBuilder.append("- Doğum Tarihi: ").append(reading.getBirthDate()).append("\n");
        promptBuilder.append("- İlişki Durumu: ").append(reading.getRelationshipStatus()).append("\n");
        promptBuilder.append("- İş Durumu: ").append(reading.getJobStatus()).append("\n\n");

        // Add selected topics
        promptBuilder.append("Kişinin merak ettiği konular: ").append(reading.getTopics()).append("\n\n");

        // Add photo information (for face and palm readings)
        if (reading.getType().equals("face") || reading.getType().equals("palm")) {
            promptBuilder.append("Kişinin ").append(reading.getType().equals("face") ? "yüz" : "el")
                    .append(" fotoğrafı var ve bu fotoğrafa göre fal bakıyorsun.\n\n");
        }

        // Add specific instructions
        promptBuilder.append("Lütfen bu kişiye detaylı bir fal bak ve belirtilen konularda (")
                .append(reading.getTopics())
                .append(") gelecek hakkında bilgiler ver. Olumlu ve olumsuz yönleri dengeli bir şekilde anlat. ")
                .append("Fal sonucunu 3-4 paragraf şeklinde yaz ve son olarak kişiye şans getirecek bir renk, ")
                .append("şanslı gün ve şanslı sayı öner.");

        return promptBuilder.toString();
    }

    private void showReadingReadyNotification(FortuneReading reading) {
        // Get fortune teller
        FortuneTeller fortuneTeller = dbHelper.getFortuneTeller(reading.getFortuneTellerId());
        String fortuneTellerName = fortuneTeller != null ? fortuneTeller.getName() : "Falcı";

        // Show notification
        notificationHelper.showReadingReadyNotification(
                reading.getId(),
                reading.getReadableType(),
                fortuneTellerName);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}