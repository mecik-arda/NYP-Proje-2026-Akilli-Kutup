# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi [v3.0.0-Beta.2] - 2026 Nesneye Yönelik Programlama Proje Ödevi

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
Proje, kullanıcı verilerini ve sunucu bütünlüğünü korumak amacıyla gelişmiş güvenlik mekanizmaları ve derinlemesine savunma (Defense-in-Depth) prensipleri içerir:
*   Kriptografik Şifreleme (Hashing & Salting): Kullanıcı parolaları veritabanında kesinlikle açık metin (plaintext) olarak saklanmaz. Parolalar, güvenlik standartlarına uygun olarak hash algoritmaları (SHA-256) kullanılarak şifrelenir. Ayrıca hassas konfigürasyon verileri AES-256 algoritmasıyla uçtan uca korunur.
*   Kimlik Doğrulama ve Yetkilendirme (Auth & Authorization): Frontend ile Backend arasındaki API iletişiminde yetkisiz erişimleri engellemek için güvenlik mekanizmaları devrededir. Sistemde En Az Ayrıcalık (Least Privilege) prensibi uygulanır; sıradan bir Uye sadece okuma yapabilirken, CRUD operasyonlarını yalnızca Admin yetkisine sahip kullanıcılar gerçekleştirebilir.
*   Girdi Denetimi ve Sanitizasyon (Input Validation): İstemciden (Web arayüzünden) gelen her türlü veri, Backend tarafında işlenmeden önce süzgeçten geçirilir. Bu sayede JSON Injection ve XSS gibi saldırı vektörleri engellenir.
*   Dosya Yolu Güvenliği (Path Traversal Protection): Sistem, yerel JSON dosyalarını kullandığından dışarıdan manipüle edilmiş dosya yolu isteklerine karşı sıkı bir dizin denetimi uygular. ApiServer statik dosya sunucusu, `getCanonicalPath()` kontrolü sayesinde isteklerin `frontend` klasörü dışına çıkmasını engeller.
*   Gizli Anahtar Güvenliği (Key Management): Kaynak koda gömülü olan statik AES anahtarı kaldırılmış, bunun yerine otomatik oluşturulan ve güvenli bir şekilde saklanan `data/secret.key` yapısına geçilmiştir.
*   Güvenli Şifre Kıyaslaması: AuthManager tarafında hash kıyaslamalarında platformlar arası byte dönüşüm hatalarını ve olası sızıntıları önlemek için UTF-8 standartları zorunlu kılınmıştır.
*   Brute-Force Koruması ve Hız Sınırlama (Rate Limiting): API ve Masaüstü GUI üzerinden yapılan hatalı giriş denemeleri IP/İstemci bazında ConcurrentHashMap ile takip edilmektedir. Aynı IP'den veya masaüstü istemcisinden üst üste 5 başarısız giriş yapıldığında, erişim 5 dakika boyunca kilitlenir.
*   IDOR (Insecure Direct Object Reference) Önleme: `/api/kitaplar/*` rotalarına (`DELETE`, `PUT` işlemleri için) sıkı rol kontrolleri (Admin) eklenerek kullanıcıların yetkisiz materyal silmesi veya değiştirmesi engellenmiştir.
*   Hassas Veri Sızıntısının Engellenmesi: `/api/giris` rotasından dönen kullanıcı profil nesnesinden `geminiApiKey` alanı çıkarılarak, hassas API anahtarlarının istemci tarafına sızdırılması engellenmiştir.
*   API Key Güvenliği: Yapay zeka asistanı isteklerinde Gemini API anahtarı, URL Query parametreleri yerine HTTP Header (`x-goog-api-key`) aracılığıyla güvenli şekilde taşınmaktadır.
*   Yedekleme Güvenliği (Backup Isolation): Sistemde otomatik veya manuel yedek (backup) zip dosyaları oluşturulurken, `secret.key` gibi kriptografik anahtar dosyalarının yedek klasörüne kopyalanması engellenmiştir.
*   Race Condition Engelleme: `DatabaseManager` içerisindeki veri listelerinin (`getKullaniciListesi` vb.) yüklenmesinde Double-Checked Locking kalıbı uygulanarak thread-safety ve veri bütünlüğü sağlanmıştır.
*   Transaction ve Veri Güvenliği: Dosya tabanlı veritabanı eşitleme işlemlerinde oluşabilecek hatalarda veri tutarlılığını korumak için rollback (`conn.rollback()`) mekanizması entegre edilmiştir.
*   Sıkılaştırılmış CORS Başlıkları: `ApiServer` tarafındaki tüm yetkisiz erişim `401` yanıtlarında ve `login` rotalarında tutarlı ve güvenli CORS yapılandırması mevcuttur.

