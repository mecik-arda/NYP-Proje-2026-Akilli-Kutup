package com.akillikutup.core;

public class Klasor extends Materyal {

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
