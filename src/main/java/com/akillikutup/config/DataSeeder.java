package com.akillikutup.config;

import com.akillikutup.core.Admin;
import com.akillikutup.core.Kitap;
import com.akillikutup.core.Uye;
import com.akillikutup.repository.KitapRepository;
import com.akillikutup.repository.KullaniciRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final KullaniciRepository kullaniciRepository;
    private final KitapRepository kitapRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(KullaniciRepository kullaniciRepository, KitapRepository kitapRepository, PasswordEncoder passwordEncoder) {
        this.kullaniciRepository = kullaniciRepository;
        this.kitapRepository = kitapRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (kullaniciRepository.count() == 0) {
            System.out.println("[DataSeeder] Veritabani bos. Ornek test verileri yukleniyor...");

            String encodedPassword = passwordEncoder.encode("12345678");

            kullaniciRepository.save(new Admin("Ahmet Guler", "11111111111", encodedPassword));
            kullaniciRepository.save(new Admin("Arda Mecik", "22222222222", encodedPassword));
            kullaniciRepository.save(new Admin("Eren Gider", "33333333333", encodedPassword));
            kullaniciRepository.save(new Uye("Goktug Berke Kuzucu", "44444444444", encodedPassword));

            Kitap k1 = new Kitap("1984", 5, 25.0, "978-0451524935");
            k1.setYazar("George Orwell");
            k1.setKategori("Roman");
            kitapRepository.save(k1);

            Kitap k2 = new Kitap("Seker Portakali", 3, 15.0, "978-9750719387");
            k2.setYazar("Jose Mauro de Vasconcelos");
            k2.setKategori("Roman");
            kitapRepository.save(k2);

            Kitap k3 = new Kitap("Kucuk Prens", 7, 20.0, "978-9750726439");
            k3.setYazar("Antoine de Saint-Exupéry");
            k3.setKategori("Cocuk");
            kitapRepository.save(k3);

            System.out.println("[DataSeeder] Ornek veriler basariyla eklendi!");
        } else {
            System.out.println("[DataSeeder] Veritabani zaten dolu. Atla.");
        }
    }
}
