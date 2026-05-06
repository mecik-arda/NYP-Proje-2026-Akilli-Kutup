package com.akillikutup.core;

public class Admin extends Kullanici {

    public Admin(String isim, String tcNo) {
        super(isim, tcNo, "ADMIN");
    }

    @Override
    public void goreviniYap() {
        System.out.println("Admin Yonetim Paneli Aktif. Envanter guncellenebilir.");
    }

    // IS MANTIGI: Yeni materyal girisi
    public void envanterEkle(Materyal m) {
        System.out.println("ENVANTER GUNCELLEME: '" + m.getBaslik() + "' sisteme kaydedildi.");
        // Buradan sonra Arda'nin DatabaseManager'i cagirilir.
    }
}