package com.akillikutup.core;

public abstract class Kullanici {
    private String id;
    private String isim;
    private String tcNo;
    private String rol;
    private String sifre;
    private String token;
    protected int krediPuani;
    protected java.util.List<String> oduncAlinanMateryaller;
    protected java.util.List<Bildirim> bildirimler;
    protected String geminiApiKey;


    public Kullanici(String isim, String tcNo, String rol, String sifre) {
        this.id = java.util.UUID.randomUUID().toString();
        this.isim = isim;
        this.tcNo = tcNo;
        this.rol = rol;
        this.sifre = sifre;
        this.krediPuani = 100;
        this.oduncAlinanMateryaller = new java.util.ArrayList<>();
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
    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }
}