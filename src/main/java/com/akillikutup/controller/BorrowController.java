package com.akillikutup.controller;

import com.akillikutup.service.BorrowService;
import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BorrowController {

    private final Gson gson = new Gson();
    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping("/odunc")
    public ResponseEntity<String> borrowBook(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"), com.google.gson.JsonObject.class);
            if (body == null) body = new com.google.gson.JsonObject();
            String userId = body.has("userId") ? body.get("userId").getAsString() : "";
            String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";

            Map<String, Object> result = borrowService.borrowBook(userId, bookId);
            return ResponseEntity.ok(gson.toJson(result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok("{\"basarili\":false,\"mesaj\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("{\"basarili\":false,\"mesaj\":\"Sunucu hatasi.\"}");
        }
    }

    @PostMapping("/iade")
    public ResponseEntity<String> returnBook(jakarta.servlet.http.HttpServletRequest request) {
        try {
            com.google.gson.JsonObject body = gson.fromJson(
                new InputStreamReader(request.getInputStream(), "UTF-8"), com.google.gson.JsonObject.class);
            if (body == null) body = new com.google.gson.JsonObject();
            String userId = body.has("userId") ? body.get("userId").getAsString() : "";
            String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";

            Map<String, Object> result = borrowService.returnBook(userId, bookId);
            return ResponseEntity.ok(gson.toJson(result));
        } catch (RuntimeException e) {
            return ResponseEntity.ok("{\"basarili\":false,\"mesaj\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("{\"basarili\":false,\"mesaj\":\"Sunucu hatasi.\"}");
        }
    }

    @GetMapping("/odunc-gecmisi")
    public ResponseEntity<String> getBorrowHistory() {
        return ResponseEntity.ok(gson.toJson(borrowService.getBorrowHistory()));
    }
}
