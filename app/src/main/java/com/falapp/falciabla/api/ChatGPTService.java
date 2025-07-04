package com.falapp.falciabla.api;

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
    private static final String API_KEY = ""
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    public ChatGPTService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public String getResponse(String prompt) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("model", "gpt-3.5-turbo");

            JSONArray messagesArray = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Sen profesyonel bir falcısın. Tarot, burç, aşk uyumu ve rüya yorumları yapıyorsun. Cevaplarını Türkçe, sadece yorum formatında ver. Giriş veya 'Elbette' gibi kelimelerle başlama. Her cevap 3-4 paragraf uzunluğunda ve samimi bir falcı üslubunda olsun.");
            messagesArray.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messagesArray.put(userMessage);

            requestBody.put("messages", messagesArray);
            requestBody.put("temperature", 0.8);
            requestBody.put("max_tokens", 700);

        } catch (JSONException e) {
            Log.e(TAG, "JSON hazırlama hatası: " + e.getMessage());
            return "Fal oluşturulurken bir hata oluştu.";
        }

        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(requestBody.toString(), JSON))
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    Log.e(TAG, "Raw response body: " + responseBody);

                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                    return message.getString("content");
                } else {
                    Log.w(TAG, "Yanıt başarısız: " + response.code());
                }

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Deneme " + attempt + " hatası: " + e.getMessage());
            }
        }

        return "Üzgünüm, falınızı okurken bir sorun oluştu. Lütfen daha sonra tekrar deneyin.";
    }

    public String getDreamInterpretation(String dream) {
        String prompt = "Aşağıdaki rüyayı yorumla. Sadece yorum yaz, giriş yapma. En az 3 paragraf olsun. Rüya: " + dream;
        return getResponse(prompt);
    }

    public String getHoroscopeReading(String zodiacSign, String period) {
        String prompt = zodiacSign + " burcu için " + period + " yorumu yap. Sadece yorum ver, giriş cümlesi kurma. Aşk, sağlık ve kariyer alanlarına özel paragraflarla yaz.";
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
        prompt.append("Tarot falı yorumu yap. Lütfen sadece yorum ver, giriş ifadeleri (örneğin: 'Elbette', 'Tabii ki') kullanma. Kartlara göre 3 paragraf halinde anlamlı bir fal yorumu yaz. Kartlar: ");
        for (int i = 0; i < selectedCards.length; i++) {
            if (i > 0) prompt.append(", ");
            prompt.append(selectedCards[i]);
        }
        return getResponse(prompt.toString());
    }
}
