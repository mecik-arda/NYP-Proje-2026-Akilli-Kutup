package com.akillikutup.core;

public abstract class Kullanici {
    private String isim;
    private String tcNo;
    private String rol;
    protected int krediPuani; // Uye'de degisecek

    public Kullanici(String isim, String tcNo, String rol) {
        this.isim = isim;
        this.tcNo = tcNo;
        this.rol = rol;
        this.krediPuani = 100; // Herkes 100 puanla baslar
    }

    // GUVENLIK: TC bilgisini sadece Admin veya kisi kendisi gorebilir
    public String getTcNo(Kullanici talepEden) {
        if (talepEden.getRol().equals("ADMIN") || talepEden.equals(this)) {
            return this.tcNo;
        }
        return "ERISIM ENGELLENDI: Yetkisiz sorgulama!";
    }

    public abstract void goreviniYap();

    public String getIsim() { return isim; }
    public String getRol() { return rol; }
    public int getKrediPuani() { return krediPuani; }
}