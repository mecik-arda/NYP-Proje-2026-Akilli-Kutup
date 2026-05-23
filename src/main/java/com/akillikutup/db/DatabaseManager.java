package com.akillikutup.db;

import com.akillikutup.core.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static DatabaseManager tekOrnek;

    private final String VERI_KLASORU = "data";
    private final String YEDEK_KLASORU = "data" + File.separator + "backup";
    private final String KULLANICI_DOSYASI = "data" + File.separator + "users.json";
    private final String MATERYAL_DOSYASI = "data" + File.separator + "materials.json";

    private final String IZINLI_KOK_DIZIN;

    private List<Kullanici> kullaniciListesi;
    private List<Materyal> materyalListesi;

    private DatabaseManager() {
        kullaniciListesi = new ArrayList<>();
        materyalListesi = new ArrayList<>();

        try {
            IZINLI_KOK_DIZIN = new File(VERI_KLASORU).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Veri klasoru yolu cozumlenemedi: " + e.getMessage());
        }

        klasorleriOlustur();
    }

    public static DatabaseManager tekOrnekAl() {
        if (tekOrnek == null) {
            tekOrnek = new DatabaseManager();
        }
        return tekOrnek;
    }

    public static void tekOrnekSifirla() {
        tekOrnek = null;
    }


    private void klasorleriOlustur() {
        File veriKlasoru = new File(VERI_KLASORU);
        if (!veriKlasoru.exists()) {
            boolean olusturuldu = veriKlasoru.mkdirs();
            if (!olusturuldu) {
                System.err.println("HATA: data/ klasoru olusturulamadi.");
            }
        }

        File yedekKlasoru = new File(YEDEK_KLASORU);
        if (!yedekKlasoru.exists()) {
            boolean olusturuldu = yedekKlasoru.mkdirs();
            if (!olusturuldu) {
                System.err.println("HATA: data/backup/ klasoru olusturulamadi.");
            }
        }
    }


    private void yolGuvenligi(String dosyaYolu) {
        try {
            String gercekYol = new File(dosyaYolu).getCanonicalPath();
            if (!gercekYol.startsWith(IZINLI_KOK_DIZIN)) {
                throw new SecurityException(
                        "GUVENLIK IHLALI: Dosya yolu izinli dizin disina cikiyor! "
                                + "Istenen: " + dosyaYolu + " | "
                                + "Cozumlenen: " + gercekYol + " | "
                                + "Izinli kok: " + IZINLI_KOK_DIZIN
                );
            }
        } catch (IOException e) {
            throw new SecurityException("Dosya yolu cozumlenirken hata olustu: " + e.getMessage());
        }
    }


    private String kullaniciListesiniJsonaSerialize(List<Kullanici> liste) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < liste.size(); i++) {
            Kullanici k = liste.get(i);
            sb.append("  {\n");
            sb.append("    \"isim\": \"").append(jsonKacis(k.getIsim())).append("\",\n");
            sb.append("    \"tcNo\": \"").append(jsonKacis(k.getTcNoDogrudan())).append("\",\n");
            sb.append("    \"rol\": \"").append(jsonKacis(k.getRol())).append("\",\n");
            sb.append("    \"sifre\": \"").append(jsonKacis(k.getSifre())).append("\",\n");
            sb.append("    \"krediPuani\": ").append(k.getKrediPuani()).append("\n");
            sb.append("  }");

            if (i < liste.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    private String materyalListesiniJsonaSerialize(List<Materyal> liste) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < liste.size(); i++) {
            Materyal m = liste.get(i);
            sb.append("  {\n");
            sb.append("    \"id\": \"").append(jsonKacis(m.getId())).append("\",\n");
            sb.append("    \"baslik\": \"").append(jsonKacis(m.getBaslik())).append("\",\n");
            sb.append("    \"birimFiyat\": ").append(m.getBirimFiyat()).append(",\n");
            sb.append("    \"stokAdedi\": ").append(m.getStokAdedi()).append(",\n");

            if (m instanceof Kitap) {
                Kitap kitap = (Kitap) m;
                sb.append("    \"tur\": \"Kitap\",\n");
                sb.append("    \"isbn\": \"").append(jsonKacis(kitap.getIsbn())).append("\"\n");
            } else if (m instanceof DijitalMedya) {
                DijitalMedya dm = (DijitalMedya) m;
                sb.append("    \"tur\": \"DijitalMedya\",\n");
                sb.append("    \"dosyaFormati\": \"").append(jsonKacis(dm.getDosyaFormati())).append("\",\n");
                sb.append("    \"toplamErisimSayisi\": ").append(dm.getToplamErisimSayisi()).append(",\n");

                String lisans = dm.getSonUretilenLisans();
                if (lisans != null) {
                    sb.append("    \"sonUretilenLisans\": \"").append(jsonKacis(lisans)).append("\"\n");
                } else {
                    sb.append("    \"sonUretilenLisans\": null\n");
                }
            }

            sb.append("  }");

            if (i < liste.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    private String jsonKacis(String girdi) {
        if (girdi == null) return "";
        return girdi
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }


    public List<Kullanici> kullanicilariJsondanYukle(String jsonIcerik) {
        List<Kullanici> sonuc = new ArrayList<>();

        if (jsonIcerik == null || jsonIcerik.trim().isEmpty()) {
            return sonuc;
        }

        jsonIcerik = jsonIcerik.trim();

        if (!jsonIcerik.startsWith("[") || !jsonIcerik.endsWith("]")) {
            throw new RuntimeException("HATA: Kullanici JSON formati gecersiz. Dizi bekleniyor.");
        }

        jsonIcerik = jsonIcerik.substring(1, jsonIcerik.length() - 1).trim();

        if (jsonIcerik.isEmpty()) {
            return sonuc;
        }

        List<String> nesneler = jsonNesneleriniAyir(jsonIcerik);

        for (String nesneStr : nesneler) {
            String isim = jsonDegerOku(nesneStr, "isim");
            String tcNo = jsonDegerOku(nesneStr, "tcNo");
            String rol = jsonDegerOku(nesneStr, "rol");
            String sifre = jsonDegerOku(nesneStr, "sifre");
            int krediPuani = jsonSayiDegerOku(nesneStr, "krediPuani");

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

            sonuc.add(kullanici);
        }

        return sonuc;
    }

    public List<Materyal> materyallariJsondanYukle(String jsonIcerik) {
        List<Materyal> sonuc = new ArrayList<>();

        if (jsonIcerik == null || jsonIcerik.trim().isEmpty()) {
            return sonuc;
        }

        jsonIcerik = jsonIcerik.trim();

        if (!jsonIcerik.startsWith("[") || !jsonIcerik.endsWith("]")) {
            throw new RuntimeException("HATA: Materyal JSON formati gecersiz. Dizi bekleniyor.");
        }

        jsonIcerik = jsonIcerik.substring(1, jsonIcerik.length() - 1).trim();

        if (jsonIcerik.isEmpty()) {
            return sonuc;
        }

        List<String> nesneler = jsonNesneleriniAyir(jsonIcerik);

        for (String nesneStr : nesneler) {
            String tur = jsonDegerOku(nesneStr, "tur");
            String id = jsonDegerOku(nesneStr, "id");
            String baslik = jsonDegerOku(nesneStr, "baslik");
            double birimFiyat = jsonOndalikDegerOku(nesneStr, "birimFiyat");
            int stokAdedi = jsonSayiDegerOku(nesneStr, "stokAdedi");

            Materyal materyal;

            if ("Kitap".equals(tur)) {
                String isbn = jsonDegerOku(nesneStr, "isbn");
                materyal = new Kitap(baslik, stokAdedi, birimFiyat, isbn);
            } else if ("DijitalMedya".equals(tur)) {
                String dosyaFormati = jsonDegerOku(nesneStr, "dosyaFormati");
                materyal = new DijitalMedya(baslik, birimFiyat, dosyaFormati);
            } else {
                System.err.println("UYARI: Bilinmeyen materyal turu atlanıyor: " + tur);
                continue;
            }

            materyal.setId(id);
            sonuc.add(materyal);
        }

        return sonuc;
    }


    private List<String> jsonNesneleriniAyir(String jsonDizisi) {
        List<String> nesneler = new ArrayList<>();
        int derinlik = 0;
        int baslangic = -1;

        for (int i = 0; i < jsonDizisi.length(); i++) {
            char c = jsonDizisi.charAt(i);

            if (c == '{') {
                if (derinlik == 0) {
                    baslangic = i;
                }
                derinlik++;
            } else if (c == '}') {
                derinlik--;
                if (derinlik == 0 && baslangic != -1) {
                    nesneler.add(jsonDizisi.substring(baslangic, i + 1));
                    baslangic = -1;
                }
            }
        }

        return nesneler;
    }

    private String jsonDegerOku(String json, String anahtar) {
        String aranan = "\"" + anahtar + "\"";
        int idx = json.indexOf(aranan);
        if (idx == -1) return "";

        int ikiNokta = json.indexOf(":", idx + aranan.length());
        if (ikiNokta == -1) return "";

        String kalanKisim = json.substring(ikiNokta + 1).trim();

        if (kalanKisim.startsWith("null")) {
            return null;
        }

        if (kalanKisim.startsWith("\"")) {
            int tirnak1 = 0;
            int tirnak2 = -1;

            for (int i = 1; i < kalanKisim.length(); i++) {
                if (kalanKisim.charAt(i) == '"' && kalanKisim.charAt(i - 1) != '\\') {
                    tirnak2 = i;
                    break;
                }
            }

            if (tirnak2 == -1) return "";

            String deger = kalanKisim.substring(tirnak1 + 1, tirnak2);
            return jsonKacislariGeriAl(deger);
        }

        return "";
    }

    private int jsonSayiDegerOku(String json, String anahtar) {
        String aranan = "\"" + anahtar + "\"";
        int idx = json.indexOf(aranan);
        if (idx == -1) return 0;

        int ikiNokta = json.indexOf(":", idx + aranan.length());
        if (ikiNokta == -1) return 0;

        String kalanKisim = json.substring(ikiNokta + 1).trim();

        StringBuilder sayiStr = new StringBuilder();
        for (int i = 0; i < kalanKisim.length(); i++) {
            char c = kalanKisim.charAt(i);
            if (Character.isDigit(c) || c == '-') {
                sayiStr.append(c);
            } else if (sayiStr.length() > 0) {
                break;
            }
        }

        if (sayiStr.length() == 0) return 0;

        try {
            return Integer.parseInt(sayiStr.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double jsonOndalikDegerOku(String json, String anahtar) {
        String aranan = "\"" + anahtar + "\"";
        int idx = json.indexOf(aranan);
        if (idx == -1) return 0.0;

        int ikiNokta = json.indexOf(":", idx + aranan.length());
        if (ikiNokta == -1) return 0.0;

        String kalanKisim = json.substring(ikiNokta + 1).trim();

        StringBuilder sayiStr = new StringBuilder();
        for (int i = 0; i < kalanKisim.length(); i++) {
            char c = kalanKisim.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '-') {
                sayiStr.append(c);
            } else if (sayiStr.length() > 0) {
                break;
            }
        }

        if (sayiStr.length() == 0) return 0.0;

        try {
            return Double.parseDouble(sayiStr.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String jsonKacislariGeriAl(String girdi) {
        if (girdi == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < girdi.length(); i++) {
            char c = girdi.charAt(i);
            if (c == '\\' && i + 1 < girdi.length()) {
                char sonraki = girdi.charAt(i + 1);
                switch (sonraki) {
                    case '\\': sb.append('\\'); i++; break;
                    case '"': sb.append('"'); i++; break;
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    default: sb.append(c); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    public void kaydet(List<Kullanici> kullanicilar, List<Materyal> materyaller) {
        this.kullaniciListesi = new ArrayList<>(kullanicilar);
        this.materyalListesi = new ArrayList<>(materyaller);

        kullanicilariKaydet();
        materyallariKaydet();
    }

    public void kullanicilariKaydet() {
        yolGuvenligi(KULLANICI_DOSYASI);

        String jsonIcerik = kullaniciListesiniJsonaSerialize(kullaniciListesi);

        try (BufferedWriter yazici = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(KULLANICI_DOSYASI), StandardCharsets.UTF_8))) {
            yazici.write(jsonIcerik);
            System.out.println("BASARILI: Kullanici verileri kaydedildi. (" + kullaniciListesi.size() + " kayit)");
        } catch (FileNotFoundException e) {
            System.err.println("HATA: Kullanici dosyasi olusturulamadi: " + e.getMessage());
            throw new RuntimeException("Kullanici dosyasi yazma hatasi", e);
        } catch (IOException e) {
            System.err.println("HATA: Kullanici dosyasina yazarken hata olustu: " + e.getMessage());
            throw new RuntimeException("Kullanici dosyasi I/O hatasi", e);
        }
    }

    public void materyallariKaydet() {
        yolGuvenligi(MATERYAL_DOSYASI);

        String jsonIcerik = materyalListesiniJsonaSerialize(materyalListesi);

        try (BufferedWriter yazici = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(MATERYAL_DOSYASI), StandardCharsets.UTF_8))) {
            yazici.write(jsonIcerik);
            System.out.println("BASARILI: Materyal verileri kaydedildi. (" + materyalListesi.size() + " kayit)");
        } catch (FileNotFoundException e) {
            System.err.println("HATA: Materyal dosyasi olusturulamadi: " + e.getMessage());
            throw new RuntimeException("Materyal dosyasi yazma hatasi", e);
        } catch (IOException e) {
            System.err.println("HATA: Materyal dosyasina yazarken hata olustu: " + e.getMessage());
            throw new RuntimeException("Materyal dosyasi I/O hatasi", e);
        }
    }

    public List<Kullanici> kullanicilariYukle() {
        yolGuvenligi(KULLANICI_DOSYASI);

        File dosya = new File(KULLANICI_DOSYASI);

        if (!dosya.exists()) {
            System.out.println("BILGI: Kullanici dosyasi bulunamadi, bos liste dondurulecek.");
            kullaniciListesi = new ArrayList<>();
            return kullaniciListesi;
        }

        if (!dosya.canRead()) {
            System.err.println("HATA: Kullanici dosyasi okunamiyor. Yetki problemi olabilir.");
            throw new RuntimeException("Kullanici dosyasi okuma yetkisi yok: " + KULLANICI_DOSYASI);
        }

        String jsonIcerik = dosyadanOku(dosya);

        try {
            kullaniciListesi = kullanicilariJsondanYukle(jsonIcerik);
            System.out.println("BASARILI: Kullanici verileri yuklendi. (" + kullaniciListesi.size() + " kayit)");
        } catch (Exception e) {
            System.err.println("HATA: Kullanici JSON verisi bozuk. Yedekten kurtarma deneniyor...");
            boolean kurtarildi = yedektenKurtar(KULLANICI_DOSYASI);
            if (kurtarildi) {
                String yedekIcerik = dosyadanOku(dosya);
                kullaniciListesi = kullanicilariJsondanYukle(yedekIcerik);
                System.out.println("KURTARMA BASARILI: Yedekten " + kullaniciListesi.size() + " kullanici yuklendi.");
            } else {
                System.err.println("KRITIK HATA: Yedekten kurtarma basarisiz. Bos liste dondurulecek.");
                kullaniciListesi = new ArrayList<>();
            }
        }

        return kullaniciListesi;
    }

    public List<Materyal> materyallariYukle() {
        yolGuvenligi(MATERYAL_DOSYASI);

        File dosya = new File(MATERYAL_DOSYASI);

        if (!dosya.exists()) {
            System.out.println("BILGI: Materyal dosyasi bulunamadi, bos liste dondurulecek.");
            materyalListesi = new ArrayList<>();
            return materyalListesi;
        }

        if (!dosya.canRead()) {
            System.err.println("HATA: Materyal dosyasi okunamiyor. Yetki problemi olabilir.");
            throw new RuntimeException("Materyal dosyasi okuma yetkisi yok: " + MATERYAL_DOSYASI);
        }

        String jsonIcerik = dosyadanOku(dosya);

        try {
            materyalListesi = materyallariJsondanYukle(jsonIcerik);
            System.out.println("BASARILI: Materyal verileri yuklendi. (" + materyalListesi.size() + " kayit)");
        } catch (Exception e) {
            System.err.println("HATA: Materyal JSON verisi bozuk. Yedekten kurtarma deneniyor...");
            boolean kurtarildi = yedektenKurtar(MATERYAL_DOSYASI);
            if (kurtarildi) {
                String yedekIcerik = dosyadanOku(dosya);
                materyalListesi = materyallariJsondanYukle(yedekIcerik);
                System.out.println("KURTARMA BASARILI: Yedekten " + materyalListesi.size() + " materyal yuklendi.");
            } else {
                System.err.println("KRITIK HATA: Yedekten kurtarma basarisiz. Bos liste dondurulecek.");
                materyalListesi = new ArrayList<>();
            }
        }

        return materyalListesi;
    }

    private String dosyadanOku(File dosya) {
        StringBuilder icerik = new StringBuilder();

        try (BufferedReader okuyucu = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(dosya), StandardCharsets.UTF_8))) {
            String satir;
            while ((satir = okuyucu.readLine()) != null) {
                icerik.append(satir).append("\n");
            }
        } catch (IOException e) {
            System.err.println("HATA: Dosya okunurken hata olustu: " + dosya.getName() + " - " + e.getMessage());
            throw new RuntimeException("Dosya okuma hatasi: " + dosya.getAbsolutePath(), e);
        }

        return icerik.toString();
    }


    public void yedekle() {
        DateTimeFormatter bicim = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String zamanDamgasi = LocalDateTime.now().format(bicim);

        File kullaniciDosyasi = new File(KULLANICI_DOSYASI);
        if (kullaniciDosyasi.exists()) {
            String yedekAdi = YEDEK_KLASORU + File.separator + "users_" + zamanDamgasi + ".json";
            dosyaKopyala(kullaniciDosyasi, new File(yedekAdi));
            System.out.println("YEDEK: Kullanici verileri yedeklendi -> " + yedekAdi);
        }

        File materyalDosyasi = new File(MATERYAL_DOSYASI);
        if (materyalDosyasi.exists()) {
            String yedekAdi = YEDEK_KLASORU + File.separator + "materials_" + zamanDamgasi + ".json";
            dosyaKopyala(materyalDosyasi, new File(yedekAdi));
            System.out.println("YEDEK: Materyal verileri yedeklendi -> " + yedekAdi);
        }

        eskiYedekleriTemizle(10);
    }

    private boolean yedektenKurtar(String hedefDosyaYolu) {
        String onek;
        if (hedefDosyaYolu.contains("users")) {
            onek = "users_";
        } else if (hedefDosyaYolu.contains("materials")) {
            onek = "materials_";
        } else {
            return false;
        }

        File yedekKlasoru = new File(YEDEK_KLASORU);
        if (!yedekKlasoru.exists() || !yedekKlasoru.isDirectory()) {
            return false;
        }

        File[] yedekDosyalar = yedekKlasoru.listFiles((dizin, ad) ->
                ad.startsWith(onek) && ad.endsWith(".json"));

        if (yedekDosyalar == null || yedekDosyalar.length == 0) {
            System.err.println("UYARI: Hicbir yedek dosyasi bulunamadi: " + onek);
            return false;
        }

        File enSonYedek = yedekDosyalar[0];
        for (File yedek : yedekDosyalar) {
            if (yedek.lastModified() > enSonYedek.lastModified()) {
                enSonYedek = yedek;
            }
        }

        try {
            dosyaKopyala(enSonYedek, new File(hedefDosyaYolu));
            System.out.println("KURTARMA: " + enSonYedek.getName() + " -> " + hedefDosyaYolu);
            return true;
        } catch (Exception e) {
            System.err.println("HATA: Yedekten kurtarma sirasinda hata: " + e.getMessage());
            return false;
        }
    }

    private void eskiYedekleriTemizle(int maxYedekSayisi) {
        File yedekKlasoru = new File(YEDEK_KLASORU);
        if (!yedekKlasoru.exists()) return;

        String[] onekler = {"users_", "materials_"};

        for (String onek : onekler) {
            File[] dosyalar = yedekKlasoru.listFiles((dizin, ad) ->
                    ad.startsWith(onek) && ad.endsWith(".json"));

            if (dosyalar == null || dosyalar.length <= maxYedekSayisi) continue;

            java.util.Arrays.sort(dosyalar, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

            for (int i = maxYedekSayisi; i < dosyalar.length; i++) {
                boolean silindi = dosyalar[i].delete();
                if (silindi) {
                    System.out.println("TEMIZLIK: Eski yedek silindi -> " + dosyalar[i].getName());
                }
            }
        }
    }

    private void dosyaKopyala(File kaynak, File hedef) {
        yolGuvenligi(hedef.getPath());

        try (InputStream giren = new FileInputStream(kaynak);
             OutputStream cikan = new FileOutputStream(hedef)) {

            byte[] tampon = new byte[4096];
            int okunan;
            while ((okunan = giren.read(tampon)) != -1) {
                cikan.write(tampon, 0, okunan);
            }
        } catch (IOException e) {
            System.err.println("HATA: Dosya kopyalama basarisiz: " + kaynak.getName() + " -> " + hedef.getName());
            throw new RuntimeException("Dosya kopyalama hatasi", e);
        }
    }


    public void senkronizeEt(List<Kullanici> kullanicilar, List<Materyal> materyaller) {
        yedekle();
        kaydet(kullanicilar, materyaller);
        System.out.println("SENKRONIZASYON TAMAMLANDI: Veriler yedeklendi ve kaydedildi.");
    }


    public void kullaniciEkle(Kullanici yeniKullanici) {
        for (Kullanici mevcut : kullaniciListesi) {
            if (mevcut.getIsim().equals(yeniKullanici.getIsim())) {
                System.err.println("UYARI: Bu isimde bir kullanici zaten mevcut: " + yeniKullanici.getIsim());
                return;
            }
        }
        kullaniciListesi.add(yeniKullanici);
        kullanicilariKaydet();
    }

    public void kullaniciSil(String kullaniciIsmi) {
        boolean silindi = kullaniciListesi.removeIf(k -> k.getIsim().equals(kullaniciIsmi));
        if (silindi) {
            kullanicilariKaydet();
            System.out.println("SILME BASARILI: " + kullaniciIsmi + " sistemden kaldirildi.");
        } else {
            System.err.println("UYARI: Silinecek kullanici bulunamadi: " + kullaniciIsmi);
        }
    }

    public void materyalEkle(Materyal yeniMateryal) {
        materyalListesi.add(yeniMateryal);
        materyallariKaydet();
    }

    public void materyalSil(String materyalId) {
        boolean silindi = materyalListesi.removeIf(m -> m.getId().equals(materyalId));
        if (silindi) {
            materyallariKaydet();
            System.out.println("SILME BASARILI: Materyal (ID: " + materyalId + ") sistemden kaldirildi.");
        } else {
            System.err.println("UYARI: Silinecek materyal bulunamadi: " + materyalId);
        }
    }

    public Kullanici kullaniciBul(String isim) {
        for (Kullanici k : kullaniciListesi) {
            if (k.getIsim().equals(isim)) {
                return k;
            }
        }
        return null;
    }

    public Materyal materyalBul(String id) {
        for (Materyal m : materyalListesi) {
            if (m.getId().equals(id)) {
                return m;
            }
        }
        return null;
    }

    public List<Materyal> materyalAra(String aramaKelimesi) {
        List<Materyal> sonuclar = new ArrayList<>();
        String kucukHarfArama = aramaKelimesi.toLowerCase();

        for (Materyal m : materyalListesi) {
            if (m.getBaslik().toLowerCase().contains(kucukHarfArama)) {
                sonuclar.add(m);
            }
        }

        return sonuclar;
    }


    public boolean veritabaniMevcutMu() {
        return new File(KULLANICI_DOSYASI).exists() && new File(MATERYAL_DOSYASI).exists();
    }

    public long kullaniciDosyasiBoyutu() {
        File dosya = new File(KULLANICI_DOSYASI);
        return dosya.exists() ? dosya.length() : 0;
    }

    public long materyalDosyasiBoyutu() {
        File dosya = new File(MATERYAL_DOSYASI);
        return dosya.exists() ? dosya.length() : 0;
    }

    public int yedekSayisi() {
        File yedekKlasoru = new File(YEDEK_KLASORU);
        if (!yedekKlasoru.exists()) return 0;

        File[] dosyalar = yedekKlasoru.listFiles((dizin, ad) -> ad.endsWith(".json"));
        return dosyalar != null ? dosyalar.length : 0;
    }

    public String durumRaporu() {
        StringBuilder rapor = new StringBuilder();
        rapor.append("========== VERITABANI DURUM RAPORU ==========\n");
        rapor.append("Kullanici dosyasi: ").append(new File(KULLANICI_DOSYASI).exists() ? "MEVCUT" : "YOK").append("\n");
        rapor.append("Materyal dosyasi: ").append(new File(MATERYAL_DOSYASI).exists() ? "MEVCUT" : "YOK").append("\n");
        rapor.append("Kullanici dosya boyutu: ").append(kullaniciDosyasiBoyutu()).append(" byte\n");
        rapor.append("Materyal dosya boyutu: ").append(materyalDosyasiBoyutu()).append(" byte\n");
        rapor.append("Bellekteki kullanici sayisi: ").append(kullaniciListesi.size()).append("\n");
        rapor.append("Bellekteki materyal sayisi: ").append(materyalListesi.size()).append("\n");
        rapor.append("Toplam yedek sayisi: ").append(yedekSayisi()).append("\n");
        rapor.append("=============================================\n");
        return rapor.toString();
    }


    public List<Kullanici> getKullaniciListesi() {
        return kullaniciListesi;
    }

    public List<Materyal> getMateryalListesi() {
        return materyalListesi;
    }

    public String getVeriKlasoru() {
        return VERI_KLASORU;
    }

    public String getYedekKlasoru() {
        return YEDEK_KLASORU;
    }
}
