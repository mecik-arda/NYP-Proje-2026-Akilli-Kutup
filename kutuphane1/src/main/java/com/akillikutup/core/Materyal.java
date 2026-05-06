package com.akillikutup.core;

import java.util.UUID;

public abstract class Materyal {
    private String id;
    private String baslik;
    protected int stokAdedi; // Alt siniflar (Kitap) degistirebilmeli
    private double birimFiyat;

    public Materyal(String baslik, int stokAdedi, double birimFiyat) {
        this.id = UUID.randomUUID().toString(); // Her materyale essiz kimlik
        this.baslik = baslik;
        this.stokAdedi = stokAdedi;
        this.birimFiyat = birimFiyat;
    }

    // Is Mantigi: Stokta urun var mi kontrolu
    public boolean stoktaVarMi() {
        return this.stokAdedi > 0;
    }

    // Soyut metodlar: Her materyal tipi (Kitap/Dijital) kendi kuralini yazacak
    public abstract void oduncVer();
    public abstract void iadeEt();
    public abstract double cezaHesapla(int gecikmeGunu);

    // Getterlar
    public String getBaslik() { return baslik; }
    public double getBirimFiyat() { return birimFiyat; }
    public int getStokAdedi() { return stokAdedi; }
    public String getId() { return id; }
}