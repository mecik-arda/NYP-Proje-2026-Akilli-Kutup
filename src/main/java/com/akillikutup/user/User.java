package com.akillikutup.user;

import com.akillikutup.user.Bildirim;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.*;

@Entity
@Table(name = "kullanicilar")
@Getter
@NoArgsConstructor
public class User {

    public enum Role {
        ADMIN, UYE
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "isim", nullable = false)
    private String isim;

    @Column(name = "tc_no", length = 11)
    private String tcNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Role rol;

    @Column(name = "sifre")
    private String sifre;

    @Column(name = "token", length = 500)
    private String token;

    @Column(name = "token_expiry")
    private long tokenExpiry;

    @Column(name = "kredi_puani")
    private int krediPuani;

    @Column(name = "totp_secret_key", length = 100)
    private String totpSecretKey;

    @Column(name = "iki_fa_etkin")
    private boolean ikiFAEtkin;

    @Column(name = "email")
    private String email;

    @Column(name = "gemini_api_key", length = 500)
    private String geminiApiKey;

    @ElementCollection
    @CollectionTable(name = "kullanici_odunc_materyaller", joinColumns = @JoinColumn(name = "kullanici_id"))
    @Column(name = "materyal_id")
    private List<String> oduncAlinanMateryaller;

    @ElementCollection
    @CollectionTable(name = "kullanici_odunc_tarihleri", joinColumns = @JoinColumn(name = "kullanici_id"))
    @MapKeyColumn(name = "materyal_id")
    @Column(name = "tarih")
    private Map<String, String> oduncTarihleri;

    @ElementCollection
    @CollectionTable(name = "kullanici_iade_tarihleri", joinColumns = @JoinColumn(name = "kullanici_id"))
    @MapKeyColumn(name = "materyal_id")
    @Column(name = "tarih")
    private Map<String, String> iadeTarihleri;

    @ElementCollection
    @CollectionTable(name = "kullanici_odunc_ceza", joinColumns = @JoinColumn(name = "kullanici_id"))
    @MapKeyColumn(name = "materyal_id")
    @Column(name = "ceza_tutari")
    private Map<String, Double> oduncCeza;

    @ElementCollection
    @CollectionTable(name = "kullanici_bildirimler", joinColumns = @JoinColumn(name = "kullanici_id"))
    private List<Bildirim> bildirimler;

    public User(String isim, String tcNo, Role rol, String sifre) {
        this.id = UUID.randomUUID().toString();
        this.isim = isim;
        this.tcNo = tcNo;
        this.rol = rol;
        this.sifre = sifre;
        this.krediPuani = 100;
        this.oduncAlinanMateryaller = new ArrayList<>();
        this.oduncTarihleri = new HashMap<>();
        this.iadeTarihleri = new HashMap<>();
        this.oduncCeza = new HashMap<>();
        this.bildirimler = new ArrayList<>();
        this.bildirimler.add(new Bildirim("primary", "fa-user-plus", "Sisteme hoş geldiniz!", "Şimdi"));
    }

    // --- Setter'lar (@Data yerine @Getter kullanılıyor, setter'lar açıkça tanımlanmıştır) ---
    public void setId(String id) { this.id = id; }
    public void setIsim(String isim) { this.isim = isim; }
    public void setTcNo(String tcNo) { this.tcNo = tcNo; }
    // DİKKAT: setRol() yetki yükseltme riski taşır — yalnızca admin işlemlerinde çağrılmalıdır
    public void setRol(Role rol) { this.rol = rol; }
    // DİKKAT: setSifre() yalnızca AuthManager.hashPassword()/registerPassword() çıktısı ile çağrılmalıdır
    public void setSifre(String sifre) { this.sifre = sifre; }
    public void setToken(String token) { this.token = token; }
    public void setTokenExpiry(long tokenExpiry) { this.tokenExpiry = tokenExpiry; }
    public void setKrediPuani(int krediPuani) { this.krediPuani = krediPuani; }
    public void setTotpSecretKey(String totpSecretKey) { this.totpSecretKey = totpSecretKey; }
    public void setIkiFAEtkin(boolean ikiFAEtkin) { this.ikiFAEtkin = ikiFAEtkin; }
    public void setEmail(String email) { this.email = email; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }

    public String getTcNo(User talepEden) {
        if (talepEden.getRol() == Role.ADMIN || talepEden.equals(this)) {
            return this.tcNo;
        }
        return "ERISIM ENGELLENDI: Yetkisiz sorgulama!";
    }

    public String getTcNoDogrudan() { return tcNo; }

    public List<Bildirim> getBildirimler() {
        if (bildirimler == null) {
            bildirimler = new ArrayList<>();
        }
        return bildirimler;
    }

    public void materyalOduncAl(String materyalId) {
        if (!oduncAlinanMateryaller.contains(materyalId)) oduncAlinanMateryaller.add(materyalId);
    }

    public void materyalIadeEt(String materyalId) {
        oduncAlinanMateryaller.remove(materyalId);
    }

    public void setOduncTarihi(String materyalId, String tarih) { oduncTarihleri.put(materyalId, tarih); }
    public String getOduncTarihi(String materyalId) { return oduncTarihleri.get(materyalId); }
    public void setIadeTarihi(String materyalId, String tarih) { iadeTarihleri.put(materyalId, tarih); }
    public String getIadeTarihi(String materyalId) { return iadeTarihleri.get(materyalId); }
    public void setOduncCeza(String materyalId, double ceza) { oduncCeza.put(materyalId, ceza); }
    public double getOduncCeza(String materyalId) { return oduncCeza.getOrDefault(materyalId, 0.0); }

    public void puanGuncelle(int miktar) {
        this.krediPuani += miktar;
        if (this.krediPuani > 100) this.krediPuani = 100;
        else if (this.krediPuani < 0) this.krediPuani = 0;
    }
}
