package com.akillikutup.core;

public abstract class Kullanici {
    private String id;
    private String isim;
    private String tcNo;
    private String rol;
    private String sifre;
    private String token;
    private long tokenExpiry;
    protected int krediPuani;
    protected java.util.List<String> oduncAlinanMateryaller;
    protected java.util.Map<String, String> oduncTarihleri;   // materyalId -> oduncVerilisTarihi (ISO)
    protected java.util.Map<String, String> iadeTarihleri;    // materyalId -> iadeTarihi (ISO)
    protected java.util.Map<String, Double> oduncCeza;        // materyalId -> cezaTutari
    protected java.util.List<Bildirim> bildirimler;
    protected String geminiApiKey;
    private String email;


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