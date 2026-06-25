package com.akillikutup.material;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kitaplar")
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Kitap extends Materyal implements IOduncAlinabilir {

    @Column(name = "isbn", length = 20)
    private String isbn;

    @Column(name = "yazar")
    private String yazar;

    @Column(name = "kategori")
    private String kategori;

    @Column(name = "kapak_gorseli", length = 500)
    private String kapakGorseli;

    public Kitap(String baslik, int stokAdedi, double birimFiyat, String isbn) {
        super(baslik, stokAdedi, birimFiyat);
        this.isbn = isbn;
    }

    // Setter'lar JPA, JSON deserializasyonu ve diğer paketler için public
    public void setYazar(String yazar) { this.yazar = yazar; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setKapakGorseli(String kapakGorseli) { this.kapakGorseli = kapakGorseli; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    @Override
    public String getMateryalTuru() { return "Kitap"; }

    @Override
    public void oduncVer() {
        if (stoktaVarMi()) {
            this.stokAdedi--;
        }
    }

    @Override
    public void iadeEt() {
        this.stokAdedi++;
    }

    @Override
    public double cezaHesapla(int gecikmeGunu) {
        if (gecikmeGunu <= 0) return 0;
        return (gecikmeGunu * 2.5) + (getBirimFiyat() * 0.10);
    }
}
