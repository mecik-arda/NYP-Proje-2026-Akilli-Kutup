package com.akillikutup.server;

import com.akillikutup.core.ConfigManager;
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
        "gemini-3.5-flash",
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
        if (apiKey == null || apiKey.isEmpty()) {
            return "API_KEY_ERROR";
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
                        .uri(URI.create(API_BASE_URL + model + ":generateContent?key=" + apiKey))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseGeminiResponse(response.body());
                } else {
                    lastError = "API_ERROR: HTTP " + response.statusCode() + " (" + model + ") - " + response.body();
                    // If the API key is completely invalid, no point in retrying other models
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
}
