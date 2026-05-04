# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi - 2026 Nesneye Yönelik Programlama Proje ödevi

## 1. Proje Özeti
Bu proje, geleneksel kütüphane otomasyonlarını modern web teknolojileri ve **derinlemesine savunma (Defense-in-Depth)** prensipleriyle birleştiren, C++ tabanlı bir dijital varlık yönetim sistemidir. Sistem; fiziksel kitaplar, dijital medyalar ve süreli yayınlar gibi farklı materyalleri Nesneye Yönelik Programlama (OOP) standartlarıyla tek bir merkezden yönetir. Klasik ödev projelerinden farklı olarak, C++ bir web sunucusu (Backend) gibi konumlandırılmış ve istemci (Frontend) ile haberleşmesi güvenli bir REST mimarisi üzerine inşa edilmiştir.

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
*   Sistem, SQL kullanmak yerine kendi özel **Dosya Tabanlı Veritabanı Motorunu** (File-Based Database Engine) C++ ile sıfırdan yönetir.
*   Veriler `JSON` formatında serialize/deserialize edilerek saklanır.
*   Gelişmiş **Exception Handling (Hata Yakalama)** blokları sayesinde; dosya bulunamaması, okuma/yazma yetkisi olmaması veya JSON formatının dışarıdan bozulması (Corrupted Data) gibi durumlarda sistemin çökmesi engellenir ve otomatik kurtarma/uyarı senaryoları devreye girer.

## 5. Modern Web Arayüzü (Frontend Integration)
*   C++ backend'i, gömülü bir HTTP sunucusu modülü (`httplib`) barındırarak ağ üzerinden gelen istekleri dinler.
*   Kullanıcılar sisteme HTML, CSS ve JavaScript ile geliştirilmiş modern, asenkron (`fetch API`) ve kullanıcı dostu bir web paneli (Dashboard) üzerinden erişim sağlar. Tıklama, arama ve form gönderme işlemleri arka plana güvenli HTTP istekleri olarak iletilir.

## EKİP GÖREV DAĞILIMI

## Backend & Core Architect - Ahmet Güler:
Projenin nesneye yönelik tasarım hiyerarşisini ve iş mantığını (Business Logic) kurgular. Sistemdeki tüm nesnelerin atası olan Abstract (Soyut) sınıfları ve ortak davranışları belirleyen Interface (Arayüz) yapılarını tasarlar. Kalıtım (Inheritance) mekanizması ile materyal çeşitliliğini yönetirken; kredi puanı hesaplama, dinamik ceza sistemi ve stok kontrolü gibi çekirdek algoritmaları kodlar. Ayrıca, sınıflar arası ilişkilerin (Composition/Aggregation) sağlam bir mimaride yürümesini sağlayarak projenin genişletilebilir olmasını garanti altına alır.
## Yaptığı dosyalar
```
include/core/IMateryal.h
include/core/Materyal.h
include/core/Kitap.h
include/core/DijitalMedya.h
src/core/Materyal.cpp
src/core/Kitap.cpp
src/core/DijitalMedya.cpp
tests/test_core.cpp
src/main.cpp
```

## Database & Data Persistence Manager / Penetration Tester - Arda Meçik:
Sistemin veri kalıcılığı katmanını tasarlar ve yönetir. Verileri SQL yerine C++ veya Java kullanarak dosya tabanlı (JSON/TXT/Binary) bir yapıda saklayacak olan "Database Engine" mekanizmasını kurar. Nesnelerin diske yazılması (Save) ve açılışta tekrar belleğe yüklenmesi (Load) süreçlerini yürütür. Ayrıca, dosya okuma/yazma sırasında oluşabilecek tüm senaryolar için Hata Yönetimi (Exception Handling) mimarisini ve veritabanı güvenliğini (Backup/Sync) sağlar. Projenin canlıya alınma durumunda host penetrasyon işlemini yapar.
## Yaptığı dosyalar
```
include/db/DatabaseManager.h
src/db/DatabaseManager.cpp
data/users.json
data/materials.json
data/backup/
tests/test_db.cpp
```
## UI/UX Developer - Göktuğ Berke Kuzucu:
Sistemin kullanıcı ile temas eden tüm görsel arayüzlerini ve etkileşim senaryolarını tasarlar. JavaFX, Qt veya Swing gibi teknolojileri kullanarak, karmaşık kütüphane işlemlerini (materyal arama, ödünç alma, kullanıcı kaydı) son kullanıcı için basit ve sezgisel bir deneyime dönüştürür. Görsel hiyerarşi, renk paleti ve tipografi seçimleriyle kullanıcı deneyimini (UX) iyileştirirken; Backend'den gelen verileri dinamik grafikler, tablolar ve uyarı pencereleriyle görselleştirir. Ayrıca, arayüzün sistem mantığıyla entegrasyonunu sağlayarak akıcı bir navigasyon yapısı oluşturur.
## Yaptığı dosyalar
```
frontend/index.html
frontend/dashboard.html
frontend/css/login.css
frontend/css/main.css
frontend/js/dashboard.js
```


## Security & Integration Specialist - Eren Gider:
Sistemin güvenlik altyapısını ve proje entegrasyon süreçlerini yönetir. Kullanıcı kayıt ve giriş işlemlerinde güvenli yetkilendirme (Authentication & Authorization) mekanizmalarını kurar. Hassas verilerin korunması için Hashing (SHA-256 vb.) algoritmalarını kullanarak şifreleme katmanını oluşturur. Ayrıca, projenin profesyonel bir portfolyo öğesi haline gelmesi için GitHub depo yönetimi, kapsamlı README dokümantasyonu ve teknik raporlama süreçlerini yürütür. Kodun farklı branch’ler üzerinden tutarlı bir şekilde birleştirilmesini (Merge/Integration) sağlayarak ekip içi teknik koordinasyonu denetler.
## Yaptığı dosyalar
```
include/auth/AuthManager.h
src/auth/AuthManager.cpp
include/server/HttpServer.h
src/server/HttpServer.cpp
frontend/js/api.js
frontend/js/auth.js
tests/test_auth.cpp
README.md
docs/UML_Sema.md
docs/Proje_Raporu.md
```
## Proje Dosya Şeması
```
NYP-Proje-2026-Akilli-Kutup/
├── .gitignore
├── README.md
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
├── include/
│   ├── auth/
│   │   └── AuthManager.h
│   ├── core/
│   │   ├── IMateryal.h
│   │   ├── Materyal.h
│   │   ├── Kitap.h
│   │   └── DijitalMedya.h
│   ├── db/
│   │   └── DatabaseManager.h
│   └── server/
│       └── HttpServer.h
├── lib/
│   ├── nlohmann_json/
│   └── httplib/
├── src/
│   ├── auth/
│   │   └── AuthManager.cpp
│   ├── core/
│   │   ├── Materyal.cpp
│   │   ├── Kitap.cpp
│   │   └── DijitalMedya.cpp
│   ├── db/
│   │   └── DatabaseManager.cpp
│   ├── server/
│   │   └── HttpServer.cpp
│   └── main.cpp
└── tests/
    ├── test_auth.cpp
    ├── test_core.cpp
    └── test_db.cpp
```
