package com.akillikutup.controller;

import com.akillikutup.config.JwtUtil;
import com.akillikutup.config.RateLimiterService;
import com.akillikutup.config.TotpService;
import com.akillikutup.core.Admin;
import com.akillikutup.core.Kullanici;
import com.akillikutup.service.KullaniciService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final Gson gson = new Gson();
    private final JwtUtil jwtUtil;
    private final RateLimiterService rateLimiter;
    private final TotpService totpService;
    private final KullaniciService kullaniciService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, RateLimiterService rateLimiter,
                          TotpService totpService, KullaniciService kullaniciService,
                          PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.rateLimiter = rateLimiter;
        this.totpService = totpService;
        this.kullaniciService = kullaniciService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/giris")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body,
                                         jakarta.servlet.http.HttpServletRequest request) {
        String tcNo = body.getOrDefault("tcKimlikNo", "");
        String password = body.getOrDefault("sifre", body.getOrDefault("sifreHash", ""));
        String totpCode = body.getOrDefault("totpCode", null);
        String ipAddress = request.getRemoteAddr();

        if (rateLimiter.isBlocked(ipAddress)) {
            return ResponseEntity.status(429)
                .body("{\"basarili\":false,\"mesaj\":\"Cok fazla basarisiz deneme. 5 dakika sonra tekrar deneyin.\"}");
        }

        Optional<Kullanici> userOpt = kullaniciService.findByTcNo(tcNo);
        if (userOpt.isEmpty()) {
            rateLimiter.recordFailedAttempt(ipAddress);
            return ResponseEntity.status(401)
                .body("{\"basarili\":false,\"mesaj\":\"Gecersiz kimlik bilgileri\"}");
        }

        Kullanici user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getSifre())) {
            rateLimiter.recordFailedAttempt(ipAddress);
            return ResponseEntity.status(401)
                .body("{\"basarili\":false,\"mesaj\":\"Gecersiz kimlik bilgileri\"}");
        }

        if (user instanceof Admin admin && admin.isIkiFAEtkin()) {
            if (totpCode == null || totpCode.isEmpty()) {
                return ResponseEntity.ok(
                    "{\"basarili\":true,\"ikiFARequired\":true,"
                        + "\"mesaj\":\"Admin hesabi icin 2FA kodu gerekli.\"}");
            }
            if (!totpService.verifyCode(admin.getTotpSecretKey(), totpCode)) {
                rateLimiter.recordFailedAttempt(ipAddress);
                return ResponseEntity.status(401)
                    .body("{\"basarili\":false,\"mesaj\":\"Gecersiz 2FA kodu.\"}");
            }
        }

        rateLimiter.resetAttempts(ipAddress);
        String jwtToken = jwtUtil.generateToken(user.getId(), user.getTcNoDogrudan(), user.getRol(), user.getIsim());

        JsonObject response = new JsonObject();
        response.addProperty("basarili", true);
        response.addProperty("ad", user.getIsim());
        response.addProperty("rol", user.getRol());
        response.addProperty("id", user.getId());
        response.addProperty("token", jwtToken);
        return ResponseEntity.ok(gson.toJson(response));
    }

    @PostMapping("/admin/2fa-setup")
    public ResponseEntity<String> setup2FA(jakarta.servlet.http.HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("{\"basarili\":false,\"mesaj\":\"Yetkisiz.\"}");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).body("{\"basarili\":false,\"mesaj\":\"Gecersiz token.\"}");
        }
        String userId = jwtUtil.extractUserId(token);
        String rol = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(rol)) {
            return ResponseEntity.status(403).body("{\"basarili\":false,\"mesaj\":\"Sadece adminler 2FA kurabilir.\"}");
        }

        Optional<Kullanici> userOpt = kullaniciService.findById(userId);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Admin)) {
            return ResponseEntity.status(404).body("{\"basarili\":false,\"mesaj\":\"Admin kullanicisi bulunamadi.\"}");
        }

        Admin admin = (Admin) userOpt.get();
        String secretKey = totpService.generateSecretKey();
        admin.setTotpSecretKey(secretKey);
        admin.setIkiFAEtkin(true);
        kullaniciService.save(admin);

        String uri = totpService.generateProvisioningUri(admin.getIsim(), secretKey);
        JsonObject response = new JsonObject();
        response.addProperty("basarili", true);
        response.addProperty("secretKey", secretKey);
        response.addProperty("qrUri", uri);
        response.addProperty("mesaj", "2FA basariyla kuruldu. Google Authenticator ile QR kodu tarayin.");
        return ResponseEntity.ok(gson.toJson(response));
    }
}
