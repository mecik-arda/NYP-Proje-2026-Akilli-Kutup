# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi V2 🚀

![Java](https://img.shields.io/badge/Java-ED8B00?logo=java&logoColor=white)
![Frontend](https://img.shields.io/badge/Frontend-HTML%20%7C%20CSS%20%7C%20JS-E34F26?logo=html5&logoColor=white)
![Database](https://img.shields.io/badge/Database-JSON-orange)
![Security](https://img.shields.io/badge/Security-SHA--256-red?logo=springsecurity&logoColor=white)
![AI](https://img.shields.io/badge/AI-Google_Gemini_1.5-blue?logo=google&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-brightgreen)

## 📖 1. Proje Özeti
Bu proje, geleneksel kütüphane otomasyonlarını modern web teknolojileri, **Yapay Zeka (AI)** ve **Derinlemesine Savunma (Defense-in-Depth)** prensipleriyle birleştiren, Java tabanlı yenilikçi bir dijital varlık yönetim sistemidir. 

Sistem; fiziksel kitaplar, dijital medyalar ve süreli yayınlar gibi farklı materyalleri Nesneye Yönelik Programlama (OOP) standartlarıyla tek bir merkezden yönetir. **V2 Güncellemesi** ile birlikte sistem salt bir masaüstü yazılımı olmaktan çıkmış, Java'nın arka planda bir REST API sunucusu olarak çalıştığı ve modern bir Web Gösterge Paneli (Frontend Dashboard) ile haberleştiği tam teşekküllü bir otomasyona dönüşmüştür.

---

## ✨ 2. Yeni Sürüm Özellikleri (V2 Güncellemeleri)

*   **🤖 Gemini Yapay Zeka Entegrasyonu:** `GeminiClient` modülü sayesinde sistem Google Gemini 1.5 altyapısını kullanır. Kullanıcılara okuma geçmişlerine göre kitap önerileri sunar, kütüphane koleksiyonunu analiz eder ve akıllı asistan hizmeti sağlar. API anahtarları `ConfigManager` ile AES-256 standardında şifrelenerek korunur.
*   **🌐 Modern Web Arayüzü (Dashboard):** Saf HTML, CSS ve Vanilla JS ile geliştirilmiş, kurumsal koyu temaya (Dark Slate) sahip, mobil uyumlu ve animasyonlu kullanıcı arayüzü. 
*   **🔌 Gelişmiş API Sunucusu:** Java backend'i içerisinde koşan yerleşik `ApiServer` modülü, web arayüzü ile asenkron (Fetch API) haberleşerek veri iletişimini sağlar.
*   **🛡️ Gelişmiş Güvenlik ve Şifreleme:** SHA-256 ile kullanıcı şifrelerinin korunmasının yanı sıra, hassas konfigürasyon dosyaları AES-256 ile uçtan uca şifrelenir.
*   **💾 Otomatik Veri Yedekleme:** `DatabaseManager` her kritik işlemden önce `data/backup/` klasörüne zaman damgalı JSON yedekleri alır, böylece sistem çökmelerine veya veri bozulmalarına karşı %100 koruma sağlar.

---

## 🏗️ 3. Çekirdek Mimari ve OOP Uygulamaları
Projenin temel iskeleti, yazılım mühendisliği standartlarına uygun olarak tasarlanmıştır:
*   **Kalıtım (Inheritance) ve Soyutlama (Abstraction):** `IMateryal` arayüzü ve `Materyal` soyut sınıfı üzerinden `Kitap` ve `DijitalMedya` gibi alt sınıflar türetilerek, genişletilebilir (Scalable) bir yapı kurulmuştur.
*   **Çok Biçimlilik (Polymorphism):** Her materyal türünün ceza hesaplama veya ödünç verilme mantığı çalışma zamanında (Runtime) dinamik olarak belirlenir.
*   **Kapsülleme (Encapsulation):** Kritik iş mantığı, ceza puanları ve sistemin iç durumu dış müdahalelere kapatılarak nesne bütünlüğü korunmuştur.

---

## 🔐 4. Siber Güvenlik Katmanı (Cybersecurity Framework)
Proje, kullanıcı verilerini ve sunucu bütünlüğünü korumak amacıyla üst düzey güvenlik mekanizmaları içerir:
*   **Kriptografik Şifreleme (Hashing & AES):** Kullanıcı parolaları SHA-256 ile şifrelenirken, sistemin API yapılandırmaları AES-256 ile korunur.
*   **Kimlik Doğrulama ve Yetkilendirme (Auth & Authorization):** `AuthManager` sayesinde oturum yönetimi sağlanır. **En Az Ayrıcalık (Least Privilege)** prensibi uygulanır. Admin ve standart kullanıcı yetkileri kesin çizgilerle ayrılmıştır.
*   **Girdi Denetimi (Input Validation):** İstemciden (Web arayüzünden) gelen her türlü veri süzgeçten geçirilir, SQL/JSON Injection ve XSS saldırı vektörleri engellenir.
*   **Dosya Yolu Güvenliği:** İzole edilmiş `data/` klasörü sayesinde Path Traversal saldırıları engellenir.

---

## 📂 5. Veri Kalıcılığı ve Hata Yönetimi (Database & Persistence)
*   Sistem, SQL veya dış bir servis kullanmak yerine kendi özel **Dosya Tabanlı Veritabanı Motorunu** (File-Based Database Engine) Java ile sıfırdan yönetir.
*   Veriler `JSON` formatında asenkron olarak serialize/deserialize edilir.
*   Kapsamlı **Exception Handling** ile dosya bulunamaması veya bozulması durumlarında sistem otomatik olarak son sağlam yedekten kurtarma senaryolarını devreye sokar.

---

## 👥 6. Ekip Görev Dağılımı

**🏗️ Backend & Core Architect - Ahmet Güler:**
Projenin nesneye yönelik tasarım hiyerarşisini ve iş mantığını kurgular. `Abstract` sınıfları ve `Interface` yapılarını tasarlar. Kredi puanı hesaplama, dinamik ceza sistemi ve stok kontrolü gibi çekirdek algoritmaları kodlar. Genişletilebilir backend sisteminin ana kurucusudur.
*(Dosyalar: IMateryal.java, Materyal.java, Kitap.java, DijitalMedya.java, CoreTest.java, Main.java)*

**💾 Database Manager & Penetration Tester - Arda Meçik:**
Sistemin veri kalıcılığı katmanını (Database Engine) tasarlar ve yönetir. JSON tabanlı depolama, nesnelerin diske yazılması/okunması ve hata yönetimi mekanizmalarını kurar. Ayrıca V2 ile gelen **Otomatik Yedekleme** sistemini kodlamış ve host penetrasyon işlemlerini yürütmüştür.
*(Dosyalar: DatabaseManager.java, DatabaseManagerTest.java, data/users.json, data/materials.json, data/backup/)*

**🎨 UI/UX Developer - Göktuğ Berke Kuzucu:**
Sistemin kullanıcı arayüzünü (Frontend) sıfırdan tasarlar. Web teknolojilerini kullanarak karmaşık işlemleri modern bir "Dashboard" yapısında sunar. V2 sürümünde karanlık/kurumsal tema, animasyonlu geçişler, dinamik grafikler ve bildirim (Toast/Modal) bileşenlerini kurgulamıştır.
*(Dosyalar: frontend/index.html, frontend/dashboard.html, frontend/css/*.css, frontend/js/dashboard.js)*

**🛡️ Security & Integration Specialist - Eren Gider:**
Sistemin güvenlik altyapısını ve iletişim ağını kurar. V2'de **Gemini AI Asistanı**, AES/SHA-256 şifreleme sistemleri ve `ApiServer` entegrasyonlarını kodlamıştır. GitHub depo yönetimi, CI/CD süreçleri ve projenin genel entegrasyonundan sorumludur.
*(Dosyalar: AuthManager.java, ApiServer.java, GeminiClient.java, ConfigManager.java, AESUtil.java, frontend/js/api.js, pom.xml, README.md, docs/)*

---

## 📁 7. Genel Proje Dosya Şeması
```text
NYP-Proje-2026-Akilli-Kutup/
├── .gitignore
├── README.md
├── pom.xml
├── data/
│   ├── config.json             # Şifrelenmiş Yapılandırmalar
│   ├── materials.json          # Materyal Veritabanı
│   ├── users.json              # Kullanıcı Veritabanı
│   └── backup/                 # Otomatik JSON Yedekleri
├── docs/
│   ├── Proje_Raporu.md
│   └── UML_Sema.md
├── frontend/                   # Modern Web Arayüzü (V2)
│   ├── css/
│   │   ├── login.css
│   │   └── main.css            # Kurumsal Temalı Stil Dosyası
│   ├── js/
│   │   ├── api.js              # Fetch API Entegrasyonu
│   │   ├── auth.js             # İstemci Kimlik Doğrulaması
│   │   └── dashboard.js        # Dinamik DOM Etkileşimleri
│   ├── index.html              # Ana Gösterge Paneli
│   └── dashboard.html
└── src/
    ├── main/java/com/akillikutup/
    │   ├── auth/               # Güvenlik ve Yetkilendirme
    │   ├── core/               # OOP Çekirdek Sınıfları ve Şifreleme Araçları
    │   ├── db/                 # Dosya Tabanlı Veritabanı Motoru
    │   ├── gui/                # Swing Arayüzleri (Eski/Alternatif Sürüm)
    │   └── server/             # API Sunucusu ve AI Client
    └── test/java/com/akillikutup/
        ├── auth/
        ├── core/
        └── db/                 # Birim (Unit) Testleri
```

## 🚀 8. Kurulum ve Çalıştırma
1. **Gereksinimler:** Java 17+ ve Maven
2. **Derleme:** Proje dizininde `mvn clean install` komutunu çalıştırın.
3. **Başlatma:** Java backend'ini başlatmak için `mvn exec:java "-Dexec.mainClass=com.akillikutup.Main"` kullanın.
4. **Erişim:** Konsolda sunucu başlatıldı uyarısını gördükten sonra tarayıcınızdan `http://localhost:8080` adresine giderek modern web paneline erişebilirsiniz.

---
*Geliştiriciler tarafından 2026 Nesneye Yönelik Programlama dersi için titizlikle hazırlanmıştır.*
