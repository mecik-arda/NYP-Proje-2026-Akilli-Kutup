# NYP-Proje-2026-Akilli-Kutup — Uçtan Uca Kod Denetim Promptu

> Bu prompt, repoya gerçek erişimi olan bir kodlama ajanına (örn. Claude Code, Cursor, vb.) verilmek üzere tasarlanmıştır. Sohbet arayüzünde repo klonlanamayacağı için en iyi sonucu terminal/dosya erişimi olan bir araçta alırsın.

---

## ROL

Sen kıdemli bir yazılım denetçisisin (code auditor). Görevin, aşağıdaki Java/Maven projesini **uçtan uca** denetlemek, README'de iddia edilen özellikler ile gerçek kod davranışını **karşılaştırmak**, ve bulguları yapılandırılmış bir raporla sunmaktır. Varsayımda bulunma; her iddiayı kodu okuyarak doğrula. Doğrulayamadığın bir şeyi "doğrulanamadı" olarak işaretle, "muhtemelen doğru" diye geçme.

**Repo:** https://github.com/mecik-arda/NYP-Proje-2026-Akilli-Kutup

---

## 1. HAZIRLIK

1. Repoyu klonla: `git clone https://github.com/mecik-arda/NYP-Proje-2026-Akilli-Kutup.git`
2. Dosya ağacını çıkar, `pom.xml`'i oku (Java sürümü, bağımlılıklar, eklentiler).
3. `mvn clean compile` çalıştır → derleme hatalarını, uyarılarını kaydet.
4. `mvn test` çalıştır → her test sınıfının (CoreTest, DatabaseManagerTest, AuthManagerTest) sonucunu, geçen/kalan test sayısını ve varsa stack trace'leri raporla.

## 2. MİMARİ VE OOP DENETİMİ

`src/main/java/com/akillikutup/core/` altındaki sınıfları incele:

- **Kalıtım/Soyutlama:** `IMateryal` arayüzü ile `Materyal` abstract sınıfı arasındaki ilişki tutarlı mı? `Kitap` ve `DijitalMedya` gerçekten `Materyal`'i extend ediyor mu, yoksa kod tekrarı mı var?
- **Çok biçimlilik:** Ceza hesaplama / ödünç verme mantığı her alt sınıfta runtime'da farklı mı çalışıyor, yoksa tek bir if/else zinciriyle mi sahte polymorphism yapılmış?
- **Kapsülleme:** Kritik alanlar (`private`) doğru korunuyor mu? Gereksiz `public setter`'lar var mı?
- `Kullanici`, `Admin`, `Uye` sınıfları arası hiyerarşi mantıklı mı; rol ayrımı (least privilege) kod seviyesinde mi yoksa sadece UI seviyesinde mi uygulanıyor?

**Çıktı:** Her ilke için "✅ Doğrulandı / ⚠️ Kısmen / ❌ İddia ile uyuşmuyor" + ilgili dosya/satır referansı.

## 3. GÜVENLİK DENETİMİ (Kritik — README iddialarını tek tek doğrula)

README şu iddiaları içeriyor, her birini kodda ara ve doğrula:

| İddia | Doğrulanacak yer | Kontrol noktaları |
|---|---|---|
| Parolalar SHA-256 ile hashleniyor | `AuthManager.java` | Salt kullanılıyor mu? Tek başına SHA-256 (saltsız) modern standartlara göre zayıftır (bcrypt/Argon2/PBKDF2 yok) — bunu eleştir |
| Konfigürasyon AES-256 ile şifreleniyor | `AESUtil.java`, `ConfigManager.java` | Anahtar (key) nerede saklanıyor? Kod içine hardcoded mı? IV (initialization vector) doğru üretiliyor mu, sabit mi? |
| Girdi doğrulama / XSS / JSON Injection koruması | `ApiServer.java`, frontend `js/api.js` | Gerçekten sanitizasyon var mı yoksa sadece README'de mi yazıyor? |
| Path traversal koruması | `DatabaseManager.java` | Dosya yolları `data/` dizini dışına çıkabiliyor mu? `../` gibi girişler test edilmiş mi? |
| Rol bazlı yetkilendirme (Admin/Üye) | `ApiServer.java`, `AuthManager.java` | Yetki kontrolü backend'de mi yapılıyor, yoksa sadece frontend'de buton gizleme mi (gerçek bir güvenlik açığı olur)? |
| Gemini API key güvenliği | `GeminiClient.java`, `ConfigManager.java` | API key repoya commit edilmiş mi (`.gitignore` kontrolü)? `data/config.json` repoda mı, gerçek key içeriyor mu? |

