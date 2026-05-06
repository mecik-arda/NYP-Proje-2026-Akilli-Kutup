package com.akillikutup.core;

import java.util.UUID;

public class DijitalMedya extends Materyal {
    private String dosyaFormati;
    private String sonUretilenLisans;
    private int toplamErisimSayisi;
    private final int MAX_ERISIM_LIMITI = 1000; // Is kurali: Bir dijital varlik en fazla 1000 kez acilabilir

    public DijitalMedya(String baslik, double birimFiyat, String dosyaFormati) {
        // Dijital varliklarda stok kavramini teknik olarak 'erisim limiti' gibi dusunuyoruz
        super(baslik, 1000, birimFiyat);
        this.dosyaFormati = dosyaFormati;
        this.toplamErisimSayisi = 0;
    }

    @Override
    public void oduncVer() {
        // IS MANTIGI 1: Erisim limiti kontrolu
        if (toplamErisimSayisi >= MAX_ERISIM_LIMITI) {
            System.out.println("HATA: Bu dijital varligin maksimum erisim limiti dolmustur.");
            return;
        }

        // IS MANTIGI 2: Dinamik Lisans Anahtari Uretimi (UUID kullanarak)
        this.sonUretilenLisans = UUID.randomUUID().toString();

        // IS MANTIGI 3: Durum Guncelleme
        this.toplamErisimSayisi++;
        this.stokAdedi--; // Soyut siniftaki stokAdedi'ni (lisans sayisi gibi) azaltiyoruz

        // Sonucun raporlanmasi (Sadece yazi degil, islem sonucu gosterimi)
        System.out.println("ISLEM BASARILI: " + getBaslik() + " icin erisim izni verildi.");
        System.out.println("LISANS ANAHTARI: " + sonUretilenLisans);
        System.out.println("FORMAT: " + dosyaFormati);
    }

    @Override
    public void iadeEt() {
        // IS MANTIGI: Dijitalde iade, lisansin gecersiz kilinmasidir
        System.out.println("LISANS IPTAL EDILDI: " + sonUretilenLisans + " artik kullanilamaz.");
        this.sonUretilenLisans = null;
    }

    @Override
    public double cezaHesapla(int gun) {
        if (gun <= 0) return 0;
        // Dijital varlik cezasi: (Gun x 0.5) + (Birim Fiyat %5)
        return (gun * 0.5) + (getBirimFiyat() * 0.05);
    }

    // Getterlar (Arda'nin DatabaseManager'da kullanmasi icin gerekecek)
    public String getSonUretilenLisans() { return sonUretilenLisans; }
    public int getToplamErisimSayisi() { return toplamErisimSayisi; }
}