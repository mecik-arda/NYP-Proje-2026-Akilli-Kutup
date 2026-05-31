# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi - Teknik Proje Raporu

## 1. Proje Özeti
Bu proje, geleneksel kütüphane otomasyonlarını modern web teknolojileri ve **derinlemesine savunma (Defense-in-Depth)** prensipleriyle birleştiren, Java tabanlı bir dijital varlık yönetim sistemidir. Sistem; fiziksel kitaplar, dijital medyalar ve süreli yayınlar gibi farklı materyalleri tek bir merkezden yönetir. Java bir web sunucusu (Backend) gibi konumlandırılmış ve istemci (Frontend) ile haberleşmesi güvenli bir REST mimarisi üzerine inşa edilmiştir.

## 2. Çekirdek Mimari ve OOP Uygulamaları
Projenin temel iskeleti, yazılım mühendisliği standartlarına uygun olarak tasarlanmıştır:
*   **Kalıtım (Inheritance) ve Soyutlama (Abstraction):** `IMateryal` arayüzü ve `Materyal` soyut sınıfı üzerinden `Kitap` ve `DijitalMedya` gibi alt sınıflar türetilerek, genişletilebilir (Scalable) bir yapı kurulmuştur.
*   **Çok Biçimlilik (Polymorphism):** Her materyal türünün ceza hesaplama veya ödünç verilme mantığı çalışma zamanında (Runtime) dinamik olarak belirlenir.
*   **Kapsülleme (Encapsulation):** Kritik iş mantığı, ceza puanları ve sistemin iç durumu dış müdahalelere kapatılarak nesne bütünlüğü korunmuştur.

## 3. Siber Güvenlik Katmanı (Cybersecurity Framework)
Proje, kullanıcı verilerini ve sunucu bütünlüğünü korumak amacıyla gelişmiş güvenlik mekanizmaları içerir:
*   **Kriptografik Şifreleme:** Kullanıcı parolaları veritabanında açık metin olarak saklanmaz, güvenlik standartlarına uygun olarak `SHA-256` ile hashlenir. Dış servisler için kullanılan hassas API anahtarları ise `AES` şifreleme algoritması ile sistem belleğinde güvenli bir şekilde saklanmaktadır.
*   **Yetkilendirme:** Frontend ile Backend iletişiminde sistemde **Least Privilege (En Az Ayrıcalık)** prensibi uygulanır. CRUD operasyonlarını yalnızca "Admin" yetkisine sahip kullanıcılar gerçekleştirebilir.
*   **Girdi Denetimi (Input Validation):** İstemciden gelen veriler işlenmeden önce süzgeçten geçirilerek JSON Injection ve XSS saldırı vektörleri engellenir.
*   **Dosya Yolu Güvenliği:** Yerel JSON dosyalarına dışarıdan manipüle edilmiş dosya yolu (Path Traversal) isteklerine karşı sıkı bir dizin denetimi uygulanır.

## 4. Veri Kalıcılığı ve Backend İletişimi
*   Sistem, SQL kullanmak yerine kendi özel Dosya Tabanlı Veritabanı Motorunu yönetir.
*   Veriler `JSON` formatında (`data/users.json`, `data/materials.json`) saklanır ve gelişmiş hata yakalama (Exception Handling) blokları sayesinde sistemin çökmesi engellenir.
*   Tamamen saf Java'nın standart kütüphanesi olan `com.sun.net.httpserver.HttpServer` kullanılarak hafif ve hızlı bir REST API sunucusu inşa edilmiştir.

## 5. Modern Web Arayüzü (Frontend Integration)
*   Ön yüz, modern web standartlarına (HTML5, CSS3) uygun, saf (Vanilla) JavaScript ile Tek Sayfa Uygulaması (SPA) şeklinde tasarlanmıştır. Minimalist ve tepkisel bir tasarıma (Solid UX) sahiptir.
*   **AI Asistan (GeminiClient):** Sisteme entegre edilen yapay zeka birimidir. "Kitap Öner" veya "Koleksiyon Analizi" sorularını doğrudan Java Backend'i üzerinden Google Gemini (1.5-Flash) REST API'sine aktarır ve asenkron yanıtlar sunar.

## 6. Geliştirme Metodolojisi ve Süreç Yönetimi
Proje, "Çevik Yazılım Geliştirme" (Agile) prensiplerine uygun olarak ve iteratif bir yaklaşımla hayata geçirilmiştir. Modern yazılım mühendisliği süreçlerinde verimliliği ve kod kalitesini artırmak amacıyla yeni nesil yapay zeka (AI) destekli kodlama asistanlarından (AI-powered Code Completion) endüstri standartlarına uygun şekilde faydalanılmıştır.
* **AI Destekli Hızlı Prototipleme:** Tasarım aşamasında (özellikle HTML/CSS arayüz iskeletlerinin oluşturulmasında ve standart JavaScript DOM işlemlerinde) AI asistan eklentileri bir geliştirme yardımcısı (pair-programmer) olarak kullanılarak prototipleme süresi kısaltılmıştır. Bu yaklaşım, sistemin çekirdek Java algoritmalarına ve karmaşık güvenlik mimarisine odaklanılması için önemli bir zaman tasarrufu sağlamıştır.
* **Manuel Kod İnceleme (Code-Review):** AI araçları tarafından sunulan kod kalıpları körü körüne projeye dahil edilmemiş; tüm kod blokları nesne yönelimli programlama (OOP) prensipleri, veritabanı kararlılığı ve derinlemesine savunma stratejileri bizzat geliştirici tarafından gözetilerek titiz bir manuel denetimden (code-review) geçirildikten sonra ana mimariye entegre edilmiştir.
