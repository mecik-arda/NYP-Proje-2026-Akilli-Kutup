package com.akillikutup.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.akillikutup.user.User;
import com.akillikutup.material.*;
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
    private final com.akillikutup.auth.AuthManager authManager = new com.akillikutup.auth.AuthManager();

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
            server.createContext("/api/dijital/upload", new DijitalUploadHandler());
            server.createContext("/api/dijital/klasor", new KlasorCreateHandler());
            server.createContext("/api/istatistikler", new IstatistiklerHandler());
            server.createContext("/api/odunc-gecmisi", new OduncGecmisiHandler());
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
        t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        t.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private User verifyAuth(HttpExchange t) throws IOException {
        String authHeader = t.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            User user = authManager.getUserByToken(token);
            if (user != null) return user;
        }
        sendResponse(t, 401, "{\"basarili\":false, \"mesaj\":\"Yetkisiz islem. Lutfen giris yapin.\"}");
        return null;
    }

    private boolean handleCors(HttpExchange t, String allowedMethods) throws IOException {
        String origin = t.getRequestHeaders().getFirst("Origin");
        if (origin != null && (origin.startsWith("http://localhost:") || origin.startsWith("http://127.0.0.1:"))) {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", origin);
        } else {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:8080");
        }
        t.getResponseHeaders().add("Access-Control-Allow-Methods", allowedMethods);
        t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
            t.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private User findUserById(DatabaseManager db, String id) {
        if (id == null) return null;
        for (User k : db.getKullaniciListesi()) {
            if (k.getId().equals(id) || ("M-" + Math.abs(k.getTcNoDogrudan().hashCode())).equals(id) || k.getTcNoDogrudan().equals(id)) {
                return k;
            }
        }
        return null;
    }

    private Materyal findMateryalById(DatabaseManager db, String id) {
        if (id == null) return null;
        for (Materyal m : db.getMateryalListesi()) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return null;
    }

    class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "GET, OPTIONS")) return;
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                sendResponse(t, 200, "{\"status\":\"UP\"}");
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class KitaplarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "GET, POST, OPTIONS")) return;
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                List<Materyal> materyaller = DatabaseManager.tekOrnekAl().getMateryalListesi();
                JsonArray jsonArray = new JsonArray();
                for (Materyal m : materyaller) {
                    JsonObject dto = new JsonObject();
                    dto.addProperty("id", m.getId());
                    dto.addProperty("baslik", m.getBaslik());
                    dto.addProperty("birimFiyat", m.getBirimFiyat());
                    dto.addProperty("stokAdedi", m.getStokAdedi());
                    dto.addProperty("tur", m.getMateryalTuru());
                    if (m instanceof Kitap) {
                        Kitap kitap = (Kitap) m;
                        dto.addProperty("yazar", kitap.getYazar() != null ? kitap.getYazar() : "Bilinmeyen Yazar");
                        dto.addProperty("kategori", kitap.getKategori() != null ? kitap.getKategori() : "Diger");
                        if (kitap.getKapakGorseli() != null) dto.addProperty("kapakGorseli", kitap.getKapakGorseli());
                        dto.addProperty("isbn", kitap.getIsbn());
                    } else if (m instanceof DijitalMedya) {
                        dto.addProperty("yazar", "Dijital Icerik");
                        dto.addProperty("dosyaFormati", ((DijitalMedya) m).getDosyaFormati());
                        dto.addProperty("dijitalTur", ((DijitalMedya) m).getTur());
                        dto.addProperty("boyut", ((DijitalMedya) m).getBoyut());
                    }
                    jsonArray.add(dto);
                }
                sendResponse(t, 200, gson.toJson(jsonArray));
            } else if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;
                if (reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Sadece yoneticiler kitap ekleyebilir.\"}");
                    return;
                }

                try {
                    JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                    if (body == null) body = new JsonObject();

                    String baslik = body.has("baslik") ? body.get("baslik").getAsString() : "Bilinmeyen Kitap";
                    String yazar = body.has("yazar") ? body.get("yazar").getAsString() : "Bilinmeyen Yazar";
                    String kategori = body.has("kategori") ? body.get("kategori").getAsString() : "Diger";
                    int stokAdedi = body.has("stokAdedi") ? body.get("stokAdedi").getAsInt() : 1;
                    double birimFiyat = body.has("birimFiyat") ? body.get("birimFiyat").getAsDouble() : 50.0;
                    String isbn = body.has("isbn") ? body.get("isbn").getAsString() : "978-0000000000";

                    Kitap yeniKitap = new Kitap(baslik, stokAdedi, birimFiyat, isbn);
                    yeniKitap.setYazar(yazar);
                    yeniKitap.setKategori(kategori);

                    if (body.has("kapakGorseliBase64") && body.has("kapakGorseliAdi")) {
                        String base64Str = body.get("kapakGorseliBase64").getAsString();
                        String rawFileName = body.get("kapakGorseliAdi").getAsString();

                        String safeName = rawFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

                        String lowerName = safeName.toLowerCase();
                        if (!lowerName.endsWith(".jpg") && !lowerName.endsWith(".jpeg") && !lowerName.endsWith(".png")) {
                            sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Sadece JPG ve PNG dosyalari yuklenebilir.\"}");
                            return;
                        }

                        if (base64Str.contains(",")) {
                            base64Str = base64Str.split(",")[1];
                        }
                        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Str);

                        if (imageBytes.length > 5 * 1024 * 1024) {
                            sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Dosya boyutu 5MB sinirini asiyor.\"}");
                            return;
                        }

                        String uploadDir = "frontend/uploads/covers/";
                        Path dirPath = Paths.get(uploadDir);
                        if (!Files.exists(dirPath)) {
                            Files.createDirectories(dirPath);
                        }

                        String uniqueFileName = yeniKitap.getId() + "_" + safeName;
                        Path filePath = dirPath.resolve(uniqueFileName);
                        Files.write(filePath, imageBytes);

                        yeniKitap.setKapakGorseli("/uploads/covers/" + uniqueFileName);
                    }

                    DatabaseManager db = DatabaseManager.tekOrnekAl();
                    db.materyalEkle(yeniKitap);

                    sendResponse(t, 200, "{\"basarili\":true, \"mesaj\":\"Kitap eklendi.\"}");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(t, 500, "{\"basarili\":false, \"mesaj\":\"Sunucu hatasi olustu.\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class KullanicilarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "GET, POST, PUT, DELETE, OPTIONS")) return;
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;
                if (reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Yonetici yetkisi gerekli\"}");
                    return;
                }

                List<User> kullanicilar = DatabaseManager.tekOrnekAl().getKullaniciListesi();
                JsonArray jsonArray = new JsonArray();
                for (User k : kullanicilar) {
                    JsonObject dto = new JsonObject();
                    dto.addProperty("id", "M-" + Math.abs(k.getTcNoDogrudan().hashCode()));
                    dto.addProperty("isim", k.getIsim());
                    String tc = k.getTcNoDogrudan();
                    if (tc != null && tc.length() == 11) {
                        dto.addProperty("tcKimlikNo", tc.substring(0, 3) + "*****" + tc.substring(8));
                    } else {
                        dto.addProperty("tcKimlikNo", tc);
                    }
                    dto.addProperty("email", k.getEmail() != null ? k.getEmail() : "Yok");
                    dto.addProperty("rol", k.getRol().name());

                    JsonArray oduncArr = new JsonArray();
                    for (String mid : k.getOduncAlinanMateryaller()) {
                        JsonObject oduncDetay = new JsonObject();
                        oduncDetay.addProperty("materyalId", mid);
                        if (k.getOduncTarihleri().containsKey(mid)) {
                            oduncDetay.addProperty("oduncTarihi", k.getOduncTarihleri().get(mid));
                        }
                        if (k.getIadeTarihleri().containsKey(mid)) {
                            oduncDetay.addProperty("iadeTarihi", k.getIadeTarihleri().get(mid));
                        }
                        if (k.getOduncCeza().containsKey(mid)) {
                            oduncDetay.addProperty("ceza", k.getOduncCeza().get(mid));
                        }
                        oduncArr.add(oduncDetay);
                    }
                    dto.add("oduncAlinanMateryaller", oduncArr);

                    jsonArray.add(dto);
                }
                sendResponse(t, 200, gson.toJson(jsonArray));
            } else if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;
                if (reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Yonetici yetkisi gerekli\"}");
                    return;
                }
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String isim = body.has("isim") ? body.get("isim").getAsString() : null;
                String tcNo = body.has("tcKimlikNo") ? body.get("tcKimlikNo").getAsString() : null;
                String email = body.has("email") ? body.get("email").getAsString() : null;
                String rol = body.has("rol") ? body.get("rol").getAsString() : "uye";
                String sifre = body.has("sifre") ? body.get("sifre").getAsString() : "123456";

                if (isim == null || tcNo == null || email == null) {
                    sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Eksik bilgi\"}");
                    return;
                }

                DatabaseManager db = DatabaseManager.tekOrnekAl();
                for (User u : db.getKullaniciListesi()) {
                    if (isim.equalsIgnoreCase(u.getIsim()) || tcNo.equals(u.getTcNoDogrudan()) || email.equalsIgnoreCase(u.getEmail())) {
                        sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Bu isim, TC No veya E-posta sistemde kayitli.\"}");
                        return;
                    }
                }

                User.Role role = "ADMIN".equals(rol) ? User.Role.ADMIN : User.Role.UYE;
                User yeniKullanici = new User(isim, tcNo, role, sifre);
                yeniKullanici.setEmail(email);
                db.getKullaniciListesi().add(yeniKullanici);
                db.kullanicilariKaydet();
                sendResponse(t, 200, "{\"basarili\":true}");
            } else if ("PUT".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;
                if (reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Yonetici yetkisi gerekli\"}");
                    return;
                }

                String uri = t.getRequestURI().getPath();
                String[] parts = uri.split("/");
                String userId = parts[parts.length - 1];

                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String isim = body.has("isim") ? body.get("isim").getAsString() : null;
                String tcNo = body.has("tcKimlikNo") ? body.get("tcKimlikNo").getAsString() : null;
                String email = body.has("email") ? body.get("email").getAsString() : null;

                DatabaseManager db = DatabaseManager.tekOrnekAl();
                User k = findUserById(db, userId);

                if (k != null) {
                    for (User u : db.getKullaniciListesi()) {
                        if (!u.getId().equals(k.getId())) {
                            boolean isimCakisiyor = isim != null && isim.equalsIgnoreCase(u.getIsim());
                            boolean tcCakisiyor = tcNo != null && tcNo.equals(u.getTcNoDogrudan());
                            boolean emailCakisiyor = email != null && email.equalsIgnoreCase(u.getEmail());
                            if (isimCakisiyor || tcCakisiyor || emailCakisiyor) {
                                sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Bu isim, TC No veya E-posta sistemde kayitli.\"}");
                                return;
                            }
                        }
                    }

                    if (isim != null) k.setIsim(isim);
                    if (tcNo != null) k.setTcNo(tcNo);
                    if (email != null) k.setEmail(email);
                    db.kullanicilariKaydet();
                    sendResponse(t, 200, "{\"basarili\":true}");
                } else {
                    sendResponse(t, 404, "{\"basarili\":false, \"mesaj\":\"Kullanici bulunamadi\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;
                if (reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Yonetici yetkisi gerekli\"}");
                    return;
                }

                String uri = t.getRequestURI().getPath();
                String[] parts = uri.split("/");
                String userId = parts[parts.length - 1];

                DatabaseManager db = DatabaseManager.tekOrnekAl();
                User k = findUserById(db, userId);
                if (k != null) {
                    db.getKullaniciListesi().remove(k);
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
            if (handleCors(t, "POST, OPTIONS")) return;

            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String tcNo = body.has("tcKimlikNo") ? body.get("tcKimlikNo").getAsString() : "";
                String password = body.has("sifre") ? body.get("sifre").getAsString() : (body.has("sifreHash") ? body.get("sifreHash").getAsString() : "");

                String ipAddress = t.getRemoteAddress().getAddress().getHostAddress();
                try {
                    User user = authManager.login(tcNo, password, ipAddress);

                    if (user != null) {
                        String token = authManager.createSession(user);
                        JsonObject response = new JsonObject();
                        response.addProperty("basarili", true);
                        response.addProperty("ad", user.getIsim());
                        response.addProperty("rol", user.getRol().name());
                        response.addProperty("id", user.getId());
                        response.addProperty("token", token);
                        sendResponse(t, 200, gson.toJson(response));
                    } else {
                        sendResponse(t, 401, "{\"basarili\":false,\"mesaj\":\"Gecersiz kimlik bilgileri\"}");
                    }
                } catch (SecurityException e) {
                    sendResponse(t, 429, "{\"basarili\":false,\"mesaj\":\"" + e.getMessage() + "\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class OduncHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "POST, OPTIONS")) return;
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;

                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String userId = body.has("userId") ? body.get("userId").getAsString() : "";
                String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";

                DatabaseManager db = DatabaseManager.tekOrnekAl();
                User targetUser = findUserById(db, userId);
                Materyal targetMat = findMateryalById(db, bookId);

                if (targetUser != null && !reqUser.getId().equals(targetUser.getId()) && reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Baska bir kullanici adina islem yapamazsiniz\"}");
                    return;
                }

                if (targetUser != null && targetMat != null) {
                    if (targetMat.stoktaVarMi()) {
                        if (targetMat instanceof IOduncAlinabilir) {
                            ((IOduncAlinabilir) targetMat).oduncVer();
                        } else {
                            sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Bu materyal turu odunc alinamaz.\"}");
                            return;
                        }
                        targetUser.materyalOduncAl(targetMat.getId());
                        String bugun = java.time.LocalDate.now().toString();
                        targetUser.setOduncTarihi(targetMat.getId(), bugun);

                        targetUser.getBildirimler().add(new com.akillikutup.user.Bildirim(
                            "info", "fa-book",
                            "\"" + targetMat.getBaslik() + "\" kitabı ödünç alındı.",
                            "Şimdi"
                        ));

                        String iadeGunu = java.time.LocalDate.now().plusDays(14).toString();
                        JsonObject response = new JsonObject();
                        response.addProperty("basarili", true);
                        response.addProperty("mesaj", "Kitap ödünç verildi. İade tarihi: " + iadeGunu);
                        response.addProperty("oduncTarihi", bugun);
                        response.addProperty("iadeTarihi", iadeGunu);

                        db.kullanicilariKaydet();
                        db.materyallariKaydet();
                        sendResponse(t, 200, gson.toJson(response));
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
            if (handleCors(t, "POST, OPTIONS")) return;
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;

                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String userId = body.has("userId") ? body.get("userId").getAsString() : "";
                String bookId = body.has("bookId") ? body.get("bookId").getAsString() : "";

                DatabaseManager db = DatabaseManager.tekOrnekAl();
                User targetUser = findUserById(db, userId);
                Materyal targetMat = findMateryalById(db, bookId);

                if (targetUser != null && !reqUser.getId().equals(targetUser.getId()) && reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Baska bir kullanici adina islem yapamazsiniz\"}");
                    return;
                }

                if (targetUser != null && targetMat != null) {
                    if (targetMat instanceof IOduncAlinabilir) {
                        ((IOduncAlinabilir) targetMat).iadeEt();
                    } else {
                        sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Bu materyal turu iade edilemez.\"}");
                        return;
                    }
                    targetUser.materyalIadeEt(targetMat.getId());
                    String bugun = java.time.LocalDate.now().toString();
                    targetUser.setIadeTarihi(targetMat.getId(), bugun);

                    String oduncTarihiStr = targetUser.getOduncTarihi(targetMat.getId());
                    double ceza = 0.0;
                    if (oduncTarihiStr != null) {
                        try {
                            java.time.LocalDate oduncTarihi = java.time.LocalDate.parse(oduncTarihiStr);
                            java.time.LocalDate iadeTarihi = java.time.LocalDate.parse(bugun);
                            java.time.LocalDate sonIadeGunu = oduncTarihi.plusDays(14);
                            long gecikmeGunu = java.time.temporal.ChronoUnit.DAYS.between(sonIadeGunu, iadeTarihi);
                            if (gecikmeGunu > 0) {
                                ceza = targetMat.cezaHesapla((int) gecikmeGunu);
                                targetUser.setOduncCeza(targetMat.getId(), ceza);
                            }
                        } catch (Exception ex) {
                        }
                    }

                    targetUser.getBildirimler().add(new com.akillikutup.user.Bildirim(
                        "success", "fa-check-circle",
                        "\"" + targetMat.getBaslik() + "\" iade edildi." + (ceza > 0 ? " Gecikme cezası: " + String.format("%.2f", ceza) + " TL" : ""),
                        "Şimdi"
                    ));

                    JsonObject response = new JsonObject();
                    response.addProperty("basarili", true);
                    response.addProperty("mesaj", "Kitap iade alındı." + (ceza > 0 ? " Gecikme cezası: " + String.format("%.2f", ceza) + " TL" : ""));
                    response.addProperty("ceza", ceza);

                    db.kullanicilariKaydet();
                    db.materyallariKaydet();
                    sendResponse(t, 200, gson.toJson(response));
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
            if (handleCors(t, "POST, OPTIONS")) return;

            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User user = verifyAuth(t);
                if (user == null) return;

                String userApiKey = user.getGeminiApiKey();

                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String prompt = body.has("prompt") ? body.get("prompt").getAsString() : "";
                String aiResponse = prompt.isEmpty() ? "Lutfen bir soru girin." : com.akillikutup.chat.GeminiClient.askQuestion(prompt, userApiKey);

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
            if (handleCors(t, "POST, OPTIONS")) return;
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User user = verifyAuth(t);
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
            if (handleCors(t, "POST, OPTIONS")) return;
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User user = verifyAuth(t);
                if (user == null) return;

                JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                if (body == null) body = new JsonObject();
                String eskiSifre = body.has("eskiSifre") ? body.get("eskiSifre").getAsString() : "";
                String yeniSifre = body.has("yeniSifre") ? body.get("yeniSifre").getAsString() : "";

                String ipAddress = t.getRemoteAddress().getAddress().getHostAddress();
                try {
                    if (authManager.login(user.getTcNoDogrudan(), eskiSifre, ipAddress) != null) {
                        user.setSifre(authManager.registerPassword(yeniSifre));
                        DatabaseManager.tekOrnekAl().kullanicilariKaydet();
                        sendResponse(t, 200, "{\"basarili\":true, \"mesaj\":\"Sifre degistirildi\"}");
                    } else {
                        sendResponse(t, 401, "{\"basarili\":false, \"mesaj\":\"Mevcut sifre hatali\"}");
                    }
                } catch (SecurityException e) {
                    sendResponse(t, 429, "{\"basarili\":false,\"mesaj\":\"" + e.getMessage() + "\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class BildirimlerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "GET, OPTIONS")) return;
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                User user = verifyAuth(t);
                if (user == null) return;

                List<com.akillikutup.user.Bildirim> bildirimler = user.getBildirimler();
                sendResponse(t, 200, gson.toJson(bildirimler));
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class BildirimOkunduHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "POST, OPTIONS")) return;
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User user = verifyAuth(t);
                if (user == null) return;

                for (com.akillikutup.user.Bildirim b : user.getBildirimler()) {
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
            if (handleCors(t, "GET, POST, OPTIONS")) return;

            User user = verifyAuth(t);
            if (user == null) return;
            if (user.getRol() != User.Role.ADMIN) {
                sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Bu islem icin yonetici yetkisi gerekiyor.\"}");
                return;
            }

            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                JsonObject originalConfig = com.akillikutup.config.ConfigManager.getConfigData();
                JsonObject safeConfig = new JsonObject();
                String[] safeKeys = {"sessionTimeout", "keyRotationNotify", "auditTrail", "aiTemperature", "maxTokens", "systemPrompt", "backupPeriod", "lateFee", "maxPenalty", "gracePeriod"};
                for (String key : safeKeys) {
                    if (originalConfig.has(key)) safeConfig.add(key, originalConfig.get(key));
                }
                safeConfig.addProperty("geminiApiKeyRaw", "********");
                sendResponse(t, 200, gson.toJson(safeConfig));
            } else if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                try {
                    JsonObject body = gson.fromJson(new InputStreamReader(t.getRequestBody(), "UTF-8"), JsonObject.class);
                    if (body == null) body = new JsonObject();
                    com.akillikutup.config.ConfigManager.updateConfigData(body);
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
            if (handleCors(t, "GET, OPTIONS")) return;

            User user = verifyAuth(t);
            if (user == null) return;
            if (user.getRol() != User.Role.ADMIN) {
                sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Bu islem icin yonetici yetkisi gerekiyor.\"}");
                return;
            }

            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                t.getResponseHeaders().add("Content-Type", "application/zip");
                t.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"kutuphane_yedek.zip\"");
                t.sendResponseHeaders(200, 0);

                try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(t.getResponseBody())) {
                    String[] files = {"data/database.db", "data/config.json"};
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

    class IstatistiklerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "GET, OPTIONS")) return;
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                DatabaseManager db = DatabaseManager.tekOrnekAl();
                List<Materyal> materyaller = db.getMateryalListesi();
                List<User> kullanicilar = db.getKullaniciListesi();

                JsonObject stats = new JsonObject();

                long kitapSayisi = materyaller.stream().filter(m -> "Kitap".equals(m.getMateryalTuru())).count();
                long dijitalSayisi = materyaller.stream().filter(m -> "DijitalMedya".equals(m.getMateryalTuru())).count();
                long klasorSayisi = materyaller.stream().filter(m -> "Klasor".equals(m.getMateryalTuru())).count();
                long uyeSayisi = kullanicilar.stream().filter(k -> k.getRol() != User.Role.ADMIN).count();

                stats.addProperty("toplamKitap", kitapSayisi);
                stats.addProperty("toplamDijitalVarlik", dijitalSayisi + klasorSayisi);
                stats.addProperty("toplamUye", uyeSayisi);

                int aktifOdunc = 0;
                int gecikmis = 0;
                double toplamBekleyenCeza = 0.0;
                double tahsilEdilenCeza = 0.0;
                java.time.LocalDate bugun = java.time.LocalDate.now();

                for (User k : kullanicilar) {
                    for (String mid : k.getOduncAlinanMateryaller()) {
                        aktifOdunc++;
                        String oduncTarihiStr = k.getOduncTarihi(mid);
                        if (oduncTarihiStr != null) {
                            try {
                                java.time.LocalDate oduncTarihi = java.time.LocalDate.parse(oduncTarihiStr);
                                java.time.LocalDate sonGun = oduncTarihi.plusDays(14);
                                if (bugun.isAfter(sonGun)) {
                                    gecikmis++;
                                    long gecikmeGunu = java.time.temporal.ChronoUnit.DAYS.between(sonGun, bugun);
                                    double ceza = gecikmeGunu * 5.0;
                                    toplamBekleyenCeza += ceza;
                                }
                            } catch (Exception e) { }
                        }
                    }
                    for (Double ceza : k.getOduncCeza().values()) {
                        tahsilEdilenCeza += ceza;
                    }
                }

                stats.addProperty("aktifOdunc", aktifOdunc);
                stats.addProperty("gecikmis", gecikmis);
                stats.addProperty("toplamBekleyenCeza", Math.round(toplamBekleyenCeza * 100.0) / 100.0);
                stats.addProperty("tahsilEdilenCeza", Math.round(tahsilEdilenCeza * 100.0) / 100.0);

                JsonObject haftalik = new JsonObject();
                String[] gunAdlari = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
                java.time.DayOfWeek bugunGun = bugun.getDayOfWeek();
                int bugunIndex = bugunGun.getValue() - 1;

                int[] gunlukOdunc = new int[7];
                for (User k : kullanicilar) {
                    for (String mid : k.getOduncAlinanMateryaller()) {
                        String tarihStr = k.getOduncTarihi(mid);
                        if (tarihStr != null) {
                            try {
                                java.time.LocalDate oduncGunu = java.time.LocalDate.parse(tarihStr);
                                long gunFarki = java.time.temporal.ChronoUnit.DAYS.between(oduncGunu, bugun);
                                if (gunFarki >= 0 && gunFarki < 7) {
                                    int gunIndex = (int) ((bugunIndex - gunFarki + 7) % 7);
                                    gunlukOdunc[gunIndex]++;
                                }
                            } catch (Exception e) { }
                        }
                    }
                }

                for (int i = 0; i < 7; i++) {
                    int gunIndex = (bugunIndex - (6 - i) + 7) % 7;
                    haftalik.addProperty(gunAdlari[i], gunlukOdunc[gunIndex]);
                }
                stats.add("haftalikEtkilesim", haftalik);

                JsonObject kategoriDagilim = new JsonObject();
                java.util.Map<String, Integer> kategoriSayac = new java.util.HashMap<>();
                for (Materyal m : materyaller) {
                    if ("Kitap".equals(m.getMateryalTuru())) {
                        String kat = ((Kitap) m).getKategori();
                        if (kat == null || kat.isEmpty()) kat = "Diğer";
                        kategoriSayac.put(kat, kategoriSayac.getOrDefault(kat, 0) + 1);
                    }
                }
                for (java.util.Map.Entry<String, Integer> entry : kategoriSayac.entrySet()) {
                    kategoriDagilim.addProperty(entry.getKey(), entry.getValue());
                }
                stats.add("kategoriDagilimi", kategoriDagilim);

                JsonArray populerKitaplar = new JsonArray();
                java.util.Map<String, Integer> oduncSayaci = new java.util.HashMap<>();
                for (User k : kullanicilar) {
                    for (String mid : k.getOduncAlinanMateryaller()) {
                        oduncSayaci.put(mid, oduncSayaci.getOrDefault(mid, 0) + 1);
                    }
                }
                oduncSayaci.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(5)
                    .forEach(entry -> {
                        Materyal m = findMateryalById(db, entry.getKey());
                        if (m != null) {
                            JsonObject kitapObj = new JsonObject();
                            kitapObj.addProperty("id", m.getId());
                            kitapObj.addProperty("baslik", m.getBaslik());
                            kitapObj.addProperty("oduncSayisi", entry.getValue());
                            if ("Kitap".equals(m.getMateryalTuru())) {
                                kitapObj.addProperty("yazar", ((Kitap) m).getYazar());
                            }
                            populerKitaplar.add(kitapObj);
                        }
                    });
                stats.add("populerKitaplar", populerKitaplar);

                JsonArray sonIslemler = new JsonArray();
                for (User k : kullanicilar) {
                    for (String mid : k.getOduncAlinanMateryaller()) {
                        String tarihStr = k.getOduncTarihi(mid);
                        Materyal m = findMateryalById(db, mid);
                        if (tarihStr != null && m != null) {
                            JsonObject islem = new JsonObject();
                            islem.addProperty("tip", "odunc");
                            islem.addProperty("kitapAdi", m.getBaslik());
                            islem.addProperty("kullaniciAdi", k.getIsim());
                            islem.addProperty("tarih", tarihStr);
                            islem.addProperty("kitapId", mid);
                            islem.addProperty("kullaniciId", k.getId());
                            sonIslemler.add(islem);
                        }
                    }
                }
                JsonArray siraliIslemler = new JsonArray();
                sonIslemler.asList().stream()
                    .map(e -> e.getAsJsonObject())
                    .sorted((a, b) -> b.get("tarih").getAsString().compareTo(a.get("tarih").getAsString()))
                    .limit(10)
                    .forEach(siraliIslemler::add);
                stats.add("sonIslemler", siraliIslemler);

                sendResponse(t, 200, gson.toJson(stats));
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class OduncGecmisiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "GET, OPTIONS")) return;
            if ("GET".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;

                DatabaseManager db = DatabaseManager.tekOrnekAl();
                JsonArray gecmis = new JsonArray();

                for (User k : db.getKullaniciListesi()) {
                    if (reqUser.getRol() != User.Role.ADMIN && !reqUser.getId().equals(k.getId())) {
                        continue;
                    }

                    for (String mid : k.getOduncAlinanMateryaller()) {
                        Materyal m = findMateryalById(db, mid);
                        JsonObject kayit = new JsonObject();
                        kayit.addProperty("kullaniciId", k.getId());
                        kayit.addProperty("kullaniciAdi", k.getIsim());
                        kayit.addProperty("materyalId", mid);
                        kayit.addProperty("kitapAdi", m != null ? m.getBaslik() : "Bilinmeyen");
                        kayit.addProperty("oduncTarihi", k.getOduncTarihi(mid));
                        kayit.addProperty("iadeTarihi", k.getIadeTarihi(mid));
                        kayit.addProperty("ceza", k.getOduncCeza(mid));
                        kayit.addProperty("durum", k.getIadeTarihi(mid) != null ? "İade Edildi" : "Aktif");
                        gecmis.add(kayit);
                    }
                }

                sendResponse(t, 200, gson.toJson(gecmis));
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
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".gif")) return "image/gif";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            if (path.endsWith(".woff2")) return "font/woff2";
            if (path.endsWith(".woff")) return "font/woff";
            return "application/octet-stream";
        }
    }

    class DijitalUploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "POST, OPTIONS")) return;
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;
                if (reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Yonetici yetkisi gerekli\"}");
                    return;
                }

                try {
                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "UTF-8");
                    JsonObject body = com.google.gson.JsonParser.parseReader(isr).getAsJsonObject();

                    String baslik = body.has("baslik") && !body.get("baslik").isJsonNull() ? body.get("baslik").getAsString() : null;
                    String tur = body.has("tur") && !body.get("tur").isJsonNull() ? body.get("tur").getAsString() : null;
                    String boyut = body.has("boyut") && !body.get("boyut").isJsonNull() ? body.get("boyut").getAsString() : null;
                    String format = body.has("format") && !body.get("format").isJsonNull() ? body.get("format").getAsString() : null;

                    if (baslik == null || tur == null || boyut == null || format == null) {
                        sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Gecersiz veya eksik veri gonderildi.\"}");
                        return;
                    }

                    DijitalMedya dm = new DijitalMedya(baslik, 0.0, format, tur, boyut);
                    DatabaseManager db = DatabaseManager.tekOrnekAl();
                    db.materyalEkle(dm);

                    sendResponse(t, 200, "{\"basarili\":true, \"mesaj\":\"Dijital varlik eklendi.\"}");
                } catch (Exception e) {
                    sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Gecersiz veri formatı.\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }

    class KlasorCreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (handleCors(t, "POST, OPTIONS")) return;
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                User reqUser = verifyAuth(t);
                if (reqUser == null) return;
                if (reqUser.getRol() != User.Role.ADMIN) {
                    sendResponse(t, 403, "{\"basarili\":false, \"mesaj\":\"Yonetici yetkisi gerekli\"}");
                    return;
                }

                try {
                    InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "UTF-8");
                    JsonObject body = com.google.gson.JsonParser.parseReader(isr).getAsJsonObject();

                    if (!body.has("baslik") || body.get("baslik").isJsonNull()) {
                        sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Baslik alani zorunludur.\"}");
                        return;
                    }
                    String baslik = body.get("baslik").getAsString();

                    Klasor klasor = new Klasor(baslik);
                    DatabaseManager db = DatabaseManager.tekOrnekAl();
                    db.materyalEkle(klasor);

                    sendResponse(t, 200, "{\"basarili\":true, \"mesaj\":\"Klasor olusturuldu.\"}");
                } catch (Exception e) {
                    sendResponse(t, 400, "{\"basarili\":false, \"mesaj\":\"Gecersiz veri formatı.\"}");
                }
            } else {
                t.sendResponseHeaders(405, -1);
            }
        }
    }
}
