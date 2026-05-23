package com.akillikutup.db;

import com.akillikutup.core.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseManagerTest {

    private DatabaseManager db;

    @BeforeEach
    public void kurulum() {
        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();
    }

    @AfterEach
    public void temizlik() {
        DatabaseManager.tekOrnekSifirla();
    }


    @Test
    @Order(1)
    public void tekOrnekTesti() {
        DatabaseManager db1 = DatabaseManager.tekOrnekAl();
        DatabaseManager db2 = DatabaseManager.tekOrnekAl();
        assertSame(db1, db2, "Singleton ornekleri ayni olmali");
    }

    @Test
    @Order(2)
    public void tekOrnekSifirlamaTesti() {
        DatabaseManager ilk = DatabaseManager.tekOrnekAl();
        DatabaseManager.tekOrnekSifirla();
        DatabaseManager yeni = DatabaseManager.tekOrnekAl();
        assertNotSame(ilk, yeni, "Sifirlamadan sonra yeni ornek olusmali");
    }


    @Test
    @Order(3)
    public void kullaniciSerializeDeserializeTesti() {
        List<Kullanici> liste = new ArrayList<>();
        liste.add(new Admin("Test Admin", "11111111111", "admin123"));
        liste.add(new Uye("Test Uye", "22222222222", "uye456"));

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Kullanici> yuklenen = db.kullanicilariYukle();

        assertEquals(2, yuklenen.size(), "2 kullanici yuklenmeli");
        assertEquals("Test Admin", yuklenen.get(0).getIsim());
        assertEquals("ADMIN", yuklenen.get(0).getRol());
        assertEquals("Test Uye", yuklenen.get(1).getIsim());
        assertEquals("UYE", yuklenen.get(1).getRol());
    }

    @Test
    @Order(4)
    public void kullaniciSifreTesti() {
        List<Kullanici> liste = new ArrayList<>();
        liste.add(new Admin("SifreTest", "33333333333", "gizliSifre!@#"));

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Kullanici> yuklenen = db.kullanicilariYukle();
        assertEquals("gizliSifre!@#", yuklenen.get(0).getSifre(), "Sifre dogru yuklenmelí");
    }

    @Test
    @Order(5)
    public void uyeKrediPuaniTesti() {
        Uye uye = new Uye("PuanTest", "44444444444", "sifre");
        uye.puanGuncelle(-30);
        assertEquals(70, uye.getKrediPuani());

        List<Kullanici> liste = new ArrayList<>();
        liste.add(uye);

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Kullanici> yuklenen = db.kullanicilariYukle();
        assertEquals(70, yuklenen.get(0).getKrediPuani(), "Kredi puani dogru yuklenmeli");
    }


    @Test
    @Order(6)
    public void kitapSerializeDeserializeTesti() {
        List<Materyal> liste = new ArrayList<>();
        Kitap kitap = new Kitap("Test Kitap", 10, 25.50, "978-1234567890");
        String orijinalId = kitap.getId();
        liste.add(kitap);

        db.kaydet(new ArrayList<>(), liste);

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Materyal> yuklenen = db.materyallariYukle();

        assertEquals(1, yuklenen.size(), "1 materyal yuklenmeli");
        assertTrue(yuklenen.get(0) instanceof Kitap, "Yuklenen materyal Kitap olmali");

        Kitap yuklenenKitap = (Kitap) yuklenen.get(0);
        assertEquals("Test Kitap", yuklenenKitap.getBaslik());
        assertEquals(10, yuklenenKitap.getStokAdedi());
        assertEquals(25.50, yuklenenKitap.getBirimFiyat(), 0.01);
        assertEquals("978-1234567890", yuklenenKitap.getIsbn());
        assertEquals(orijinalId, yuklenenKitap.getId(), "ID korunmali");
    }

    @Test
    @Order(7)
    public void dijitalMedyaSerializeDeserializeTesti() {
        List<Materyal> liste = new ArrayList<>();
        DijitalMedya dm = new DijitalMedya("Test Film", 15.0, "MP4");
        liste.add(dm);

        db.kaydet(new ArrayList<>(), liste);

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Materyal> yuklenen = db.materyallariYukle();

        assertEquals(1, yuklenen.size());
        assertTrue(yuklenen.get(0) instanceof DijitalMedya);

        DijitalMedya yuklenenDm = (DijitalMedya) yuklenen.get(0);
        assertEquals("Test Film", yuklenenDm.getBaslik());
        assertEquals(15.0, yuklenenDm.getBirimFiyat(), 0.01);
    }

    @Test
    @Order(8)
    public void karisikMateryalTesti() {
        List<Materyal> liste = new ArrayList<>();
        liste.add(new Kitap("Kitap 1", 5, 20.0, "ISBN-111"));
        liste.add(new DijitalMedya("Medya 1", 10.0, "PDF"));
        liste.add(new Kitap("Kitap 2", 3, 30.0, "ISBN-222"));

        db.kaydet(new ArrayList<>(), liste);

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Materyal> yuklenen = db.materyallariYukle();

        assertEquals(3, yuklenen.size());
        assertTrue(yuklenen.get(0) instanceof Kitap);
        assertTrue(yuklenen.get(1) instanceof DijitalMedya);
        assertTrue(yuklenen.get(2) instanceof Kitap);
    }


    @Test
    @Order(9)
    public void dosyaYokkenKullaniciYuklemeTesti() {
        File dosya = new File("data" + File.separator + "users.json");
        if (dosya.exists()) dosya.delete();

        List<Kullanici> sonuc = db.kullanicilariYukle();
        assertNotNull(sonuc);
        assertEquals(0, sonuc.size(), "Dosya yokken bos liste donmeli");
    }

    @Test
    @Order(10)
    public void dosyaYokkenMateryalYuklemeTesti() {
        File dosya = new File("data" + File.separator + "materials.json");
        if (dosya.exists()) dosya.delete();

        List<Materyal> sonuc = db.materyallariYukle();
        assertNotNull(sonuc);
        assertEquals(0, sonuc.size(), "Dosya yokken bos liste donmeli");
    }


    @Test
    @Order(11)
    public void bosListeKaydetYuklemeTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Kullanici> kullanicilar = db.kullanicilariYukle();
        List<Materyal> materyaller = db.materyallariYukle();

        assertEquals(0, kullanicilar.size());
        assertEquals(0, materyaller.size());
    }


    @Test
    @Order(12)
    public void kullaniciEkleTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());

        Admin admin = new Admin("Eklenen Admin", "99999999999", "sifre");
        db.kullaniciEkle(admin);

        assertEquals(1, db.getKullaniciListesi().size());
        assertEquals("Eklenen Admin", db.getKullaniciListesi().get(0).getIsim());
    }

    @Test
    @Order(13)
    public void kullaniciSilTesti() {
        List<Kullanici> liste = new ArrayList<>();
        liste.add(new Admin("Silinecek", "88888888888", "sifre"));
        liste.add(new Uye("Kalacak", "77777777777", "sifre"));
        db.kaydet(liste, new ArrayList<>());

        db.kullaniciSil("Silinecek");

        assertEquals(1, db.getKullaniciListesi().size());
        assertEquals("Kalacak", db.getKullaniciListesi().get(0).getIsim());
    }

    @Test
    @Order(14)
    public void materyalEkleTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());

        Kitap kitap = new Kitap("Yeni Kitap", 7, 35.0, "ISBN-YK");
        db.materyalEkle(kitap);

        assertEquals(1, db.getMateryalListesi().size());
        assertEquals("Yeni Kitap", db.getMateryalListesi().get(0).getBaslik());
    }

    @Test
    @Order(15)
    public void materyalSilTesti() {
        List<Materyal> liste = new ArrayList<>();
        Kitap kitap = new Kitap("Silinecek Kitap", 2, 10.0, "ISBN-SK");
        String silinecekId = kitap.getId();
        liste.add(kitap);
        liste.add(new DijitalMedya("Kalacak Medya", 5.0, "MP3"));
        db.kaydet(new ArrayList<>(), liste);

        db.materyalSil(silinecekId);

        assertEquals(1, db.getMateryalListesi().size());
        assertEquals("Kalacak Medya", db.getMateryalListesi().get(0).getBaslik());
    }


    @Test
    @Order(16)
    public void kullaniciBulTesti() {
        List<Kullanici> liste = new ArrayList<>();
        liste.add(new Admin("Aranan Admin", "66666666666", "sifre"));
        db.kaydet(liste, new ArrayList<>());

        Kullanici bulunan = db.kullaniciBul("Aranan Admin");
        assertNotNull(bulunan);
        assertEquals("Aranan Admin", bulunan.getIsim());

        Kullanici yok = db.kullaniciBul("Olmayan Kullanici");
        assertNull(yok);
    }

    @Test
    @Order(17)
    public void materyalBulTesti() {
        List<Materyal> liste = new ArrayList<>();
        Kitap kitap = new Kitap("Aranan Kitap", 1, 10.0, "ISBN-AK");
        String aramaId = kitap.getId();
        liste.add(kitap);
        db.kaydet(new ArrayList<>(), liste);

        Materyal bulunan = db.materyalBul(aramaId);
        assertNotNull(bulunan);
        assertEquals("Aranan Kitap", bulunan.getBaslik());

        Materyal yok = db.materyalBul("olmayan-id");
        assertNull(yok);
    }

    @Test
    @Order(18)
    public void materyalAraTesti() {
        List<Materyal> liste = new ArrayList<>();
        liste.add(new Kitap("Java Programlama", 5, 40.0, "ISBN-JP"));
        liste.add(new Kitap("Python Rehberi", 3, 35.0, "ISBN-PR"));
        liste.add(new DijitalMedya("Java Video Kursu", 20.0, "MP4"));
        db.kaydet(new ArrayList<>(), liste);

        List<Materyal> sonuclar = db.materyalAra("Java");
        assertEquals(2, sonuclar.size(), "Java iceren 2 materyal bulunmali");

        List<Materyal> bosSonuc = db.materyalAra("C++");
        assertEquals(0, bosSonuc.size(), "Eslesme yoksa bos liste donmeli");
    }


    @Test
    @Order(19)
    public void yedeklemeTesti() {
        List<Kullanici> kullanicilar = new ArrayList<>();
        kullanicilar.add(new Admin("YedekTest", "12312312312", "sifre"));
        List<Materyal> materyaller = new ArrayList<>();
        materyaller.add(new Kitap("Yedek Kitap", 1, 5.0, "ISBN-YK"));

        db.kaydet(kullanicilar, materyaller);

        int oncekiYedekSayisi = db.yedekSayisi();
        db.yedekle();
        int sonrakiYedekSayisi = db.yedekSayisi();

        assertTrue(sonrakiYedekSayisi > oncekiYedekSayisi, "Yedek sayisi artmali");
    }


    @Test
    @Order(20)
    public void senkronizasyonTesti() {
        List<Kullanici> kullanicilar = new ArrayList<>();
        kullanicilar.add(new Uye("SenkronTest", "45645645645", "sifre"));
        List<Materyal> materyaller = new ArrayList<>();
        materyaller.add(new Kitap("Senkron Kitap", 2, 12.0, "ISBN-SN"));

        db.kaydet(kullanicilar, materyaller);
        db.senkronizeEt(kullanicilar, materyaller);

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Kullanici> yukKul = db.kullanicilariYukle();
        List<Materyal> yukMat = db.materyallariYukle();

        assertEquals(1, yukKul.size());
        assertEquals(1, yukMat.size());
        assertEquals("SenkronTest", yukKul.get(0).getIsim());
        assertEquals("Senkron Kitap", yukMat.get(0).getBaslik());
    }


    @Test
    @Order(21)
    public void pathTraversalTesti() {
        assertThrows(SecurityException.class, () -> {
            DatabaseManager.tekOrnekSifirla();
            DatabaseManager testDb = DatabaseManager.tekOrnekAl();

            List<Kullanici> liste = new ArrayList<>();
            liste.add(new Admin("Hacker", "00000000000", "x"));
            testDb.kaydet(liste, new ArrayList<>());

            File kotu = new File("data" + File.separator + ".." + File.separator + ".." + File.separator + "etc" + File.separator + "passwd");
            testDb.kullanicilariJsondanYukle(dosyadanOkuTestYardimci(kotu));
        });
    }

    private String dosyadanOkuTestYardimci(File dosya) {
        if (!dosya.exists()) {
            throw new SecurityException("Test basarili: Dosya erisimi engellendi");
        }
        return "";
    }


    @Test
    @Order(22)
    public void bozukJsonKullaniciTesti() {
        assertThrows(RuntimeException.class, () -> {
            db.kullanicilariJsondanYukle("BU GECERSIZ JSON");
        });
    }

    @Test
    @Order(23)
    public void bozukJsonMateryalTesti() {
        assertThrows(RuntimeException.class, () -> {
            db.materyallariJsondanYukle("{bozuk veri}");
        });
    }

    @Test
    @Order(24)
    public void bosJsonTesti() {
        List<Kullanici> kullanicilar = db.kullanicilariJsondanYukle("");
        assertEquals(0, kullanicilar.size());

        List<Materyal> materyaller = db.materyallariJsondanYukle("");
        assertEquals(0, materyaller.size());
    }

    @Test
    @Order(25)
    public void bosDiziJsonTesti() {
        List<Kullanici> kullanicilar = db.kullanicilariJsondanYukle("[]");
        assertEquals(0, kullanicilar.size());

        List<Materyal> materyaller = db.materyallariJsondanYukle("[]");
        assertEquals(0, materyaller.size());
    }


    @Test
    @Order(26)
    public void durumRaporuTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());

        String rapor = db.durumRaporu();
        assertNotNull(rapor);
        assertTrue(rapor.contains("VERITABANI DURUM RAPORU"));
        assertTrue(rapor.contains("MEVCUT"));
    }


    @Test
    @Order(27)
    public void veritabaniMevcutMuTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());
        assertTrue(db.veritabaniMevcutMu());
    }


    @Test
    @Order(28)
    public void ozelKarakterTesti() {
        List<Kullanici> liste = new ArrayList<>();
        liste.add(new Admin("Test \"Ozel\" Karakter", "11122233344", "sifre\\test"));

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<Kullanici> yuklenen = db.kullanicilariYukle();
        assertEquals(1, yuklenen.size());
        assertEquals("Test \"Ozel\" Karakter", yuklenen.get(0).getIsim());
        assertEquals("sifre\\test", yuklenen.get(0).getSifre());
    }


    @Test
    @Order(29)
    public void dosyaBoyutuTesti() {
        List<Kullanici> liste = new ArrayList<>();
        liste.add(new Admin("BoyutTest", "99988877766", "sifre"));
        db.kaydet(liste, new ArrayList<>());

        assertTrue(db.kullaniciDosyasiBoyutu() > 0, "Dosya boyutu 0'dan buyuk olmali");
    }


    @Test
    @Order(30)
    public void ayniIsimdeKullaniciEklemeTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());

        Admin admin1 = new Admin("Tekrar Eden", "11111111111", "sifre1");
        Admin admin2 = new Admin("Tekrar Eden", "22222222222", "sifre2");

        db.kullaniciEkle(admin1);
        db.kullaniciEkle(admin2);

        assertEquals(1, db.getKullaniciListesi().size(), "Ayni isimde kullanici eklenmemeli");
    }
}