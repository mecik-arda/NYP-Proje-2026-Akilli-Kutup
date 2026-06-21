package com.akillikutup.core;

import jakarta.persistence.*;

@Entity
@Table(name = "klasorler")
public class Klasor extends Materyal {

    /** JPA için zorunlu no-arg constructor (protected). */
    protected Klasor() {
        super();
    }

    public Klasor(String baslik) {
        super(baslik, 0, 0.0);
    }

    @Override
    public boolean stoktaVarMi() {
        return true;
    }


    @Override
    public double cezaHesapla(int gecikmeGunu) {
        return 0.0;
    }
}
