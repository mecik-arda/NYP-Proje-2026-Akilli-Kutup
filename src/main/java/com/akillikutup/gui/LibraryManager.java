package com.akillikutup.gui;

import com.akillikutup.core.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Swing GUI'nin veri erisim katmani.
 * V4: SQLite yerine Spring Boot REST API uzerinden PostgreSQL'e baglanir.
 * Split-brain sorunu cozulmustur — Swing ve Web ayni veritabanini kullanir.
 */
public class LibraryManager {

    private static volatile LibraryManager instance;

    private Kullanici currentUser;
    private List<Kullanici> cachedUsers;
    private List<Materyal> cachedMaterials;

    private LibraryManager() {
        cachedUsers = new ArrayList<>();
        cachedMaterials = new ArrayList<>();
    }

    public static LibraryManager getInstance() {
        if (instance == null) {
            synchronized (LibraryManager.class) {
                if (instance == null) {
                    instance = new LibraryManager();
                }
            }
        }
        return instance;
    }

    // ─── Oturum ────────────────────────────────────────────────────

    public Kullanici getCurrentUser() { return currentUser; }
    public void setCurrentUser(Kullanici u) { this.currentUser = u; }

    public boolean login(String tcNo, String password) {
        try {
            JsonObject res = ApiClient.getInstance().login(tcNo, password);
            if (res.has("basarili") && res.get("basarili").getAsBoolean()) {
                String rol = res.get("rol").getAsString();
                String id = res.get("id").getAsString();
                String ad = res.get("ad").getAsString();
                String token = res.get("token").getAsString();

                ApiClient.getInstance().setSession(token, id, rol);

                if ("ADMIN".equals(rol)) {
                    currentUser = new Admin(ad, tcNo, "");
                } else {
                    currentUser = new Uye(ad, tcNo, "");
                }
                currentUser.setId(id);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Login API hatasi: " + e.getMessage());
        }
        return false;
    }

    public void logout() {
        ApiClient.getInstance().clearSession();
        currentUser = null;
    }

    // ─── Kullanicilar ──────────────────────────────────────────────

    public List<Kullanici> getUsers() {
        if (!ApiClient.getInstance().isAuthenticated()) return cachedUsers;
        try {
            JsonArray arr = ApiClient.getInstance().getUsers();
            List<Kullanici> list = new ArrayList<>();
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                String isim = o.get("isim").getAsString();
                String rol = o.get("rol").getAsString();
                String tc = o.has("tcKimlikNo") ? o.get("tcKimlikNo").getAsString() : "***********";
                String apiId = o.get("id").getAsString();

                Kullanici k = "ADMIN".equals(rol) ? new Admin(isim, tc, "") : new Uye(isim, tc, "");
                k.setId(apiId);
                if (o.has("email")) k.setEmail(o.get("email").getAsString());
                list.add(k);
            }
            cachedUsers = list;
            return list;
        } catch (Exception e) {
            System.err.println("Kullanici listesi API hatasi: " + e.getMessage());
            return cachedUsers;
        }
    }

