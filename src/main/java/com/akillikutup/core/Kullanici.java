package com.akillikutup.core;

import jakarta.persistence.*;

@Entity
@Table(name = "kullanicilar")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "rol", discriminatorType = DiscriminatorType.STRING)
public abstract class Kullanici {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "isim", nullable = false)
    private String isim;

    @Column(name = "tc_no", length = 11)
    private String tcNo;

    @Column(name = "rol", insertable = false, updatable = false)
    private String rol;

    @Column(name = "sifre")
    private String sifre;

    @Column(name = "token", length = 500)
    private String token;

    @Column(name = "token_expiry")
    private long tokenExpiry;

    @Column(name = "kredi_puani")
    protected int krediPuani;

    @ElementCollection
    @CollectionTable(name = "kullanici_odunc_materyaller", joinColumns = @JoinColumn(name = "kullanici_id"))
    @Column(name = "materyal_id")
    protected java.util.List<String> oduncAlinanMateryaller;

    @ElementCollection
    @CollectionTable(name = "kullanici_odunc_tarihleri", joinColumns = @JoinColumn(name = "kullanici_id"))
    @MapKeyColumn(name = "materyal_id")
    @Column(name = "tarih")
    protected java.util.Map<String, String> oduncTarihleri;

    @ElementCollection
    @CollectionTable(name = "kullanici_iade_tarihleri", joinColumns = @JoinColumn(name = "kullanici_id"))
    @MapKeyColumn(name = "materyal_id")
    @Column(name = "tarih")
    protected java.util.Map<String, String> iadeTarihleri;

    @ElementCollection
    @CollectionTable(name = "kullanici_odunc_ceza", joinColumns = @JoinColumn(name = "kullanici_id"))
    @MapKeyColumn(name = "materyal_id")
    @Column(name = "ceza_tutari")
    protected java.util.Map<String, Double> oduncCeza;

    @ElementCollection
    @CollectionTable(name = "kullanici_bildirimler", joinColumns = @JoinColumn(name = "kullanici_id"))
    protected java.util.List<Bildirim> bildirimler;

    @Column(name = "gemini_api_key", length = 500)
    protected String geminiApiKey;

    @Column(name = "email")
    private String email;

    /** JPA için zorunlu no-arg constructor (protected). */
    protected Kullanici() {
        this.id = java.util.UUID.randomUUID().toString();
        this.oduncAlinanMateryaller = new java.util.ArrayList<>();
        this.oduncTarihleri = new java.util.HashMap<>();
        this.iadeTarihleri = new java.util.HashMap<>();
        this.oduncCeza = new java.util.HashMap<>();
        this.bildirimler = new java.util.ArrayList<>();
    }

    public Kullanici(String isim, String tcNo, String rol, String sifre) {
        this.id = java.util.UUID.randomUUID().toString();
        this.isim = isim;
        this.tcNo = tcNo;
        this.rol = rol;
        this.sifre = sifre;
        this.krediPuani = 100;
        this.oduncAlinanMateryaller = new java.util.ArrayList<>();
        this.oduncTarihleri = new java.util.HashMap<>();
        this.iadeTarihleri = new java.util.HashMap<>();
        this.oduncCeza = new java.util.HashMap<>();
        this.bildirimler = new java.util.ArrayList<>();

        // Hoş geldin bildirimi
        this.bildirimler.add(new Bildirim("primary", "fa-user-plus", "Sisteme hoş geldiniz!", "Şimdi"));
    }

    public String getTcNo(Kullanici talepEden) {
        if (talepEden.getRol().equals("ADMIN") || talepEden.equals(this)) {
            return this.tcNo;
        }
        return "ERISIM ENGELLENDI: Yetkisiz sorgulama!";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public long getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(long tokenExpiry) { this.tokenExpiry = tokenExpiry; }

    public abstract void goreviniYap();

    public String getIsim() { return isim; }
    public String getRol() { return rol; }
    public String getSifre() { return sifre; }
    public int getKrediPuani() { return krediPuani; }
    public String getTcNoDogrudan() { return tcNo; }

    public void setTcNo(String tcNo) { this.tcNo = tcNo; }
    public void setSifre(String sifre) { this.sifre = sifre; }
    public void setIsim(String isim) { this.isim = isim; }
    public java.util.List<String> getOduncAlinanMateryaller() { return oduncAlinanMateryaller; }
    public java.util.List<Bildirim> getBildirimler() { 
        if (bildirimler == null) {
            bildirimler = new java.util.ArrayList<>();
        }
        return bildirimler; 
    }
    public void materyalOduncAl(String materyalId) { if(!oduncAlinanMateryaller.contains(materyalId)) oduncAlinanMateryaller.add(materyalId); }
    public void materyalIadeEt(String materyalId) { oduncAlinanMateryaller.remove(materyalId); }

    public java.util.Map<String, String> getOduncTarihleri() { return oduncTarihleri; }
    public void setOduncTarihi(String materyalId, String tarih) { oduncTarihleri.put(materyalId, tarih); }
    public String getOduncTarihi(String materyalId) { return oduncTarihleri.get(materyalId); }

    public java.util.Map<String, String> getIadeTarihleri() { return iadeTarihleri; }
    public void setIadeTarihi(String materyalId, String tarih) { iadeTarihleri.put(materyalId, tarih); }
    public String getIadeTarihi(String materyalId) { return iadeTarihleri.get(materyalId); }

    public java.util.Map<String, Double> getOduncCeza() { return oduncCeza; }
    public void setOduncCeza(String materyalId, double ceza) { oduncCeza.put(materyalId, ceza); }
    public double getOduncCeza(String materyalId) { return oduncCeza.getOrDefault(materyalId, 0.0); }
    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kullanici kullanici = (Kullanici) o;
        return java.util.Objects.equals(id, kullanici.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}