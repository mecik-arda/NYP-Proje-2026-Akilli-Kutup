package com.akillikutup.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final String CONFIG_FILE = "data/config.json";
    private static String geminiApiKey = null;

    // Hardcoded init for demonstration. In a real app, this might come from env var before encryption
    private static final String DEFAULT_API_KEY = "YOUR_GEMINI_API_KEY_HERE";

    public static void init() {
        Path path = Paths.get(CONFIG_FILE);
        try {
            if (!Files.exists(path)) {
                // Sifrele ve kaydet
                String encrypted = com.akillikutup.db.FileEncryptionService.encrypt(DEFAULT_API_KEY);
                String jsonContent = "{\n  \"gemini_api_key_encrypted\": \"" + encrypted + "\"\n}";
                
                // data klasoru yoksa olustur
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                Files.write(path, jsonContent.getBytes("UTF-8"));
                geminiApiKey = DEFAULT_API_KEY;
                System.out.println("API Anahtari sifrelenerek " + CONFIG_FILE + " dosyasina kaydedildi.");
            } else {
                // Dosyadan oku ve sifreyi coz
                String content = new String(Files.readAllBytes(path), "UTF-8");
                // Basit json parsing (Kutuphane olmadan)
                String keyStr = "\"gemini_api_key_encrypted\":";
                int start = content.indexOf(keyStr);
                if (start != -1) {
                    start += keyStr.length();
                    int firstQuote = content.indexOf("\"", start);
                    int lastQuote = content.indexOf("\"", firstQuote + 1);
                    if (firstQuote != -1 && lastQuote != -1) {
                        String encrypted = content.substring(firstQuote + 1, lastQuote);
                        geminiApiKey = com.akillikutup.db.FileEncryptionService.decrypt(encrypted);
                        System.out.println("API Anahtari config dosyasindan okundu ve desifre edildi.");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("ConfigManager hatasi: " + e.getMessage());
        }
    }

    public static String getGeminiApiKey() {
        return geminiApiKey;
    }
}
