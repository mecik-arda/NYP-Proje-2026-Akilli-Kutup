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
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    public static String askQuestion(String prompt) {
        String apiKey = ConfigManager.getGeminiApiKey();
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

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseGeminiResponse(response.body());
            } else {
                return "API_ERROR";
            }
        } catch (Exception e) {
            return "NETWORK_ERROR";
        }
    }

    private static String parseGeminiResponse(String jsonResponse) {
        try {
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            return jsonObject.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            return "PARSE_ERROR";
        }
    }
}
