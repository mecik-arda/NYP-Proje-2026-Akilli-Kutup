package com.akillikutup.config;

import com.akillikutup.db.FileEncryptionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private static final String CONFIG_FILE = "data/config.json";
    private static String geminiApiKey = null;
    private static JsonObject configData = new JsonObject();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final String DEFAULT_API_KEY = "YOUR_GEMINI_API_KEY_HERE";

    public static void init() {
        Path path = Paths.get(CONFIG_FILE);
        try {
            if (!Files.exists(path)) {
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                configData = createDefaultConfig();
                saveConfig();
                geminiApiKey = DEFAULT_API_KEY;
            } else {
                String content = new String(Files.readAllBytes(path), "UTF-8");
                configData = gson.fromJson(content, JsonObject.class);
                if (configData == null) {
                    configData = new JsonObject();
                }

                if (configData.has("gemini_api_key_encrypted")) {
                    String encrypted = configData.get("gemini_api_key_encrypted").getAsString();
                    try {
                        geminiApiKey = FileEncryptionService.decrypt(encrypted);
                    } catch (Exception e) {
                        geminiApiKey = DEFAULT_API_KEY;
                    }
                } else {
                    geminiApiKey = DEFAULT_API_KEY;
                }

                checkDefaults();
            }
        } catch (Exception e) {
            System.err.println("ConfigManager hatasi: " + e.getMessage());
        }
    }

    private static JsonObject createDefaultConfig() throws Exception {
        JsonObject obj = new JsonObject();
        obj.addProperty("gemini_api_key_encrypted", FileEncryptionService.encrypt(DEFAULT_API_KEY));
        obj.addProperty("sessionTimeout", 30);
        obj.addProperty("keyRotationNotify", false);
        obj.addProperty("auditTrail", true);
        obj.addProperty("aiTemperature", 0.7);
        obj.addProperty("maxTokens", 800);
        obj.addProperty("systemPrompt", "Sen resmi bir kütüphane asistanısın ve sadece Türkçe yanıt verirsin.");
        obj.addProperty("backupPeriod", "daily");
        obj.addProperty("lateFee", 5);
        obj.addProperty("maxPenalty", 100);
        obj.addProperty("gracePeriod", 2);
        return obj;
    }

    private static void checkDefaults() throws Exception {
        boolean updated = false;
        if (!configData.has("sessionTimeout")) { configData.addProperty("sessionTimeout", 30); updated = true; }
        if (!configData.has("keyRotationNotify")) { configData.addProperty("keyRotationNotify", false); updated = true; }
        if (!configData.has("auditTrail")) { configData.addProperty("auditTrail", true); updated = true; }
        if (!configData.has("aiTemperature")) { configData.addProperty("aiTemperature", 0.7); updated = true; }
        if (!configData.has("maxTokens")) { configData.addProperty("maxTokens", 800); updated = true; }
        if (!configData.has("systemPrompt")) { configData.addProperty("systemPrompt", "Sen resmi bir kütüphane asistanısın ve sadece Türkçe yanıt verirsin."); updated = true; }
        if (!configData.has("backupPeriod")) { configData.addProperty("backupPeriod", "daily"); updated = true; }
        if (!configData.has("lateFee")) { configData.addProperty("lateFee", 5); updated = true; }
        if (!configData.has("maxPenalty")) { configData.addProperty("maxPenalty", 100); updated = true; }
        if (!configData.has("gracePeriod")) { configData.addProperty("gracePeriod", 2); updated = true; }
        if (!configData.has("gemini_api_key_encrypted")) {
            configData.addProperty("gemini_api_key_encrypted", FileEncryptionService.encrypt(DEFAULT_API_KEY));
            updated = true;
        }

        if (updated) saveConfig();
    }

    public static synchronized void saveConfig() {
        try {
            Path path = Paths.get(CONFIG_FILE);
            Files.write(path, gson.toJson(configData).getBytes("UTF-8"));
        } catch (IOException e) {
            System.err.println("Ayarlar kaydedilemedi: " + e.getMessage());
        }
    }

    public static JsonObject getConfigData() {
        return configData;
    }

    public static void updateConfigData(JsonObject newData) throws Exception {
        List<String> allowedKeys = Arrays.asList(
            "sessionTimeout", "keyRotationNotify", "auditTrail", "aiTemperature",
            "maxTokens", "systemPrompt", "backupPeriod", "lateFee", "maxPenalty", "gracePeriod"
        );
        for (String key : newData.keySet()) {
            if (key.equals("geminiApiKeyRaw")) {
                String rawKey = newData.get(key).getAsString();
                if (!rawKey.isEmpty() && !rawKey.equals("********")) {
                    geminiApiKey = rawKey;
                    configData.addProperty("gemini_api_key_encrypted", FileEncryptionService.encrypt(rawKey));
                }
            } else if (allowedKeys.contains(key)) {
                configData.add(key, newData.get(key));
            }
        }
        saveConfig();
    }

    public static String getGeminiApiKey() {
        return geminiApiKey;
    }
}
