package com.akillikutup.core;

public class Kitap extends Materyal {
    private String isbn;

    public Kitap(String baslik, int stokAdedi, double birimFiyat, String isbn) {
        super(baslik, stokAdedi, birimFiyat);
        this.isbn = isbn;
    }

    @Override
    public void oduncVer() {
        // IS MANTIGI: Fiziksel kitap stoktan duser
        if (stoktaVarMi()) {
            this.stokAdedi--;
        }
    }

    @Override
    public void iadeEt() {
        // IS MANTIGI: Iade edilen kitap stoga geri girer
        this.stokAdedi++;
    }

    @Override
    public double cezaHesapla(int gecikmeGunu) {
        if (gecikmeGunu <= 0) return 0;
        // Fiziksel kitap cezasi: Gun x 2.5 TL + Birim Fiyat %10
        return (gecikmeGunu * 2.5) + (getBirimFiyat() * 0.10);
    }
}