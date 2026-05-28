# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi - 2026 Nesneye Yönelik Programlama Proje ödevi

![Java](https://img.shields.io/badge/Java-ED8B00?logo=java&logoColor=white)
![Frontend](https://img.shields.io/badge/Frontend-HTML%20%7C%20CSS%20%7C%20JS-E34F26?logo=html5&logoColor=white)
![Database](https://img.shields.io/badge/Database-JSON-orange)
![Security](https://img.shields.io/badge/Security-SHA--256-red?logo=springsecurity&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux-lightgrey?logo=linux)
![License](https://img.shields.io/badge/License-MIT-brightgreen)

## 1. Proje Özeti
Bu proje, geleneksel kütüphane otomasyonlarını modern web teknolojileri ve **derinlemesine savunma (Defense-in-Depth)** prensipleriyle birleştiren, Java tabanlı bir dijital varlık yönetim sistemidir. Sistem; fiziksel kitaplar, dijital medyalar ve süreli yayınlar gibi farklı materyalleri Nesneye Yönelik Programlama (OOP) standartlarıyla tek bir merkezden yönetir. Klasik ödev projelerinden farklı olarak, Java bir web sunucusu (Backend) gibi konumlandırılmış ve istemci (Frontend) ile haberleşmesi güvenli bir REST mimarisi üzerine inşa edilmiştir.

## 2. Çekirdek Mimari ve OOP Uygulamaları
Projenin temel iskeleti, yazılım mühendisliği standartlarına uygun olarak tasarlanmıştır:
*   **Kalıtım (Inheritance) ve Soyutlama (Abstraction):** `IMateryal` arayüzü ve `Materyal` soyut sınıfı üzerinden `Kitap` ve `DijitalMedya` gibi alt sınıflar türetilerek, genişletilebilir (Scalable) bir yapı kurulmuştur.
*   **Çok Biçimlilik (Polymorphism):** Her materyal türünün ceza hesaplama veya ödünç verilme mantığı çalışma zamanında (Runtime) dinamik olarak belirlenir.
*   **Kapsülleme (Encapsulation):** Kritik iş mantığı, ceza puanları ve sistemin iç durumu dış müdahalelere kapatılarak nesne bütünlüğü korunmuştur.

## 3. Siber Güvenlik Katmanı (Cybersecurity Framework)
Proje, kullanıcı verilerini ve sunucu bütünlüğünü korumak amacıyla gelişmiş güvenlik mekanizmaları içerir:
*   **Kriptografik Şifreleme (Hashing & Salting):** Kullanıcı parolaları veritabanında (JSON) kesinlikle açık metin (plaintext) olarak saklanmaz. Parolalar, güvenlik standartlarına uygun olarak hash algoritmaları (Örn: SHA-256) kullanılarak şifrelenir.
*   **Kimlik Doğrulama ve Yetkilendirme (Auth & Authorization):** Frontend ile Backend arasındaki API iletişiminde yetkisiz erişimleri engellemek için oturum (Session) veya Token tabanlı güvenlik mekanizmaları devrededir. Sistemde **Least Privilege (En Az Ayrıcalık)** prensibi uygulanır; sıradan bir "Üye" sadece okuma (Read) yapabilirken, CRUD operasyonlarını yalnızca "Admin" yetkisine sahip kullanıcılar gerçekleştirebilir.
*   **Girdi Denetimi ve Sanitizasyon (Input Validation):** İstemciden (Web arayüzünden) gelen her türlü veri, Backend tarafında işlenmeden önce süzgeçten geçirilir. Bu sayede **JSON Injection** ve **XSS (Cross-Site Scripting)** gibi saldırı vektörleri engellenir.
*   **Dosya Yolu Güvenliği (Path Traversal Protection):** Sistem, veri kalıcılığı için yerel JSON dosyalarını kullandığından, dışarıdan manipüle edilmiş dosya yolu isteklerine karşı (Örn: `../../etc/passwd` veya `../backup/`) sıkı bir dizin denetimi uygular. Dosya okuma/yazma işlemleri izole edilmiş bir `data/` klasörü dışına çıkamaz.

## 4. Veri Kalıcılığı ve Hata Yönetimi (Database & Persistence)
*   Sistem, SQL kullanmak yerine kendi özel **Dosya Tabanlı Veritabanı Motorunu** (File-Based Database Engine) Java ile sıfırdan yönetir.
*   Veriler `JSON` formatında serialize/deserialize edilerek saklanır.
*   Gelişmiş **Exception Handling (Hata Yakalama)** blokları sayesinde; dosya bulunamaması, okuma/yazma yetkisi olmaması veya JSON formatının dışarıdan bozulması (Corrupted Data) gibi durumlarda sistemin çökmesi engellenir ve otomatik kurtarma/uyarı senaryoları devreye girer.

## 5. Modern Web Arayüzü (Frontend Integration)
*   Java backend'i, gömülü bir HTTP sunucusu modülü (`httplib`) barındırarak ağ üzerinden gelen istekleri dinler.
*   Kullanıcılar sisteme HTML, CSS ve JavaScript ile geliştirilmiş modern, asenkron (`fetch API`) ve kullanıcı dostu bir web paneli (Dashboard) üzerinden erişim sağlar. Tıklama, arama ve form gönderme işlemleri arka plana güvenli HTTP istekleri olarak iletilir.

## EKİP GÖREV DAĞILIMI

Backend & Core Architect - Ahmet Güler:

Projenin nesneye yönelik tasarım hiyerarşisini ve iş mantığını (Business Logic) kurgular. Sistemdeki tüm nesnelerin atası olan Abstract (Soyut) sınıfları ve ortak davranışları belirleyen Interface (Arayüz) yapılarını tasarlar. Kalıtım (Inheritance) mekanizması ile materyal çeşitliliğini yönetirken; kredi puanı hesaplama, dinamik ceza sistemi ve stok kontrolü gibi çekirdek algoritmaları kodlar. Ayrıca, sınıflar arası ilişkilerin (Composition/Aggregation) sağlam bir mimaride yürümesini sağlayarak projenin genişletilebilir olmasını garanti altına alır.

Yaptığı dosyalar:

```
src/main/java/com/akillikutup/core/IMateryal.java
src/main/java/com/akillikutup/core/Materyal.java
src/main/java/com/akillikutup/core/Kitap.java
src/main/java/com/akillikutup/core/DijitalMedya.java
src/test/java/com/akillikutup/core/CoreTest.java
src/main/java/com/akillikutup/Main.java
```
Database & Data Persistence Manager / Penetration Tester - Arda Meçik:

Sistemin veri kalıcılığı katmanını tasarlar ve yönetir. Verileri SQL yerine Java kullanarak dosya tabanlı (JSON/TXT/Binary) bir yapıda saklayacak olan "Database Engine" mekanizmasını kurar. Nesnelerin diske yazılması (Save) ve açılışta tekrar belleğe yüklenmesi (Load) süreçlerini yürütür. Ayrıca, dosya okuma/yazma sırasında oluşabilecek tüm senaryolar için Hata Yönetimi (Exception Handling) mimarisini ve veritabanı güvenliğini (Backup/Sync) sağlar. Projenin canlıya alınma durumunda host penetrasyon işlemini yapar.

Yaptığı dosyalar:

```
src/main/java/com/akillikutup/db/DatabaseManager.java
src/test/java/com/akillikutup/db/DatabaseManagerTest.java
data/users.json
data/materials.json
data/backup/
```
UI/UX Developer - Göktuğ Berke Kuzucu:

Sistemin kullanıcı ile temas eden tüm görsel arayüzlerini ve etkileşim senaryolarını tasarlar. Web teknolojilerini (HTML, CSS, JavaScript) kullanarak, karmaşık kütüphane işlemlerini (materyal arama, ödünç alma, kullanıcı kaydı) son kullanıcı için basit ve sezgisel bir deneyime dönüştürür. Görsel hiyerarşi, renk paleti ve tipografi seçimleriyle kullanıcı deneyimini (UX) iyileştirirken; Backend'den gelen verileri dinamik grafikler, tablolar ve uyarı pencereleriyle görselleştirir. Ayrıca, arayüzün sistem mantığıyla entegrasyonunu sağlayarak akıcı bir navigasyon yapısı oluşturur.

Yaptığı dosyalar:

```
frontend/index.html
frontend/dashboard.html
frontend/css/login.css
frontend/css/main.css
frontend/js/dashboard.js
```
Security & Integration Specialist - Eren Gider:

Sistemin güvenlik altyapısını ve proje entegrasyon süreçlerini yönetir. Kullanıcı kayıt ve giriş işlemlerinde güvenli yetkilendirme (Authentication & Authorization) mekanizmalarını kurar. Hassas verilerin korunması için Hashing (SHA-256 vb.) algoritmalarını kullanarak şifreleme katmanını oluşturur. Ayrıca, projenin profesyonel bir portfolyo öğesi haline gelmesi için GitHub depo yönetimi, kapsamlı README dokümantasyonu ve teknik raporlama süreçlerini yürütür. Kodun farklı branch’ler üzerinden tutarlı bir şekilde birleştirilmesini (Merge/Integration) sağlayarak ekip içi teknik koordinasyonu denetler.

Yaptığı dosyalar:

```
src/main/java/com/akillikutup/auth/AuthManager.java
src/main/java/com/akillikutup/server/ApiServer.java
src/test/java/com/akillikutup/auth/AuthManagerTest.java
frontend/js/api.js
frontend/js/auth.js
pom.xml
README.md
docs/UML_Sema.md
docs/Proje_Raporu.md
```
## Genel Proje Dosya Şeması
```
NYP-Proje-2026-Akilli-Kutup/
├── .gitignore
├── README.md
├── pom.xml
├── data/
│   ├── users.json
│   ├── materials.json
│   └── backup/
├── docs/
│   ├── UML_Sema.md
│   └── Proje_Raporu.md
├── frontend/
│   ├── index.html
│   ├── dashboard.html
│   ├── css/
│   │   ├── login.css
│   │   └── main.css
│   └── js/
│       ├── api.js
│       ├── auth.js
│       └── dashboard.js
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── akillikutup/
    │               ├── auth/
    │               │   └── AuthManager.java
    │               ├── core/
    │               │   ├── IMateryal.java
    │               │   ├── Materyal.java
    │               │   ├── Kitap.java
    │               │   ├── DijitalMedya.java
    │               │   ├── Kullanici.java
    │               │   ├── Admin.java
    │               │   └── Uye.java
    │               ├── db/
    │               │   └── DatabaseManager.java
    │               ├── gui/
    │               │   ├── MainFrame.java
    │               │   ├── LoginPanel.java
    │               │   ├── AdminPanel.java
    │               │   ├── UserPanel.java
    │               │   └── LibraryManager.java
    │               ├── server/
    │               │   └── ApiServer.java
    │               └── Main.java
    └── test/
        └── java/
            └── com/
                └── akillikutup/
                    ├── auth/
                    │   └── AuthManagerTest.java
                    ├── core/
                    │   └── CoreTest.java
                    └── db/
                        └── DatabaseManagerTest.java
```

### Gelistirme Durumu (Development Status)

Yukaridaki semada belirtilen dosyalardan **tamamlananlar** ve henuz **gelistirme asamasinda olanlar** asagida listelenmistir:

**Tamamlanan Kisimlar:**
*   `src/main/java/com/akillikutup/core/` (Cekirdek OOP Modelleri)
*   `src/main/java/com/akillikutup/db/` (Veritabani Motoru ve JSON Islemleri)
*   `src/main/java/com/akillikutup/gui/` (Swing Masaustu Arayuzu)
*   `src/main/java/com/akillikutup/auth/` (SHA-256 Sifreleme ve Kimlik Dogrulama)
*   `src/main/java/com/akillikutup/server/` (REST API Sunucu Altyapisi)
*   `src/test/` (Birim Testler - Tum testler basariyla gecmektedir)
*   `frontend/` (Modern Web Arayuzu - HTML/CSS/JS)
*   `docs/UML_Sema.md` (UML sinif diyagrami)
*   `docs/Proje_Raporu.md` (Proje raporu)

