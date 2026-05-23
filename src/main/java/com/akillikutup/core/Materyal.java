package com.akillikutup.core;

import java.util.UUID;

public abstract class Materyal implements IMateryal {
    private String id;
    private String baslik;
    protected int stokAdedi;
    private double birimFiyat;

    public Materyal(String baslik, int stokAdedi, double birimFiyat) {
        this.id = UUID.randomUUID().toString();
        this.baslik = baslik;
        this.stokAdedi = stokAdedi;
        this.birimFiyat = birimFiyat;
    }

    @Override
    public boolean stoktaVarMi() {
        return this.stokAdedi > 0;
    }

    @Override
    public abstract void oduncVer();

    @Override
    public abstract void iadeEt();

    @Override
    public abstract double cezaHesapla(int gecikmeGunu);

    public String getBaslik() { return baslik; }
    public double getBirimFiyat() { return birimFiyat; }
    public int getStokAdedi() { return stokAdedi; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}