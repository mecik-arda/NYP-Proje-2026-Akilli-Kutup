package com.akillikutup.core;

public class Kitap extends Materyal {
    private String isbn;

    public Kitap(String baslik, int stokAdedi, double birimFiyat, String isbn) {
        super(baslik, stokAdedi, birimFiyat);
        this.isbn = isbn;
    }

    @Override
    public void oduncVer() {
        // DUZELTME 2: oduncVer() artik geri bildirim veriyor.
        // Daha onceden stok azaltiliyordu ama hicbir cikti uretilmiyordu.
        // Stok kontrolu Uye.materyalAl() tarafindan yapilir; bu metod sadece islemleri uygular.
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
        // IS MANTIGI: Iade edilen kitap stoga geri girer
        this.stokAdedi++;
        System.out.println("IADE BASARILI: '" + getBaslik() + "' stoga geri eklendi. Guncel stok: " + this.stokAdedi);
    }

    @Override
    public double cezaHesapla(int gecikmeGunu) {
        if (gecikmeGunu <= 0) return 0;
        // Fiziksel kitap cezasi: Gun x 2.5 TL + Birim Fiyat %10
        return (gecikmeGunu * 2.5) + (getBirimFiyat() * 0.10);
    }
}