package com.akillikutup.auth;

import com.akillikutup.core.Kullanici;
import com.akillikutup.db.DatabaseManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {
    private final DatabaseManager db;
    private static final Map<String, Kullanici> aktifOturumlar = new ConcurrentHashMap<>();

    public AuthManager() {
        this.db = DatabaseManager.tekOrnekAl();
    }

    public String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] encodedhash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public Kullanici login(String tcNo, String plainPassword) {
        List<Kullanici> kullanicilar = db.getKullaniciListesi();

        Optional<Kullanici> eslesenKullanici = kullanicilar.stream()
                .filter(k -> {
                    if (k.getTcNoDogrudan().equals(tcNo)) {
                        String sifre = k.getSifre();
                        if (sifre == null || !sifre.contains(":")) return false;
                        String[] parts = sifre.split(":");
                        if (parts.length == 2) {
                            try {
                                byte[] salt = Base64.getDecoder().decode(parts[0]);
                                String expectedHash = parts[1];
                                String actualHash = hashPassword(plainPassword, salt);
                                return MessageDigest.isEqual(expectedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8), actualHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                            } catch (Exception e) {
                                return false;
                            }
                        }
                    }
                    return false;
                })
                .findFirst();

        return eslesenKullanici.orElse(null);
    }
    
    public String registerPassword(String plainPassword) {
        byte[] salt = generateSalt();
        String hash = hashPassword(plainPassword, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + hash;
    }

    public String createSession(Kullanici user) {
        String token = java.util.UUID.randomUUID().toString();
        aktifOturumlar.put(token, user);
        return token;
    }

    public Kullanici getUserByToken(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        return aktifOturumlar.get(token);
    }
}
