# Akıllı Kütüphane V2 — Denetim Raporu

> Denetim Tarihi: 2026-06-25 | Denetçi: Kıdemli Kod Denetim Ajanı

---

## Yönetici Özeti

Proje genel olarak çalışan bir durumda (37/37 test başarılı, derleme hatasız) ancak **güvenlik, mimari ve dokümantasyon** alanlarında kritik bulgular mevcut. En acil sorun: JWT imzalama anahtarı kaynak kodda hardcoded (`JwtUtil.java:23`), bu sayede saldırgan geçerli admin token'ları üretebilir. İkinci kritik bulgu: 16+ `instanceof` zinciri proje geneline yayılmış, OOP'nin Açık/Kapalı prensibini ihlal ediyor. Üçüncü olarak: UML ve README dokümantasyonu mevcut V4.2 kod yapısıyla ciddi şekilde uyuşmuyor. Olumlu olarak: PBKDF2 şifre hashleme, AES-256/GCM şifreleme, path traversal koruması, CORS kısıtlaması ve .gitignore yapılandırması doğru uygulanmış.

---

## Derleme & Test Sonuçları

```
mvn clean compile: BUILD SUCCESS (69 Java dosyası, 7.5sn)
mvn test:          BUILD SUCCESS (37/37 geçti, 0 başarısız)

Test Detayı:
  AkilliKutupV4IntegrationTest:  4/4  ✅
  AuthManagerTest:                1/1  ✅
  CoreTest:                       4/4  ✅
  DatabaseManagerTest:           28/28 ✅
```

---

## Bulgular Tablosu

| # | Kategori | Önem | Bulgu | Dosya:Satır | Öneri |
|---|---|---|---|---|---|
| 1 | Güvenlik | **Kritik** | JWT secret hardcoded fallback | `JwtUtil.java:23` | `app.jwt.secret` zorunlu yap, default kaldır |
| 2 | Güvenlik | **Kritik** | PostgreSQL varsayılan şifresi "postgres" | `DataMigrator.java:21` | `PG_PASS` env var zorunlu yap |
| 3 | Güvenlik | **Yüksek** | AES anahtarı düz dosyada saklanıyor | `FileEncryptionService.java:45` | Parola türetme (KDF) veya HSM kullan |
| 4 | Güvenlik | **Yüksek** | API key hata mesajlarında sızıyor | `GeminiClient.java:67` | `response.body()` log'a yaz, kullanıcıya generic dön |
| 5 | Güvenlik | **Yüksek** | TC Kimlik No JWT payload'da (Base64) | `JwtUtil.java:39` | PII'ı JWT'den çıkar, gerekirse DB'den lookup yap |
| 6 | Mimari | **Yüksek** | 16+ instanceof zinciri (fake polymorphism) | `ApiServer.java:143`, `JsonParser.java:99`, vd. | `getTur()` polimorfik metodu ekle |
| 7 | Mimari | **Yüksek** | `@Data` tüm entity'lerde — invariant'ları by-pass eden public setter'lar | `Materyal.java:9`, `User.java:11`, `Kitap.java:10` | `@Getter` + package-private setter kullan |
| 8 | Mimari | **Orta** | User/Admin/Uye kalıtımı yok, enum ile rol | `User.java:15-17` | Roller için kalıtım veya composition-based yaklaşım |
| 9 | API | **Yüksek** | `DELETE /api/kitaplar/{id}` endpoint'i eksik | `BookController.java` | `@DeleteMapping` ekle |
| 10 | API | **Yüksek** | 4 endpoint stub (profil, sifre, bildirimler) | `BackupController.java:91-108` | Gerçek implementasyon yap veya kaldır |
| 11 | API | **Orta** | `BorrowController` hataları 200 dönüyor | `BorrowController.java:33,53` | HTTP 4xx/5xx kullan |
| 12 | Frontend | **Orta** | HTML'de çift DOCTYPE/html/head etiketi | `index.html:1-2,7-8` | Duplikasyonu temizle |
| 13 | Frontend | **Orta** | Login formunda label yok | `login.html` | `<label>` + `aria-required` ekle |
| 14 | Doküman | **Kritik** | UML V3/V4.0 mimarisini gösteriyor, şu anki kod V4.2 | `docs/UML_Sema.md` | UML'yi güncelle |
| 15 | Doküman | **Orta** | Versiyon tutarsızlığı: pom.xml `4.1.0`, README `V4.2` | `pom.xml:16`, `README.md` | Tek bir versiyon belirle |
| 16 | Doküman | **Orta** | `DataInitializer.java` README'de var ama kodda `DataSeeder.java` | `README.md`, `DataSeeder.java` | README'yi düzelt |
| 17 | Chat | **Yüksek** | Geçersiz Gemini model adı `"gemini-3.5-flash"` | `GeminiClient.java:16` | `"gemini-1.5-flash"` veya `"gemini-2.0-flash"` olarak düzelt |
| 18 | Güvenlik | **Düşük** | PBKDF2 iterasyon 210K — OWASP minimum 600K öneriyor | `AuthManager.java:29` | 600.000'e yükselt |