Ayrıca genel olarak ara:
- Hardcoded credential/secret var mı (`grep -r "password\|secret\|key" --include=*.java`)
- `ApiServer`'da CORS, rate-limiting, oturum/token yönetimi var mı yoksa her istek güvensiz mi?
- Hata mesajları stack trace'i istemciye sızdırıyor mu (information disclosure)?

## 4. VERİ KALICILIĞI DENETİMİ

- `DatabaseManager.java`: JSON okuma/yazma işlemleri atomik mi (yarıda kesilen yazma veri bozulmasına yol açar mı)?
- Otomatik yedekleme (`data/backup/`) gerçekten her kritik işlemden önce mi tetikleniyor, yoksa sadece bazı yollarda mı?
- Eşzamanlılık (concurrency): Birden fazla istek aynı anda aynı JSON dosyasına yazarsa ne olur? Thread-safety/locking var mı?
- Exception handling: Bozuk JSON / dosya bulunamadı senaryosu gerçekten test edilmiş mi (testlerde simüle edilmiş mi)?

## 5. API VE AI ENTEGRASYONU

- `ApiServer.java`: Hangi endpoint'ler var, hangi HTTP metodları destekleniyor, response formatı tutarlı mı?
- `GeminiClient.java`: API çağrısı hata durumunda (timeout, kota aşımı, geçersiz key) sistemin geri kalanını çökertiyor mu? Fallback davranışı var mı?
- Frontend (`frontend/js/api.js`, `dashboard.js`): Backend ile haberleşme gerçekten asenkron ve hataya dayanıklı mı?

## 6. FRONTEND DENETİMİ

- `index.html`, `dashboard.html`, CSS/JS dosyalarında: erişilebilirlik (a11y), responsive tasarım, kullanılmayan/ölü kod.
- `auth.js`: token/oturum bilgisi nerede saklanıyor (localStorage XSS riski taşır — kontrol et)?

## 7. DOKÜMANTASYON-KOD UYUMU

- `docs/UML_Sema.md` gerçek sınıf yapısıyla uyuşuyor mu?
- README'deki "Geliştirme Durumu" bölümündeki "tamamlandı" işaretleri gerçek kod durumuyla örtüşüyor mu?
- Ekip görev dağılımındaki dosya listesi gerçek repo içeriğiyle eşleşiyor mu (eksik/yanlış dosya var mı)?

## 8. RAPOR FORMATI (çıktı bu şekilde olsun)

```
# Akıllı Kütüphane V2 — Denetim Raporu

## Yönetici Özeti
(3-5 cümle: genel durum, en kritik 2-3 bulgu)

## Derleme & Test Sonuçları
- mvn compile: ...
- mvn test: X/Y geçti, başarısızlıklar: ...

## Bulgular Tablosu
| # | Kategori | Önem (Kritik/Yüksek/Orta/Düşük) | Bulgu | Dosya:Satır | Öneri |
|---|---|---|---|---|---|

## Mimari/OOP Değerlendirmesi
## Güvenlik Değerlendirmesi (iddia vs gerçek)
## Veri Katmanı Değerlendirmesi
## API/AI Entegrasyon Değerlendirmesi
## Frontend Değerlendirmesi
## Dokümantasyon Uyumu

## Genel Risk Skoru (1-10) ve Gerekçesi
## Öncelikli Aksiyon Listesi (en kritik 5 madde)
```

## KISITLAR
- Gerçek olmayan API key/secret üretme veya kullanma.
- Test etmediğin bir şeyi "çalışıyor" diye işaretleme.
- README metnini olduğu gibi rapor olarak geri yazma — her iddiayı kodla çapraz doğrula.
- Bulamadığın/erişemediğin (örn. gerçek Gemini API key gerektiren canlı test) kısımları "test edilemedi, sebep: ..." diye açıkça belirt.
