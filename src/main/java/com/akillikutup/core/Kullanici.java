package com.akillikutup.core;

public abstract class Kullanici {
    private String id;
    private String isim;
    private String tcNo;
    private String rol;
    private String sifre;
    protected int krediPuani;
    protected java.util.List<String> oduncAlinanMateryaller;

    public Kullanici(String isim, String tcNo, String rol, String sifre) {
        this.id = java.util.UUID.randomUUID().toString();
        this.isim = isim;
        this.tcNo = tcNo;
        this.rol = rol;
        this.sifre = sifre;
        this.krediPuani = 100;
        this.oduncAlinanMateryaller = new java.util.ArrayList<>();
    }

    public String getTcNo(Kullanici talepEden) {
        if (talepEden.getRol().equals("ADMIN") || talepEden.equals(this)) {
            return this.tcNo;
        }
        return "ERISIM ENGELLENDI: Yetkisiz sorgulama!";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
    public void materyalOduncAl(String materyalId) { if(!oduncAlinanMateryaller.contains(materyalId)) oduncAlinanMateryaller.add(materyalId); }
    public void materyalIadeEt(String materyalId) { oduncAlinanMateryaller.remove(materyalId); }
}