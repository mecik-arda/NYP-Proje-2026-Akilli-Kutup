package com.akillikutup.material;

import com.akillikutup.chat.GeminiClient;
import com.akillikutup.material.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api")
public class MaterialController {

    private final DijitalMedyaRepository dijitalMedyaRepository;
    private final KlasorRepository klasorRepository;

    public MaterialController(DijitalMedyaRepository dijitalMedyaRepository,
                              KlasorRepository klasorRepository) {
        this.dijitalMedyaRepository = dijitalMedyaRepository;
        this.klasorRepository = klasorRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/dijital/upload")
    public ResponseEntity<Map<String, Object>> uploadDigitalAsset(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            String baslik = body.has("baslik") && !body.get("baslik").isJsonNull()
                ? body.get("baslik").getAsString() : null;
            String tur = body.has("tur") && !body.get("tur").isJsonNull()
                ? body.get("tur").getAsString() : null;
            String boyut = body.has("boyut") && !body.get("boyut").isJsonNull()
                ? body.get("boyut").getAsString() : null;
            String format = body.has("format") && !body.get("format").isJsonNull()
                ? body.get("format").getAsString() : null;
            if (baslik == null || tur == null || boyut == null || format == null) {
                Map<String, Object> err = new HashMap<>();
                err.put("basarili", false);
                err.put("mesaj", "Eksik veri.");
                return ResponseEntity.badRequest().body(err);
            }
            DijitalMedya dm = new DijitalMedya(baslik, 0.0, format, tur, boyut);
            dijitalMedyaRepository.save(dm);
            Map<String, Object> ok = new HashMap<>();
            ok.put("basarili", true);
            ok.put("mesaj", "Dijital varlik eklendi.");
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/dijital/klasor")
    public ResponseEntity<Map<String, Object>> createFolder(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (!body.has("baslik") || body.get("baslik").isJsonNull()) {
                Map<String, Object> err = new HashMap<>();
                err.put("basarili", false);
                err.put("mesaj", "Baslik zorunludur.");
                return ResponseEntity.badRequest().body(err);
            }
            Klasor klasor = new Klasor(body.get("baslik").getAsString());
            klasorRepository.save(klasor);
            Map<String, Object> ok = new HashMap<>();
            ok.put("basarili", true);
            ok.put("mesaj", "Klasor olusturuldu.");
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PostMapping("/kitap-kapak-tara")
    public ResponseEntity<Map<String, Object>> scanBookCover(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body == null) body = new com.google.gson.JsonObject();
            String base64Image = body.has("image") ? body.get("image").getAsString() : null;
            if (base64Image == null || base64Image.isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("basarili", false);
                err.put("mesaj", "Gorsel gerekli.");
                return ResponseEntity.badRequest().body(err);
            }
            if (base64Image.contains(",")) base64Image = base64Image.split(",")[1];
            String result = GeminiClient.analyzeBookCover(base64Image, "image/jpeg", null);
            Map<String, Object> ok = new HashMap<>();
            ok.put("basarili", true);
            ok.put("sonuc", result);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            return ResponseEntity.internalServerError().body(err);
        }
    }
}
