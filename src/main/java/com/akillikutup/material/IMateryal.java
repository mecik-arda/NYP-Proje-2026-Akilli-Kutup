package com.akillikutup.material;

public interface IMateryal {
    double cezaHesapla(int gecikmeGunu);
    boolean stoktaVarMi();
    /** Polimorfik materyal türü — instanceof zincirlerini ortadan kaldırmak için */
    String getMateryalTuru();
}
