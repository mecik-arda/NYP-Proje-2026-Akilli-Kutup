package com.akillikutup.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CoreTest {

    @Test
    public void testFizikselKitapOduncVeCeza() {
        Kitap kitap = new Kitap("Java Programming", 1, 100.0, "123-456");

        kitap.oduncVer();
        assertEquals(0, kitap.getStokAdedi());
        assertFalse(kitap.stoktaVarMi());

        double beklenenCeza = 22.5;
        assertEquals(beklenenCeza, kitap.cezaHesapla(5));
    }

    @Test
    public void testDijitalMedyaErisimVeCeza() {
        DijitalMedya dijital = new DijitalMedya("AI Handbook", 50.0, "PDF");

        assertTrue(dijital.stoktaVarMi());

        dijital.oduncVer();
        assertNotNull(dijital.getSonUretilenLisans());
        assertEquals(1, dijital.getToplamErisimSayisi());

        dijital.iadeEt();
        assertNull(dijital.getSonUretilenLisans());
        assertEquals(0, dijital.getToplamErisimSayisi());

        assertEquals(7.5, dijital.cezaHesapla(10));
    }

    @Test
    public void testUyeKrediPuaniVeOduncSistemi() {
        Uye uye = new Uye("Ahmet Guler", "11122233344", "test123");
        Kitap kitap = new Kitap("Test Kitabi", 5, 100.0, "000");

        uye.puanGuncelle(-90); 
        uye.materyalAl(kitap);
        assertEquals(5, kitap.getStokAdedi()); 

        uye.puanGuncelle(50);
        uye.materyalAl(kitap);
        assertEquals(4, kitap.getStokAdedi()); 
    }

    @Test
    public void testGuvenlikTCNoErisimi() {
        Admin admin = new Admin("Eren Gider", "12345678901", "admin123");
        Uye uye = new Uye("Mehmet Yilmaz", "98765432109", "uye123");

        assertEquals("98765432109", uye.getTcNo(admin));

        assertEquals("98765432109", uye.getTcNo(uye));

        assertEquals("ERISIM ENGELLENDI: Yetkisiz sorgulama!", admin.getTcNo(uye));
    }
}
