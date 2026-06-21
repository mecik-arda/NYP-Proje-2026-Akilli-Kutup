package com.akillikutup.config;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class TotpService {

    private static final Logger log = LoggerFactory.getLogger(TotpService.class);

    private final TimeBasedOneTimePasswordGenerator totpGenerator;

    public TotpService() {
        this.totpGenerator = new TimeBasedOneTimePasswordGenerator(
            Duration.ofSeconds(30),   // 30 saniyelik zaman adımı
            6                         // 6 haneli kod
        );
    }

    public String generateSecretKey() {
        byte[] secret = new byte[20];
        new SecureRandom().nextBytes(secret);
        return Base64.getEncoder().encodeToString(secret);
    }

    public boolean verifyCode(String base64Secret, String code) {
        if (base64Secret == null || code == null || code.length() != 6) return false;
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
            Key key = new SecretKeySpec(keyBytes, "HmacSHA1");
            String expected = totpGenerator.generateOneTimePasswordString(key, Instant.now());
            // ±1 adım tolerans
            String expectedPrev = totpGenerator.generateOneTimePasswordString(key,
                Instant.now().minus(Duration.ofSeconds(30)));
            String expectedNext = totpGenerator.generateOneTimePasswordString(key,
                Instant.now().plus(Duration.ofSeconds(30)));
            return code.equals(expected) || code.equals(expectedPrev) || code.equals(expectedNext);
        } catch (Exception e) {
            log.error("TOTP doğrulama hatası: {}", e.getMessage());
            return false;
        }
    }

    public String generateProvisioningUri(String accountName, String secretKey) {
        return String.format("otpauth://totp/AkilliKutuphane:%s?secret=%s&issuer=AkilliKutuphane",
            accountName.replace(" ", "%20"), secretKey);
    }
}
