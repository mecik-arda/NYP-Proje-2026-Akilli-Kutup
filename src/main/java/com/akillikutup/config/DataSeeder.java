package com.akillikutup.config;

import com.akillikutup.material.Kitap;
import com.akillikutup.material.KitapRepository;
import com.akillikutup.user.User;
import com.akillikutup.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final KitapRepository kitapRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, KitapRepository kitapRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.kitapRepository = kitapRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("[DataSeeder] Veritabani bos. Ornek test verileri yukleniyor...");

            String encodedPassword = passwordEncoder.encode("12345678");

            User admin1 = new User("Ahmet Guler", "11111111111", User.Role.ADMIN, encodedPassword);
            admin1.setEmail("ahmet.guler@kutuphane.local");
            userRepository.save(admin1);

            User admin2 = new User("Eren Gider", "33333333333", User.Role.ADMIN, encodedPassword);
            admin2.setEmail("eren.gider@kutuphane.local");
            userRepository.save(admin2);

            User arda = new User("Arda Mecik", "22222222222", User.Role.ADMIN, encodedPassword);
            arda.setEmail("arda.mecik@kutuphane.local");
            userRepository.save(arda);

            User uye1 = new User("Goktug Berke Kuzucu", "44444444444", User.Role.UYE, encodedPassword);
            uye1.setEmail("goktug@kutuphane.local");
            userRepository.save(uye1);

            User splitTestUye = new User("SplitBrain Test Kullanici", "99999999999", User.Role.UYE, passwordEncoder.encode("test123"));
            splitTestUye.setEmail("sbtest@test.com");
            userRepository.save(splitTestUye);

            // Fetch books from Trakya University DSpace XML File (Local Mirror)
            System.out.println("[DataSeeder] Trakya Universitesi Acik Erisim API'sinden gercek akademik yayinlar cekiliyor (Yerel XML Modu)...");
            try {
                org.springframework.core.io.ClassPathResource resource = new org.springframework.core.io.ClassPathResource("trakya_tezler.xml");
                java.io.InputStream is = resource.getInputStream();
                
                javax.xml.parsers.DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                org.w3c.dom.Document doc = dBuilder.parse(is);
                doc.getDocumentElement().normalize();

                org.w3c.dom.NodeList nList = doc.getElementsByTagName("record");
                int eklendi = 0;
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    if (eklendi >= 15) break;
                    org.w3c.dom.Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
                        
                        org.w3c.dom.NodeList titleList = eElement.getElementsByTagName("dc:title");
                        if(titleList.getLength() == 0) continue;
                        String baslik = titleList.item(0).getTextContent().trim();
                        // Prevent too long titles
                        if (baslik.length() > 200) baslik = baslik.substring(0, 197) + "...";

                        org.w3c.dom.NodeList creatorList = eElement.getElementsByTagName("dc:creator");
                        String yazar = creatorList.getLength() > 0 ? creatorList.item(0).getTextContent().trim() : "Trakya Universitesi";
                        if (yazar.length() > 100) yazar = yazar.substring(0, 97) + "...";

                        String isbn = "TRU-" + (100000000L + (long)(Math.random() * 899999999L));
                        int stok = 2 + (int)(Math.random() * 10);
                        double fiyat = 30.0 + (int)(Math.random() * 70);

                        Kitap k = new Kitap(baslik, stok, fiyat, isbn);
                        k.setYazar(yazar);
                        k.setKategori("Akademik Yayin");
                        kitapRepository.save(k);
                        System.out.println("  + Trakya Yayini Eklendi: " + baslik);
                        eklendi++;
                    }
                }
                if (eklendi == 0) {
                    throw new RuntimeException("XML icinde kayit bulunamadi");
                }
            } catch (Exception e) {
                System.err.println("[DataSeeder] API'den veri cekilirken hata olustu (" + e.getMessage() + "). Varsayilan kitaplar yukleniyor...");
                
                Kitap k1 = new Kitap("1984", 5, 25.0, "978-0451524935");
                k1.setYazar("George Orwell");
                k1.setKategori("Roman");
                kitapRepository.save(k1);
            }

            System.out.println("[DataSeeder] Ornek veriler basariyla eklendi!");
        } else {
            System.out.println("[DataSeeder] Veritabani zaten dolu. Atla.");
        }
    }
}

