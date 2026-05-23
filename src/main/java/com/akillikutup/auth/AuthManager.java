package com.akillikutup.auth;

import com.akillikutup.core.Kullanici;
import com.akillikutup.db.DatabaseManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class AuthManager {

    private final DatabaseManager db;

    public AuthManager() {
        this.db = DatabaseManager.tekOrnekAl();
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algoritmasi bulunamadi", e);
        }
    }

    public Kullanici login(String tcNo, String plainPassword) {
        List<Kullanici> kullanicilar = db.getKullaniciListesi();
        String hashedPassword = hashPassword(plainPassword);

        Optional<Kullanici> eslesenKullanici = kullanicilar.stream()
                .filter(k -> k.getTcNoDogrudan().equals(tcNo) && k.getSifre().equals(plainPassword))
                .findFirst();

        return eslesenKullanici.orElse(null);
    }
}
