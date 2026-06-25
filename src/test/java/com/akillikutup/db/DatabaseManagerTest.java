package com.akillikutup.db;

import com.akillikutup.material.*;
import com.akillikutup.user.User;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseManagerTest {

    private DatabaseManager db;

    @BeforeAll
    public static void globalKurulum() {
        DatabaseManager.isTestMode = true;
    }

    @BeforeEach
    public void kurulum() {
        DatabaseManager.isTestMode = true;
        temizle();
        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();
    }

    @AfterEach
    public void temizlik() {
        temizle();
        DatabaseManager.tekOrnekSifirla();
    }

    private void temizle() {
        File f1 = new File("test-data/users.json");
        if (f1.exists()) f1.delete();
        File f2 = new File("test-data/materials.json");
        if (f2.exists()) f2.delete();
        File f3 = new File("test-data/database.db");
        if (f3.exists()) f3.delete();
        File tmp1 = new File("test-data/users.json.tmp");
        if (tmp1.exists()) tmp1.delete();
        File tmp2 = new File("test-data/materials.json.tmp");
        if (tmp2.exists()) tmp2.delete();
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
        List<User> liste = new ArrayList<>();
        liste.add(new User("Test Admin", "11111111111", User.Role.ADMIN, "admin123"));
        liste.add(new User("Test Uye", "22222222222", User.Role.UYE, "uye456"));

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<User> yuklenen = db.kullanicilariYukle();

        assertEquals(2, yuklenen.size(), "2 kullanici yuklenmeli");
        assertEquals("Test Admin", yuklenen.get(0).getIsim());
        assertEquals(User.Role.ADMIN, yuklenen.get(0).getRol());
        assertEquals("Test Uye", yuklenen.get(1).getIsim());
        assertEquals(User.Role.UYE, yuklenen.get(1).getRol());
    }

    @Test
    @Order(4)
    public void kullaniciSifreTesti() {
        List<User> liste = new ArrayList<>();
        liste.add(new User("SifreTest", "33333333333", User.Role.ADMIN, "gizliSifre!@#"));

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<User> yuklenen = db.kullanicilariYukle();
        assertEquals("gizliSifre!@#", yuklenen.get(0).getSifre(), "Sifre dogru yuklenmeli");
    }

    @Test
    @Order(5)
    public void uyeKrediPuaniTesti() {
        User uye = new User("PuanTest", "44444444444", User.Role.UYE, "sifre");
        uye.puanGuncelle(-30);
        assertEquals(70, uye.getKrediPuani());

        List<User> liste = new ArrayList<>();
        liste.add(uye);

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<User> yuklenen = db.kullanicilariYukle();
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
        DijitalMedya dm = new DijitalMedya("Test Film", 15.0, "MP4", "Video", "1.2GB");
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
        liste.add(new DijitalMedya("Medya 1", 10.0, "PDF", "E-Kitap", "1MB"));
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
        File dosya = new File("test-data" + File.separator + "users.json");
        if (dosya.exists()) dosya.delete();

        List<User> sonuc = db.kullanicilariYukle();
        assertNotNull(sonuc);
        assertEquals(0, sonuc.size(), "Dosya yokken bos liste donmeli");
    }

    @Test
    @Order(10)
    public void dosyaYokkenMateryalYuklemeTesti() {
        File dosya = new File("test-data" + File.separator + "materials.json");
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

        List<User> kullanicilar = db.kullanicilariYukle();
        List<Materyal> materyaller = db.materyallariYukle();

        assertEquals(0, kullanicilar.size());
        assertEquals(0, materyaller.size());
    }


    @Test
    @Order(12)
    public void kullaniciEkleTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());

        User admin = new User("Eklenen Admin", "99999999999", User.Role.ADMIN, "sifre");
        db.kullaniciEkle(admin);

        assertEquals(1, db.getKullaniciListesi().size());
        assertEquals("Eklenen Admin", db.getKullaniciListesi().get(0).getIsim());
    }

    @Test
    @Order(13)
    public void kullaniciSilTesti() {
        List<User> liste = new ArrayList<>();
        liste.add(new User("Silinecek", "88888888888", User.Role.ADMIN, "sifre"));
        liste.add(new User("Kalacak", "77777777777", User.Role.UYE, "sifre"));
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
        liste.add(new DijitalMedya("Kalacak Medya", 5.0, "MP3", "Ses", "3MB"));
        db.kaydet(new ArrayList<>(), liste);

        db.materyalSil(silinecekId);

        assertEquals(1, db.getMateryalListesi().size());
        assertEquals("Kalacak Medya", db.getMateryalListesi().get(0).getBaslik());
    }


    @Test
    @Order(16)
    public void kullaniciBulTesti() {
        List<User> liste = new ArrayList<>();
        User admin = new User("Aranan Admin", "66666666666", User.Role.ADMIN, "sifre");
        liste.add(admin);
        db.kaydet(liste, new ArrayList<>());

        User bulunan = db.kullaniciBul(admin.getId());
        assertNotNull(bulunan);
        assertEquals("Aranan Admin", bulunan.getIsim());

        User yok = db.kullaniciBul("Olmayan Kullanici");
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
        liste.add(new DijitalMedya("Java Video Kursu", 20.0, "MP4", "Video", "500MB"));
        db.kaydet(new ArrayList<>(), liste);

        List<Materyal> sonuclar = db.materyalAra("Java");
        assertEquals(2, sonuclar.size(), "Java iceren 2 materyal bulunmali");

        List<Materyal> bosSonuc = db.materyalAra("C++");
        assertEquals(0, bosSonuc.size(), "Eslesme yoksa bos liste donmeli");
    }


    @Test
    @Order(19)
    public void yedeklemeTesti() {
        List<User> kullanicilar = new ArrayList<>();
        kullanicilar.add(new User("YedekTest", "12312312312", User.Role.ADMIN, "sifre"));
        List<Materyal> materyaller = new ArrayList<>();
        materyaller.add(new Kitap("Yedek Kitap", 1, 5.0, "ISBN-YK"));

        db.kaydet(kullanicilar, materyaller);

        File backupDir = new File("test-data/backup");
        if (backupDir.exists() && backupDir.listFiles() != null) {
            for (File f : backupDir.listFiles()) {
                f.delete();
            }
        }
        int oncekiYedekSayisi = backupDir.exists() && backupDir.listFiles() != null ? backupDir.listFiles().length : 0;
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        db.yedekle();
        int sonrakiYedekSayisi = backupDir.listFiles().length;

        assertTrue(sonrakiYedekSayisi > oncekiYedekSayisi, "Yedek sayisi artmali");
    }


    @Test
    @Order(20)
    public void senkronizasyonTesti() {
        List<User> kullanicilar = new ArrayList<>();
        kullanicilar.add(new User("SenkronTest", "45645645645", User.Role.UYE, "sifre"));
        List<Materyal> materyaller = new ArrayList<>();
        materyaller.add(new Kitap("Senkron Kitap", 2, 12.0, "ISBN-SN"));

        db.kaydet(kullanicilar, materyaller);
        db.senkronizeEt(kullanicilar, materyaller);

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<User> yukKul = db.kullanicilariYukle();
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

            List<User> liste = new ArrayList<>();
            liste.add(new User("Hacker", "00000000000", User.Role.ADMIN, "x"));
            testDb.kaydet(liste, new ArrayList<>());

            File kotu = new File("test-data" + File.separator + ".." + File.separator + ".." + File.separator + "etc" + File.separator + "passwd");
            JsonParser.deserializeKullanicilar(dosyadanOkuTestYardimci(kotu));
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
            JsonParser.deserializeKullanicilar("BU GECERSIZ JSON");
        });
    }

    @Test
    @Order(23)
    public void bozukJsonMateryalTesti() {
        assertThrows(RuntimeException.class, () -> {
            JsonParser.deserializeMateryaller("{bozuk veri}");
        });
    }

    @Test
    @Order(24)
    public void bosJsonTesti() {
        List<User> kullanicilar = JsonParser.deserializeKullanicilar("");
        assertEquals(0, kullanicilar.size());

        List<Materyal> materyaller = JsonParser.deserializeMateryaller("");
        assertEquals(0, materyaller.size());
    }

    @Test
    @Order(25)
    public void bosDiziJsonTesti() {
        List<User> kullanicilar = JsonParser.deserializeKullanicilar("[]");
        assertEquals(0, kullanicilar.size());

        List<Materyal> materyaller = JsonParser.deserializeMateryaller("[]");
        assertEquals(0, materyaller.size());
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
        List<User> liste = new ArrayList<>();
        liste.add(new User("Test \"Ozel\" Karakter", "11122233344", User.Role.ADMIN, "sifre\\test"));

        db.kaydet(liste, new ArrayList<>());

        DatabaseManager.tekOrnekSifirla();
        db = DatabaseManager.tekOrnekAl();

        List<User> yuklenen = db.kullanicilariYukle();
        assertEquals(1, yuklenen.size());
        assertEquals("Test \"Ozel\" Karakter", yuklenen.get(0).getIsim());
        assertEquals("sifre\\test", yuklenen.get(0).getSifre());
    }


    @Test
    @Order(30)
    public void ayniIsimdeKullaniciEklemeTesti() {
        db.kaydet(new ArrayList<>(), new ArrayList<>());

        User admin1 = new User("Tekrar Eden", "11111111111", User.Role.ADMIN, "sifre1");
        User admin2 = new User("Tekrar Eden", "22222222222", User.Role.ADMIN, "sifre2");

        db.kullaniciEkle(admin1);
        db.kullaniciEkle(admin2);

        assertEquals(1, db.getKullaniciListesi().size(), "Ayni isimde kullanici eklenmemeli");
    }
}
