package com.akillikutup.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CoreTest {

    @Test
    public void testFizikselKitapOduncVeCeza() {
        // Kitap olusturma: Baslik, Stok, Birim Fiyat, ISBN
        Kitap kitap = new Kitap("Java Programming", 1, 100.0, "123-456");

        // Odunc alma testi: Stok dussun mu?
        kitap.oduncVer();
        assertEquals(0, kitap.getStokAdedi());
        assertFalse(kitap.stoktaVarMi());

        // Ceza hesaplama testi: (5 gun * 2.5 TL) + (100 TL * %10) = 12.5 + 10 = 22.5 TL
        double beklenenCeza = 22.5;
        assertEquals(beklenenCeza, kitap.cezaHesapla(5));
    }

    @Test
    public void testDijitalMedyaErisimVeCeza() {
        // Dijital Medya: Baslik, Birim Fiyat, Format
        DijitalMedya dijital = new DijitalMedya("AI Handbook", 50.0, "PDF");

        // Erisim (Odunc) testi: Lisans uretildi mi?
        dijital.oduncVer();
        assertNotNull(dijital.getSonUretilenLisans());
        assertEquals(1, dijital.getToplamErisimSayisi());

        // Ceza testi: (10 gun * 0.5 TL) + (50 TL * %5) = 5 + 2.5 = 7.5 TL
        assertEquals(7.5, dijital.cezaHesapla(10));
    }

    @Test
    public void testUyeKrediPuaniVeOduncSistemi() {
        Uye uye = new Uye("Ahmet Guler", "11122233344"); //
        Kitap kitap = new Kitap("Test Kitabi", 5, 100.0, "000");

        // Is Kurali: Dusuk kredi puani ile odunc alinamamali (Limit: 20)
        uye.puanGuncelle(-90); // Puan 10'a duser
        uye.materyalAl(kitap);
        assertEquals(5, kitap.getStokAdedi()); // Kitap alinmamali (Stok degismemeli)

        // Normal kredi testi
        uye.puanGuncelle(50); // Puan 60 olur
        uye.materyalAl(kitap);
        assertEquals(4, kitap.getStokAdedi()); // Kitap alinmali
    }

    @Test
    public void testGuvenlikTCNoErisimi() {
        Admin admin = new Admin("Eren Gider", "12345678901"); //
        Uye uye = new Uye("Mehmet Yilmaz", "98765432109");

        // Guvenlik Katmani: Admin her seyi gorebilir
        assertEquals("98765432109", uye.getTcNo(admin));

        // Guvenlik Katmani: Uye sadece kendisininkini gorebilir
        assertEquals("98765432109", uye.getTcNo(uye));

        // Guvenlik Katmani: Uye baskasinin TC'sini goremez
        assertEquals("ERISIM ENGELLENDI: Yetkisiz sorgulama!", admin.getTcNo(uye));
    }
}