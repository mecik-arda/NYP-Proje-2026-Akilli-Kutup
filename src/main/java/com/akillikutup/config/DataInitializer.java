package com.akillikutup.config;

import com.akillikutup.core.Admin;
import com.akillikutup.core.Kitap;
import com.akillikutup.core.Uye;
import com.akillikutup.repository.KitapRepository;
import com.akillikutup.repository.KullaniciRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final KullaniciRepository kullaniciRepository;
    private final KitapRepository kitapRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(KullaniciRepository kullaniciRepository,
                           KitapRepository kitapRepository,
                           PasswordEncoder passwordEncoder) {
        this.kullaniciRepository = kullaniciRepository;
        this.kitapRepository = kitapRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (kullaniciRepository.count() == 0) {
            log.info("Veritabani bos. Ilk admin ve ornek veriler olusturuluyor...");

            Admin admin1 = new Admin("Ahmet Guler", "11111111111", passwordEncoder.encode("12345678"));
            admin1.setEmail("ahmet.guler@kutuphane.local");
            kullaniciRepository.save(admin1);

            Admin admin2 = new Admin("Eren Gider", "33333333333", passwordEncoder.encode("12345678"));
            admin2.setEmail("eren.gider@kutuphane.local");
            kullaniciRepository.save(admin2);

            Uye uye1 = new Uye("Goktug Berke Kuzucu", "44444444444", passwordEncoder.encode("12345678"));
            uye1.setEmail("goktug@kutuphane.local");
            kullaniciRepository.save(uye1);

            Uye splitTestUye = new Uye("SplitBrain Test Kullanici", "99999999999", passwordEncoder.encode("test123"));
            splitTestUye.setEmail("sbtest@test.com");
            kullaniciRepository.save(splitTestUye);

            kitapRepository.save(kitap("1984", "George Orwell", "Roman", 5, 25.0, "978-0451524935"));
            kitapRepository.save(kitap("Seker Portakali", "Jose Mauro de Vasconcelos", "Roman", 3, 15.0, "978-9750719387"));
            kitapRepository.save(kitap("Kucuk Prens", "Antoine de Saint-Exupery", "Cocuk", 7, 20.0, "978-9750726439"));
            kitapRepository.save(kitap("Clean Code", "Robert C. Martin", "Yazilim", 5, 50.0, "9780132350884"));
            kitapRepository.save(kitap("Effective Java", "Joshua Bloch", "Yazilim", 3, 60.0, "9780134685991"));

            log.info("✅ Seed data olusturuldu: 3 admin/uye, 5 kitap");
        } else {
            log.info("Veritabaninda {} kullanici mevcut. Seed data atlandi.",
                kullaniciRepository.count());
        }
    }

    private Kitap kitap(String baslik, String yazar, String kategori, int stok, double fiyat, String isbn) {
        Kitap k = new Kitap(baslik, stok, fiyat, isbn);
        k.setYazar(yazar);
        k.setKategori(kategori);
        return k;
    }
}
