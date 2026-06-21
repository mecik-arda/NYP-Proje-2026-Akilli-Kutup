package com.akillikutup;

import com.akillikutup.gui.ApiClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class E2ESplitBrainTest {

    private static ApiClient api;
    private static String adminToken, adminId, uyeToken, uyeId, testBookId;
    private static final String RUN = String.valueOf(System.currentTimeMillis() % 100000);
    private static final String UYE_ISIM = "E2E_Test_" + RUN;
    private static final String KITAP_AD = "E2E_Kitap_" + RUN;
    private static final String UYE_TC = "777" + String.format("%08d", System.currentTimeMillis() % 100000000);
    private static final String UYE_SIFRE = "test123";

    @BeforeAll
    static void checkServer() {
        api = ApiClient.getInstance();
        assertTrue(api.isServerAlive(), "Spring Boot calismiyor! mvn spring-boot:run");
        System.out.println("✅ Sunucu aktif | Test ID: " + RUN);
    }

    @Test @Order(1)
    @DisplayName("1. Admin girisi → JWT")
    void adminLogin() throws Exception {
        JsonObject r = api.login("11111111111", "12345678");
        assertTrue(r.get("basarili").getAsBoolean());
        adminToken = r.get("token").getAsString();
        adminId = r.get("id").getAsString();
        api.setSession(adminToken, adminId, "ADMIN");
        System.out.println("  ✅ Admin JWT alindi");
    }

    @Test @Order(2)
    @DisplayName("2. WEB → Kullanici ekle")
    void webAddUser() throws Exception {
        Map<String, Object> d = new HashMap<>();
        d.put("isim", UYE_ISIM);
        d.put("tcKimlikNo", UYE_TC);
        d.put("email", "e2e_" + RUN + "@test.local");
        d.put("rol", "uye");
        d.put("sifre", UYE_SIFRE);
        JsonObject r = api.addUser(d);
        assertTrue(r.get("basarili").getAsBoolean(), "Kullanici eklenemedi: " + r);
        System.out.println("  ✅ Kullanici eklendi: " + UYE_ISIM);
    }

    @Test @Order(3)
    @DisplayName("3. API → Kullanici listesinde dogrula")
    void apiVerifyUser() throws Exception {
        JsonArray users = api.getUsers();
        boolean found = false;
        for (JsonElement e : users) {
            if (e.getAsJsonObject().get("isim").getAsString().equals(UYE_ISIM)) {
                found = true; break;
            }
        }
        assertTrue(found);
        System.out.println("  ✅ Kullanici listede");
    }

    @Test @Order(4)
    @DisplayName("4. WEB → Kitap ekle")
    void webAddBook() throws Exception {
        Map<String, Object> d = new HashMap<>();
        d.put("baslik", KITAP_AD);
        d.put("yazar", "Test Yazar");
        d.put("kategori", "Test");
        d.put("stokAdedi", 10);
        d.put("birimFiyat", 150.0);
        d.put("isbn", "978-" + RUN);
        JsonObject r = api.addBook(d);
        assertTrue(r.get("basarili").getAsBoolean(), "Kitap eklenemedi: " + r);
        System.out.println("  ✅ Kitap eklendi: " + KITAP_AD);
    }

    @Test @Order(5)
    @DisplayName("5. Swing ApiClient → Ayni kitap (Split-Brain)")
    void swingReadsSameBook() throws Exception {
        JsonArray books = api.getBooks();
        for (JsonElement e : books) {
            JsonObject b = e.getAsJsonObject();
            if (KITAP_AD.equals(b.get("baslik").getAsString())) {
                testBookId = b.get("id").getAsString();
                System.out.println("  ✅ Swing kitabi gordu → Split-Brain YOK ✅");
                return;
            }
        }
        fail("Kitap bulunamadi → Split-Brain!");
    }

    @Test @Order(6)
    @DisplayName("6. Uye girisi → JWT")
    void uyeLogin() throws Exception {
        JsonObject r = api.login(UYE_TC, UYE_SIFRE);
        assertTrue(r.get("basarili").getAsBoolean());
        uyeToken = r.get("token").getAsString();
        uyeId = r.get("id").getAsString();
        System.out.println("  ✅ Uye JWT alindi");
    }

    @Test @Order(7)
    @DisplayName("7. Odunc alma")
    void borrowBook() throws Exception {
        api.setSession(uyeToken, uyeId, "UYE");
        JsonObject r = api.borrowBook(uyeId, testBookId);
        assertTrue(r.get("basarili").getAsBoolean(), "Odunc basarisiz: " + r);
        System.out.println("  ✅ Odunc alindi | Tarih: " + r.get("oduncTarihi").getAsString());
    }

    @Test @Order(8)
    @DisplayName("8. Odunc gecmisi")
    void borrowHistoryCheck() throws Exception {
        JsonArray h = api.getBorrowHistory();
        boolean found = false;
        for (JsonElement e : h) {
            JsonObject o = e.getAsJsonObject();
            if (KITAP_AD.equals(o.get("kitapAdi").getAsString()) && "Aktif".equals(o.get("durum").getAsString())) {
                found = true; break;
            }
        }
        assertTrue(found);
        System.out.println("  ✅ Gecmiste Aktif olarak gorunuyor");
    }

    @Test @Order(9)
    @DisplayName("9. Istatistikler")
    void statsUpdated() throws Exception {
        int aktif = api.getStats().get("aktifOdunc").getAsInt();
        assertTrue(aktif > 0, "Aktif odunc 0!");
        System.out.println("  ✅ Aktif odunc: " + aktif);
    }

    @Test @Order(10)
    @DisplayName("10. Iade etme")
    void returnBook() throws Exception {
        JsonObject r = api.returnBook(uyeId, testBookId);
        assertTrue(r.get("basarili").getAsBoolean(), "Iade basarisiz: " + r);
        System.out.println("  ✅ Iade edildi | Ceza: " + r.get("ceza").getAsDouble() + " TL");
    }

    @Test @Order(11)
    @DisplayName("11. Guvenlik: Tokensiz kitap ekleme → 403")
    void securityNoTokenBook() {
        api.clearSession();
        try {
            Map<String, Object> d = new HashMap<>();
            d.put("baslik", "HACK");
            api.addBook(d);
            fail("Token olmadan kitap eklenebildi!");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("403"));
        }
        System.out.println("  ✅ 403 FORBIDDEN");
    }

    @Test @Order(12)
    @DisplayName("12. Guvenlik: Tokensiz kullanici listesi → 403")
    void securityNoTokenUsers() {
        try {
            api.getUsers();
            fail("Token olmadan kullanici listesi alinabildi!");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("403"));
        }
        System.out.println("  ✅ 403 FORBIDDEN");
    }

    @Test @Order(13)
    @DisplayName("13. Nihai: Swing + Web = TEK Veritabani")
    void finalVerification() throws Exception {
        JsonObject r = api.login("11111111111", "12345678");
        api.setSession(r.get("token").getAsString(), r.get("id").getAsString(), "ADMIN");

        long userCount = 0;
        for (JsonElement e : api.getUsers()) {
            if (UYE_ISIM.equals(e.getAsJsonObject().get("isim").getAsString())) userCount++;
        }
        assertEquals(1, userCount, "Kullanici sayisi 1 olmali!");

        long bookCount = 0;
        for (JsonElement e : api.getBooks()) {
            if (KITAP_AD.equals(e.getAsJsonObject().get("baslik").getAsString())) bookCount++;
        }
        assertEquals(1, bookCount, "Kitap sayisi 1 olmali!");

        System.out.println("  ✅ Kullanici: " + userCount + " | Kitap: " + bookCount);
        System.out.println("  ✅✅✅ SPLIT-BRAIN COZULDU ✅✅✅");
    }
}
