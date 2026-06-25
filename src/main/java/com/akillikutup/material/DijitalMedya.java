package com.akillikutup.material;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "dijital_medyalar")
@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DijitalMedya extends Materyal implements IOduncAlinabilir {

    @Column(name = "dosya_formati")
    private String dosyaFormati;

    @Column(name = "tur")
    private String tur;

    @Column(name = "boyut")
    private String boyut;

    @Column(name = "son_uretilen_lisans", length = 36)
    private String sonUretilenLisans;

    @Column(name = "toplam_erisim_sayisi")
    private int toplamErisimSayisi;

    @Transient
    private final int MAX_ERISIM_LIMITI = 1000;

    public DijitalMedya(String baslik, double birimFiyat, String dosyaFormati, String tur, String boyut) {
        super(baslik, 0, birimFiyat);
        this.dosyaFormati = dosyaFormati;
        this.tur = tur;
        this.boyut = boyut;
        this.toplamErisimSayisi = 0;
    }

    // Setter'lar JPA, JSON deserializasyonu ve diğer paketler için public
    public void setDosyaFormati(String dosyaFormati) { this.dosyaFormati = dosyaFormati; }
    public void setTur(String tur) { this.tur = tur; }
    public void setBoyut(String boyut) { this.boyut = boyut; }
    public void setToplamErisimSayisi(int toplamErisimSayisi) { this.toplamErisimSayisi = toplamErisimSayisi; }

    @Override
    public String getMateryalTuru() { return "DijitalMedya"; }

    @Override
    public boolean stoktaVarMi() {
        return toplamErisimSayisi < MAX_ERISIM_LIMITI;
    }

    @Override
    public void oduncVer() {
        if (!stoktaVarMi()) return;
        this.sonUretilenLisans = UUID.randomUUID().toString();
        this.toplamErisimSayisi++;
    }

    @Override
    public void iadeEt() {
        if (sonUretilenLisans == null) return;
        this.sonUretilenLisans = null;
        if (toplamErisimSayisi > 0) this.toplamErisimSayisi--;
    }

    @Override
    public double cezaHesapla(int gun) {
        if (gun <= 0) return 0;
        return (gun * 0.5) + (getBirimFiyat() * 0.05);
    }
}
