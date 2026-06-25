package com.akillikutup.chat;

import com.akillikutup.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GeminiClient {
    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String[] MODELS = {
        "gemini-2.0-flash",
        "gemini-1.5-pro-latest",
        "gemini-1.5-flash-latest",
        "gemini-pro"
    };

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    public static String askQuestion(String prompt, String userApiKey) {
        String apiKey = userApiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = ConfigManager.getGeminiApiKey();
        }
        if (apiKey == null || apiKey.isEmpty() || "YOUR_GEMINI_API_KEY_HERE".equals(apiKey) || apiKey.startsWith("YOUR_GEMINI")) {
            return "API_KEY_ERROR: Lutfen gecerli bir Gemini API Anahtari ayarlayin.";
        }

        try {
            JsonObject requestBody = new JsonObject();
            JsonArray contentsArray = new JsonArray();
            JsonObject contentsObj = new JsonObject();
            JsonArray partsArray = new JsonArray();
            JsonObject partObj = new JsonObject();

            partObj.addProperty("text", prompt);
            partsArray.add(partObj);
            contentsObj.add("parts", partsArray);
            contentsArray.add(contentsObj);
            requestBody.add("contents", contentsArray);

            String requestJson = gson.toJson(requestBody);
            String lastError = "API_ERROR";

            for (String model : MODELS) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + model + ":generateContent"))
                        .header("Content-Type", "application/json")
                        .header("x-goog-api-key", apiKey)
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseGeminiResponse(response.body());
                } else {
                    // Güvenlik: API yanıt gövdesi hassas bilgi içerebileceğinden log'a yaz, kullanıcıya generic dön
                    System.err.println("Gemini API hatası [" + model + "]: HTTP " + response.statusCode()
                        + " - " + response.body());
                    lastError = "API_ERROR: HTTP " + response.statusCode() + " (" + model + ")";
                    if (response.statusCode() == 400 && response.body().contains("API_KEY_INVALID")) {
                        break;
                    }
                }
            }
            return lastError;
        } catch (Exception e) {
            return "NETWORK_ERROR";
        }
    }

    private static String parseGeminiResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            if (jsonObject.has("error")) {
                JsonObject errorObj = jsonObject.getAsJsonObject("error");
                return "API_ERROR: " + (errorObj.has("message") ? errorObj.get("message").getAsString() : "Bilinmeyen hata");
            }
            return jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            return "PARSE_ERROR: API yaniti islenemedi.";
        }
    }

    public static String analyzeBookCover(String base64Image, String mimeType, String userApiKey) {
        String apiKey = resolveApiKey(userApiKey);
        if (apiKey == null) return "API_KEY_ERROR: Lutfen gecerli bir Gemini API Anahtari ayarlayin.";

        try {
            JsonObject requestBody = new JsonObject();
            JsonArray contentsArray = new JsonArray();
            JsonObject contentsObj = new JsonObject();
            JsonArray partsArray = new JsonArray();

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text",
                "Bu bir kitap kapağı fotoğrafıdır. Lütfen şu bilgileri JSON formatında çıkar: " +
                "{\"baslik\": \"Kitap Adı\", \"yazar\": \"Yazar Adı\", \"isbn\": \"ISBN numarası (görünüyorsa)\", " +
                "\"kategori\": \"Tahmini kategori (Roman, Bilim, Tarih, vb.)\"}. " +
                "Sadece JSON döndür, başka açıklama ekleme.");
            partsArray.add(textPart);

            JsonObject imagePart = new JsonObject();
            JsonObject inlineData = new JsonObject();
            inlineData.addProperty("mime_type", mimeType != null ? mimeType : "image/jpeg");
            inlineData.addProperty("data", base64Image);
            imagePart.add("inline_data", inlineData);
            partsArray.add(imagePart);

            contentsObj.add("parts", partsArray);
            contentsArray.add(contentsObj);
            requestBody.add("contents", contentsArray);

            String requestJson = gson.toJson(requestBody);
            String result = tryModels(requestJson, apiKey, "gemini-2.0-flash-exp", "gemini-1.5-flash-latest");

            if (result != null && !result.startsWith("API_ERROR") && !result.startsWith("NETWORK_ERROR")) {
                String cleaned = result.trim();
                if (cleaned.startsWith("```json")) {
                    cleaned = cleaned.substring(7);
                }
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.substring(3);
                }
                if (cleaned.endsWith("```")) {
                    cleaned = cleaned.substring(0, cleaned.length() - 3);
                }
                return cleaned.trim();
            }
            return "{\"error\": \"" + (result != null ? result : "Bilinmeyen hata") + "\"}";

        } catch (Exception e) {
            return "{\"error\": \"NETWORK_ERROR\"}";
        }
    }

    public static float[] generateEmbedding(String text, String userApiKey) {
        String apiKey = resolveApiKey(userApiKey);
        if (apiKey == null) return null;

        try {
            String model = "text-embedding-004";
            JsonObject requestBody = new JsonObject();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", text);
            parts.add(part);
            content.add("parts", parts);
            requestBody.add("content", content);

            String requestJson = gson.toJson(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + model + ":embedContent"))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseEmbeddingResponse(response.body());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) return 0.0;
        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static String resolveApiKey(String userApiKey) {
        String apiKey = userApiKey;
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = ConfigManager.getGeminiApiKey();
        }
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("YOUR_GEMINI")) {
            return null;
        }
        return apiKey;
    }

    private static String tryModels(String requestJson, String apiKey, String... models) {
        String lastError = "API_ERROR";
        String[] modelsToTry = models.length > 0 ? models : MODELS;

        for (String model : modelsToTry) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + model + ":generateContent"))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseGeminiResponse(response.body());
                } else {
                    lastError = "API_ERROR: HTTP " + response.statusCode() + " (" + model + ")";
                    if (response.statusCode() == 400 && response.body().contains("API_KEY_INVALID")) {
                        break;
                    }
                }
            } catch (Exception e) {
                lastError = "NETWORK_ERROR";
            }
        }
        return lastError;
    }

    private static float[] parseEmbeddingResponse(String jsonResponse) {
        try {
            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray values = json.getAsJsonObject("embedding")
                .getAsJsonArray("values");
            float[] embedding = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                embedding[i] = values.get(i).getAsFloat();
            }
            return embedding;
        } catch (Exception e) {
            return null;
        }
    }
}
