package com.falapp.fortunetelle.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTService {

    private static final String TAG = "ChatGPTService";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    public ChatGPTService() {

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String getResponse(String prompt) {
        try {

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");


            JSONArray messagesArray = new JSONArray();

            // Add system message
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Sen profesyonel bir falcısın. Fal bakma konusunda uzmanlaşmış birisin. " +
                    "Cevaplarını Türkçe olarak ver ve fal formatında konuş. Cevapların 3-4 paragraf olsun.");
            messagesArray.put(systemMessage);


            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.put(userMessage);


            requestBody.put("messages", messagesArray);


            requestBody.put("temperature", 0.7);


            requestBody.put("max_tokens", 1000);


            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();


            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();


            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray choices = jsonResponse.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            return content;
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error getting response from ChatGPT: " + e.getMessage());
            return "Üzgünüm, falınızı okurken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.";
        }
    }

    public String getDreamInterpretation(String dream) {
        String prompt = "Ben bir rüya gördüm. Bu rüyamın yorumunu yapar mısın? Rüyam: " + dream;
        return getResponse(prompt);
    }

    public String getHoroscopeReading(String zodiacSign, String period) {
        String prompt = period + " için " + zodiacSign + " burcu yorumunu yapar mısın? Aşk, sağlık ve kariyer konularını kapsamalı.";
        return getResponse(prompt);
    }

    public String getLoveCompatibility(String person1Name, String person1BirthDate,
                                       String person2Name, String person2BirthDate) {
        String prompt = person1Name + " (" + person1BirthDate + ") ve " +
                person2Name + " (" + person2BirthDate + ") arasındaki aşk uyumunu yorumlar mısın? " +
                "Aşk, diyalog ve tutku konularındaki uyumlarını yüzdelik olarak belirt ve " +
                "ilişkilerinin geleceği hakkında bilgi ver.";
        return getResponse(prompt);
    }

    public String getTarotReading(String[] selectedCards) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tarot falı bakar mısın? Seçilen kartlar: ");

        for (int i = 0; i < selectedCards.length; i++) {
            if (i > 0) {
                prompt.append(", ");
            }
            prompt.append(selectedCards[i]);
        }

        return getResponse(prompt.toString());
    }
}