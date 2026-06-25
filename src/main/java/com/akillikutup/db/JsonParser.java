package com.akillikutup.db;

import com.akillikutup.material.*;
import com.akillikutup.user.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    public static String serializeKullanici(User k) {
        return serializeKullanicilar(java.util.Collections.singletonList(k));
    }

    public static String serializeMateryal(Materyal m) {
        return serializeMateryaller(java.util.Collections.singletonList(m));
    }

    public static User deserializeKullanici(String json) {
        List<User> list = deserializeKullanicilar(json);
        return list.isEmpty() ? null : list.get(0);
    }

    public static Materyal deserializeMateryal(String json) {
        List<Materyal> list = deserializeMateryaller(json);
        return list.isEmpty() ? null : list.get(0);
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String serializeKullanicilar(List<User> kullaniciListesi) {
        JsonArray array = new JsonArray();
        for (User k : kullaniciListesi) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", k.getId());
            obj.addProperty("isim", k.getIsim());
            obj.addProperty("tcNo", k.getTcNoDogrudan());
            obj.addProperty("rol", k.getRol().name());
            obj.addProperty("sifre", k.getSifre());
            if (k.getToken() != null) {
                obj.addProperty("token", k.getToken());
            }
            if (k.getGeminiApiKey() != null) {
                obj.addProperty("geminiApiKey", k.getGeminiApiKey());
            }
            if (k.getEmail() != null) {
                obj.addProperty("email", k.getEmail());
            }
            obj.addProperty("krediPuani", k.getKrediPuani());
            JsonArray oduncArray = new JsonArray();
            for (String matId : k.getOduncAlinanMateryaller()) {
                JsonObject oduncObj = new JsonObject();
                oduncObj.addProperty("materyalId", matId);
                if (k.getOduncTarihleri().containsKey(matId)) {
                    oduncObj.addProperty("oduncTarihi", k.getOduncTarihleri().get(matId));
                }
                if (k.getIadeTarihleri().containsKey(matId)) {
                    oduncObj.addProperty("iadeTarihi", k.getIadeTarihleri().get(matId));
                }
                if (k.getOduncCeza().containsKey(matId)) {
                    oduncObj.addProperty("ceza", k.getOduncCeza().get(matId));
                }
                oduncArray.add(oduncObj);
            }
            obj.add("oduncAlinanMateryaller", oduncArray);

            JsonArray bildirimlerArray = new JsonArray();
            if (k.getBildirimler() != null) {
                for (Bildirim b : k.getBildirimler()) {
                    JsonObject bObj = new JsonObject();
                    bObj.addProperty("id", b.getId());
                    bObj.addProperty("type", b.getType());
                    bObj.addProperty("icon", b.getIcon());
                    bObj.addProperty("text", b.getText());
                    bObj.addProperty("time", b.getTime());
                    bObj.addProperty("unread", b.isUnread());
                    bildirimlerArray.add(bObj);
                }
            }
            obj.add("bildirimler", bildirimlerArray);

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

            obj.addProperty("tur", m.getMateryalTuru());
            if (m instanceof Kitap) {
                Kitap kitap = (Kitap) m;
                obj.addProperty("isbn", kitap.getIsbn());
                if (kitap.getYazar() != null) obj.addProperty("yazar", kitap.getYazar());
                if (kitap.getKategori() != null) obj.addProperty("kategori", kitap.getKategori());
                if (kitap.getKapakGorseli() != null) obj.addProperty("kapakGorseli", kitap.getKapakGorseli());
            } else if (m instanceof DijitalMedya) {
                obj.addProperty("dosyaFormati", ((DijitalMedya) m).getDosyaFormati());
                obj.addProperty("dijitalTur", ((DijitalMedya) m).getTur());
                obj.addProperty("boyut", ((DijitalMedya) m).getBoyut());
                obj.addProperty("toplamErisimSayisi", ((DijitalMedya) m).getToplamErisimSayisi());
                obj.addProperty("sonUretilenLisans", ((DijitalMedya) m).getSonUretilenLisans());
            }
            array.add(obj);
        }
        return gson.toJson(array);
    }

    public static List<User> deserializeKullanicilar(String json) {
        List<User> sonuc = new ArrayList<>();
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

                User.Role role = "ADMIN".equals(rol) ? User.Role.ADMIN : User.Role.UYE;
                User user = new User(isim, tcNo, role, sifre);

                int fark = krediPuani - 100;
                if (fark != 0) {
                    user.puanGuncelle(fark);
                }
                if (id != null) {
                    user.setId(id);
                }
                String token = getAsString(obj, "token");
                if (token != null) {
                    user.setToken(token);
                }
                String geminiApiKey = getAsString(obj, "geminiApiKey");
                if (geminiApiKey != null) {
                    user.setGeminiApiKey(geminiApiKey);
                }
                String email = getAsString(obj, "email");
                if (email != null) {
                    user.setEmail(email);
                } else {
                    user.setEmail("Yok");
                }
                if (obj.has("oduncAlinanMateryaller") && !obj.get("oduncAlinanMateryaller").isJsonNull()) {
                    JsonArray oduncArr = obj.getAsJsonArray("oduncAlinanMateryaller");
                    for (JsonElement e : oduncArr) {
                        if (e.isJsonObject()) {
                            JsonObject oduncObj = e.getAsJsonObject();
                            String matId = getAsString(oduncObj, "materyalId");
                            if (matId != null) {
                                user.materyalOduncAl(matId);
                                if (oduncObj.has("oduncTarihi") && !oduncObj.get("oduncTarihi").isJsonNull()) {
                                    user.setOduncTarihi(matId, oduncObj.get("oduncTarihi").getAsString());
                                }
                                if (oduncObj.has("iadeTarihi") && !oduncObj.get("iadeTarihi").isJsonNull()) {
                                    user.setIadeTarihi(matId, oduncObj.get("iadeTarihi").getAsString());
                                }
                                if (oduncObj.has("ceza") && !oduncObj.get("ceza").isJsonNull()) {
                                    user.setOduncCeza(matId, oduncObj.get("ceza").getAsDouble());
                                }
                            }
                        } else {
                            user.materyalOduncAl(e.getAsString());
                        }
                    }
                }
                if (obj.has("bildirimler") && !obj.get("bildirimler").isJsonNull()) {
                    JsonArray bArr = obj.getAsJsonArray("bildirimler");
                    user.getBildirimler().clear();
                    for (JsonElement e : bArr) {
                        JsonObject bObj = e.getAsJsonObject();
                        Bildirim b = new Bildirim(
                            getAsString(bObj, "type"),
                            getAsString(bObj, "icon"),
                            getAsString(bObj, "text"),
                            getAsString(bObj, "time")
                        );
                        b.setId(getAsString(bObj, "id"));
                        if (bObj.has("unread")) {
                            b.setUnread(bObj.get("unread").getAsBoolean());
                        }
                        user.getBildirimler().add(b);
                    }
                }
                sonuc.add(user);
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
                    Kitap kitap = new Kitap(baslik, stokAdedi, birimFiyat, isbn);
                    kitap.setYazar(getAsString(obj, "yazar"));
                    kitap.setKategori(getAsString(obj, "kategori"));
                    kitap.setKapakGorseli(getAsString(obj, "kapakGorseli"));
                    materyal = kitap;
                } else if ("DijitalMedya".equals(tur)) {
                    String dosyaFormati = getAsString(obj, "dosyaFormati");
                    String dijitalTur = getAsString(obj, "dijitalTur");
                    String boyut = getAsString(obj, "boyut");
                    materyal = new DijitalMedya(baslik, birimFiyat, dosyaFormati, dijitalTur, boyut);
                    if (obj.has("toplamErisimSayisi") && !obj.get("toplamErisimSayisi").isJsonNull()) {
                        ((DijitalMedya) materyal).setToplamErisimSayisi(getAsInt(obj, "toplamErisimSayisi"));
                    }
                } else if ("Klasor".equals(tur)) {
                    materyal = new Klasor(baslik);
                } else {
                    continue;
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
