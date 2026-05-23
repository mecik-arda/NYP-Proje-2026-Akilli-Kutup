package com.akillikutup.core;

import java.util.UUID;

public class DijitalMedya extends Materyal {
    private String dosyaFormati;
    private String sonUretilenLisans;
    private int toplamErisimSayisi;
    // DUZELTME 3: stokAdedi ve toplamErisimSayisi ikili sayaci kaldirildi.
    // Artik tek dogruluk kaynagi olarak toplamErisimSayisi kullaniliyor.
    // Materyal'daki stokAdedi'nin dijital varlikta anlami yok; soyut sinif
    // constructor'i icin 0 gonderiliyor (stoktaVarMi() override ediliyor).
    private final int MAX_ERISIM_LIMITI = 1000;

    public DijitalMedya(String baslik, double birimFiyat, String dosyaFormati) {
        // stokAdedi'ne 0 gonderiyoruz; dijital varlik icin stok kavramini
        // stoktaVarMi() override'i ile erisim limitine gore yonetiyoruz.
        super(baslik, 0, birimFiyat);
        this.dosyaFormati = dosyaFormati;
        this.toplamErisimSayisi = 0;
    }

    // DUZELTME 3 (devam): stoktaVarMi() override ediliyor.
    // Dijital varlik icin "stokta var mi" = "erisim limiti dolmadi mi" demektir.
    @Override
    public boolean stoktaVarMi() {
        return toplamErisimSayisi < MAX_ERISIM_LIMITI;
    }

    @Override
    public void oduncVer() {
        // IS MANTIGI 1: Erisim limiti kontrolu
        if (!stoktaVarMi()) {
            System.out.println("HATA: Bu dijital varligin maksimum erisim limiti dolmustur.");
            return;
        }

        // IS MANTIGI 2: Dinamik Lisans Anahtari Uretimi (UUID kullanarak)
        this.sonUretilenLisans = UUID.randomUUID().toString();

        // IS MANTIGI 3: Durum Guncelleme (artik tek sayac: toplamErisimSayisi)
        this.toplamErisimSayisi++;

        System.out.println("ISLEM BASARILI: " + getBaslik() + " icin erisim izni verildi.");
        System.out.println("LISANS ANAHTARI: " + sonUretilenLisans);
        System.out.println("FORMAT: " + dosyaFormati);
        System.out.println("Toplam erisim: " + toplamErisimSayisi + " / " + MAX_ERISIM_LIMITI);
    }

    @Override
    public void iadeEt() {
        // DUZELTME 4: iadeEt() artik erisim sayacini da geri aliyor.
        // Daha onceden sadece lisans null yapiliyordu; toplamErisimSayisi ve stokAdedi
        // geri alinmiyordu. Bu, lisansin iptal edilmesine ragmen erisim kotasinin
        // tuketilmis sayilmasi anlamina geliyordu — mantiksal bir hatadir.
        if (sonUretilenLisans == null) {
            System.out.println("UYARI: Iptal edilecek aktif bir lisans bulunamadi.");
            return;
        }
        System.out.println("LISANS IPTAL EDILDI: " + sonUretilenLisans + " artik kullanilamaz.");
        this.sonUretilenLisans = null;
        if (toplamErisimSayisi > 0) {
            this.toplamErisimSayisi--;
        }
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
    public String getDosyaFormati() { return dosyaFormati; }
}