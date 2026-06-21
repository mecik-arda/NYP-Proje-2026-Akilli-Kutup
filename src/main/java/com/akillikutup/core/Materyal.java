package com.akillikutup.core;

import java.util.UUID;
import jakarta.persistence.*;

@MappedSuperclass
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

    /** JPA için zorunlu no-arg constructor (protected). */
    protected Materyal() {
        this.id = UUID.randomUUID().toString();
    }

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
    public abstract double cezaHesapla(int gecikmeGunu);

    public String getBaslik() { return baslik; }
    public double getBirimFiyat() { return birimFiyat; }
    public int getStokAdedi() { return stokAdedi; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Materyal materyal = (Materyal) o;
        return java.util.Objects.equals(id, materyal.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}