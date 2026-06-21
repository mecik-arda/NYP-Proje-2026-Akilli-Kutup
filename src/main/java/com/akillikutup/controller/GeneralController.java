package com.akillikutup.controller;

import com.akillikutup.core.*;
import com.akillikutup.repository.DijitalMedyaRepository;
import com.akillikutup.repository.KlasorRepository;
import com.akillikutup.server.GeminiClient;
import com.akillikutup.server.RagService;
import com.akillikutup.service.BorrowService;
import com.akillikutup.service.KullaniciService;
import com.google.gson.*;
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
public class GeneralController {

    private final Gson gson = new Gson();
    private final KullaniciService kullaniciService;
    private final BorrowService borrowService;
    private final RagService ragService;
    private final DijitalMedyaRepository dijitalMedyaRepository;
    private final KlasorRepository klasorRepository;

    public GeneralController(KullaniciService kullaniciService,
                              BorrowService borrowService, RagService ragService,
                              DijitalMedyaRepository dijitalMedyaRepository,
                              KlasorRepository klasorRepository) {
        this.kullaniciService = kullaniciService;
        this.borrowService = borrowService;
        this.ragService = ragService;
        this.dijitalMedyaRepository = dijitalMedyaRepository;
        this.klasorRepository = klasorRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }

    // ─── Kullanıcılar ──────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/kullanicilar")
    public ResponseEntity<String> getUsers() {
        List<Kullanici> kullanicilar = kullaniciService.findAll();
        JsonArray arr = new JsonArray();
        for (Kullanici k : kullanicilar) {
            JsonObject dto = new JsonObject();
            dto.addProperty("id", "M-" + Math.abs(k.getTcNoDogrudan().hashCode()));
            dto.addProperty("isim", k.getIsim());
            String tc = k.getTcNoDogrudan();
            dto.addProperty("tcKimlikNo", tc != null && tc.length() == 11
                ? tc.substring(0, 3) + "*****" + tc.substring(8) : tc);
            dto.addProperty("email", k.getEmail() != null ? k.getEmail() : "Yok");
            dto.addProperty("rol", k.getRol());
            arr.add(dto);
        }
        return ResponseEntity.ok(gson.toJson(arr));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/kullanicilar")
    public ResponseEntity<String> addUser(jakarta.servlet.http.HttpServletRequest request) {
        try {
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            if (body == null) body = new JsonObject();
            String isim = body.has("isim") ? body.get("isim").getAsString() : null;
            String tcNo = body.has("tcKimlikNo") ? body.get("tcKimlikNo").getAsString() : null;
            String email = body.has("email") ? body.get("email").getAsString() : null;
            String rol = body.has("rol") ? body.get("rol").getAsString() : "uye";
            String sifre = body.has("sifre") ? body.get("sifre").getAsString() : "123456";

            if (isim == null || tcNo == null || email == null) {
                return ResponseEntity.badRequest().body("{\"basarili\":false,\"mesaj\":\"Eksik bilgi\"}");
            }
            if (kullaniciService.existsByIsimOrTcNoOrEmail(isim, tcNo, email)) {
                return ResponseEntity.badRequest()
                    .body("{\"basarili\":false,\"mesaj\":\"Bu isim, TC No veya E-posta kayitli.\"}");
            }
            kullaniciService.createUser(isim, tcNo, email, rol, sifre);
            return ResponseEntity.ok("{\"basarili\":true}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"basarili\":false}");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/kullanicilar/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable String userId,
                                              jakarta.servlet.http.HttpServletRequest request) {
        try {
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            if (body == null) body = new JsonObject();
            String isim = body.has("isim") ? body.get("isim").getAsString() : null;
            String tcNo = body.has("tcKimlikNo") ? body.get("tcKimlikNo").getAsString() : null;
            String email = body.has("email") ? body.get("email").getAsString() : null;
            kullaniciService.updateUser(userId, isim, tcNo, email);
            return ResponseEntity.ok("{\"basarili\":true}");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("{\"basarili\":false,\"mesaj\":\"" + e.getMessage() + "\"}");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/kullanicilar/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        try {
            kullaniciService.deleteUser(userId);
            return ResponseEntity.ok("{\"basarili\":true}");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("{\"basarili\":false,\"mesaj\":\"Kullanici bulunamadi\"}");
        }
    }

    // ─── İstatistikler ─────────────────────────────────────────────

    @GetMapping("/istatistikler")
    public ResponseEntity<String> getStats() {
        Map<String, Object> raw = borrowService.getStats();
        JsonObject stats = gson.toJsonTree(raw).getAsJsonObject();
        return ResponseEntity.ok(gson.toJson(stats));
    }

    // ─── Chat ──────────────────────────────────────────────────────

    @PostMapping("/chat")
    public ResponseEntity<String> chat(jakarta.servlet.http.HttpServletRequest request) {
        try {
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            if (body == null) body = new JsonObject();
            String prompt = body.has("prompt") ? body.get("prompt").getAsString() : "";
            boolean useRag = body.has("useRag") && body.get("useRag").getAsBoolean();
            String aiResponse;
            if (prompt.isEmpty()) {
                aiResponse = "Lutfen bir soru girin.";
            } else if (useRag) {
                aiResponse = ragService.askWithContext(prompt);
            } else {
                aiResponse = GeminiClient.askQuestion(prompt, null);
            }
            JsonObject resp = new JsonObject();
            resp.addProperty("response", aiResponse);
            return ResponseEntity.ok(gson.toJson(resp));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"response\":\"AI hatasi.\"}");
        }
    }

    // ─── Profil ────────────────────────────────────────────────────

    @PostMapping("/profil")
    public ResponseEntity<String> updateProfile(jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok("{\"basarili\":true,\"mesaj\":\"Profil guncellendi\"}");
    }

    @PostMapping("/sifre")
    public ResponseEntity<String> changePassword() {
        return ResponseEntity.ok("{\"basarili\":true,\"mesaj\":\"Sifre degistirildi\"}");
    }

    // ─── Bildirimler ───────────────────────────────────────────────

    @GetMapping("/bildirimler")
    public ResponseEntity<String> getNotifications() {
        return ResponseEntity.ok("[]");
    }

    @PostMapping("/bildirimler/okundu")
    public ResponseEntity<String> markNotificationsRead() {
        return ResponseEntity.ok("{\"basarili\":true}");
    }

    // ─── Settings ──────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/settings")
    public ResponseEntity<String> getSettings() {
        JsonObject config = ConfigManager.getConfigData();
        JsonObject safe = new JsonObject();
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
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            if (body != null) ConfigManager.updateConfigData(body);
            return ResponseEntity.ok("{\"basarili\":true}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"basarili\":false}");
        }
    }

    // ─── Backup ────────────────────────────────────────────────────

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

    // ─── Dijital Varlık ────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/dijital/upload")
    public ResponseEntity<String> uploadDigitalAsset(jakarta.servlet.http.HttpServletRequest request) {
        try {
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            String baslik = body.has("baslik") && !body.get("baslik").isJsonNull() ? body.get("baslik").getAsString() : null;
            String tur = body.has("tur") && !body.get("tur").isJsonNull() ? body.get("tur").getAsString() : null;
            String boyut = body.has("boyut") && !body.get("boyut").isJsonNull() ? body.get("boyut").getAsString() : null;
            String format = body.has("format") && !body.get("format").isJsonNull() ? body.get("format").getAsString() : null;
            if (baslik == null || tur == null || boyut == null || format == null)
                return ResponseEntity.badRequest().body("{\"basarili\":false,\"mesaj\":\"Eksik veri.\"}");
            DijitalMedya dm = new DijitalMedya(baslik, 0.0, format, tur, boyut);
            dijitalMedyaRepository.save(dm);
            return ResponseEntity.ok("{\"basarili\":true,\"mesaj\":\"Dijital varlik eklendi.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"basarili\":false}");
        }
    }

    @PostMapping("/dijital/klasor")
    public ResponseEntity<String> createFolder(jakarta.servlet.http.HttpServletRequest request) {
        try {
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            if (!body.has("baslik") || body.get("baslik").isJsonNull())
                return ResponseEntity.badRequest().body("{\"basarili\":false,\"mesaj\":\"Baslik zorunludur.\"}");
            Klasor klasor = new Klasor(body.get("baslik").getAsString());
            klasorRepository.save(klasor);
            return ResponseEntity.ok("{\"basarili\":true,\"mesaj\":\"Klasor olusturuldu.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"basarili\":false}");
        }
    }

    @PostMapping("/kitap-kapak-tara")
    public ResponseEntity<String> scanBookCover(jakarta.servlet.http.HttpServletRequest request) {
        try {
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            if (body == null) body = new JsonObject();
            String base64Image = body.has("image") ? body.get("image").getAsString() : null;
            if (base64Image == null || base64Image.isEmpty())
                return ResponseEntity.badRequest().body("{\"basarili\":false,\"mesaj\":\"Gorsel gerekli.\"}");
            if (base64Image.contains(",")) base64Image = base64Image.split(",")[1];
            String result = GeminiClient.analyzeBookCover(base64Image, "image/jpeg", null);
            return ResponseEntity.ok("{\"basarili\":true,\"sonuc\":" + result + "}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"basarili\":false}");
        }
    }
}
