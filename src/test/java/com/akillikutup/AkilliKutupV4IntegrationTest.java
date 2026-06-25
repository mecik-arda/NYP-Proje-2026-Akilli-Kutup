package com.akillikutup;

import com.akillikutup.material.Kitap;
import com.akillikutup.material.KitapRepository;
import com.akillikutup.user.User;
import com.akillikutup.user.UserRepository;
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

    @Autowired private UserRepository userRepository;
    @Autowired private KitapRepository kitapRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        kitapRepository.deleteAll();
    }

    @Test
    @DisplayName("JPA context yuklenmeli, repository bean'leri hazir olmali")
    void contextLoads() {
        assertNotNull(userRepository);
        assertNotNull(kitapRepository);
    }

    @Test
    @DisplayName("Admin kullanicisi olusturma, PostgreSQL'e kaydetme ve geri okuma")
    void createAndRetrieveAdmin() {
        User admin = new User("Test Admin", "11111111111", User.Role.ADMIN, "bcrypt_encoded_password");
        admin.setEmail("admin@test.com");
        User saved = userRepository.save(admin);
        assertNotNull(saved.getId());
        assertEquals("Test Admin", saved.getIsim());
        assertEquals(User.Role.ADMIN, saved.getRol());

        Optional<User> found = userRepository.findByTcNo("11111111111");
        assertTrue(found.isPresent());
        assertEquals(User.Role.ADMIN, found.get().getRol());
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
        User admin = new User("Test Admin 2", "22222222222", User.Role.ADMIN, "pass");
        admin.setEmail("admin2@test.com");
        userRepository.save(admin);

        assertTrue(userRepository.existsByIsimIgnoreCase("Test Admin 2"));
        assertTrue(userRepository.existsByTcNo("22222222222"));
        assertTrue(userRepository.existsByEmailIgnoreCase("admin2@test.com"));
        assertFalse(userRepository.existsByTcNo("99999999999"));
    }
}
