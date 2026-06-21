package com.akillikutup.service;

import com.akillikutup.core.Admin;
import com.akillikutup.core.Kullanici;
import com.akillikutup.core.Uye;
import com.akillikutup.repository.KullaniciRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    public KullaniciService(KullaniciRepository kullaniciRepository, PasswordEncoder passwordEncoder) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Kullanici> findAll() {
        return kullaniciRepository.findAll();
    }

    public Optional<Kullanici> findById(String id) {
        return kullaniciRepository.findById(id);
    }

    public Optional<Kullanici> findByTcNo(String tcNo) {
        return kullaniciRepository.findByTcNo(tcNo);
    }

    public Kullanici createUser(String isim, String tcNo, String email, String rol, String sifre) {
        Kullanici yeni;
        String encodedPassword = passwordEncoder.encode(sifre);
        if ("ADMIN".equalsIgnoreCase(rol)) {
            yeni = new Admin(isim, tcNo, encodedPassword);
        } else {
            yeni = new Uye(isim, tcNo, encodedPassword);
        }
        yeni.setEmail(email);
        return kullaniciRepository.save(yeni);
    }

    public Kullanici updateUser(String id, String isim, String tcNo, String email) {
        Kullanici k = kullaniciRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Kullanici bulunamadi: " + id));
        if (isim != null) k.setIsim(isim);
        if (tcNo != null) k.setTcNo(tcNo);
        if (email != null) k.setEmail(email);
        return kullaniciRepository.save(k);
    }

    public void deleteUser(String id) {
        kullaniciRepository.deleteById(id);
    }

    public boolean existsByIsimOrTcNoOrEmail(String isim, String tcNo, String email) {
        return kullaniciRepository.existsByIsimIgnoreCase(isim)
            || kullaniciRepository.existsByTcNo(tcNo)
            || kullaniciRepository.existsByEmailIgnoreCase(email);
    }

    public Kullanici save(Kullanici kullanici) {
        return kullaniciRepository.save(kullanici);
    }

    public long countNonAdmin() {
        return kullaniciRepository.findAll().stream()
            .filter(k -> !"ADMIN".equals(k.getRol()))
            .count();
    }
}
