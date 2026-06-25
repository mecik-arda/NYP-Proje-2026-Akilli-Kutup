package com.akillikutup.material;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@MappedSuperclass
@Getter
@NoArgsConstructor
public abstract class Materyal implements IMateryal {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "baslik", nullable = false)
    private String baslik;

    @Column(name = "stok_adedi")
    protected int stokAdedi;

    @Column(name = "birim_fiyat")
    private double birimFiyat;

    public Materyal(String baslik, int stokAdedi, double birimFiyat) {
        this.id = UUID.randomUUID().toString();
        this.baslik = baslik;
        this.stokAdedi = stokAdedi;
        this.birimFiyat = birimFiyat;
    }

    // Setter'lar JPA, JSON deserializasyonu ve diğer paketler için public
    public void setId(String id) { this.id = id; }
    public void setBaslik(String baslik) { this.baslik = baslik; }
    public void setBirimFiyat(double birimFiyat) { this.birimFiyat = birimFiyat; }

    @Override
    public boolean stoktaVarMi() {
        return this.stokAdedi > 0;
    }

    @Override
    public abstract double cezaHesapla(int gecikmeGunu);

    /**
     * Polimorfik tür bilgisi — instanceof zincirlerini ortadan kaldırır.
     */
    public abstract String getMateryalTuru();
}
