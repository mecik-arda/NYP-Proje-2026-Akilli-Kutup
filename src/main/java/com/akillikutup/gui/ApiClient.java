package com.akillikutup.gui;

import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Swing GUI ile Spring Boot REST API arasindaki HTTP koprusu.
 *
 * JWT token yonetimi, JSON serilestirme, tum CRUD islemleri bu sinif uzerinden yapilir.
 * Singleton — uygulama boyunca tek bir HttpClient ve token havuzu kullanilir.
 */
public class ApiClient {

    private static volatile ApiClient instance;

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;

    private String jwtToken;
    private String currentUserId;
    private String currentUserRol;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.gson = new Gson();
        this.baseUrl = "http://localhost:8080";
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = new ApiClient();
                }
            }
        }
        return instance;
    }

    // ─── Oturum ────────────────────────────────────────────────────

    public String getToken() { return jwtToken; }
    public String getCurrentUserId() { return currentUserId; }
    public String getCurrentUserRol() { return currentUserRol; }

    public void setSession(String token, String userId, String rol) {
        this.jwtToken = token;
        this.currentUserId = userId;
        this.currentUserRol = rol;
    }

    public void clearSession() {
        this.jwtToken = null;
        this.currentUserId = null;
        this.currentUserRol = null;
    }

    public boolean isAuthenticated() {
        return jwtToken != null && !jwtToken.isEmpty();
    }

    // ─── HTTP Yardimci ─────────────────────────────────────────────

    private HttpRequest.Builder authRequest(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(15));
        if (jwtToken != null) {
            builder.header("Authorization", "Bearer " + jwtToken);
        }
        return builder;
    }

    private JsonElement get(String path) throws IOException, InterruptedException {
        HttpRequest request = authRequest(path).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return gson.fromJson(response.body(), JsonElement.class);
        }
        throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }

    private JsonElement post(String path, Object body) throws IOException, InterruptedException {
        String json = gson.toJson(body);
        HttpRequest request = authRequest(path)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return gson.fromJson(response.body(), JsonElement.class);
        }
        throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }

    private void delete(String path) throws IOException, InterruptedException {
        HttpRequest request = authRequest(path).DELETE().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    // ─── Auth ──────────────────────────────────────────────────────

    public JsonObject login(String tcNo, String password) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("tcKimlikNo", tcNo);
        body.addProperty("sifre", password);
        JsonElement result = post("/api/giris", gson.fromJson(body.toString(), Map.class));
        return result.getAsJsonObject();
    }

    // ─── Kitaplar ──────────────────────────────────────────────────

    public JsonArray getBooks() throws IOException, InterruptedException {
        return get("/api/kitaplar").getAsJsonArray();
    }

    public JsonObject addBook(Map<String, Object> bookData) throws IOException, InterruptedException {
        JsonElement result = post("/api/kitaplar", bookData);
        return result.getAsJsonObject();
    }

    public void deleteBook(String id) throws IOException, InterruptedException {
        delete("/api/kitaplar/" + id);
    }

    // ─── Kullanicilar ──────────────────────────────────────────────

    public JsonArray getUsers() throws IOException, InterruptedException {
        return get("/api/kullanicilar").getAsJsonArray();
    }

    public JsonObject addUser(Map<String, Object> userData) throws IOException, InterruptedException {
        JsonElement result = post("/api/kullanicilar", userData);
        return result.getAsJsonObject();
    }

    public void updateUser(String id, Map<String, Object> data) throws IOException, InterruptedException {
        String json = gson.toJson(data);
        HttpRequest request = authRequest("/api/kullanicilar/" + id)
            .method("PUT", HttpRequest.BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode());
        }
    }

    public void deleteUser(String id) throws IOException, InterruptedException {
        delete("/api/kullanicilar/" + id);
    }

    // ─── Odunc Islemleri ───────────────────────────────────────────

    public JsonObject borrowBook(String userId, String bookId) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        body.addProperty("bookId", bookId);
        JsonElement result = post("/api/odunc", gson.fromJson(body.toString(), Map.class));
        return result.getAsJsonObject();
    }

    public JsonObject returnBook(String userId, String bookId) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("userId", userId);
        body.addProperty("bookId", bookId);
        JsonElement result = post("/api/iade", gson.fromJson(body.toString(), Map.class));
        return result.getAsJsonObject();
    }

    public JsonArray getBorrowHistory() throws IOException, InterruptedException {
        return get("/api/odunc-gecmisi").getAsJsonArray();
    }

    // ─── Istatistikler ─────────────────────────────────────────────

    public JsonObject getStats() throws IOException, InterruptedException {
        return get("/api/istatistikler").getAsJsonObject();
    }

    // ─── Ayarlar ───────────────────────────────────────────────────

    public JsonObject getSettings() throws IOException, InterruptedException {
        return get("/api/settings").getAsJsonObject();
    }

    // ─── Aktif Kullanicilar ─────────────────────────────────────────

    public JsonObject getActiveUsers() throws IOException, InterruptedException {
        return get("/api/aktif-kullanicilar").getAsJsonObject();
    }

    public JsonObject reportActivity(String action) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("action", action);
        JsonElement result = post("/api/aktif-kullanicilar/aktivite",
            gson.fromJson(body.toString(), Map.class));
        return result.getAsJsonObject();
    }

    public JsonObject getHourlyActiveStats() throws IOException, InterruptedException {
        return get("/api/istatistikler/saatlik-aktif").getAsJsonObject();
    }

    public JsonObject sendAnnouncement(String message) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("mesaj", message);
        JsonElement result = post("/api/duyuru",
            gson.fromJson(body.toString(), Map.class));
        return result.getAsJsonObject();
    }

    public JsonObject terminateAllSessions() throws IOException, InterruptedException {
        JsonElement result = post("/api/oturumlari-kapat", Map.of());
        return result.getAsJsonObject();
    }

    // ─── Saglik Kontrolu ───────────────────────────────────────────

    public boolean isServerAlive() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/status"))
                .timeout(Duration.ofSeconds(2))
                .GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
