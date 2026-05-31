# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi V2 - Teknik Proje Raporu

## 1. GİRİŞ & PROJENİN TANITIMI
Bu projenin amacı, geleneksel kütüphane otomasyonlarını modern web teknolojileri, Yapay Zeka (AI) ve derinlemesine savunma (Defense-in-Depth) prensipleriyle birleştirerek güvenli ve yenilikçi bir dijital varlık yönetim sistemi oluşturmaktır. Proje; fiziksel kitaplar, dijital medyalar ve süreli yayınlar gibi farklı materyalleri Nesneye Yönelik Programlama (OOP) standartlarıyla tek bir merkezden yönetmeyi kapsar. 

Mevcut kütüphane otomasyonlarından (örneğin Koha vb.) farklı olarak, sistemimize Google Gemini 1.5 altyapısı entegre edilmiştir. Bu sayede kullanıcılara kişiselleştirilmiş okuma önerileri ve akıllı asistan hizmetleri sunulmaktadır. Ayrıca, projenin arka planı tamamen özel bir Java REST API sunucusu olarak çalışırken, veritabanı dışarıdan bağımsız JSON tabanlı bir motorla yürütülmektedir. Bu yönüyle hem öğrenci projeleri arasında mimari açıdan öne çıkmakta hem de hafif ve taşınabilir bir çözüm sunmaktadır.

## 2. GEREKSİNİM ANALİZİ
**Hedef Platform ve İşletim Sistemi:** Sistem, Java 17+ yüklü olan Windows, macOS veya Linux tabanlı herhangi bir sunucu veya masaüstü bilgisayarda çalışabilmektedir. İstemci tarafı (Frontend) ise modern bir web tarayıcısı (Chrome, Firefox, Safari) olan tüm cihazlarda (mobil, tablet, masaüstü) tam uyumlu olarak çalışır.

**Hedef Kitle ve Kullanıcı Rolleri:** Projenin hedef kitlesi yerel kütüphaneler, okul kütüphaneleri ve kişisel arşiv sahipleridir. Sistemde iki ana rol bulunmaktadır:
*   **Admin:** Tüm materyalleri ve kullanıcıları (CRUD) yönetebilen, sistemin yapılandırmasına hakim yönetici sınıfıdır. Sınırsız materyal ödünç alabilir.
*   **Üye:** Sadece materyalleri listeleyebilen, okuma asistanından faydalanabilen ve belirli limitler dahilinde materyal ödünç alabilen standart kullanıcılardır.

**Araç ve Teknolojiler:**
*   **Backend (Java 17):** OOP prensiplerine tam uyum sağlamak ve yerleşik `HttpServer` modülü ile hafif bir REST API kurmak için seçilmiştir. Spring Boot gibi ağır frameworkler yerine mimariyi sıfırdan kurmak tercih edilmiştir.
*   **Frontend (HTML, CSS, Vanilla JS):** Saf (Vanilla) JavaScript kullanılarak asenkron (Fetch API) yapılar oluşturulmuş, SPA (Tek Sayfa Uygulaması) hissiyatı yaratılmıştır.
*   **Veritabanı (JSON Motoru):** SQL veritabanı kurma zorunluluğunu ortadan kaldırmak, sistemi "tak-çalıştır" (plug-and-play) hale getirmek ve Java dosya işleme (I/O) yeteneklerini göstermek amacıyla tercih edilmiştir.
*   **Güvenlik:** Parola hashleme için SHA-256 ve API anahtarı yapılandırması için AES-256 şifreleme algoritmaları entegre edilmiştir.
*   **Yapay Zeka:** Kitap öneri algoritmaları ve metin işleme için Google Gemini API entegre edilmiştir.

## 3. TASARIM
Projemiz, "Client-Server" (İstemci-Sunucu) mimarisine dayanmaktadır. 

**Mimari ve OOP Uygulamaları:**
*   `IMateryal` arayüzü ve `Materyal` soyut sınıfından `Kitap` ve `DijitalMedya` sınıfları türetilerek kalıtım (Inheritance) sağlanmıştır.
*   Ödünç alma süreleri ve dinamik ceza puanları (Polymorphism) üzerinden materyal türüne göre farklılık göstermektedir.
*   `ApiServer` modülü arka planda 8080 portunu dinleyerek gelen HTTP isteklerini yönlendirir (Routing). 
*   Kullanıcı giriş işlemlerinde Session bazlı bir yetkilendirme altyapısı mevcuttur. İstekler frontend tarafındaki `auth.js` ve `api.js` modülleri ile JSON tabanlı olarak sunucuya aktarılır.

