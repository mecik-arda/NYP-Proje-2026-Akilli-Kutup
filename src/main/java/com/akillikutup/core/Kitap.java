package com.akillikutup.core;

public class Kitap extends Materyal implements IOduncAlinabilir {
    private String isbn;
    private String yazar;
    private String kategori;
    private String kapakGorseli;

    public Kitap(String baslik, int stokAdedi, double birimFiyat, String isbn) {
        super(baslik, stokAdedi, birimFiyat);
        this.isbn = isbn;
    }

    @Override
    public void oduncVer() {
        if (stoktaVarMi()) {
            this.stokAdedi--;
            System.out.println("ISLEM BASARILI: '" + getBaslik() + "' fiziksel kitabi teslim edildi.");
            System.out.println("ISBN: " + isbn + " | Kalan stok: " + this.stokAdedi);
        } else {
            System.out.println("HATA: '" + getBaslik() + "' icin stok bulunmamaktadir.");
        }
    }

    @Override
    public void iadeEt() {
        this.stokAdedi++;
        System.out.println("IADE BASARILI: '" + getBaslik() + "' stoga geri eklendi. Guncel stok: " + this.stokAdedi);
    }

    @Override
    public double cezaHesapla(int gecikmeGunu) {
        if (gecikmeGunu <= 0) return 0;
        return (gecikmeGunu * 2.5) + (getBirimFiyat() * 0.10);
    }

    public String getIsbn() { return isbn; }
    
    public String getYazar() { return yazar; }
    public void setYazar(String yazar) { this.yazar = yazar; }
    
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    
    public String getKapakGorseli() { return kapakGorseli; }
    public void setKapakGorseli(String kapakGorseli) { this.kapakGorseli = kapakGorseli; }
}