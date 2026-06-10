# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi V3 - 2026 Nesneye Yönelik Programlama Proje Ödevi

![Java](https://img.shields.io/badge/Java-ED8B00?logo=java&logoColor=white)
![Frontend](https://img.shields.io/badge/Frontend-HTML%20%7C%20CSS%20%7C%20JS-E34F26?logo=html5&logoColor=white)
![Database](https://img.shields.io/badge/Database-JSON-orange)
![Security](https://img.shields.io/badge/Security-SHA--256-red?logo=springsecurity&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux-lightgrey?logo=linux)
![License](https://img.shields.io/badge/License-MIT-brightgreen)

### Uygulama Ekran Görüntüleri

<p align="center">
  <img src="ekran_goruntuleri/1.png" width="48%" />
  <img src="ekran_goruntuleri/2.png" width="48%" />
</p>
<p align="center">
  <img src="ekran_goruntuleri/3.png" width="48%" />
  <img src="ekran_goruntuleri/4.png" width="48%" />
</p>
<p align="center">
  <img src="ekran_goruntuleri/5.png" width="48%" />
  <img src="ekran_goruntuleri/6.png" width="48%" />
</p>

---

## 1. Proje Özeti
Bu proje, geleneksel kütüphane otomasyonlarını modern web teknolojileri, Yapay Zeka (AI) ve derinlemesine savunma (Defense-in-Depth) prensipleriyle birleştiren, Java tabanlı bir dijital varlık yönetim sistemidir. Sistem; fiziksel kitaplar, dijital medyalar ve süreli yayınlar gibi farklı materyalleri Nesneye Yönelik Programlama (OOP) standartlarıyla tek bir merkezden yönetir. Klasik ödev projelerinden farklı olarak, Java bir web sunucusu (Backend) gibi konumlandırılmış ve istemci (Frontend) ile haberleşmesi güvenli bir REST mimarisi üzerine inşa edilmiştir. V3 sürümü ile birlikte sisteme Kişisel Gemini API Anahtarı desteği, Gelişmiş Yönetici Ayarları Paneli, Veritabanı Dışa Aktarma/Zip ve PostgreSQL geçiş hazırlığı entegre edilerek sistemin yetenekleri ve yönetilebilirliği üst düzeye taşınmıştır.

## 2. Çekirdek Mimari ve OOP Uygulamaları
Projenin temel iskeleti, yazılım mühendisliği standartlarına uygun olarak tasarlanmıştır:
*   Kalıtım (Inheritance) ve Soyutlama (Abstraction): IMateryal arayüzü ve Materyal soyut sınıfı üzerinden Kitap ve DijitalMedya gibi alt sınıflar türetilerek, genişletilebilir (Scalable) bir yapı kurulmuştur.
*   Çok Biçimlilik (Polymorphism): Her materyal türünün ceza hesaplama veya ödünç verilme mantığı çalışma zamanında (Runtime) dinamik olarak belirlenir.
*   Kapsülleme (Encapsulation): Kritik iş mantığı, ceza puanları ve sistemin iç durumu dış müdahalelere kapatılarak nesne bütünlüğü korunmuştur.
*   Tek Sorumluluk Prensibi (Single Responsibility Principle - SRP): Devasa DatabaseManager sınıfı parçalanarak sorumluluklar; yüksek performanslı JSON dönüşümü için JsonParser'a, zamanlanmış yedeklemeler ve otomatik temizlik için BackupManager'a, şifreleme ve anahtar yönetimi için FileEncryptionService sınıfına devredilmiştir.

## 3. Siber Güvenlik Katmanı (Cybersecurity Framework)
Proje, kullanıcı verilerini ve sunucu bütünlüğünü korumak amacıyla gelişmiş güvenlik mekanizmaları içerir:
*   Kriptografik Şifreleme (Hashing & Salting): Kullanıcı parolaları veritabanında kesinlikle açık metin (plaintext) olarak saklanmaz. Parolalar, güvenlik standartlarına uygun olarak hash algoritmaları (SHA-256) kullanılarak şifrelenir. Ayrıca hassas konfigürasyon verileri AES-256 algoritmasıyla uçtan uca korunur.
*   Kimlik Doğrulama ve Yetkilendirme (Auth & Authorization): Frontend ile Backend arasındaki API iletişiminde yetkisiz erişimleri engellemek için güvenlik mekanizmaları devrededir. Sistemde En Az Ayrıcalık (Least Privilege) prensibi uygulanır; sıradan bir Uye sadece okuma yapabilirken, CRUD operasyonlarını yalnızca Admin yetkisine sahip kullanıcılar gerçekleştirebilir.
*   Girdi Denetimi ve Sanitizasyon (Input Validation): İstemciden (Web arayüzünden) gelen her türlü veri, Backend tarafında işlenmeden önce süzgeçten geçirilir. Bu sayede JSON Injection ve XSS gibi saldırı vektörleri engellenir.
*   Dosya Yolu Güvenliği (Path Traversal Protection): Sistem, yerel JSON dosyalarını kullandığından dışarıdan manipüle edilmiş dosya yolu isteklerine karşı sıkı bir dizin denetimi uygular. ApiServer statik dosya sunucusu, `getCanonicalPath()` kontrolü sayesinde isteklerin `frontend` klasörü dışına çıkmasını engeller.
*   Gizli Anahtar Güvenliği (Key Management): Kaynak koda gömülü olan statik AES anahtarı kaldırılmış, bunun yerine otomatik oluşturulan ve güvenli bir şekilde saklanan `data/secret.key` yapısına geçilmiştir.
*   Güvenli Şifre Kıyaslaması: AuthManager tarafında hash kıyaslamalarında platformlar arası byte dönüşüm hatalarını ve olası sızıntıları önlemek için UTF-8 standartları zorunlu kılınmıştır.

## 4. Veri Kalıcılığı ve Hata Yönetimi (Database & Persistence)
*   Sistem, SQL kullanmak yerine kendi özel Dosya Tabanlı Veritabanı Motorunu (File-Based Database Engine) Java ile sıfırdan yönetir.
*   Veriler JSON formatında serialize/deserialize edilerek saklanır.
*   Gelişmiş Hata Yakalama (Exception Handling) blokları sayesinde; dosya bulunamaması veya JSON formatının bozulması gibi durumlarda sistem çökmez. Ayrıca her kritik işlemden önce zaman damgalı otomatik JSON yedekleri oluşturularak veri kaybı tamamen önlenir.

## 5. Modern Web Arayüzü ve AI Entegrasyonu (Frontend Integration)
*   Java backend'i, gömülü bir HTTP sunucusu modülü (ApiServer) barındırarak ağ üzerinden gelen istekleri dinler.
*   Kullanıcılar sisteme kurumsal tasarıma sahip, karanlık tema destekli, asenkron (Fetch API) ve kullanıcı dostu bir web paneli üzerinden erişim sağlar.
*   Sisteme entegre edilen GeminiClient modülü sayesinde kullanıcılara okuma geçmişlerine uygun materyal önerileri, akıllı asistan hizmetleri ve katalog analizleri sağlanır.

## 6. Sistemin Temel Özellikleri (Features)

Projeye V3 sürümü ile birlikte kazandırılan ve yapılan son geliştirmelerle sistemin temelini oluşturan tüm fonksiyonel yetenekler aşağıda detaylandırılmıştır:

*   **Kapsamlı Materyal Yönetimi:** Admin kullanıcıları sisteme Kitap veya Dijital Medya gibi farklı özelliklere sahip materyaller ekleyebilir, güncelleyebilir, stok durumlarını kontrol edebilir ve silebilir.
*   **Rol Tabanlı Gelişmiş Kimlik Doğrulama:** Admin ve standart Üye rolleri birbirinden tamamen ayrılmıştır. SHA-256 ile şifrelenen hesaplara giriş yapıldığında, yetkilendirmeye göre sadece ilgili butonlar, menüler ve işlemler (CRUD) aktif hale gelir.
*   **Ödünç Alma ve İade Süreçleri:** Kullanıcılar kütüphane materyallerini ödünç alabilir. Ödünç alma limitleri (Admin için sınırsız, Üye için belirli limitler) nesneye yönelik programlama kurallarıyla dinamik olarak yönetilir.
*   **Dinamik Ceza Puanı ve Kredi Sistemi:** Zamanında iade edilmeyen materyaller için sisteme entegre algoritma sayesinde dinamik ceza puanı hesaplanır. Kredisi eksilere düşen üyeler otomatik olarak ödünç alma işlemlerinden kısıtlanır.
*   **Kişisel Gemini API Anahtarı Desteği:** Üyeler, profil düzenleme ekranından kendi kişisel Google Gemini API anahtarlarını sisteme tanımlayabilirler. Tanımlanan anahtarlar veritabanında güvenli bir şekilde saklanır. Bu sayede kullanıcılar kendi kotaları üzerinden AI Asistanı kullanabilirler.
*   **Yedekli Yapay Zeka İstek Mekanizması (Fallback):** Yapay zeka asistanı ile yapılan sohbetlerde sistem, öncelikle istek atan üyenin kendi kişisel API anahtarının olup olmadığını kontrol eder. Eğer kişisel anahtar tanımlanmamışsa, sistem otomatik olarak admin tarafından belirlenen global (sistem) API anahtarını kullanır. Bu sayede kesintisiz bir AI deneyimi sağlanır.
*   **Gelişmiş Yönetici Ayarları Paneli:** Admin yetkisine sahip kullanıcılar, dashboard üzerinden tüm sistem konfigürasyonlarını gerçek zamanlı olarak yönetebilir:
    *   *Güvenlik Ayarları:* Oturum zaman aşımı süresi, anahtar rotasyon bildirimleri ve detaylı log izleme (Audit Trail).
    *   *Yapay Zeka Konfigürasyonu:* Model sıcaklık değeri (Temperature), maksimum token sınırı (Max Tokens), asistan sistem promptu (System Prompt) ve global Gemini API anahtarı.
    *   *Kütüphane Kuralları:* Günlük gecikme cezası puanı, maksimum ceza puanı sınırı ve iade tolerans süresi (Grace Period).
*   **Tek Tıkla Veritabanı Dışa Aktarma (Export):** Yönetici ayarlarında bulunan "Veritabanını Dışa Aktar" butonu sayesinde, sistemdeki güncel JSON veritabanı dosyaları (`users.json`, `materials.json`, `config.json`) sunucu tarafında dinamik olarak zip formatında paketlenir ve `kutuphane_yedek.zip` adıyla anında indirilir.
*   **PostgreSQL Geçiş Hazırlığı:** Gelecek sürümlerde sunulması planlanan SQL veritabanı geçişi için gerekli bağlantı ayarları arayüzü (Host, Port, Veritabanı Adı, Kullanıcı Adı ve Şifre) ayarlar paneline eklenmiş ve gelecek entegrasyonlar için mimari altyapı hazırlanmıştır (Mevcut sürümde pasif/disabled olarak yer almaktadır).
*   **Finansal Analiz ve Otomatik PDF Raporlama:** Sistemin finansal durumu, kesilen cezalar ve genel istatistikler admin paneli üzerinden anlık olarak PDF formatında (veya ekranda tablo olarak) raporlanabilir.
*   **Simüle Edilmiş Barkod/Hızlı Tarama Sistemi:** Ön yüz (Dashboard) üzerinde yer alan hızlı işlemler menüsü ile kütüphane barkod sistemi simüle edilerek tek tıkla en çok okunanlara erişim imkanı tanınır.
*   **Dinamik Bildirimler ve Gerçek Zamanlı Profil Senkronizasyonu:** Kullanıcıların web paneli üzerinden anlık profil güncellemeleri ve bildirim yönetimi yapabilmesi sağlanmıştır. Bu güncellemeler eşzamanlı olarak Java Desktop GUI ile de senkronize edilir.
*   **Asenkron Çalışan Arka Plan Sunucusu:** Java tabanlı ApiServer sayesinde tüm arayüz (Frontend) işlemleri sayfayı yenilemeden arka planda hızlı ve güvenli bir şekilde sunucu ile haberleşir.
*   **Gelişmiş AI Hata Yönetimi ve Model Fallback:** Google Gemini API tarafındaki aşırı yüklenmeler (503 High Demand) ve hatalara karşı otomatik yedek (fallback) modeller (`gemini-1.5-pro-latest`, `gemini-pro`) denenir. Ayrıca çapraz alan (CORS) sorunları Native Java HTTP sunucusunda çözülmüştür.
*   **Felaket Kurtarma ve Otomatik Yedekleme:** Sistemde yapılan her kritik okuma/yazma işlemi öncesinde `DatabaseManager` modülü tüm veritabanı (JSON) dosyalarının tam yedeğini alır.

## EKİP GÖREV DAĞILIMI

Backend & Core Architect - Ahmet Güler:

Projenin nesneye yönelik tasarım hiyerarşisini ve iş mantığını kurgular. Sistemdeki tüm nesnelerin atası olan Abstract sınıfları ve ortak davranışları belirleyen Interface yapılarını tasarlar. Kalıtım mekanizması ile materyal çeşitliliğini yönetirken; kredi puanı hesaplama, dinamik ceza sistemi ve stok kontrolü gibi çekirdek algoritmaları kodlar. Ayrıca, sınıflar arası ilişkilerin sağlam bir mimaride yürümesini sağlayarak projenin genişletilebilir olmasını garanti altına alır.

Yaptığı dosyalar:

```text
src/main/java/com/akillikutup/core/IMateryal.java
src/main/java/com/akillikutup/core/Materyal.java
src/main/java/com/akillikutup/core/Kitap.java
src/main/java/com/akillikutup/core/DijitalMedya.java
src/test/java/com/akillikutup/core/CoreTest.java
src/main/java/com/akillikutup/Main.java
```

Database & Data Persistence Manager / Penetration Tester - Arda Meçik:

Sistemin veri kalıcılığı katmanını tasarlar ve yönetir. Verileri SQL yerine Java kullanarak dosya tabanlı bir yapıda saklayacak olan Database Engine mekanizmasını kurar. Nesnelerin diske yazılması ve açılışta tekrar belleğe yüklenmesi süreçlerini yürütür. Ayrıca, dosya okuma/yazma sırasında oluşabilecek tüm senaryolar için Hata Yönetimi mimarisini, otomatik yedekleme süreçlerini kurar. Projenin canlıya alınma durumunda host penetrasyon işlemini yapar.

Yaptığı dosyalar:

```text
src/main/java/com/akillikutup/db/DatabaseManager.java
src/main/java/com/akillikutup/db/BackupManager.java
src/main/java/com/akillikutup/db/JsonParser.java
src/main/java/com/akillikutup/db/FileEncryptionService.java
src/test/java/com/akillikutup/db/DatabaseManagerTest.java
data/users.json
data/materials.json
data/backup/
data/secret.key
```

UI/UX Developer - Göktuğ Berke Kuzucu:

Sistemin kullanıcı ile temas eden tüm görsel arayüzlerini ve etkileşim senaryolarını tasarlar. Web teknolojilerini kullanarak karmaşık kütüphane işlemlerini son kullanıcı için basit bir deneyime dönüştürür. Kurumsal koyu tema, görsel hiyerarşi, renk paleti ve tipografi seçimleriyle kullanıcı deneyimini iyileştirirken; Backend'den gelen verileri dinamik grafikler, tablolar ve uyarı pencereleriyle görselleştirir.

Yaptığı dosyalar:

```text
frontend/index.html
frontend/dashboard.html
frontend/css/login.css
frontend/css/main.css
frontend/js/dashboard.js
```

Security & Integration Specialist - Eren Gider:

Sistemin güvenlik altyapısını ve iletişim ağını kurar. AES-256 ve SHA-256 şifreleme sistemleri üzerinden güvenli veri ve konfigürasyon depolama mimarilerini kurar. ApiServer ile Java backend - web frontend haberleşmesini sağlar. V2 güncellemesi ile Gemini AI entegrasyonunu gerçekleştirerek sisteme yapay zeka özelliklerini kazandırır. Kapsamlı README dokümantasyonunu yönetir.

Yaptığı dosyalar:

```text
src/main/java/com/akillikutup/auth/AuthManager.java
src/main/java/com/akillikutup/server/ApiServer.java
src/main/java/com/akillikutup/server/GeminiClient.java
src/main/java/com/akillikutup/core/ConfigManager.java
src/test/java/com/akillikutup/auth/AuthManagerTest.java
frontend/js/api.js
frontend/js/auth.js
pom.xml
README.md
docs/UML_Sema.md
docs/Proje_Raporu.md
```

## Genel Proje Dosya Şeması
```text
NYP-Proje-2026-Akilli-Kutup/
├── .gitignore
├── LICENSE
├── README.md
├── pom.xml
├── run.sh
├── run_linux.sh
├── run_mac.sh
├── run_win.bat
├── data/
│   ├── config.json
│   ├── materials.json
│   ├── users.json
│   ├── secret.key
│   └── backup/
├── docs/
│   ├── UML_Sema.md
│   └── Proje_Raporu.md
├── ekran_goruntuleri/
│   ├── 1.png
│   ├── 2.png
│   ├── 3.png
│   ├── 4.png
│   ├── 5.png
│   └── 6.png
├── frontend/
│   ├── index.html
│   ├── dashboard.html
│   ├── login.html
│   ├── css/
│   │   ├── login.css
│   │   └── main.css
│   └── js/
│       ├── api.js
│       ├── auth.js
│       └── dashboard.js
└── src/
    ├── main/java/com/akillikutup/
    │   ├── auth/
    │   │   └── AuthManager.java
    │   ├── core/
    │   │   ├── ConfigManager.java
    │   │   ├── IMateryal.java
    │   │   ├── Materyal.java
    │   │   ├── Kitap.java
    │   │   ├── DijitalMedya.java
    │   │   ├── Kullanici.java
    │   │   ├── Admin.java
    │   │   └── Uye.java
    │   ├── db/
    │   │   ├── DatabaseManager.java
    │   │   ├── BackupManager.java
    │   │   ├── JsonParser.java
    │   │   └── FileEncryptionService.java
    │   ├── gui/
    │   │   ├── MainFrame.java
    │   │   ├── LoginPanel.java
    │   │   ├── AdminPanel.java
    │   │   ├── UserPanel.java
    │   │   └── LibraryManager.java
    │   ├── server/
    │   │   ├── ApiServer.java
    │   │   └── GeminiClient.java
    │   └── Main.java
    └── test/java/com/akillikutup/
        ├── auth/
        │   └── AuthManagerTest.java
        ├── core/
        │   └── CoreTest.java
        └── db/
            └── DatabaseManagerTest.java
```

### Geliştirme Durumu

Yukarıdaki şemada belirtilen dosyalardan tamamlananlar ve güncellenen modüller aşağıda listelenmiştir:

Tamamlanan Kısımlar:
*   **src/main/java/com/akillikutup/core/** (Çekirdek OOP Modelleri; `Kullanici.java` sınıfına kişisel API anahtarı desteği; `ConfigManager.java` sınıfına dinamik konfigürasyon güncelleme ve AES-256 şifrelemeli depolama eklendi)
*   **src/main/java/com/akillikutup/db/** (Veritabanı Motoru, JSON İşlemleri, Güvenli AES-256 Dosya Şifreleme ve Otomatik Yedekleme Mekanizması)
*   **src/main/java/com/akillikutup/gui/** (Swing Masaüstü Arayüzü)
*   **src/main/java/com/akillikutup/auth/** (SHA-256 Şifreleme, Oturum Yönetimi ve Kimlik Doğrulama)
*   **src/main/java/com/akillikutup/server/** (REST API Sunucu Altyapısı; `/api/settings` ve `/api/backup` endpoint'leri eklendi; `/api/profil`, `/api/chat` ve `/api/login` modülleri kişisel API anahtarı desteği ve yedekli AI sorgulama mimarisi ile güncellendi)
*   **src/test/** (Birim Testler - Çekirdek iş mantığı, veritabanı ve kimlik doğrulama testleri başarıyla geçmektedir)
*   **frontend/** (Karanlık Temalı Modern Web Arayüzü; Profil düzenlemede "Kişisel Gemini API Key" alanı, Admin panelinde "Sistem Ayarları" yönetim paneli, "Veritabanını Dışa Aktar" ve "PostgreSQL Bağlantı Ayarları" alanları eklendi)
*   **docs/UML_Sema.md** (UML sınıf diyagramı ve nesne ilişkileri)
*   **docs/Proje_Raporu.md** (Detaylı proje raporu ve OOP prensipleri dokümantasyonu)
