package com.akillikutup;

import com.akillikutup.core.Admin;
import com.akillikutup.core.Kitap;
import com.akillikutup.core.Kullanici;
import com.akillikutup.repository.KitapRepository;
import com.akillikutup.repository.KullaniciRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AkilliKutupV4IntegrationTest {

    @Autowired private KullaniciRepository kullaniciRepository;
    @Autowired private KitapRepository kitapRepository;

    @BeforeEach
    void setUp() {
        kullaniciRepository.deleteAll();
        kitapRepository.deleteAll();
    }

    @Test
    @DisplayName("JPA context yuklenmeli, repository bean'leri hazir olmali")
    void contextLoads() {
        assertNotNull(kullaniciRepository);
        assertNotNull(kitapRepository);
    }

    @Test
    @DisplayName("Admin kullanicisi olusturma, PostgreSQL'e kaydetme ve geri okuma")
    void createAndRetrieveAdmin() {
        Admin admin = new Admin("Test Admin", "11111111111", "bcrypt_encoded_password");
        admin.setEmail("admin@test.com");
        Admin saved = (Admin) kullaniciRepository.save(admin);
        assertNotNull(saved.getId());
        assertEquals("Test Admin", saved.getIsim());
        assertEquals("ADMIN", saved.getRol());

        Optional<Kullanici> found = kullaniciRepository.findByTcNo("11111111111");
        assertTrue(found.isPresent());
        assertInstanceOf(Admin.class, found.get());
    }

    @Test
    @DisplayName("Kitap CRUD islemleri JPA uzerinden calismali")
    void bookCrudOperations() {
        Kitap kitap = new Kitap("Java Programming", 5, 100.0, "978-1234567890");
        kitap.setYazar("John Doe");
        kitap.setKategori("Yazilim");
        kitapRepository.save(kitap);

        List<Kitap> kitaplar = kitapRepository.findAll();
        assertEquals(1, kitaplar.size());
        assertEquals(5, kitaplar.get(0).getStokAdedi());
        assertTrue(kitaplar.get(0).stoktaVarMi());

        List<Kitap> searchResults = kitapRepository.findByBaslikContainingIgnoreCase("java");
        assertEquals(1, searchResults.size());

        kitapRepository.deleteById(kitaplar.get(0).getId());
        assertEquals(0, kitapRepository.count());
    }

    @Test
    @DisplayName("Kullanici kopya kontrolu calismali")
    void duplicateUserCheck() {
        Admin admin = new Admin("Test Admin 2", "22222222222", "pass");
        admin.setEmail("admin2@test.com");
        kullaniciRepository.save(admin);

        assertTrue(kullaniciRepository.existsByIsimIgnoreCase("Test Admin 2"));
        assertTrue(kullaniciRepository.existsByTcNo("22222222222"));
        assertTrue(kullaniciRepository.existsByEmailIgnoreCase("admin2@test.com"));
        assertFalse(kullaniciRepository.existsByTcNo("99999999999"));
    }
}
