package com.akillikutup.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.akillikutup.core.Kullanici;
import com.akillikutup.core.Materyal;
import com.akillikutup.db.DatabaseManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ApiServer {
    private HttpServer server;
    private final Gson gson = new Gson();

    public void startServer(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/kitaplar", new KitaplarHandler());
            server.createContext("/api/kullanicilar", new KullanicilarHandler());
            server.createContext("/api/chat", new ChatHandler());
            server.createContext("/api/giris", new LoginHandler());
            server.createContext("/api/odunc", new OduncHandler());
            server.createContext("/api/iade", new IadeHandler());
            server.createContext("/api/profil", new ProfilHandler());
            server.createContext("/api/sifre", new SifreHandler());
            server.createContext("/api/bildirimler", new BildirimlerHandler());
            server.createContext("/api/bildirimler/okundu", new BildirimOkunduHandler());
            server.createContext("/api/settings", new SettingsHandler());
            server.createContext("/api/backup", new BackupHandler());
            server.createContext("/", new StaticFileHandler("frontend"));
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes("UTF-8");
        t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        t.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private Kullanici verifyAuth(HttpExchange t) throws IOException {
        String authHeader = t.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            com.akillikutup.auth.AuthManager authManager = new com.akillikutup.auth.AuthManager();
            Kullanici user = authManager.getUserByToken(token);
            if (user != null) return user;
        }
        sendResponse(t, 401, "{\"basarili\":false, \"mesaj\":\"Yetkisiz islem. Lutfen giris yapin.\"}");
        return null;
    }

    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                sendResponse(t, 200, "{\"status\":\"UP\"}");
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class JsonFileHandler implements HttpHandler {
        private final String filePath;
        private final boolean protectedEndpoint;

        public JsonFileHandler(String filePath, boolean protectedEndpoint) {
            this.filePath = filePath;
            this.protectedEndpoint = protectedEndpoint;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:8080");
            t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");

            if (protectedEndpoint) {
                t.sendResponseHeaders(403, -1);
                return;
            }

            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                byte[] data = Files.readAllBytes(path);
                t.sendResponseHeaders(200, data.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(data);
                }
            } else {
                byte[] errorBytes = "[]".getBytes("UTF-8");
                t.sendResponseHeaders(200, errorBytes.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(errorBytes);
                }
            }
        }
    }

    class KitaplarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                List<com.akillikutup.core.Materyal> materyaller = DatabaseManager.tekOrnekAl().getMateryalListesi();
                JsonArray jsonArray = new JsonArray();
                for (com.akillikutup.core.Materyal m : materyaller) {
                    JsonObject dto = new JsonObject();
                    dto.addProperty("id", m.getId());
                    dto.addProperty("baslik", m.getBaslik());
                    dto.addProperty("birimFiyat", m.getBirimFiyat());
                    dto.addProperty("stokAdedi", m.getStokAdedi());
                    if (m instanceof com.akillikutup.core.Kitap) {
                        dto.addProperty("tur", "Kitap");
                        dto.addProperty("yazar", "Bilinmeyen Yazar");
                        dto.addProperty("isbn", ((com.akillikutup.core.Kitap) m).getIsbn());
                    } else if (m instanceof com.akillikutup.core.DijitalMedya) {
                        dto.addProperty("tur", "DijitalMedya");
                        dto.addProperty("yazar", "Dijital Icerik");
                        dto.addProperty("dosyaFormati", ((com.akillikutup.core.DijitalMedya) m).getDosyaFormati());
                    }
                    jsonArray.add(dto);
                }
                sendResponse(t, 200, gson.toJson(jsonArray));
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class KullanicilarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                if (verifyAuth(t) == null) return;
                
                List<Kullanici> kullanicilar = DatabaseManager.tekOrnekAl().getKullaniciListesi();
                JsonArray jsonArray = new JsonArray();
                for (Kullanici k : kullanicilar) {
                    JsonObject dto = new JsonObject();
                    dto.addProperty("id", "M-" + Math.abs(k.getTcNoDogrudan().hashCode()));
                    dto.addProperty("isim", k.getIsim());
                    dto.addProperty("tcKimlikNo", k.getTcNoDogrudan());
                    dto.addProperty("email", k.getIsim().toLowerCase().replace(" ", "") + "@example.com");
                    dto.addProperty("rol", k.getRol());
                    
                    JsonArray oduncArr = new JsonArray();
                    for(String mid : k.getOduncAlinanMateryaller()) {
                        oduncArr.add(mid);
                    }
                    dto.add("oduncAlinanMateryaller", oduncArr);
                    
                    jsonArray.add(dto);
                }
                sendResponse(t, 200, gson.toJson(jsonArray));
            } else if ("PUT".equalsIgnoreCase(t.getRequestMethod())) {
                if (verifyAuth(t) == null) return;
                
                String uri = t.getRequestURI().getPath();
                String[] parts = uri.split("/");
                String userId = parts[parts.length - 1];
                
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String isim = body.has("isim") ? body.get("isim").getAsString() : null;
                String tcNo = body.has("tcKimlikNo") ? body.get("tcKimlikNo").getAsString() : null;
                
                DatabaseManager db = DatabaseManager.tekOrnekAl();
                boolean found = false;
                for (Kullanici k : db.getKullaniciListesi()) {
                    if (k.getId().equals(userId) || ("M-" + Math.abs(k.getTcNoDogrudan().hashCode())).equals(userId)) {
                        if (isim != null) k.setIsim(isim);
                        if (tcNo != null) k.setTcNo(tcNo);
                        found = true;
                        break;
                    }
                }
                
                if (found) {
                    db.kullanicilariKaydet();
                    sendResponse(t, 200, "{\"basarili\":true}");
                } else {
                    sendResponse(t, 404, "{\"basarili\":false, \"mesaj\":\"Kullanici bulunamadi\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String tcNo = body.has("tcKimlikNo") ? body.get("tcKimlikNo").getAsString() : "";
                String password = body.has("sifreHash") ? body.get("sifreHash").getAsString() : "";
                
                com.akillikutup.auth.AuthManager authManager = new com.akillikutup.auth.AuthManager();
                Kullanici user = authManager.login(tcNo, password);
                
                if (user != null) {
                    String token = authManager.createSession(user);
                    JsonObject response = new JsonObject();
                    response.addProperty("basarili", true);
                    response.addProperty("ad", user.getIsim());
                    response.addProperty("rol", user.getRol());
                    response.addProperty("id", user.getId());
                    response.addProperty("token", token);
                    if (user.getGeminiApiKey() != null) {
                        response.addProperty("geminiApiKey", user.getGeminiApiKey());
                    }
                    sendResponse(t, 200, gson.toJson(response));
                } else {
                    sendResponse(t, 401, "{\"basarili\":false,\"mesaj\":\"Gecersiz kimlik bilgileri\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class OduncHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                if (verifyAuth(t) == null) return;
                
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String userId = body.has("userId") ? body.get("userId").getAsString() : "";
                String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";
                
                DatabaseManager db = DatabaseManager.tekOrnekAl();
                Kullanici targetUser = null;
                for (Kullanici k : db.getKullaniciListesi()) {
                    if (k.getId().equals(userId) || ("M-" + Math.abs(k.getTcNoDogrudan().hashCode())).equals(userId) || k.getTcNoDogrudan().equals(userId)) {
                        targetUser = k; break;
                    }
                }
                
                Materyal targetMat = null;
                for (Materyal m : db.getMateryalListesi()) {
                    if (m.getId().equals(bookId)) {
                        targetMat = m; break;
                    }
                }

                if (targetUser != null && targetMat != null) {
                    if (targetMat.stoktaVarMi()) {
                        targetMat.oduncVer();
                        targetUser.materyalOduncAl(targetMat.getId());
                        db.kullanicilariKaydet();
                        db.materyallariKaydet();
                        sendResponse(t, 200, "{\"basarili\":true}");
                    } else {
                        sendResponse(t, 200, "{\"basarili\":false, \"mesaj\":\"Stokta yok\"}");
                    }
                } else {
                    sendResponse(t, 200, "{\"basarili\":false, \"mesaj\":\"Kullanici veya Materyal bulunamadi\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class IadeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                if (verifyAuth(t) == null) return;
                
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String userId = body.has("userId") ? body.get("userId").getAsString() : "";
                String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";
                
                DatabaseManager db = DatabaseManager.tekOrnekAl();
                Kullanici targetUser = null;
                for (Kullanici k : db.getKullaniciListesi()) {
                    if (k.getId().equals(userId) || ("M-" + Math.abs(k.getTcNoDogrudan().hashCode())).equals(userId) || k.getTcNoDogrudan().equals(userId)) {
                        targetUser = k; break;
                    }
                }
                
                Materyal targetMat = null;
                for (Materyal m : db.getMateryalListesi()) {
                    if (m.getId().equals(bookId)) {
                        targetMat = m; break;
                    }
                }

                if (targetUser != null && targetMat != null) {
                    targetMat.iadeEt();
                    targetUser.materyalIadeEt(targetMat.getId());
                    db.kullanicilariKaydet();
                    db.materyallariKaydet();
                    sendResponse(t, 200, "{\"basarili\":true}");
                } else {
                    sendResponse(t, 200, "{\"basarili\":false, \"mesaj\":\"Kullanici veya Materyal bulunamadi\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                String userApiKey = null;
                String authHeader = t.getRequestHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    com.akillikutup.auth.AuthManager authManager = new com.akillikutup.auth.AuthManager();
                    Kullanici user = authManager.getUserByToken(token);
                    if (user != null) {
                        userApiKey = user.getGeminiApiKey();
                    }
                }
                
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String prompt = body.has("prompt") ? body.get("prompt").getAsString() : "";
                String aiResponse = prompt.isEmpty() ? "Lutfen bir soru girin." : GeminiClient.askQuestion(prompt, userApiKey);
                
                JsonObject response = new JsonObject();
                response.addProperty("response", aiResponse);
                sendResponse(t, 200, gson.toJson(response));
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class ProfilHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                Kullanici user = verifyAuth(t);
                if (user == null) return;
                
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String isim = body.has("isim") ? body.get("isim").getAsString() : null;
                String geminiApiKey = body.has("geminiApiKey") ? body.get("geminiApiKey").getAsString() : null;
                
                boolean updated = false;
                if (isim != null && !isim.isEmpty()) {
                    user.setIsim(isim);
                    updated = true;
                }
                if (geminiApiKey != null) {
                    if (geminiApiKey.isEmpty()) {
                        user.setGeminiApiKey(null);
                    } else {
                        user.setGeminiApiKey(geminiApiKey);
                    }
                    updated = true;
                }
                
                if (updated) {
                    DatabaseManager.tekOrnekAl().kullanicilariKaydet();
                    sendResponse(t, 200, "{\"basarili\":true, \"mesaj\":\"Profil guncellendi\"}");
                } else {
                    sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Gecersiz veri\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class SifreHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                Kullanici user = verifyAuth(t);
                if (user == null) return;
                
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String eskiSifre = body.has("eskiSifre") ? body.get("eskiSifre").getAsString() : "";
                String yeniSifre = body.has("yeniSifre") ? body.get("yeniSifre").getAsString() : "";
                
                com.akillikutup.auth.AuthManager authManager = new com.akillikutup.auth.AuthManager();
                if (authManager.login(user.getTcNoDogrudan(), eskiSifre) != null) {
                    user.setSifre(authManager.registerPassword(yeniSifre));
                    DatabaseManager.tekOrnekAl().kullanicilariKaydet();
                    sendResponse(t, 200, "{\"basarili\":true, \"mesaj\":\"Sifre degistirildi\"}");
                } else {
                    sendResponse(t, 401, "{\"basarili\":false, \"mesaj\":\"Mevcut sifre hatali\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class BildirimlerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                Kullanici user = verifyAuth(t);
                if (user == null) return;
                
                List<com.akillikutup.core.Bildirim> bildirimler = user.getBildirimler();
                sendResponse(t, 200, gson.toJson(bildirimler));
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class BildirimOkunduHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                Kullanici user = verifyAuth(t);
                if (user == null) return;
                
                for (com.akillikutup.core.Bildirim b : user.getBildirimler()) {
                    b.setUnread(false);
                }
                DatabaseManager.tekOrnekAl().kullanicilariKaydet();
                sendResponse(t, 200, "{\"basarili\":true}");
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class SettingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            Kullanici user = verifyAuth(t);
            if (user == null) return;
            if (!"ADMIN".equals(user.getRol())) {
                sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Bu islem icin yonetici yetkisi gerekiyor.\"}");
                return;
            }

            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                JsonObject config = com.akillikutup.core.ConfigManager.getConfigData().deepCopy();
                config.remove("gemini_api_key_encrypted");
                config.addProperty("geminiApiKeyRaw", "********");
                sendResponse(t, 200, gson.toJson(config));
            } else if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                try {
                    JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                    if (body == null) body = new JsonObject();
                    com.akillikutup.core.ConfigManager.updateConfigData(body);
                    sendResponse(t, 200, "{\"basarili\":true}");
                } catch (Exception e) {
                    sendResponse(t, 500, "{\"basarili\":false, \"mesaj\":\"Ayarlar kaydedilemedi\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class BackupHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            Kullanici user = verifyAuth(t);
            if (user == null) return;
            if (!"ADMIN".equals(user.getRol())) {
                sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Bu islem icin yonetici yetkisi gerekiyor.\"}");
                return;
            }

            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                t.getResponseHeaders().add("Content-Type", "application/zip");
                t.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"kutuphane_yedek.zip\"");
                t.sendResponseHeaders(200, 0);

                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(t.getResponseBody())) {
                    String[] files = {"data/users.json", "data/materials.json", "data/config.json"};
                    for (String file : files) {
                        Path p = Paths.get(file);
                        if (Files.exists(p)) {
                            java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(p.getFileName().toString());
                            zos.putNextEntry(entry);
                            Files.copy(p, zos);
                            zos.closeEntry();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class StaticFileHandler implements HttpHandler {
        private final String baseDir;
        public StaticFileHandler(String baseDir) { this.baseDir = baseDir; }
        @Override
        public void handle(HttpExchange t) throws IOException {
            String requestURI = t.getRequestURI().getPath();
            if (requestURI.equals("/")) requestURI = "/index.html";
            Path path = Paths.get(baseDir, requestURI);
            
            String canonicalBase = new java.io.File(baseDir).getCanonicalPath();
            String canonicalPath = path.toFile().getCanonicalPath();
            if (!canonicalPath.startsWith(canonicalBase)) {
                t.sendResponseHeaders(403, -1);
                return;
            }

            if (!Files.exists(path) || Files.isDirectory(path)) {
                path = Paths.get(baseDir, "/index.html");
                if (!Files.exists(path)) {
                    t.sendResponseHeaders(404, -1);
                    return;
                }
            }
            String contentType = getContentType(path.toString());
            t.getResponseHeaders().set("Content-Type", contentType);
            byte[] fileBytes = Files.readAllBytes(path);
            t.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = t.getResponseBody()) { os.write(fileBytes); }
        }
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css; charset=utf-8";
            if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (path.endsWith(".json")) return "application/json; charset=utf-8";
            return "text/plain; charset=utf-8";
        }
    }
}
