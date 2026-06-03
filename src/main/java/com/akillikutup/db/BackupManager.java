package com.akillikutup.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupManager {
    private final String YEDEK_KLASORU;
    private final String KULLANICI_DOSYASI;
    private final String MATERYAL_DOSYASI;

    public BackupManager(String yedekKlasoru, String kullaniciDosyasi, String materyalDosyasi) {
        this.YEDEK_KLASORU = yedekKlasoru;
        this.KULLANICI_DOSYASI = kullaniciDosyasi;
        this.MATERYAL_DOSYASI = materyalDosyasi;
    }

    public void yedekle() {
        DateTimeFormatter bicim = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String zamanDamgasi = LocalDateTime.now().format(bicim);

        File kullaniciDosyasi = new File(KULLANICI_DOSYASI);
        if (kullaniciDosyasi.exists()) {
            String yedekAdi = YEDEK_KLASORU + File.separator + "users_" + zamanDamgasi + ".json";
            dosyaKopyala(kullaniciDosyasi, new File(yedekAdi));
            System.out.println("YEDEK: Kullanici verileri yedeklendi -> " + yedekAdi);
        }

        File materyalDosyasi = new File(MATERYAL_DOSYASI);
        if (materyalDosyasi.exists()) {
            String yedekAdi = YEDEK_KLASORU + File.separator + "materials_" + zamanDamgasi + ".json";
            dosyaKopyala(materyalDosyasi, new File(yedekAdi));
            System.out.println("YEDEK: Materyal verileri yedeklendi -> " + yedekAdi);
        }

        eskiYedekleriTemizle(10);
    }

    public boolean yedektenKurtar(String hedefDosyaYolu) {
        String onek;
        if (hedefDosyaYolu.contains("users")) {
            onek = "users_";
        } else if (hedefDosyaYolu.contains("materials")) {
            onek = "materials_";
        } else {
            return false;
        }

        File yedekKlasoru = new File(YEDEK_KLASORU);
        if (!yedekKlasoru.exists() || !yedekKlasoru.isDirectory()) {
            return false;
        }

        File[] yedekDosyalar = yedekKlasoru.listFiles((dizin, ad) ->
                ad.startsWith(onek) && ad.endsWith(".json"));

        if (yedekDosyalar == null || yedekDosyalar.length == 0) {
            System.err.println("UYARI: Hicbir yedek dosyasi bulunamadi: " + onek);
            return false;
        }

        File enSonYedek = yedekDosyalar[0];
        for (File yedek : yedekDosyalar) {
            if (yedek.lastModified() > enSonYedek.lastModified()) {
                enSonYedek = yedek;
            }
        }

        try {
            dosyaKopyala(enSonYedek, new File(hedefDosyaYolu));
            System.out.println("KURTARMA: " + enSonYedek.getName() + " -> " + hedefDosyaYolu);
            return true;
        } catch (Exception e) {
            System.err.println("HATA: Yedekten kurtarma sirasinda hata: " + e.getMessage());
            return false;
        }
    }

    private void eskiYedekleriTemizle(int maxYedekSayisi) {
        File yedekKlasoru = new File(YEDEK_KLASORU);
        if (!yedekKlasoru.exists()) return;

        String[] onekler = {"users_", "materials_"};

        for (String onek : onekler) {
            File[] dosyalar = yedekKlasoru.listFiles((dizin, ad) ->
                    ad.startsWith(onek) && ad.endsWith(".json"));

            if (dosyalar == null || dosyalar.length <= maxYedekSayisi) continue;

            java.util.Arrays.sort(dosyalar, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

            for (int i = maxYedekSayisi; i < dosyalar.length; i++) {
                boolean silindi = dosyalar[i].delete();
                if (silindi) {
                    System.out.println("TEMIZLIK: Eski yedek silindi -> " + dosyalar[i].getName());
                }
            }
        }
    }

    private void dosyaKopyala(File kaynak, File hedef) {
        try (InputStream giren = new FileInputStream(kaynak);
             OutputStream cikan = new FileOutputStream(hedef)) {

            byte[] tampon = new byte[4096];
            int okunan;
            while ((okunan = giren.read(tampon)) != -1) {
                cikan.write(tampon, 0, okunan);
            }
        } catch (IOException e) {
            System.err.println("HATA: Dosya kopyalama basarisiz: " + kaynak.getName() + " -> " + hedef.getName());
            throw new RuntimeException("Dosya kopyalama hatasi", e);
        }
        FileEncryptionService.dosyaErisiminiKisila(hedef.toPath());
    }
}
