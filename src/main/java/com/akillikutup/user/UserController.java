package com.akillikutup.user;

import com.akillikutup.user.dto.UserCreateRequest;
import com.akillikutup.user.dto.UserResponse;
import com.akillikutup.user.dto.UserUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/kullanicilar")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<User> users = userService.findAll();
        List<UserResponse> response = users.stream().map(k -> {
            String tc = k.getTcNoDogrudan();
            return UserResponse.builder()
                .id("M-" + Math.abs(tc.hashCode()))
                .isim(k.getIsim())
                .tcKimlikNo(tc != null && tc.length() == 11
                    ? tc.substring(0, 3) + "*****" + tc.substring(8) : tc)
                .email(k.getEmail() != null ? k.getEmail() : "Yok")
                .rol(k.getRol().name())
                .build();
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/kullanicilar")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody UserCreateRequest body) {
        try {
            String isim = body.getIsim();
            String tcNo = body.getTcKimlikNo();
            String email = body.getEmail();
            String rol = body.getRol() != null ? body.getRol() : "uye";
            String sifre = body.getSifre() != null ? body.getSifre() : "123456";

            if (isim == null || tcNo == null || email == null) {
                Map<String, Object> err = new HashMap<>();
                err.put("basarili", false);
                err.put("mesaj", "Eksik bilgi");
                return ResponseEntity.badRequest().body(err);
            }
            if (userService.existsByIsimOrTcNoOrEmail(isim, tcNo, email)) {
                Map<String, Object> err = new HashMap<>();
                err.put("basarili", false);
                err.put("mesaj", "Bu isim, TC No veya E-posta kayitli.");
                return ResponseEntity.badRequest().body(err);
            }
            userService.createUser(isim, tcNo, email, rol, sifre);
            Map<String, Object> ok = new HashMap<>();
            ok.put("basarili", true);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            return ResponseEntity.internalServerError().body(err);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/kullanicilar/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String userId,
                                                           @RequestBody UserUpdateRequest body) {
        try {
            userService.updateUser(userId, body.getIsim(), body.getTcKimlikNo(), body.getEmail());
            Map<String, Object> ok = new HashMap<>();
            ok.put("basarili", true);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            err.put("mesaj", e.getMessage());
            return ResponseEntity.status(404).body(err);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/kullanicilar/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);
            Map<String, Object> ok = new HashMap<>();
            ok.put("basarili", true);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            err.put("mesaj", "Kullanici bulunamadi");
            return ResponseEntity.status(404).body(err);
        }
    }
}
