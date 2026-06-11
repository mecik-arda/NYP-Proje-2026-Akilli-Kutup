package com.akillikutup.db;

import com.akillikutup.core.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

    private static volatile DatabaseManager tekOrnek;

    public static boolean isTestMode = false;

    private String getVeriKlasoru() { return isTestMode ? "test-data" : "data"; }
    private String getDbYolu() { return "jdbc:sqlite:" + getVeriKlasoru() + "/database.db"; }
    private String getYedekKlasoru() { return getVeriKlasoru() + File.separator + "backup"; }

    private List<Kullanici> kullaniciListesi;
    private List<Materyal> materyalListesi;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private HikariDataSource dataSource;
    private ScheduledExecutorService scheduler;
    private Thread shutdownHook;

    private DatabaseManager() {
        kullaniciListesi = new ArrayList<>();
        materyalListesi = new ArrayList<>();
        
        File veriKlasoru = new File(getVeriKlasoru());
        if (!veriKlasoru.exists()) veriKlasoru.mkdirs();
        
        File yedekKlasoru = new File(getYedekKlasoru());
        if (!yedekKlasoru.exists()) yedekKlasoru.mkdirs();
        
        FileEncryptionService.init();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getDbYolu());
        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);

        initDb();
        baslatYedeklemeZamanlayici();
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
        if (tekOrnek != null) {
            if (tekOrnek.dataSource != null && !tekOrnek.dataSource.isClosed()) {
                tekOrnek.dataSource.close();
            }
            if (tekOrnek.scheduler != null && !tekOrnek.scheduler.isShutdown()) {
                tekOrnek.scheduler.shutdownNow();
            }
            if (tekOrnek.shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(tekOrnek.shutdownHook);
                } catch (IllegalStateException e) {
                    // Shutdown in progress, ignore
                }
            }
        }
        tekOrnek = null;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    private void baslatYedeklemeZamanlayici() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::yedekle, 1, 1, TimeUnit.HOURS);
        
        shutdownHook = new Thread(() -> {
            yedekle();
            if (dataSource != null && !dataSource.isClosed()) dataSource.close();
            if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdown();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void initDb() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS kullanicilar (id TEXT PRIMARY KEY, tcNo TEXT, json TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS materyaller (id TEXT PRIMARY KEY, json TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
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
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM kullanicilar");
            }
            String sql = "INSERT INTO kullanicilar (id, tcNo, json) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Kullanici k : kullaniciListesi) {
                    ps.setString(1, k.getId());
                    ps.setString(2, k.getTcNoDogrudan());
                    String json = JsonParser.serializeKullanici(k);
                    ps.setString(3, FileEncryptionService.encrypt(json));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            lock.writeLock().unlock();
        }
    }

    public void materyallariKaydet() {
        lock.writeLock().lock();
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM materyaller");
            }
            String sql = "INSERT INTO materyaller (id, json) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Materyal m : materyalListesi) {
                    ps.setString(1, m.getId());
                    String json = JsonParser.serializeMateryal(m);
                    ps.setString(2, FileEncryptionService.encrypt(json));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            lock.writeLock().unlock();
        }
    }

    public List<Kullanici> kullanicilariYukle() {
        kullaniciListesi.clear();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT json FROM kullanicilar")) {
            while (rs.next()) {
                String encryptedJson = rs.getString("json");
                String json = FileEncryptionService.decrypt(encryptedJson);
                Kullanici k = JsonParser.deserializeKullanici(json);
                if (k != null) kullaniciListesi.add(k);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kullaniciListesi;
    }

    public List<Materyal> materyallariYukle() {
        materyalListesi.clear();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT json FROM materyaller")) {
            while (rs.next()) {
                String encryptedJson = rs.getString("json");
                String json = FileEncryptionService.decrypt(encryptedJson);
                Materyal m = JsonParser.deserializeMateryal(json);
                if (m != null) materyalListesi.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return materyalListesi;
    }

    public void yedekle() {
        try {
            File dbFile = new File(getVeriKlasoru() + "/database.db");
            if (dbFile.exists()) {
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                Path backupPath = Path.of(getYedekKlasoru(), "database_" + timestamp + ".db");
                
                // SQLite JDBC backup command
                try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("backup to '" + backupPath.toString().replace("\\", "/") + "'");
                }
                
                System.out.println("YEDEK: Veritabani yedeklendi -> " + backupPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void senkronizeEt(List<Kullanici> kullanicilar, List<Materyal> materyaller) {
        yedekle();
        kaydet(kullanicilar, materyaller);
        System.out.println("SENKRONIZASYON TAMAMLANDI: Veriler yedeklendi ve SQLite'a kaydedildi.");
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
        return new File(getVeriKlasoru() + "/database.db").exists();
    }

    public List<Kullanici> getKullaniciListesi() {
        if (kullaniciListesi.isEmpty() && veritabaniMevcutMu()) {
            lock.writeLock().lock();
            try {
                if (kullaniciListesi.isEmpty() && veritabaniMevcutMu()) {
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
        if (materyalListesi.isEmpty() && veritabaniMevcutMu()) {
            lock.writeLock().lock();
            try {
                if (materyalListesi.isEmpty() && veritabaniMevcutMu()) {
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
