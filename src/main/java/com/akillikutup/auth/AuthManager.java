package com.akillikutup.auth;

import com.akillikutup.user.User;
import com.akillikutup.db.DatabaseManager;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    private final DatabaseManager db;
    private final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockoutTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> activeSessions = new ConcurrentHashMap<>();
    // OWASP 2025: PBKDF2 için minimum 600.000 iterasyon öneriliyor
    private static final int PBKDF2_ITERATIONS = 600_000;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000;

    public AuthManager() {
        this.db = DatabaseManager.tekOrnekAl();
    }

    public String hashPassword(String password, byte[] salt) {
        return hashPassword(password, salt, PBKDF2_ITERATIONS);
    }

    public String hashPassword(String password, byte[] salt, int iterations) {
        try {
            int keyLength = 256;
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Parola hashlenirken hata: " + e.getMessage(), e);
        }
    }

    public byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public User login(String tcNo, String plainPassword, String ipAddress) {
        if (ipAddress != null) {
            Long lockoutTime = lockoutTimes.get(ipAddress);
            if (lockoutTime != null) {
                if (System.currentTimeMillis() < lockoutTime) {
                    throw new SecurityException("Çok fazla başarısız giriş denemesi. Lütfen daha sonra tekrar deneyin.");
                } else {
                    lockoutTimes.remove(ipAddress);
                    failedAttempts.remove(ipAddress);
                }
            }
        }

        List<User> kullanicilar = db.getKullaniciListesi();

        Optional<User> eslesenKullanici = kullanicilar.stream()
                .filter(k -> {
                    if (k.getTcNoDogrudan().equals(tcNo)) {
                        String sifre = k.getSifre();
                        if (sifre == null || !sifre.contains(":")) return false;
                        String[] parts = sifre.split(":");
                        if (parts.length == 2) {
                            try {
                                byte[] salt = Base64.getDecoder().decode(parts[0]);
                                String expectedHash = parts[1];
                                String actualHash = hashPassword(plainPassword, salt, 65536);
                                if (!expectedHash.equals(actualHash)) {
                                    actualHash = hashPassword(plainPassword, salt, PBKDF2_ITERATIONS);
                                }
                                return expectedHash.equals(actualHash);
                            } catch (Exception e) {
                                return false;
                            }
                        }
                    }
                    return false;
                })
                .findFirst();

        if (eslesenKullanici.isPresent()) {
            if (ipAddress != null) {
                failedAttempts.remove(ipAddress);
                lockoutTimes.remove(ipAddress);
            }
            return eslesenKullanici.get();
        } else {
            if (ipAddress != null) {
                int attempts = failedAttempts.getOrDefault(ipAddress, 0) + 1;
                failedAttempts.put(ipAddress, attempts);
                if (attempts >= MAX_FAILED_ATTEMPTS) {
                    lockoutTimes.put(ipAddress, System.currentTimeMillis() + LOCKOUT_DURATION_MS);
                }
            }
            return null;
        }
    }

    public String registerPassword(String plainPassword) {
        byte[] salt = generateSalt();
        String hash = hashPassword(plainPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + hash;
    }

    public String createSession(User user) {
        String token = java.util.UUID.randomUUID().toString();
        user.setToken(token);
        user.setTokenExpiry(System.currentTimeMillis() + (2 * 60 * 60 * 1000));
        db.kullanicilariKaydet();
        activeSessions.put(token, user);
        return token;
    }

    public User getUserByToken(String token) {
        if (token == null || token.trim().isEmpty()) return null;

        User sessionUser = activeSessions.get(token);
        if (sessionUser != null && System.currentTimeMillis() < sessionUser.getTokenExpiry()) {
            return sessionUser;
        }

        List<User> kullanicilar = db.getKullaniciListesi();
        User k = kullanicilar.stream()
                .filter(u -> token.equals(u.getToken()) && System.currentTimeMillis() < u.getTokenExpiry())
                .findFirst()
                .orElse(null);

        if (k != null) {
            activeSessions.put(token, k);
        }
        return k;
    }
}
