package com.akillikutup.core;

import java.util.UUID;

// DUZELTME 1: Materyal artik IMateryal'i implement ediyor.
// Daha onceden bu satir eksikti; interface tanimlandigi halde hiyerarsiye dahil edilmemisti.
public abstract class Materyal implements IMateryal {
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
    @Override
    public boolean stoktaVarMi() {
        return this.stokAdedi > 0;
    }

    // Soyut metodlar: Her materyal tipi (Kitap/Dijital) kendi kuralini yazacak
    @Override
    public abstract void oduncVer();

    @Override
    public abstract void iadeEt();

    @Override
    public abstract double cezaHesapla(int gecikmeGunu);

    // Getterlar
    public String getBaslik() { return baslik; }
    public double getBirimFiyat() { return birimFiyat; }
    public int getStokAdedi() { return stokAdedi; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}