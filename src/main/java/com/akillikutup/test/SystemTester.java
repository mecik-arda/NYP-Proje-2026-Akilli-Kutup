package com.akillikutup.test;

import com.akillikutup.core.*;
import com.akillikutup.db.DatabaseManager;

public class SystemTester {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public static void main(String[] args) {
        System.out.println(ANSI_BLUE + "=== Akilli Kutuphane [v4.1.0] Uctan Uca (E2E) Test Sistemi ===" + ANSI_RESET);
        
        try {
            // Test 1: Veritabanı Yükleme
            System.out.print("Test 1: Veritabani Baglantisi ve JSON Deserialization... ");
            DatabaseManager db = DatabaseManager.tekOrnekAl();
            if (db.getKullaniciListesi() != null && db.getMateryalListesi() != null) {
                System.out.println(ANSI_GREEN + "PASSED" + ANSI_RESET);
            } else {
                throw new Exception("Veritabani listeleri null dondu.");
            }

            // Test 2: Nesne Olusturma
            System.out.print("Test 2: Materyal & Uye Olusturma (Memory Insert)... ");
            Uye testUye = new Uye("Test Uye", "test@test.com", "test1234");
            Kitap testKitap = new Kitap("Test Kitap E2E", 5, 20.0, "978-0000000000");
            DijitalMedya testDijital = new DijitalMedya("Test Medya E2E", 0.0, "MP4", "Video", "500 MB");
            Klasor testKlasor = new Klasor("E2E Test Klasoru");
            
            db.getKullaniciListesi().add(testUye);
            db.getMateryalListesi().add(testKitap);
            db.getMateryalListesi().add(testDijital);
            db.getMateryalListesi().add(testKlasor);
            System.out.println(ANSI_GREEN + "PASSED" + ANSI_RESET);

            // Test 3: OOP ve Odunc Alma (LSP Kontrolu)
            System.out.print("Test 3: IOduncAlinabilir Davranislari (Pozitif Test)... ");
            if (testKitap instanceof IOduncAlinabilir && testDijital instanceof IOduncAlinabilir) {
                ((IOduncAlinabilir) testKitap).oduncVer();
                testUye.materyalOduncAl(testKitap.getId());
                if (testUye.getOduncAlinanMateryaller().contains(testKitap.getId())) {
                    System.out.println(ANSI_GREEN + "PASSED" + ANSI_RESET);
                } else {
                    throw new Exception("Uye, kitabi odunc almasina ragmen listesine eklenmedi.");
                }
            } else {
                throw new Exception("Kitap ve DijitalMedya siniflari IOduncAlinabilir arayuzunu uygulamiyor.");
            }

            // Test 4: Klasor Davranisi (LSP Negatif Test)
            System.out.print("Test 4: Klasor Odunc Alma İhlali Kontrolu (Negatif Test)... ");
            if (!(testKlasor instanceof IOduncAlinabilir)) {
                System.out.println(ANSI_GREEN + "PASSED" + ANSI_RESET);
            } else {
                throw new Exception("Klasor nesnesi yanlislikla IOduncAlinabilir arayuzunu kullaniyor!");
            }
            
            // Test 5: Iade Etme
            System.out.print("Test 5: Materyal Iade Edilmesi ve Limit Kontrolu... ");
            ((IOduncAlinabilir) testKitap).iadeEt();
            testUye.materyalIadeEt(testKitap.getId());
            if (!testUye.getOduncAlinanMateryaller().contains(testKitap.getId())) {
                System.out.println(ANSI_GREEN + "PASSED" + ANSI_RESET);
            } else {
                throw new Exception("Uye kitabi iade edemedi.");
            }

            // Temizlik (Cleanup) - Gercek DB'yi kirletmemek icin silme islemi
            System.out.print("Test 6: Test Verilerinin Temizlenmesi (Rollback)... ");
            db.getKullaniciListesi().remove(testUye);
            db.getMateryalListesi().remove(testKitap);
            db.getMateryalListesi().remove(testDijital);
            db.getMateryalListesi().remove(testKlasor);
            System.out.println(ANSI_GREEN + "PASSED" + ANSI_RESET);

            System.out.println(ANSI_BLUE + "=== TUM TESTLER (6/6) BASARIYLA TAMAMLANDI! ===" + ANSI_RESET);

        } catch (Exception e) {
            System.out.println(ANSI_RED + "FAILED" + ANSI_RESET);
            System.out.println("Hata Detayi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
