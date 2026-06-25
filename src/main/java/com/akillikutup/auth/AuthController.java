package com.akillikutup.auth;

import com.akillikutup.auth.dto.LoginRequest;
import com.akillikutup.auth.dto.LoginResponse;
import com.akillikutup.auth.dto.TwoFactorSetupResponse;
import com.akillikutup.config.JwtUtil;
import com.akillikutup.config.RateLimiterService;
import com.akillikutup.config.TotpService;
import com.akillikutup.user.ActiveSessionService;
import com.akillikutup.user.SseService;
import com.akillikutup.user.User;
import com.akillikutup.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RateLimiterService rateLimiter;
    private final TotpService totpService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ActiveSessionService sessionService;
    private final SseService sseService;

    public AuthController(JwtUtil jwtUtil, RateLimiterService rateLimiter,
                          TotpService totpService, UserService userService,
                          PasswordEncoder passwordEncoder,
                          ActiveSessionService sessionService,
                          SseService sseService) {
        this.jwtUtil = jwtUtil;
        this.rateLimiter = rateLimiter;
        this.totpService = totpService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.sseService = sseService;
    }

    @PostMapping("/giris")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest body,
                                                HttpServletRequest request) {
        String tcNo = body.getTcKimlikNo() != null ? body.getTcKimlikNo() : "";
        String password = body.getSifre() != null ? body.getSifre()
            : (body.getSifreHash() != null ? body.getSifreHash() : "");
        String totpCode = body.getTotpCode();
        String ipAddress = request.getRemoteAddr();

        if (rateLimiter.isBlocked(ipAddress)) {
            return ResponseEntity.status(429)
                .body(LoginResponse.builder().basarili(false)
                    .mesaj("Cok fazla basarisiz deneme. 5 dakika sonra tekrar deneyin.").build());
        }

        Optional<User> userOpt = userService.findByTcNo(tcNo);
        if (userOpt.isEmpty()) {
            rateLimiter.recordFailedAttempt(ipAddress);
            return ResponseEntity.status(401)
                .body(LoginResponse.builder().basarili(false)
                    .mesaj("Gecersiz kimlik bilgileri").build());
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getSifre())) {
            rateLimiter.recordFailedAttempt(ipAddress);
            return ResponseEntity.status(401)
                .body(LoginResponse.builder().basarili(false)
                    .mesaj("Gecersiz kimlik bilgileri").build());
        }

        if (user.getRol() == User.Role.ADMIN && user.isIkiFAEtkin()) {
            if (totpCode == null || totpCode.isEmpty()) {
                return ResponseEntity.ok(
                    LoginResponse.builder().basarili(true).ikiFARequired(true)
                        .mesaj("Admin hesabi icin 2FA kodu gerekli.").build());
            }
            if (!totpService.verifyCode(user.getTotpSecretKey(), totpCode)) {
                rateLimiter.recordFailedAttempt(ipAddress);
                return ResponseEntity.status(401)
                    .body(LoginResponse.builder().basarili(false)
                        .mesaj("Gecersiz 2FA kodu.").build());
            }
        }

        rateLimiter.resetAttempts(ipAddress);
        // PII (TC Kimlik No) JWT payload'da taşınmaz — Base64 decode edilebilir
        String jwtToken = jwtUtil.generateToken(user.getId(),
            user.getRol().name(), user.getIsim());

        // Aktif oturumu kaydet ve SSE ile bildir
        sessionService.registerSession(user.getId(), user.getIsim(), user.getRol().name(), ipAddress);
        sseService.broadcastActiveCount(sessionService.getActiveCount());
        sseService.broadcastUserJoined(user.getIsim(), "Giriş yaptı");

        return ResponseEntity.ok(LoginResponse.builder()
            .basarili(true)
            .ad(user.getIsim())
            .rol(user.getRol().name())
            .id(user.getId())
            .token(jwtToken)
            .build());
    }

    @PostMapping("/cikis")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                String userId = jwtUtil.extractUserId(token);
                Optional<ActiveSessionService.ActiveSession> session = sessionService.getSession(userId);
                if (session.isPresent()) {
                    sseService.broadcastUserLeft(session.get().userName);
                    sessionService.removeSession(userId);
                }
            }
        }
        sseService.broadcastActiveCount(sessionService.getActiveCount());
        return ResponseEntity.ok(Map.of("basarili", true, "mesaj", "Başarıyla çıkış yapıldı."));
    }

    @PostMapping("/admin/2fa-setup")
    public ResponseEntity<TwoFactorSetupResponse> setup2FA(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                .body(TwoFactorSetupResponse.builder().basarili(false)
                    .mesaj("Yetkisiz.").build());
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401)
                .body(TwoFactorSetupResponse.builder().basarili(false)
                    .mesaj("Gecersiz token.").build());
        }
        String userId = jwtUtil.extractUserId(token);
        String rol = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(rol)) {
            return ResponseEntity.status(403)
                .body(TwoFactorSetupResponse.builder().basarili(false)
                    .mesaj("Sadece adminler 2FA kurabilir.").build());
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRol() != User.Role.ADMIN) {
            return ResponseEntity.status(404)
                .body(TwoFactorSetupResponse.builder().basarili(false)
                    .mesaj("Admin kullanicisi bulunamadi.").build());
        }

        User user = userOpt.get();
        String secretKey = totpService.generateSecretKey();
        user.setTotpSecretKey(secretKey);
        user.setIkiFAEtkin(true);
        userService.save(user);

        String uri = totpService.generateProvisioningUri(user.getIsim(), secretKey);
        return ResponseEntity.ok(TwoFactorSetupResponse.builder()
            .basarili(true)
            .secretKey(secretKey)
            .qrUri(uri)
            .mesaj("2FA basariyla kuruldu. Google Authenticator ile QR kodu tarayin.")
            .build());
    }
}
