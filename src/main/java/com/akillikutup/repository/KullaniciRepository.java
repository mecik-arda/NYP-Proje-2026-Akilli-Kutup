package com.akillikutup.repository;

import com.akillikutup.core.Kullanici;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KullaniciRepository extends JpaRepository<Kullanici, String> {

    Optional<Kullanici> findByIsimIgnoreCase(String isim);

    Optional<Kullanici> findByEmailIgnoreCase(String email);

    Optional<Kullanici> findByTcNo(String tcNo);

    boolean existsByIsimIgnoreCase(String isim);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByTcNo(String tcNo);
}
