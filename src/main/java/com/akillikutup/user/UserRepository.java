package com.akillikutup.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByIsimIgnoreCase(String isim);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByTcNo(String tcNo);
    boolean existsByIsimIgnoreCase(String isim);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByTcNo(String tcNo);
}
