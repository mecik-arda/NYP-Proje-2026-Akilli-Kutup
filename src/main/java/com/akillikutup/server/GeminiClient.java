package com.akillikutup.server;

import com.akillikutup.core.ConfigManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiClient {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String askQuestion(String prompt) {
        String apiKey = ConfigManager.getGeminiApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            return "API Anahtari bulunamadi veya deşifre edilemedi.";
        }

        try {
            // Sistemsel kisitlama ekleyelim:
            String modifiedPrompt = prompt + "\\n\\n(Lütfen cevabını çok kısa, öz ve anlaşılır tut. En fazla 2-3 cümle veya maddeler kullan. Gereksiz detaylardan kaçın.)";
            // Basit JSON escape işlemi
            String escapedPrompt = modifiedPrompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
            
            String jsonBody = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"parts\":[{\"text\": \"" + escapedPrompt + "\"}]\n" +
                    "  }]\n" +
                    "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseGeminiResponse(response.body());
            } else {
                return "Gemini API Hatasi: " + response.statusCode() + " - " + response.body();
            }

        } catch (Exception e) {
            System.err.println("GeminiClient hatasi: " + e.getMessage());
            return "Istek sirasinda bir hata olustu: " + e.getMessage();
        }
    }

    private static String parseGeminiResponse(String json) {
        // Kutuphanesiz basit JSON parse
        // Hedeflenen alan: "text": "Cevap..."
        String targetKey = "\"text\":";
        int startIdx = json.indexOf(targetKey);
        if (startIdx != -1) {
            startIdx += targetKey.length();
            int firstQuote = json.indexOf("\"", startIdx);
            
            // "text" iceriginin nerede bittigini bulmamiz gerekiyor. Normalde sondan bir onceki cift tirnaktir.
            // Fakat metin icinde \" olabilir. 
            // Basit bir sekilde aramak icin RegExp kullanabiliriz veya indexof ile ilerleyebiliriz.
            if (firstQuote != -1) {
                StringBuilder sb = new StringBuilder();
                boolean escaped = false;
                for (int i = firstQuote + 1; i < json.length(); i++) {
                    char c = json.charAt(i);
                    if (c == '\\' && !escaped) {
                        escaped = true;
                        continue; // Bir sonraki karakteri oldugu gibi alacagiz
                    }
                    if (c == '"' && !escaped) {
                        break; // Metin bitti
                    }
                    if (escaped) {
                        if (c == 'n') sb.append('\n');
                        else if (c == 't') sb.append('\t');
                        else if (c == '"') sb.append('"');
                        else if (c == '\\') sb.append('\\');
                        else sb.append(c);
                        escaped = false;
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
        return "Cevap anlasilamadi.";
    }
}
