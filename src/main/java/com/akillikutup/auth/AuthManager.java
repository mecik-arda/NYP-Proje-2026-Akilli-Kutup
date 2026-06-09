package com.akillikutup.auth;

import com.akillikutup.core.Kullanici;
import com.akillikutup.db.DatabaseManager;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class AuthManager {
    private final DatabaseManager db;

    public AuthManager() {
        this.db = DatabaseManager.tekOrnekAl();
    }

    public String hashPassword(String password, byte[] salt) {
        try {
            int iterations = 65536;
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
                                return expectedHash.equals(actualHash);
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
        user.setToken(token);
        db.kullanicilariKaydet();
        return token;
    }

    public Kullanici getUserByToken(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        List<Kullanici> kullanicilar = db.getKullaniciListesi();
        return kullanicilar.stream()
                .filter(k -> token.equals(k.getToken()))
                .findFirst()
                .orElse(null);
    }
}
