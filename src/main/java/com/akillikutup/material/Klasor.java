package com.akillikutup.material;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "klasorler")
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Klasor extends Materyal {

    public Klasor(String baslik) {
        super(baslik, 0, 0.0);
    }

    @Override
    public String getMateryalTuru() { return "Klasor"; }

    @Override
    public boolean stoktaVarMi() {
        return true;
    }

    @Override
    public double cezaHesapla(int gecikmeGunu) {
        return 0.0;
    }
}