## 4. Veri Kalıcılığı ve Hata Yönetimi (Database & Persistence)
*   Sistem, SQL kullanmak yerine kendi özel Dosya Tabanlı Veritabanı Motorunu (File-Based Database Engine) Java ile sıfırdan yönetir.
*   Veriler JSON formatında serialize/deserialize edilerek saklanır.
*   Gelişmiş Hata Yakalama (Exception Handling) blokları sayesinde; dosya bulunamaması veya JSON formatının bozulması gibi durumlarda sistem çökmez. Ayrıca her kritik işlemden önce zaman damgalı otomatik JSON yedekleri oluşturularak veri kaybı tamamen önlenir.

## 5. Modern Web Arayüzü ve AI Entegrasyonu (Frontend Integration)
*   Java backend'i, gömülü bir HTTP sunucusu modülü (ApiServer) barındırarak ağ üzerinden gelen istekleri dinler.
*   Kullanıcılar sisteme kurumsal tasarıma sahip, karanlık tema destekli, asenkron (Fetch API) ve kullanıcı dostu bir web paneli üzerinden erişim sağlar.
*   Sisteme entegre edilen GeminiClient modülü sayesinde kullanıcılara okuma geçmişlerine uygun materyal önerileri, akıllı asistan hizmetleri ve katalog analizleri sağlanır.
*   **Modüler ES6 JavaScript Yapısı**: Dev JavaScript dosyaları sorumluluklarına göre modüllere (`main.js`, `ui.js`, `charts.js`, `utils.js`, `store.js`) ayrılmış, ortak işlevlerin `utils.js`'e toplanmasıyla modüller arası döngüsel bağımlılık (circular dependency) sorunları çözülmüştür.
*   **XSS (Cross-Site Scripting) Koruması**: Ödünç alma modal listesi, medya gridi (Asset Grid), timeline akışı, AI chat sohbet pencereleri ve PDF rapor taslakları dahil olmak üzere kullanıcıdan gelen tüm dinamik girdiler `escapeHtml` fonksiyonu ile süzülerek HTML Entity Encoding uygulanmıştır.
*   **Veri Maskeleme**: PDF rapor çıktılarında kullanıcıların hassas TC Kimlik numaraları maskelenerek gizlilik standartları yükseltilmiştir.
*   **Arayüz Performans İyileştirmeleri**: SVG donut grafik üretiminde DOM döngü yükünü (layout thrashing) azaltmak için `DocumentFragment` kullanılmıştır.

## 6. Sistemin Temel Özellikleri (Features)

Projeye V3 sürümü ile birlikte kazandırılan ve yapılan son geliştirmelerle sistemin temelini oluşturan tüm fonksiyonel yetenekler aşağıda detaylandırılmıştır:

