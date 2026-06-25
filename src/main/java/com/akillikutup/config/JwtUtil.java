package com.akillikutup.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT (JSON Web Token) yardımcı sınıfı.
 * Eski AuthManager oturum yönetiminin yerini alır.
 * Frontend uyumluluğu için token header'ı: Authorization: Bearer <token>
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
        if (secret == null || secret.isBlank() || secret.startsWith("CHANGE_ME")) {
            throw new IllegalStateException(
                "KRITIK GUVENLIK HATASI: app.jwt.secret konfigurasyonu zorunludur. "
                + "application.yml veya JWT_SECRET env degiskeninde guclu bir deger tanimlayin.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Kullanıcı için JWT token üretir.
     * NOT: PII (TC Kimlik No) JWT payload'da taşınmaz — JWT Base64 ile encode edilir, şifreli değildir.
     */
    public String generateToken(String userId, String rol, String isim) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .subject(userId)
            .claim("rol", rol)
            .claim("isim", isim)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    /**
     * Token'dan kullanıcı ID'sini çıkarır.
     */
    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Token'dan rol bilgisini çıkarır.
     */
    public String extractRole(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    /**
     * Token geçerli mi kontrol eder.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
