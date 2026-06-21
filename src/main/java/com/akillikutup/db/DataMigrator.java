package com.akillikutup.db;

import com.akillikutup.core.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Properties;
import java.util.Properties;

/**
 * Tek seferlik veri göç modülü: SQLite'taki mevcut verileri PostgreSQL'e kayıpsız aktarır.
 *
 * KULLANIM:
 *   1. PostgreSQL sunucusunun çalıştığından emin olun.
 *   2. Hedef veritabanını oluşturun: psql -c "CREATE DATABASE akilli_kutup;"
 *   3. Bu sınıfı manuel çalıştırın: DataMigrator.main()
 *
 * KORUNAN VERİLER:
 *   - Kullanıcı bilgileri (isim, TC, rol, şifre, kredi puanı)
 *   - Ödünç geçmişi (hangi kitap, ne zaman alındı, iade tarihi, ceza)
 *   - Bildirim geçmişi
 *   - Tüm materyaller (kitap, dijital medya, klasör) ve özellikleri
 */
public class DataMigrator {

    private static final String PG_HOST = System.getenv().getOrDefault("PG_HOST", "localhost");
    private static final String PG_PORT = System.getenv().getOrDefault("PG_PORT", "5432");
    private static final String PG_DB   = System.getenv().getOrDefault("PG_DB", "akilli_kutup");
    private static final String PG_USER = System.getenv().getOrDefault("PG_USER", "postgres");
    private static final String PG_PASS = System.getenv().getOrDefault("PG_PASS", "postgres");

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  AKILLI KÜTÜPHANE V4 - VERİ GÖÇ ARACI");
        System.out.println("  SQLite → PostgreSQL");
        System.out.println("═══════════════════════════════════════════════════\n");

        // ── 1. SQLite'tan verileri oku ────────────────────────────────
        System.out.println("[1/3] SQLite veritabanından veriler okunuyor...");
        DatabaseManager eskiDb = DatabaseManager.tekOrnekAl();
        List<Kullanici> kullanicilar = eskiDb.kullanicilariYukle();
        List<Materyal> materyaller = eskiDb.materyallariYukle();

        System.out.println("  ✅ Kullanıcı sayısı: " + kullanicilar.size());
        System.out.println("  ✅ Materyal sayısı:  " + materyaller.size());

        if (kullanicilar.isEmpty() && materyaller.isEmpty()) {
            System.out.println("\n  ⚠️  UYARI: SQLite'ta veri bulunamadı. PostgreSQL tabloları boş oluşturulacak.");
        }

        // Mevcut verilerin detaylı logu (veri bütünlüğü kanıtı)
        int toplamOdunc = 0;
        int toplamBildirim = 0;
        for (Kullanici k : kullanicilar) {
            int odunc = k.getOduncAlinanMateryaller().size();
            int bildirim = k.getBildirimler() != null ? k.getBildirimler().size() : 0;
            toplamOdunc += odunc;
            toplamBildirim += bildirim;
            System.out.println("    👤 " + k.getIsim() + " [" + k.getRol() + "] → Ödünç:" + odunc
                + " | Bildirim:" + bildirim + " | Kredi:" + k.getKrediPuani());
        }
        System.out.println("  📊 Toplam ödünç kaydı: " + toplamOdunc);
        System.out.println("  📊 Toplam bildirim:    " + toplamBildirim);
        System.out.println();

        // ── 2. PostgreSQL bağlantısı ve Hibernate Session ─────────────
        System.out.println("[2/3] PostgreSQL bağlantısı kuruluyor...");
        System.out.println("  → jdbc:postgresql://" + PG_HOST + ":" + PG_PORT + "/" + PG_DB);

        SessionFactory sessionFactory = null;
        int kullaniciSayisi = 0;
        int materyalSayisi = 0;

