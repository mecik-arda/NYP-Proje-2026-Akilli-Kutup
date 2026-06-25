package com.akillikutup.material;

import com.akillikutup.material.dto.*;
import com.akillikutup.chat.GeminiClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookController {

    private final KitapService kitapService;

    public BookController(KitapService kitapService) {
        this.kitapService = kitapService;
    }

    @GetMapping("/kitaplar")
    public ResponseEntity<List<BookResponse>> getBooks() {
        List<Kitap> kitaplar = kitapService.findAll();
        List<BookResponse> response = kitaplar.stream().map(k ->
            BookResponse.builder()
                .id(k.getId())
                .baslik(k.getBaslik())
                .birimFiyat(k.getBirimFiyat())
                .stokAdedi(k.getStokAdedi())
                .tur("Kitap")
                .yazar(k.getYazar() != null ? k.getYazar() : "Bilinmeyen Yazar")
                .kategori(k.getKategori() != null ? k.getKategori() : "Diger")
                .isbn(k.getIsbn())
                .kapakGorseli(k.getKapakGorseli())
                .build()
        ).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/kitaplar/{id}")
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable String id) {
        try {
            if (kitapService.findById(id).isEmpty()) {
                Map<String, Object> err = new HashMap<>();
                err.put("basarili", false);
                err.put("mesaj", "Kitap bulunamadı.");
                return ResponseEntity.status(404).body(err);
            }
            kitapService.deleteById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("basarili", true);
            result.put("mesaj", "Kitap silindi.");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            err.put("mesaj", "Sunucu hatasi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/kitaplar")
    public ResponseEntity<Map<String, Object>> addBook(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body == null) body = new com.google.gson.JsonObject();

            String baslik = body.has("baslik") ? body.get("baslik").getAsString() : "Bilinmeyen Kitap";
            String yazar = body.has("yazar") ? body.get("yazar").getAsString() : "Bilinmeyen Yazar";
            String kategori = body.has("kategori") ? body.get("kategori").getAsString() : "Diger";
            int stok = body.has("stokAdedi") ? body.get("stokAdedi").getAsInt() : 1;
            double fiyat = body.has("birimFiyat") ? body.get("birimFiyat").getAsDouble() : 50.0;
            String isbn = body.has("isbn") ? body.get("isbn").getAsString() : "978-0000000000";

            Kitap kitap = new Kitap(baslik, stok, fiyat, isbn);
            kitap.setYazar(yazar);
            kitap.setKategori(kategori);

            if (body.has("kapakGorseliBase64") && body.has("kapakGorseliAdi")) {
                String base64Str = body.get("kapakGorseliBase64").getAsString();
                String rawName = body.get("kapakGorseliAdi").getAsString();

                String lowerName = rawName.toLowerCase();
                if (!lowerName.endsWith(".jpg") && !lowerName.endsWith(".jpeg") && !lowerName.endsWith(".png")) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("basarili", false);
                    err.put("mesaj", "Sadece JPG/PNG.");
                    return ResponseEntity.badRequest().body(err);
                }

                String extension = lowerName.endsWith(".png") ? ".png" : ".jpg";

                if (base64Str.contains(",")) base64Str = base64Str.split(",")[1];
                byte[] bytes = Base64.getDecoder().decode(base64Str);
                if (bytes.length > 5 * 1024 * 1024) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("basarili", false);
                    err.put("mesaj", "5MB siniri.");
                    return ResponseEntity.badRequest().body(err);
                }

                Path dir = Paths.get("frontend/uploads/covers/").toAbsolutePath().normalize();
                if (!Files.exists(dir)) Files.createDirectories(dir);

                String randomFileName = UUID.randomUUID().toString() + extension;
                Path file = dir.resolve(randomFileName);

                if (!file.normalize().startsWith(dir)) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("basarili", false);
                    err.put("mesaj", "Path Traversal tespit edildi.");
                    return ResponseEntity.badRequest().body(err);
                }

                Files.write(file, bytes);
                kitap.setKapakGorseli("/uploads/covers/" + randomFileName);
            }

            kitapService.save(kitap);
            Map<String, Object> ok = new HashMap<>();
            ok.put("basarili", true);
            ok.put("mesaj", "Kitap eklendi.");
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("basarili", false);
            err.put("mesaj", "Sunucu hatasi: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }
}
