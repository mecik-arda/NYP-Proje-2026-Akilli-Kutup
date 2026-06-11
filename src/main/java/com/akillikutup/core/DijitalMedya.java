package com.akillikutup.core;

import java.util.UUID;

public class DijitalMedya extends Materyal {
    private String dosyaFormati;
    private String sonUretilenLisans;
    private int toplamErisimSayisi;
    private final int MAX_ERISIM_LIMITI = 1000;

    public DijitalMedya(String baslik, double birimFiyat, String dosyaFormati) {
        super(baslik, 0, birimFiyat);
        this.dosyaFormati = dosyaFormati;
        this.toplamErisimSayisi = 0;
    }

    @Override
    public boolean stoktaVarMi() {
        return toplamErisimSayisi < MAX_ERISIM_LIMITI;
    }

    @Override
    public void oduncVer() {
        if (!stoktaVarMi()) {
            System.out.println("HATA: Bu dijital varligin maksimum erisim limiti dolmustur.");
            return;
        }

        this.sonUretilenLisans = UUID.randomUUID().toString();

        this.toplamErisimSayisi++;

        System.out.println("ISLEM BASARILI: " + getBaslik() + " icin erisim izni verildi.");
        System.out.println("LISANS ANAHTARI: " + sonUretilenLisans);
        System.out.println("FORMAT: " + dosyaFormati);
        System.out.println("Toplam erisim: " + toplamErisimSayisi + " / " + MAX_ERISIM_LIMITI);
    }

    @Override
    public void iadeEt() {
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
        return (gun * 0.5) + (getBirimFiyat() * 0.05);
    }

    public String getSonUretilenLisans() { return sonUretilenLisans; }
    public int getToplamErisimSayisi() { return toplamErisimSayisi; }
    public void setToplamErisimSayisi(int toplamErisimSayisi) { this.toplamErisimSayisi = toplamErisimSayisi; }
    public String getDosyaFormati() { return dosyaFormati; }
}