        try {
            Properties props = new Properties();
            props.put(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.postgresql.Driver");
            props.put(AvailableSettings.JAKARTA_JDBC_URL,
                "jdbc:postgresql://" + PG_HOST + ":" + PG_PORT + "/" + PG_DB);
            props.put(AvailableSettings.JAKARTA_JDBC_USER, PG_USER);
            props.put(AvailableSettings.JAKARTA_JDBC_PASSWORD, PG_PASS);
            props.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
            props.put(AvailableSettings.HBM2DDL_AUTO, "update"); // Tabloları otomatik oluştur/güncelle
            props.put(AvailableSettings.SHOW_SQL, "false");
            props.put(AvailableSettings.FORMAT_SQL, "true");

            Configuration config = new Configuration();
            config.setProperties(props);

            // Tüm entity sınıflarını kaydet
            config.addAnnotatedClass(Kullanici.class);
            config.addAnnotatedClass(Admin.class);
            config.addAnnotatedClass(Uye.class);
            config.addAnnotatedClass(Materyal.class);
            config.addAnnotatedClass(Kitap.class);
            config.addAnnotatedClass(DijitalMedya.class);
            config.addAnnotatedClass(Klasor.class);
            config.addAnnotatedClass(Bildirim.class);

            sessionFactory = config.buildSessionFactory();
            System.out.println("  ✅ PostgreSQL bağlantısı başarılı.");
            System.out.println();

            // ── 3. Verileri PostgreSQL'e yaz ──────────────────────────
            System.out.println("[3/3] Veriler PostgreSQL'e aktarılıyor...");

            Session session = sessionFactory.openSession();

            // Kullanıcıları aktar
            for (Kullanici k : kullanicilar) {
                Transaction tx = session.beginTransaction();
                try {
                    session.persist(k);
                    tx.commit();
                    System.out.println("  ➕ " + k.getIsim() + " (" + k.getRol() + ")");
                    kullaniciSayisi++;
                } catch (Exception e) {
                    if (tx != null && tx.isActive()) tx.rollback();
                    // Zaten varsa merge ile dene
                    Transaction tx2 = session.beginTransaction();
                    try {
                        session.merge(k);
                        tx2.commit();
                        System.out.println("  🔄 " + k.getIsim() + " (güncellendi)");
                        kullaniciSayisi++;
                    } catch (Exception e2) {
                        if (tx2 != null && tx2.isActive()) tx2.rollback();
                        System.err.println("  ❌ HATA: " + k.getIsim() + " aktarılamadı: " + e2.getMessage());
                    }
                }
            }

            // Materyalleri aktar
            for (Materyal m : materyaller) {
                Transaction tx = session.beginTransaction();
                try {
                    session.persist(m);
                    tx.commit();
                    String tur = "Materyal";
                    if (m instanceof Kitap) tur = "Kitap";
                    else if (m instanceof DijitalMedya) tur = "Dijital";
                    else if (m instanceof Klasor) tur = "Klasör";
                    System.out.println("  ➕ [" + tur + "] " + m.getBaslik());
                    materyalSayisi++;
                } catch (Exception e) {
                    if (tx != null && tx.isActive()) tx.rollback();
                    Transaction tx2 = session.beginTransaction();
                    try {
                        session.merge(m);
                        tx2.commit();
                        System.out.println("  🔄 " + m.getBaslik() + " (güncellendi)");
                        materyalSayisi++;
                    } catch (Exception e2) {
                        if (tx2 != null && tx2.isActive()) tx2.rollback();
                        System.err.println("  ❌ HATA: " + m.getBaslik() + " aktarılamadı: " + e2.getMessage());
                    }
                }
            }

            session.close();

            System.out.println();
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("  🎉 VERİ GÖÇÜ BAŞARIYLA TAMAMLANDI!");
            System.out.println("═══════════════════════════════════════════════════");
            System.out.println("  👤 Kullanıcı: " + kullaniciSayisi + " / " + kullanicilar.size());
            System.out.println("  📚 Materyal:  " + materyalSayisi + " / " + materyaller.size());
            System.out.println("  📊 Ödünç:     " + toplamOdunc + " kayıt");
            System.out.println("  📊 Bildirim:  " + toplamBildirim + " adet");
            System.out.println("  🗄️  Hedef:     " + PG_HOST + ":" + PG_PORT + "/" + PG_DB);
            System.out.println("═══════════════════════════════════════════════════");

            // Veri bütünlüğü doğrulaması
            if (kullaniciSayisi == kullanicilar.size() && materyalSayisi == materyaller.size()) {
                System.out.println("  ✅ TÜM VERİLER EKSİKSİZ AKTARILDI.");
            } else {
                System.out.println("  ⚠️  BAZI VERİLER AKTARILAMADI! Lütfen hata loglarını kontrol edin.");
            }

        } catch (Exception e) {
            System.err.println("\n  ❌ KRİTİK HATA: Veri göçü başarısız oldu!");
            System.err.println("  Hata: " + e.getMessage());
            System.err.println("\n  Lütfen şunları kontrol edin:");
            System.err.println("    1. PostgreSQL çalışıyor mu? → pg_isready");
            System.err.println("    2. Veritabanı var mı? → psql -c \"CREATE DATABASE " + PG_DB + ";\"");
            System.err.println("    3. Kullanıcı/password doğru mu? → psql -h " + PG_HOST + " -U " + PG_USER + " -d " + PG_DB);
            System.err.println("    4. pg_hba.conf MD5/scram-sha-256 kimlik doğrulamasına izin veriyor mu?");
            e.printStackTrace();
        } finally {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
            // SQLite bağlantısını temizle (shutdown hook çalışmasın diye)
            DatabaseManager.tekOrnekSifirla();
        }
    }
}
