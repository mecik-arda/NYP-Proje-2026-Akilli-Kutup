package com.akillikutup.db;

import com.akillikutup.core.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatabaseManager {

    private static DatabaseManager tekOrnek;

    private final String VERI_KLASORU = "data";
    private final String YEDEK_KLASORU = "data" + File.separator + "backup";
    private final String KULLANICI_DOSYASI = "data" + File.separator + "users.json";
    private final String MATERYAL_DOSYASI = "data" + File.separator + "materials.json";

    private final String IZINLI_KOK_DIZIN;

    private List<Kullanici> kullaniciListesi;
    private List<Materyal> materyalListesi;
    private long sonKullaniciDosyaTarihi = 0;
    private long sonMateryalDosyaTarihi = 0;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final BackupManager backupManager;

    private DatabaseManager() {
        kullaniciListesi = new ArrayList<>();
        materyalListesi = new ArrayList<>();
        backupManager = new BackupManager(YEDEK_KLASORU, KULLANICI_DOSYASI, MATERYAL_DOSYASI);

        try {
            IZINLI_KOK_DIZIN = new File(VERI_KLASORU).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Veri klasoru yolu cozumlenemedi: " + e.getMessage());
        }

        klasorleriOlustur();
        FileEncryptionService.init();
    }

    public static DatabaseManager tekOrnekAl() {
        if (tekOrnek == null) {
            synchronized (DatabaseManager.class) {
                if (tekOrnek == null) {
                    tekOrnek = new DatabaseManager();
                }
            }
        }
        return tekOrnek;
    }

    public static void tekOrnekSifirla() {
        tekOrnek = null;
    }

    private void klasorleriOlustur() {
        File veriKlasoru = new File(VERI_KLASORU);
        if (!veriKlasoru.exists()) veriKlasoru.mkdirs();
        FileEncryptionService.dosyaErisiminiKisila(veriKlasoru.toPath());

        File yedekKlasoru = new File(YEDEK_KLASORU);
        if (!yedekKlasoru.exists()) yedekKlasoru.mkdirs();
        FileEncryptionService.dosyaErisiminiKisila(yedekKlasoru.toPath());
    }

    private void yolGuvenligi(String dosyaYolu) {
        try {
            String gercekYol = new File(dosyaYolu).getCanonicalPath();
            if (!gercekYol.startsWith(IZINLI_KOK_DIZIN)) {
                throw new SecurityException("GUVENLIK IHLALI: Dosya yolu izinli dizin disina cikiyor!");
            }
        } catch (IOException e) {
            throw new SecurityException("Dosya yolu cozumlenirken hata olustu: " + e.getMessage());
        }
    }

    public void kaydet(List<Kullanici> kullanicilar, List<Materyal> materyaller) {
        this.kullaniciListesi = new ArrayList<>(kullanicilar);
        this.materyalListesi = new ArrayList<>(materyaller);
        kullanicilariKaydet();
        materyallariKaydet();
    }

    public void kullanicilariKaydet() {
        lock.writeLock().lock();
        try {
            yolGuvenligi(KULLANICI_DOSYASI);
            String jsonIcerik = JsonParser.serializeKullanicilar(kullaniciListesi);
            String sifreliIcerik = FileEncryptionService.encrypt(jsonIcerik);
            atomikYaz(KULLANICI_DOSYASI, sifreliIcerik);
            System.out.println("BASARILI: Kullanici verileri kaydedildi. (" + kullaniciListesi.size() + " kayit)");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void materyallariKaydet() {
        lock.writeLock().lock();
        try {
            yolGuvenligi(MATERYAL_DOSYASI);
            String jsonIcerik = JsonParser.serializeMateryaller(materyalListesi);
            String sifreliIcerik = FileEncryptionService.encrypt(jsonIcerik);
            atomikYaz(MATERYAL_DOSYASI, sifreliIcerik);
            System.out.println("BASARILI: Materyal verileri kaydedildi. (" + materyalListesi.size() + " kayit)");
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void atomikYaz(String hedefDosya, String icerik) {
        try {
            Path hedef = Paths.get(hedefDosya);
            Path gecici = Paths.get(hedefDosya + ".tmp");
            Files.writeString(gecici, icerik, StandardCharsets.UTF_8);
            Files.move(gecici, hedef, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            FileEncryptionService.dosyaErisiminiKisila(hedef);
        } catch (IOException e) {
            throw new RuntimeException("Atomik yazma sirasinda hata: " + e.getMessage(), e);
        }
    }

    public List<Kullanici> kullanicilariYukle() {
        yolGuvenligi(KULLANICI_DOSYASI);
        File dosya = new File(KULLANICI_DOSYASI);

        if (!dosya.exists()) {
            kullaniciListesi = new ArrayList<>();
            return kullaniciListesi;
        }

        String okunan = dosyadanOku(dosya);
        try {
            kullaniciListesi = JsonParser.deserializeKullanicilar(okunan);
        } catch (Exception e) {
            if (backupManager.yedektenKurtar(KULLANICI_DOSYASI)) {
                okunan = dosyadanOku(dosya);
                kullaniciListesi = JsonParser.deserializeKullanicilar(okunan);
            } else {
                kullaniciListesi = new ArrayList<>();
            }
        }
        sonKullaniciDosyaTarihi = dosya.lastModified();
        return kullaniciListesi;
    }

    public List<Materyal> materyallariYukle() {
        yolGuvenligi(MATERYAL_DOSYASI);
        File dosya = new File(MATERYAL_DOSYASI);

        if (!dosya.exists()) {
            materyalListesi = new ArrayList<>();
            return materyalListesi;
        }

        String okunan = dosyadanOku(dosya);
        try {
            materyalListesi = JsonParser.deserializeMateryaller(okunan);
        } catch (Exception e) {
            if (backupManager.yedektenKurtar(MATERYAL_DOSYASI)) {
                okunan = dosyadanOku(dosya);
                materyalListesi = JsonParser.deserializeMateryaller(okunan);
            } else {
                materyalListesi = new ArrayList<>();
            }
        }
        sonMateryalDosyaTarihi = dosya.lastModified();
        return materyalListesi;
    }

    private String dosyadanOku(File dosya) {
        try {
            String icerik = Files.readString(dosya.toPath(), StandardCharsets.UTF_8).trim();
            if (!icerik.isEmpty() && !icerik.startsWith("[")) {
                return FileEncryptionService.decrypt(icerik);
            }
            return icerik;
        } catch (IOException e) {
            throw new RuntimeException("Dosya okuma hatasi: " + dosya.getAbsolutePath(), e);
        }
    }

    public void yedekle() {
        backupManager.yedekle();
    }

    public void senkronizeEt(List<Kullanici> kullanicilar, List<Materyal> materyaller) {
        yedekle();
        kaydet(kullanicilar, materyaller);
        System.out.println("SENKRONIZASYON TAMAMLANDI: Veriler yedeklendi ve kaydedildi.");
    }

    public void kullaniciEkle(Kullanici yeniKullanici) {
        for (Kullanici mevcut : kullaniciListesi) {
            if (mevcut.getIsim().equals(yeniKullanici.getIsim())) return;
        }
        kullaniciListesi.add(yeniKullanici);
        kullanicilariKaydet();
    }

    public void kullaniciSil(String kullaniciIsmi) {
        if (kullaniciListesi.removeIf(k -> k.getIsim().equals(kullaniciIsmi))) kullanicilariKaydet();
    }

    public void materyalEkle(Materyal yeniMateryal) {
        materyalListesi.add(yeniMateryal);
        materyallariKaydet();
    }

    public void materyalSil(String materyalId) {
        if (materyalListesi.removeIf(m -> m.getId().equals(materyalId))) materyallariKaydet();
    }

    public Kullanici kullaniciBul(String id) {
        lock.readLock().lock();
        try {
            for (Kullanici k : kullaniciListesi) {
                if (k.getId().equals(id)) return k;
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Materyal materyalBul(String id) {
        lock.readLock().lock();
        try {
            for (Materyal m : materyalListesi) {
                if (m.getId().equals(id)) return m;
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Materyal> materyalAra(String aramaKelimesi) {
        List<Materyal> sonuclar = new ArrayList<>();
        String kucukHarfArama = aramaKelimesi.toLowerCase();
        for (Materyal m : materyalListesi) {
            if (m.getBaslik().toLowerCase().contains(kucukHarfArama)) {
                sonuclar.add(m);
            }
        }
        return sonuclar;
    }

    public boolean veritabaniMevcutMu() {
        return new File(KULLANICI_DOSYASI).exists() && new File(MATERYAL_DOSYASI).exists();
    }

    public List<Kullanici> getKullaniciListesi() {
        File f = new File(KULLANICI_DOSYASI);
        if (f.exists() && f.lastModified() > sonKullaniciDosyaTarihi) {
            lock.writeLock().lock();
            try {
                if (f.exists() && f.lastModified() > sonKullaniciDosyaTarihi) {
                    kullanicilariYukle();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        lock.readLock().lock();
        try {
            return new ArrayList<>(kullaniciListesi);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Materyal> getMateryalListesi() {
        File f = new File(MATERYAL_DOSYASI);
        if (f.exists() && f.lastModified() > sonMateryalDosyaTarihi) {
            lock.writeLock().lock();
            try {
                if (f.exists() && f.lastModified() > sonMateryalDosyaTarihi) {
                    materyallariYukle();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        lock.readLock().lock();
        try {
            return new ArrayList<>(materyalListesi);
        } finally {
            lock.readLock().unlock();
        }
    }
}