**Arayüz Tasarımı:**
Arayüz, "Koyu Arduvaz" (Dark Slate) renk paletine sahip, göz yormayan, kurumsal ve modern bir yapıya sahiptir. Menüler, tablolar ve form elemanları tamamen duyarlı (Responsive) tasarıma uygun kodlanmıştır.

## 4. GELİŞTİRME
**Güvenlik ve Şifreleme Kod Örneği:**
Kullanıcı parolaları düz metin yerine SHA-256 ile hashlenerek saklanmaktadır. Bunun yanında, `ConfigManager.java` ile API anahtarlarımız AES-256 algoritmasıyla şifrelenmiştir.

```java
// AuthManager.java içerisinden parola hashleme metodu
private String hashPassword(String password) {
    try {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Hash algoritması bulunamadı.", e);
    }
}
```

**Kurulum ve Çalıştırma Yönergeleri:**
1.  Bilgisayarınızda Java 17 veya üzeri bir sürümün ve Apache Maven'ın kurulu olduğundan emin olun.
2.  Proje ana dizininde komut satırını açarak `mvn clean install` komutu ile bağımlılıkları yükleyin ve projeyi derleyin.
3.  Sunucuyu başlatmak için `mvn exec:java "-Dexec.mainClass=com.akillikutup.Main"` komutunu çalıştırın.
4.  Konsolda `API Sunucusu baslatildi: http://localhost:8080` mesajını gördükten sonra tarayıcınızdan `http://localhost:8080` adresine girerek sisteme ulaşabilirsiniz.

## 5. TEST ve DOĞRULAMA
Projemizde çekirdek algoritmaların ve veritabanı işlemlerinin kararlılığını test etmek için JUnit kullanılarak birim (Unit) testleri yazılmıştır.

*   **`CoreTest.java`:** Nesneye yönelik mimarinin doğru çalıştığını, materyallerin kredi puanlarını doğru hesapladığını, stok limitlerinin aşılamadığını ve yetki ihlallerini doğrulamak için yazılmıştır. (Örn: Admin olmayan birinin başka birinin TC numarasını görememesi).
*   **`DatabaseManagerTest.java`:** Sistemin JSON dosyalarını başarıyla okuyup yazdığını, aynı TC Kimlik numarasıyla ikinci bir kayıt açılamayacağını test eder.
*   **`AuthManagerTest.java`:** Kullanıcı parolalarının SHA-256 ile doğru hashlenip hashlenmediğini ve hatalı parolaların sisteme girişi engelleyip engellemediğini test eder.

Testlerimiz yerel bilgisayarda çalıştırılmış ve %100 başarı oranına (All Tests Passed) ulaşmıştır.

## 6. SONUÇ
Geliştirilen bu sistem sayesinde, klasik kütüphane otomasyon projelerinin çok ötesine geçilerek baştan sona tam güvenlikli, yapay zeka destekli ve modern bir web servisi altyapısı elde edilmiştir. Java'nın sadece masaüstü uygulamaları için değil, güçlü REST API arka planları (Backend) kurmak için de ne kadar elverişli olduğu gösterilmiştir.

**Gelecekte Yapılabilecek Geliştirmeler:**
İlerleyen dönemde sisteme gerçek zamanlı barkod okuma cihazları (Donanım Entegrasyonu) bağlanabilir. Ayrıca, dosya tabanlı veritabanı PostgreSQL gibi daha büyük ölçekli ve ilişkisel (RDBMS) bir sunucuya taşınarak sistemin binlerce kullanıcılı üniversite kampüslerinde kullanılması sağlanabilir.

## KAYNAKLAR
[1] E. Gamma, R. Helm, R. Johnson, J. Vlissides, "Design Patterns: Elements of Reusable Object-Oriented Software", Addison-Wesley, 1994.
[2] "Java SE 17 Documentation", Oracle. [Çevrimiçi]. Erişim adresi: https://docs.oracle.com/en/java/javase/17/. [Erişim tarihi: 31-Mayıs-2026].
[3] "Gemini API Documentation", Google AI for Developers. [Çevrimiçi]. Erişim adresi: https://ai.google.dev/docs. [Erişim tarihi: 31-Mayıs-2026].