---

## Mimari/OOP Değerlendirmesi

| Prensip | Durum | Detay |
|---|---|---|
| Kalıtım/Soyutlama | ✅ Doğrulandı | `IMateryal` → `Materyal` → `Kitap`/`DijitalMedya`/`Klasor` hiyerarşisi doğru |
| Çok Biçimlilik (ceza) | ✅ Doğrulandı | `cezaHesapla()` her alt sınıfta farklı, sanal metot çağrısı çalışıyor |
| Çok Biçimlilik (ödünç) | ⚠️ Kısmen | `IOduncAlinabilir` var ama `BorrowService` sadece `KitapRepository` ile çalışıyor |
| Çok Biçimlilik (tür) | ❌ Uyuşmuyor | 16+ `instanceof` zinciri; `getTur()` metodu eklenmeli |
| Kapsülleme | ❌ Uyuşmuyor | `@Data` tüm alanlara public setter üretiyor; `stokAdedi` protected |
| Kullanıcı Hiyerarşisi | ❌ Uyuşmuyor | Admin/Uye sınıf kalıtımı yok; enum ile rol ayrımı yapılıyor |

---

## Güvenlik Değerlendirmesi (İddia vs Gerçek)

| README İddiası | Gerçek Durum | Dosya:Satır |
|---|---|---|
| Parolalar SHA-256 ile hashleniyor | ⚠️ **Kısmen**: PBKDF2WithHmacSHA256 kullanılıyor (salt'lı, 210K iterasyon). Spring tarafında BCrypt var. SHA-256 iddiası eksik. | `AuthManager.java:36`, `SecurityConfig.java:33` |
| Konfigürasyon AES-256 ile şifreleniyor | ✅ **Doğrulandı**: AES-256/GCM/NoPadding, rastgele IV, 128-bit tag. Ancak anahtar düz dosyada. | `FileEncryptionService.java:57-66` |
| Girdi doğrulama / XSS koruması | ⚠️ **Kısmen**: Temel null check'ler var; kapsamlı sanitizasyon yok. Resim upload'ta uzantı/boyut kontrolü var. | `ApiServer.java:192-205` |
| Path traversal koruması | ✅ **Doğrulandı**: CanonicalPath karşılaştırması ile `../` saldırıları engellenmiş. | `ApiServer.java:959-961` |
| Rol bazlı yetkilendirme | ✅ **Doğrulandı**: Hem backend (`@PreAuthorize`) hem de eski `ApiServer.verifyAuth()` tarafında yetki kontrolü var. | `UserController.java:25`, `ApiServer.java:165` |
| Gemini API key güvenliği | ⚠️ **Kısmen**: Şifreli saklanıyor, `.gitignore`'da. Ancak hata mesajlarında API yanıtı kullanıcıya dönebiliyor. | `GeminiClient.java:67`, `ConfigManager.java:40-43` |

**Ek bulgular:**
- Hardcoded secret: JWT anahtarı (`JwtUtil.java:23`), PostgreSQL şifresi (`DataMigrator.java:21`)
- Çift auth sistemi: Eski `ApiServer` + yeni Spring Security paralel çalışıyor
- TC Kimlik No JWT payload'da Base64 ile taşınıyor (PII sızıntısı)

---

## Veri Katmanı Değerlendirmesi

| Kriter | Durum | Detay |
|---|---|---|
| Atomik JSON okuma/yazma | ⚠️ Kısmen | `DatabaseManager` eski sistemde senkronizasyon var ancak thread-safe garantisi zayıf. Spring Data JPA tarafı transaction-safe. |
| Otomatik yedekleme | ✅ Doğrulandı | `data/backup/` altında kritik işlemlerden önce yedek alınıyor. |
| Eşzamanlılık | ⚠️ Kısmen | Spring Data JPA ACID transaction'lar sağlıyor. Eski `ApiServer` `java.util.ArrayList` ile thread-safe değil. |
| Exception handling | ✅ Doğrulandı | Bozuk JSON/dosya bulunamadı senaryoları `DatabaseManagerTest`'te test edilmiş (28 test). |

---

## API/AI Entegrasyon Değerlendirmesi

| Kriter | Durum | Detay |
|---|---|---|
| Endpoint sayısı | ⚠️ | README 23 iddia ediyor, gerçekte 34+ endpoint var. 4 stub. `DELETE /api/kitaplar/{id}` eksik. |
| Response formatı | ⚠️ | Çoğu `{"basarili": true/false}` ama `BorrowController` HTTP 200 dönüp içerikte hata veriyor. |
| GeminiClient hata yönetimi | ⚠️ | Model fallback zinciri var ama ilk model adı geçersiz (`gemini-3.5-flash`). Timeout/kota aşımında generic `NETWORK_ERROR`. |
| Frontend asenkron | ✅ | `api.js` async/await kullanıyor, `SERVER_UNREACHABLE` için özel hata tipi var. |

---

## Frontend Değerlendirmesi

| Kriter | Durum | Detay |
|---|---|---|
| Token depolama | ⚠️ | `sessionStorage` kullanılıyor (localStorage'dan iyi) ancak httpOnly cookie değil, XSS'e açık. |
| Responsive tasarım | ⚠️ | Viewport meta var, mobil menü var ancak bazı tablolar taşabilir. |
| Erişilebilirlik (a11y) | ❌ | Login formda label yok; modal'larda aria-labelledby/role eksik; HTML'de duplike DOCTYPE. |
| CDN güvenliği | ⚠️ | unpkg.com, cdnjs.cloudflare.com, fonts.googleapis.com — supply-chain riski. |
| Ölü kod | ⚠️ | `dashboard.html` sadece redirect. Eski `charts_temp.js`, `dummy.js` modülleri var. |

---

## Dokümantasyon Uyumu

| Doküman | Durum | Detay |
|---|---|---|
| `docs/UML_Sema.md` | ❌ | V3/V4.0 mimarisini yansıtıyor. Spring Boot, JPA, JWT, SSE, TOTP yok. Kullanici soyut sınıf değil. |
| README "Geliştirme Durumu" | ⚠️ | Versiyon tutarsızlığı (4.1.0 vs V4.2). "DataInitializer" ismi yanlış. "Jackson @RequestBody" iddiası çoğu controller için geçersiz. |
| README dosya ağacı | ⚠️ | `WebConfig.java`, `ActiveUserController.java`, `SseService.java` eksik. |
| .gitignore | ✅ | Kapsamlı — tüm secret'lar, build artifact'ları, IDE dosyaları korunuyor. |

---

## Genel Risk Skoru

### 7 / 10

**Gerekçe:** Proje derleniyor ve tüm testler geçiyor (temel işlevsellik sağlam). Ancak JWT secret hardcoding (**kritik**), yaygın instanceof kullanımı (**yüksek**), eksik API endpoint'leri (**yüksek**), ve güncel olmayan UML dokümantasyonu (**kritik**) skoru yükseltiyor. AES/GCM şifreleme, PBKDF2 hashleme, path traversal koruması ve .gitignore hijyeni gibi olumlu uygulamalar skoru düşürüyor.

---

## Öncelikli Aksiyon Listesi

1. **JWT secret'ı değiştir** — `JwtUtil.java:23`'teki hardcoded fallback'i kaldır, `app.jwt.secret`'i application.yml'da zorunlu yap, başlangıçta default değerle çalışmayı reddet.

2. **instanceof zincirlerini temizle** — `IMateryal`/`Materyal`'e `getTur()` metodu ekle; `ApiServer.java`, `JsonParser.java`, `DataMigrator.java`, `AdminPanel.java`, `UserPanel.java`'daki tüm `instanceof` kontrollerini değiştir.

3. **Eksik API endpoint'lerini tamamla** — `DELETE /api/kitaplar/{id}` için `BookController`'a `@DeleteMapping` ekle. 4 stub endpoint'i (profil, sifre, bildirimler) ya implemente et ya da kaldır.

4. **UML ve README'yi güncelle** — `docs/UML_Sema.md`'yi mevcut Spring Boot + JPA + JWT mimarisine uygun şekilde yeniden çiz. README'deki versiyon tutarsızlığını düzelt.

5. **`@Data` kullanımını sınırla** — Entity sınıflarında `@Data` yerine `@Getter` + gerekli minimal setter'ları kullan. Özellikle `User.setRol()`, `User.setSifre()`, `Materyal.setId()` public olmamalı.

---

*Bu rapor, `akilli-kutup-denetim-prompt.md` talimatlarına uygun olarak, kodun birebir okunması ve çapraz doğrulama yöntemiyle hazırlanmıştır.*