*   **Kapsamlı Materyal Yönetimi:** Admin kullanıcıları sisteme Kitap veya Dijital Medya gibi farklı özelliklere sahip materyaller ekleyebilir, güncelleyebilir, stok durumlarını kontrol edebilir ve silebilir.
*   **Rol Tabanlı Gelişmiş Kimlik Doğrulama:** Admin ve standart Üye rolleri birbirinden tamamen ayrılmıştır. SHA-256 ile şifrelenen hesaplara giriş yapıldığında, yetkilendirmeye göre sadece ilgili butonlar, menüler ve işlemler (CRUD) aktif hale gelir.
*   **Ödünç Alma ve İade Süreçleri:** Kullanıcılar kütüphane materyallerini ödünç alabilir. Ödünç alma limitleri (Admin için sınırsız, Üye için belirli limitler) nesneye yönelik programlama kurallarıyla dinamik olarak yönetilir.
*   **Dinamik Ceza Puanı ve Kredi Sistemi:** Zamanında iade edilmeyen materyaller için sisteme entegre algoritma sayesinde dinamik ceza puanı hesaplanır. Kredisi eksilere düşen üyeler otomatik olarak ödünç alma işlemlerinden kısıtlanır.
*   **Kişisel Gemini API Anahtarı Desteği:** Üyeler, profil düzenleme ekranından kendi kişisel Google Gemini API anahtarını sisteme tanımlayabilirler. Tanımlanan anahtar veritabanında güvenli şekilde saklanır ve kullanıcı kendi kotasından AI sorguları yapabilir.
*   **Yedekli Yapay Zeka İstek Mekanizması (Fallback):** AI sohbetlerinde üyenin kişisel anahtarı bulunmuyorsa, sistem otomatik olarak admin tarafından tanımlanan global API anahtarına geçiş yapar.
*   **Gelişmiş Yönetici Ayarları Paneli:** Admin kullanıcıları; oturum zaman aşımı, AI model parametreleri (Temperature, Max Tokens, System Prompt), günlük gecikme cezası, ceza limiti ve tolerans sürelerini (Grace Period) dinamik olarak yönetebilir.
*   **Brute-Force Koruması (Rate Limiting):** API ve Masaüstü GUI girişlerinde in-memory (ConcurrentHashMap) bazlı başarısız deneme kontrolü ile brute-force saldırılarını engelleyen hız sınırlaması (5 hatalı deneme -> 5 dakika engelleme).
*   **Benzersiz ID Üretimi (Çakışma Engelleme):** Kitap ve Üye ekleme işlemlerinde length tabanlı hatalı üretim yerine dizideki en yüksek ID'yi bularak 1 artıran akıllı ID oluşturma algoritması.
*   **Doğru Sıralama Algoritması:** Katalog listelemesinde, kitap ID'lerini sayısal `(b.id - a.id)` değerleri ile sıralayan mekanizma.
*   **Tek Tıkla Veritabanı Dışa Aktarma (Export):** JSON veritabanı dosyalarını sunucu tarafında zip formatında paketleyen ve anında indirme imkanı sunan buton.
*   **PostgreSQL Geçiş Hazırlığı:** SQL veritabanı geçişi için bağlantı ayarları arayüzü (Host, Port, DB Adı, Kullanıcı Adı ve Şifre) ayarlar paneline eklenmiştir (Mevcut sürümde pasif/disabled olarak yer almaktadır).
*   **Finansal Analiz ve Raporlama:** Sistemin finansal durumu, kesilen cezalar ve genel istatistiklerin PDF formatında raporlanabilmesi.
*   **Gerçek Zamanlı Kamera Tabanlı Barkod Tarayıcı (Yeni):** `html5-qrcode` kütüphanesi entegrasyonu ile cihazın kamerasını (varsayılan olarak arka kamerayı) kullanarak EAN-13 ve EAN-8 standartlarındaki barkodları/ISBN kodlarını gerçek zamanlı okuma yeteneği. Başarılı okumalarda Web Audio API üzerinden sentezlenen bip sesi ve haptik titreşim (`navigator.vibrate`) desteği ile zenginleştirilmiş kullanıcı deneyimi.
*   **Gelişmiş Dijital Varlık Yönetimi (Yeni):** E-Kitap, Video, Ses, Belge ve Görsel gibi dijital varlıkların sisteme eklenip yönetilebilmesi. Yetkilendirilmiş RBAC kontrolleriyle (sadece Adminlere özel) klasör oluşturma, yeni medya yükleme ve dinamik filtreleme sekmeleriyle zenginleştirilmiş kullanıcı arayüzü entegrasyonu.
*   **Gelişmiş OOP Refactoring ve LSP Desteği (Yeni):** Sistemi Liskov Substitution Principle ihlallerinden korumak için `IOduncAlinabilir` arayüzü oluşturuldu. Fiziksel kitaplar ve dijital medyalar bu özellikleri uygularken, klasör nesneleri gibi ödünç alınamayan varlıkların mantık hatalarına neden olması sunucu seviyesinde type-casting (`instanceof`) ile tamamen engellendi.
*   **Gelişmiş Üye Yönetimi ve Güvenlik (Yeni):** Yalnızca yetkili (Admin) kullanıcıların erişebildiği; TC No ve E-posta bazlı mükerrer kayıt kontrolü barındıran tam donanımlı üye listeleme, ekleme, silme ve düzenleme ekranı.
*   **Gerçek Zamanlı Ödünç ve İade Takibi (Yeni):** 14 günlük standart ödünç süresi, süre aşımında formül tabanlı `(gecikmeGunu * 2.5) + (birimFiyat * 0.10)` dinamik ceza puanı hesaplaması. Tüm bu süreçleri `Activity Timeline` üzerinden grafiksel olarak takip etme imkanı.
*   **Kapsamlı Raporlar ve Analizler (Yeni):** Veritabanından gerçek zamanlı beslenen Dashboard üzerinden haftalık etkileşim çubuk grafikleri, finansal özetler (tahsil edilen cezalar), kategori dağılım pasta grafikleri ve son gerçekleştirilen işlemler tablosu.
*   **Tam Bağımsız Uçtan Uca (E2E) Test Altyapısı (Yeni):** `SystemTester.java` sınıfı ile tüm kullanıcı işlemleri, materyal oluşturma, LSP prensipleri, veritabanı simülasyonları ve güvenlik kuralları konsol üzerinden izole olarak `%100` oranında test edilebilir hale getirildi.
*   **Dinamik Bildirimler ve Profil Senkronizasyonu:** Web paneli profil güncellemelerinin anında Java Desktop GUI ile senkronize edilmesi.
*   **Asenkron Çalışan Arka Plan Sunucusu:** Java tabanlı ApiServer sayesinde tüm arayüz (Frontend) işlemleri sayfayı yenilemeden arka planda hızlı ve güvenli bir şekilde sunucu ile haberleşir.
*   **Gelişmiş AI Hata Yönetimi ve Model Fallback:** Gemini API aşırı yüklenmelerinde otomatik yedek modellerin (`gemini-1.5-pro-latest`, `gemini-pro`) denenmesi.
*   **Felaket Kurtarma ve Otomatik Yedekleme:** Her kritik okuma/yazma öncesinde `DatabaseManager` modülünün veritabanı dosyalarının tam yedeğini alması.

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
frontend/css/scanner.css
frontend/js/main.js
frontend/js/ui.js
frontend/js/charts.js
frontend/js/store.js
frontend/js/utils.js
frontend/js/scanner.js
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
│   │   ├── main.css
│   │   └── scanner.css
│   └── js/
│       ├── api.js
│       ├── auth.js
│       ├── charts.js
│       ├── main.js
│       ├── store.js
│       ├── ui.js
│       ├── utils.js
│       └── scanner.js
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
*   **src/main/java/com/akillikutup/core/** (Çekirdek OOP Modelleri; `Kullanici.java` sınıfına kişisel API anahtarı desteği ve ID tabanlı `equals`/`hashCode` metodları; `ConfigManager.java` sınıfına dinamik konfigürasyon güncelleme ve AES-256 şifrelemeli depolama eklendi; `DijitalMedya` nesnesi için `toplamErisimSayisi` setter'ı tanımlandı)
*   **src/main/java/com/akillikutup/db/** (Veritabanı Motoru, JSON İşlemleri, Güvenli AES-256 Dosya Şifreleme, Otomatik Yedekleme Mekanizması; veritabanı eşitleme işlemlerine rollback (`conn.rollback()`) desteği ve veri listelerinin yüklenmesinde Double-Checked Locking thread güvenliği eklendi)
*   **src/main/java/com/akillikutup/gui/** (Swing Masaüstü Arayüzü; `LoginPanel.java` IP kısıtlama ve brute-force lockout hata yönetimi ile güncellendi)
*   **src/main/java/com/akillikutup/auth/** (SHA-256 Şifreleme, Oturum Yönetimi, Kimlik Doğrulama; IP/İstemci bazlı in-memory Brute-Force lockout kilit mekanizması eklendi)
*   **src/main/java/com/akillikutup/server/** (REST API Sunucu Altyapısı; `/api/settings` ve `/api/backup` endpoint'leri eklendi; `/api/profil`, `/api/chat` ve `/api/login` modülleri kişisel API anahtarı desteği ve yedekli AI sorgulama mimarisi ile güncellendi; API keylerin HTTP Header üzerinden taşınması, IDOR zafiyeti kontrolü ve login yanıtlarında hassas anahtarların sızdırılmasının engellenmesi sağlandı)
*   **src/test/** (Birim Testler - Çekirdek iş mantığı, veritabanı, kimlik doğrulama testleri başarıyla geçmektedir. Seed data üretimi sadece test modunda çalıştırılacak şekilde ayrıştırıldı)
*   **frontend/** (Karanlık Temalı Modern Web Arayüzü; Profil düzenlemede "Kişisel Gemini API Key" alanı, Admin panelinde "Sistem Ayarları" yönetim paneli, "Veritabanını Dışa Aktar" ve "PostgreSQL Bağlantı Ayarları" alanları eklendi. JavaScript dosyaları ES6 modüler yapısına bölünerek döngüsel bağımlılıklar giderildi. Çakışmayan ID üretimi, XSS sanitizasyonu (`escapeHtml`), TC Kimlik maskeleme ve SVG render optimizasyonları yapıldı. Ayrıca **Kamera Tabanlı Gerçek Zamanlı Barkod Tarayıcı** modülü eklendi: Yeşil lazer tarama animasyonu, ses sentezlemeli bip geri bildirimi ve tarama sonrası otomatik kitap getirme özellikleri entegre edildi.)
*   **docs/UML_Sema.md** (UML sınıf diyagramı ve nesne ilişkileri)
*   **docs/Proje_Raporu.md** (Detaylı proje raporu ve OOP prensipleri dokümantasyonu)
