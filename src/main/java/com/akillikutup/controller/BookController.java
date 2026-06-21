package com.akillikutup.controller;

import com.akillikutup.core.Kitap;
import com.akillikutup.service.KitapService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    private final Gson gson = new Gson();
    private final KitapService kitapService;

    public BookController(KitapService kitapService) {
        this.kitapService = kitapService;
    }

    @GetMapping("/kitaplar")
    public ResponseEntity<String> getBooks() {
        List<Kitap> kitaplar = kitapService.findAll();
        JsonArray jsonArray = new JsonArray();
        for (Kitap k : kitaplar) {
            JsonObject dto = new JsonObject();
            dto.addProperty("id", k.getId());
            dto.addProperty("baslik", k.getBaslik());
            dto.addProperty("birimFiyat", k.getBirimFiyat());
            dto.addProperty("stokAdedi", k.getStokAdedi());
            dto.addProperty("tur", "Kitap");
            dto.addProperty("yazar", k.getYazar() != null ? k.getYazar() : "Bilinmeyen Yazar");
            dto.addProperty("kategori", k.getKategori() != null ? k.getKategori() : "Diger");
            dto.addProperty("isbn", k.getIsbn());
            if (k.getKapakGorseli() != null) dto.addProperty("kapakGorseli", k.getKapakGorseli());
            jsonArray.add(dto);
        }
        return ResponseEntity.ok(gson.toJson(jsonArray));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/kitaplar")
    public ResponseEntity<String> addBook(jakarta.servlet.http.HttpServletRequest request) {
        try {
            JsonObject body = gson.fromJson(new InputStreamReader(request.getInputStream(), "UTF-8"), JsonObject.class);
            if (body == null) body = new JsonObject();

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
                    return ResponseEntity.badRequest()
                        .body("{\"basarili\":false,\"mesaj\":\"Sadece JPG/PNG.\"}");
                }

                String extension = lowerName.endsWith(".png") ? ".png" : ".jpg";

                if (base64Str.contains(",")) base64Str = base64Str.split(",")[1];
                byte[] bytes = Base64.getDecoder().decode(base64Str);
                if (bytes.length > 5 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                        .body("{\"basarili\":false,\"mesaj\":\"5MB siniri.\"}");
                }

                Path dir = Paths.get("frontend/uploads/covers/").toAbsolutePath().normalize();
                if (!Files.exists(dir)) Files.createDirectories(dir);

                String randomFileName = java.util.UUID.randomUUID().toString() + extension;
                Path file = dir.resolve(randomFileName);

                if (!file.normalize().startsWith(dir)) {
                    return ResponseEntity.badRequest()
                        .body("{\"basarili\":false,\"mesaj\":\"Path Traversal tespit edildi.\"}");
                }

                Files.write(file, bytes);
                kitap.setKapakGorseli("/uploads/covers/" + randomFileName);
            }

            kitapService.save(kitap);
            return ResponseEntity.ok("{\"basarili\":true,\"mesaj\":\"Kitap eklendi.\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("{\"basarili\":false,\"mesaj\":\"Sunucu hatasi: " + e.getMessage() + "\"}");
        }
    }
}