    public void addUser(Kullanici user) {
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("isim", user.getIsim());
            data.put("tcKimlikNo", user.getTcNoDogrudan());
            data.put("email", user.getEmail() != null ? user.getEmail() : user.getIsim().replace(" ", ".").toLowerCase() + "@kutuphane.local");
            data.put("rol", user.getRol());
            data.put("sifre", user.getSifre());
            ApiClient.getInstance().addUser(data);
        } catch (Exception e) {
            System.err.println("Kullanici ekleme API hatasi: " + e.getMessage());
        }
    }

    public void removeUser(Kullanici user) {
        try {
            ApiClient.getInstance().deleteUser(user.getId());
        } catch (Exception e) {
            System.err.println("Kullanici silme API hatasi: " + e.getMessage());
        }
    }

    public void updateUser(String apiId, Kullanici updated) {
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("isim", updated.getIsim());
            data.put("tcKimlikNo", updated.getTcNoDogrudan());
            if (updated.getEmail() != null) data.put("email", updated.getEmail());
            ApiClient.getInstance().updateUser(apiId, data);
        } catch (Exception e) {
            System.err.println("Kullanici guncelleme API hatasi: " + e.getMessage());
        }
    }

    // ─── Materyaller ───────────────────────────────────────────────

    public List<Materyal> getMaterials() {
        if (!ApiClient.getInstance().isAuthenticated()) return cachedMaterials;
        try {
            JsonArray arr = ApiClient.getInstance().getBooks();
            List<Materyal> list = new ArrayList<>();
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                String baslik = o.get("baslik").getAsString();
                double fiyat = o.has("birimFiyat") ? o.get("birimFiyat").getAsDouble() : 0;
                String tur = o.has("tur") ? o.get("tur").getAsString() : "Kitap";

                if ("Kitap".equals(tur)) {
                    int stok = o.has("stokAdedi") ? o.get("stokAdedi").getAsInt() : 0;
                    String isbn = o.has("isbn") ? o.get("isbn").getAsString() : "978-0000000000";
                    Kitap kitap = new Kitap(baslik, stok, fiyat, isbn);
                    kitap.setId(id);
                    if (o.has("yazar")) kitap.setYazar(o.get("yazar").getAsString());
                    if (o.has("kategori")) kitap.setKategori(o.get("kategori").getAsString());
                    list.add(kitap);
                } else if ("DijitalMedya".equals(tur)) {
                    String format = o.has("dosyaFormati") ? o.get("dosyaFormati").getAsString() : "PDF";
                    String dijitalTur = o.has("dijitalTur") ? o.get("dijitalTur").getAsString() : "Diger";
                    String boyut = o.has("boyut") ? o.get("boyut").getAsString() : "0 MB";
                    DijitalMedya dm = new DijitalMedya(baslik, fiyat, format, dijitalTur, boyut);
                    dm.setId(id);
                    list.add(dm);
                }
            }
            cachedMaterials = list;
            return list;
        } catch (Exception e) {
            System.err.println("Materyal listesi API hatasi: " + e.getMessage());
            return cachedMaterials;
        }
    }

    public void addMaterial(Materyal m) {
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("baslik", m.getBaslik());
            data.put("birimFiyat", m.getBirimFiyat());
            if (m instanceof Kitap kitap) {
                data.put("isbn", kitap.getIsbn());
                data.put("stokAdedi", kitap.getStokAdedi());
                data.put("yazar", kitap.getYazar());
                data.put("kategori", kitap.getKategori());
            }
            JsonObject res = ApiClient.getInstance().addBook(data);
            if (res.has("basarili") && res.get("basarili").getAsBoolean()) {
                cachedMaterials.add(m);
            }
        } catch (Exception e) {
            System.err.println("Materyal ekleme API hatasi: " + e.getMessage());
        }
    }

    public void removeMaterial(Materyal m) {
        try {
            ApiClient.getInstance().deleteBook(m.getId());
            cachedMaterials.remove(m);
        } catch (Exception e) {
            System.err.println("Materyal silme API hatasi: " + e.getMessage());
        }
    }

    // ─── Odunc Islemleri ───────────────────────────────────────────

    public boolean borrowMaterial(String userId, String materialId) {
        try {
            JsonObject res = ApiClient.getInstance().borrowBook(userId, materialId);
            return res.has("basarili") && res.get("basarili").getAsBoolean();
        } catch (Exception e) {
            System.err.println("Odunc API hatasi: " + e.getMessage());
            return false;
        }
    }

    public boolean returnMaterial(String userId, String materialId) {
        try {
            JsonObject res = ApiClient.getInstance().returnBook(userId, materialId);
            return res.has("basarili") && res.get("basarili").getAsBoolean();
        } catch (Exception e) {
            System.err.println("Iade API hatasi: " + e.getMessage());
            return false;
        }
    }

    // ─── Sunucu Kontrolu ───────────────────────────────────────────

    public boolean isServerAlive() {
        return ApiClient.getInstance().isServerAlive();
    }
}
