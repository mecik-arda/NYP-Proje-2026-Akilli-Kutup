package com.akillikutup.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByTcNo(String tcNo) {
        return userRepository.findByTcNo(tcNo);
    }

    public User createUser(String isim, String tcNo, String email, String rol, String sifre) {
        User.Role role = "ADMIN".equalsIgnoreCase(rol) ? User.Role.ADMIN : User.Role.UYE;
        String encodedPassword = passwordEncoder.encode(sifre);
        User yeni = new User(isim, tcNo, role, encodedPassword);
        yeni.setEmail(email);
        return userRepository.save(yeni);
    }

    public User updateUser(String id, String isim, String tcNo, String email) {
        User k = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Kullanici bulunamadi: " + id));
        if (isim != null) k.setIsim(isim);
        if (tcNo != null) k.setTcNo(tcNo);
        if (email != null) k.setEmail(email);
        return userRepository.save(k);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public boolean existsByIsimOrTcNoOrEmail(String isim, String tcNo, String email) {
        return userRepository.existsByIsimIgnoreCase(isim)
            || userRepository.existsByTcNo(tcNo)
            || userRepository.existsByEmailIgnoreCase(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public long countNonAdmin() {
        return userRepository.findAll().stream()
            .filter(k -> k.getRol() != User.Role.ADMIN)
            .count();
    }
}
