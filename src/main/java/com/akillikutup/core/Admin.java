package com.akillikutup.core;

public class Admin extends Kullanici {

    public Admin(String isim, String tcNo, String sifre) {
        super(isim, tcNo, "ADMIN", sifre);
    }

    @Override
    public void goreviniYap() {
        System.out.println("Admin Yonetim Paneli Aktif. Envanter guncellenebilir.");
    }

    public void envanterEkle(Materyal m) {
        System.out.println("ENVANTER GUNCELLEME: '" + m.getBaslik() + "' sisteme kaydedildi.");
    }
}