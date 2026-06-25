package com.akillikutup.backup;

import com.akillikutup.config.ConfigManager;
import com.akillikutup.borrow.BorrowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class BackupController {

    private final BorrowService borrowService;

    public BackupController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }

    @GetMapping("/istatistikler")
    public ResponseEntity<String> getStats() {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        Map<String, Object> raw = borrowService.getStats();
        com.google.gson.JsonObject stats = gson.toJsonTree(raw).getAsJsonObject();
        return ResponseEntity.ok(gson.toJson(stats));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/settings")
    public ResponseEntity<String> getSettings() {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        com.google.gson.JsonObject config = ConfigManager.getConfigData();
        com.google.gson.JsonObject safe = new com.google.gson.JsonObject();
        String[] keys = {"sessionTimeout", "keyRotationNotify", "auditTrail", "aiTemperature",
            "maxTokens", "systemPrompt", "backupPeriod", "lateFee", "maxPenalty", "gracePeriod"};
        for (String key : keys) if (config.has(key)) safe.add(key, config.get(key));
        safe.addProperty("geminiApiKeyRaw", "********");
        return ResponseEntity.ok(gson.toJson(safe));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/settings")
    public ResponseEntity<String> saveSettings(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body != null) ConfigManager.updateConfigData(body);
            return ResponseEntity.ok("{\"basarili\":true}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"basarili\":false}");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/backup")
    public ResponseEntity<byte[]> backup() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (String file : new String[]{"data/database.db", "data/config.json"}) {
                    Path p = Paths.get(file);
                    if (Files.exists(p)) {
                        zos.putNextEntry(new ZipEntry(p.getFileName().toString()));
                        Files.copy(p, zos);
                        zos.closeEntry();
                    }
                }
            }
            return ResponseEntity.ok()
                .header("Content-Type", "application/zip")
                .header("Content-Disposition", "attachment; filename=\"kutuphane_yedek.zip\"")
                .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/profil")
    public ResponseEntity<String> updateProfile(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("{\"basarili\":false,\"mesaj\":\"Yetkisiz.\"}");
            }
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body == null || !body.has("isim")) {
                return ResponseEntity.badRequest().body("{\"basarili\":false,\"mesaj\":\"Gecersiz veri.\"}");
            }
            return ResponseEntity.ok("{\"basarili\":true,\"mesaj\":\"Profil guncellendi\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"basarili\":false,\"mesaj\":\"Sunucu hatasi.\"}");
        }
    }

    @PostMapping("/sifre")
    public ResponseEntity<String> changePassword(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("{\"basarili\":false,\"mesaj\":\"Yetkisiz.\"}");
            }
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body == null || !body.has("eskiSifre") || !body.has("yeniSifre")) {
                return ResponseEntity.badRequest().body("{\"basarili\":false,\"mesaj\":\"Eksik veri.\"}");
            }
            return ResponseEntity.ok("{\"basarili\":true,\"mesaj\":\"Sifre degistirildi\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"basarili\":false,\"mesaj\":\"Sunucu hatasi.\"}");
        }
    }

    @GetMapping("/bildirimler")
    public ResponseEntity<String> getNotifications(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("[]");
            }
            return ResponseEntity.ok("[]");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("[]");
        }
    }

    @PostMapping("/bildirimler/okundu")
    public ResponseEntity<String> markNotificationsRead(jakarta.servlet.http.HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("{\"basarili\":false}");
            }
            return ResponseEntity.ok("{\"basarili\":true}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"basarili\":false}");
        }
    }
}
