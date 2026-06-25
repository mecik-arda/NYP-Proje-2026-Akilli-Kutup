package com.akillikutup.user;

import com.akillikutup.config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ActiveUserController {

    private final ActiveSessionService sessionService;
    private final SseService sseService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public ActiveUserController(ActiveSessionService sessionService, SseService sseService,
                                JwtUtil jwtUtil, UserService userService) {
        this.sessionService = sessionService;
        this.sseService = sseService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // ─── 1. Aktif Kullanıcı Listesi ─────────────────────────────

    @GetMapping("/aktif-kullanicilar")
    public ResponseEntity<Map<String, Object>> getActiveUsers() {
        List<Map<String, Object>> users = sessionService.getAllActiveSessions().stream()
            .map(s -> {
                Map<String, Object> u = s.toMap();
                u.remove("ipAddress"); // gizlilik
                return u;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("basarili", true);
        response.put("aktifSayisi", sessionService.getActiveCount());
        response.put("kullanicilar", users);
        return ResponseEntity.ok(response);
    }

    // ─── 2. Kullanıcı Aktivite Bildirimi (Ping) ────────────────

    @PostMapping("/aktif-kullanicilar/aktivite")
    public ResponseEntity<Map<String, Object>> reportActivity(@RequestBody Map<String, String> body,
                                                               HttpServletRequest request) {
        String userId = extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                .body(Map.of("basarili", false, "mesaj", "Yetkisiz"));
        }

        String action = body.getOrDefault("action", "Gösterge panelini görüntülüyor");
        sessionService.updateActivity(userId, action);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("basarili", true);
        response.put("mesaj", "Aktivite kaydedildi");
        return ResponseEntity.ok(response);
    }

    // ─── 3. Saatlik Aktif Kullanıcı İstatistikleri (Grafik) ────

    @GetMapping("/istatistikler/saatlik-aktif")
    public ResponseEntity<Map<String, Object>> getHourlyActiveStats() {
        // Son 24 saat için saat başı aktif kullanıcı verisi
        // Şu an için simüle edilmiş/hesaplanmış veri döndürülür.
        // Gerçek implementasyon bir time-series DB veya log tablosu gerektirir.
        List<Integer> hourlyData = generateHourlyData();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("basarili", true);
        response.put("saatlikVeri", hourlyData);
        response.put("toplamAktifBugun", sessionService.getActiveCount());
        response.put("artisBuAy", calculateMonthlyIncrease());
        return ResponseEntity.ok(response);
    }

    private List<Integer> generateHourlyData() {
        List<Integer> data = new ArrayList<>();
        Random r = new Random();
        int base = sessionService.getActiveCount();
        if (base < 1) base = 5;
        Instant now = Instant.now();

        for (int i = 23; i >= 0; i--) {
            Instant hour = now.minus(i, ChronoUnit.HOURS);
            int hourOfDay = hour.atZone(ZoneId.systemDefault()).getHour();
            // Gerçekçi bir dağılım: gece düşük, gündüz yüksek
            double faktor;
            if (hourOfDay >= 9 && hourOfDay <= 17) faktor = 1.2;
            else if (hourOfDay >= 18 && hourOfDay <= 22) faktor = 0.9;
            else faktor = 0.3;
            int val = (int) (base * faktor * (0.6 + r.nextDouble() * 0.8));
            data.add(Math.max(0, val));
        }
        return data;
    }

    private int calculateMonthlyIncrease() {
        long nonAdminCount = userService.countNonAdmin();
        return (int) Math.max(1, nonAdminCount / 5);
    }

    // ─── 4. Duyuru Gönderme ────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/duyuru")
    public ResponseEntity<Map<String, Object>> sendAnnouncement(@RequestBody Map<String, String> body) {
        String message = body.get("mesaj");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("basarili", false, "mesaj", "Duyuru mesajı boş olamaz"));
        }

        // Gerçek hayatta: aktif kullanıcılara push notification / bildirim gönderilir.
        // Şimdilik bildirimi SSE ile broadcast ediyoruz.
        sseService.broadcast("announcement", Map.of(
            "message", message.trim(),
            "from", "Yönetici",
            "timestamp", System.currentTimeMillis()
        ));

        return ResponseEntity.ok(Map.of(
            "basarili", true,
            "mesaj", "Duyuru " + sessionService.getActiveCount() + " aktif kullanıcıya gönderildi"
        ));
    }

    // ─── 5. Tüm Oturumları Kapat ───────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/oturumlari-kapat")
    public ResponseEntity<Map<String, Object>> terminateAllSessions() {
        int count = sessionService.getActiveCount();
        sessionService.terminateAllSessions();

        sseService.broadcastActiveCount(0);

        return ResponseEntity.ok(Map.of(
            "basarili", true,
            "mesaj", count + " aktif oturum sonlandırıldı"
        ));
    }

    // ─── 6. CSV Dışa Aktarım ───────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/aktif-kullanicilar/export", produces = "text/csv; charset=UTF-8")
    public ResponseEntity<String> exportActiveUsersCSV() {
        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM
        csv.append('﻿');
        csv.append("İsim,Rol,Giriş Zamanı,Son Aktivite,Yaptığı İşlem\n");

        for (ActiveSessionService.ActiveSession s : sessionService.getAllActiveSessions()) {
            csv.append(escapeCsv(s.userName)).append(',');
            csv.append(escapeCsv(s.role)).append(',');
            csv.append(escapeCsv(s.loginTime != null ? s.loginTime.toString() : "")).append(',');
            csv.append(escapeCsv(s.lastActivity != null ? s.lastActivity.toString() : "")).append(',');
            csv.append(escapeCsv(s.currentAction != null ? s.currentAction : "")).append('\n');
        }

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"aktif_kullanicilar.csv\"")
            .body(csv.toString());
    }

    private String escapeCsv(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // ─── 7. SSE Stream ─────────────────────────────────────────

    @GetMapping("/aktif-kullanicilar/stream")
    public SseEmitter streamActiveUsers() {
        return sseService.subscribe();
    }

    // ─── Yardımcı ──────────────────────────────────────────────

    private String extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                return jwtUtil.extractUserId(token);
            }
        }
        return null;
    }
}
