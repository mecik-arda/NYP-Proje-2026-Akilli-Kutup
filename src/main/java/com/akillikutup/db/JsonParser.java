package com.akillikutup.db;

import com.akillikutup.core.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String serializeKullanicilar(List<Kullanici> kullaniciListesi) {
        JsonArray array = new JsonArray();
        for (Kullanici k : kullaniciListesi) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", k.getId());
            obj.addProperty("isim", k.getIsim());
            obj.addProperty("tcNo", k.getTcNoDogrudan());
            obj.addProperty("rol", k.getRol());
            obj.addProperty("sifre", k.getSifre());
            obj.addProperty("krediPuani", k.getKrediPuani());
            JsonArray oduncArray = new JsonArray();
            for(String matId : k.getOduncAlinanMateryaller()) {
                oduncArray.add(matId);
            }
            obj.add("oduncAlinanMateryaller", oduncArray);
            array.add(obj);
        }
        return gson.toJson(array);
    }

    public static String serializeMateryaller(List<Materyal> materyalListesi) {
        JsonArray array = new JsonArray();
        for (Materyal m : materyalListesi) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", m.getId());
            obj.addProperty("baslik", m.getBaslik());
            obj.addProperty("birimFiyat", m.getBirimFiyat());
            obj.addProperty("stokAdedi", m.getStokAdedi());
            
            if (m instanceof Kitap) {
                obj.addProperty("tur", "Kitap");
                obj.addProperty("isbn", ((Kitap) m).getIsbn());
            } else if (m instanceof DijitalMedya) {
                obj.addProperty("tur", "DijitalMedya");
                obj.addProperty("dosyaFormati", ((DijitalMedya) m).getDosyaFormati());
                obj.addProperty("toplamErisimSayisi", ((DijitalMedya) m).getToplamErisimSayisi());
                obj.addProperty("sonUretilenLisans", ((DijitalMedya) m).getSonUretilenLisans());
            }
            array.add(obj);
        }
        return gson.toJson(array);
    }

    public static List<Kullanici> deserializeKullanicilar(String json) {
        List<Kullanici> sonuc = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return sonuc;
        
        try {
            JsonArray array = com.google.gson.JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                String id = getAsString(obj, "id");
                String isim = getAsString(obj, "isim");
                String tcNo = getAsString(obj, "tcNo");
                String rol = getAsString(obj, "rol");
                String sifre = getAsString(obj, "sifre");
                int krediPuani = getAsInt(obj, "krediPuani");

                Kullanici kullanici;
                if ("ADMIN".equals(rol)) {
                    kullanici = new Admin(isim, tcNo, sifre);
                } else {
                    Uye uye = new Uye(isim, tcNo, sifre);
                    int fark = krediPuani - 100;
                    if (fark != 0) {
                        uye.puanGuncelle(fark);
                    }
                    kullanici = uye;
                }
                if (id != null) {
                    kullanici.setId(id);
                }
                if (obj.has("oduncAlinanMateryaller") && !obj.get("oduncAlinanMateryaller").isJsonNull()) {
                    JsonArray oduncArr = obj.getAsJsonArray("oduncAlinanMateryaller");
                    for (JsonElement e : oduncArr) {
                        kullanici.materyalOduncAl(e.getAsString());
                    }
                }
                sonuc.add(kullanici);
            }
        } catch (Exception e) {
            throw new RuntimeException("Kullanici JSON parse hatasi", e);
        }
        return sonuc;
    }

    public static List<Materyal> deserializeMateryaller(String json) {
        List<Materyal> sonuc = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return sonuc;
        
        try {
            JsonArray array = com.google.gson.JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                String tur = getAsString(obj, "tur");
                String id = getAsString(obj, "id");
                String baslik = getAsString(obj, "baslik");
                double birimFiyat = getAsDouble(obj, "birimFiyat");
                int stokAdedi = getAsInt(obj, "stokAdedi");

                Materyal materyal;
                if ("Kitap".equals(tur)) {
                    String isbn = getAsString(obj, "isbn");
                    materyal = new Kitap(baslik, stokAdedi, birimFiyat, isbn);
                } else if ("DijitalMedya".equals(tur)) {
                    String dosyaFormati = getAsString(obj, "dosyaFormati");
                    materyal = new DijitalMedya(baslik, birimFiyat, dosyaFormati);
                    // Internal state restore (Optional based on constructor, but we'll try to set what we can)
                } else {
                    continue; // Skip unknown
                }
                materyal.setId(id);
                sonuc.add(materyal);
            }
        } catch (Exception e) {
            throw new RuntimeException("Materyal JSON parse hatasi", e);
        }
        return sonuc;
    }

    private static String getAsString(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : null;
    }

    private static int getAsInt(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsInt() : 0;
    }
    
    private static double getAsDouble(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsDouble() : 0.0;
    }
}
