package com.akillikutup.borrow;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/odunc")
    public ResponseEntity<String> borrowBook(jakarta.servlet.http.HttpServletRequest request) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try {
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body == null) body = new com.google.gson.JsonObject();
            String userId = body.has("userId") ? body.get("userId").getAsString() : "";
            String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";

            Map<String, Object> result = borrowService.borrowBook(userId, bookId);
            boolean basarili = result.containsKey("basarili") && Boolean.TRUE.equals(result.get("basarili"));
            if (!basarili) {
                return ResponseEntity.badRequest().body(gson.toJson(result));
            }
            return ResponseEntity.ok(gson.toJson(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("{\"basarili\":false,\"mesaj\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("{\"basarili\":false,\"mesaj\":\"Sunucu hatasi.\"}");
        }
    }

    @PostMapping("/iade")
    public ResponseEntity<String> returnBook(jakarta.servlet.http.HttpServletRequest request) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try {
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"),
                com.google.gson.JsonObject.class);
            if (body == null) body = new com.google.gson.JsonObject();
            String userId = body.has("userId") ? body.get("userId").getAsString() : "";
            String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";

            Map<String, Object> result = borrowService.returnBook(userId, bookId);
            boolean basarili = result.containsKey("basarili") && Boolean.TRUE.equals(result.get("basarili"));
            if (!basarili) {
                return ResponseEntity.badRequest().body(gson.toJson(result));
            }
            return ResponseEntity.ok(gson.toJson(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body("{\"basarili\":false,\"mesaj\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("{\"basarili\":false,\"mesaj\":\"Sunucu hatasi.\"}");
        }
    }

    @GetMapping("/odunc-gecmisi")
    public ResponseEntity<String> getBorrowHistory() {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        return ResponseEntity.ok(gson.toJson(borrowService.getBorrowHistory()));
    }
}
