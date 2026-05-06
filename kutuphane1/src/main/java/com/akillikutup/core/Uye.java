package com.akillikutup.core;

public class Uye extends Kullanici {

    public Uye(String isim, String tcNo) {
        super(isim, tcNo, "UYE");
    }

    @Override
    public void goreviniYap() {
        System.out.println("Uye Materyal Islemleri Paneli Aktif.");
    }

    // IS MANTIGI: Kredi puani kontrolu yaparak odunc verme
    public void materyalAl(Materyal m) {
        if (this.krediPuani < 20) {
            System.out.println("HATA: Kredi puaniniz ( " + krediPuani + " ) cok dusuk. Islem engellendi!");
            return;
        }

        if (m.stoktaVarMi()) {
            m.oduncVer();
            System.out.println(getIsim() + " adli uye '" + m.getBaslik() + "' urununu teslim aldi.");
        } else {
            System.out.println("HATA: Urun stokta bulunmamaktadir.");
        }
    }

    public void puanGuncelle(int miktar) {
        this.krediPuani += miktar;
        if (this.krediPuani > 100) this.krediPuani = 100;
        if (this.krediPuani < 0) this.krediPuani = 0;
    }
}