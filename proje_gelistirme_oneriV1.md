# AI Geliştirici Talimatları: Akıllı Kütüphane V4 (System Prompt & Execution Guide)

**DİKKAT (YAPAY ZEKA ASİSTANINA):** Bu doküman, "Akıllı Kütüphane V3" projesini "V4 Kurumsal" sürümüne taşırken senin harfiyen uygulaman gereken bir yol haritası ve sistem talimatnamesidir. Herhangi bir adımı atlaman, veri kaybına yol açman veya kodu bozman kesinlikle yasaktır. 

---

## 🛑 ADIM 0: GÜVENLİK YEDEĞİ (KRİTİK)
Koda veya dosyalara dokunmadan önce, tüm projenin güvenli bir yedeğini oluşturmalısın.
1. Projenin ana klasörünü (`C:\Users\ardam\Desktop\Akilli-kutup-v2`), `C:\Users\ardam\Desktop\Akilli-kutup-v2 yedekV2` adı altında kopyala.
   - Önerilen komut (PowerShell): `Copy-Item -Path "C:\Users\ardam\Desktop\Akilli-kutup-v2" -Destination "C:\Users\ardam\Desktop\Akilli-kutup-v2 yedekV2" -Recurse -Force`
2. **Doğrulama:** Yedek klasörünün oluştuğundan ve içinin dolu olduğundan emin olmadan Asla `Adım 1`'e geçme.

---

## 🛠️ ADIM 1: VERİTABANI GÖÇÜ (PostgreSQL)
Mevcut sistem kendi geliştirdiğimiz "File-Based Database Engine" (JSON) kullanıyor. Bunu profesyonel SQL tabanlı yapıya geçirmelisin.
1. `pom.xml` içerisine `postgresql`, `hibernate-core` ve `spring-data-jpa` (Eğer Spring geçişi yapıyorsan) bağımlılıklarını ekle.
2. `Kullanici`, `Kitap`, `DijitalMedya` nesnelerini `@Entity`, `@Table`, `@Column` kullanarak ORM tabanlı tablolara çevir.
3. **Migration Script (ÇOK ÖNEMLİ):** `data/users.json` ve `data/materials.json` içerisindeki mevcut verileri okuyup yeni PostgreSQL veritabanına kayıpsız aktaracak tek seferlik bir Java modülü yaz (`DataMigrator.java`). 
4. **Doğrulama:** `mvn clean test` yap ve sistemin yeni veritabanına bağlanıp bağlanamadığını, eski verilerin durup durmadığını `SystemTester.java` ile E2E (Uçtan Uca) test et.

---

## 🏗️ ADIM 2: MİMARİ DÖNÜŞÜM (Spring Boot Entegrasyonu)
Saf Java ile yazılan `ApiServer.java` ve özel `DatabaseManager` yapılarını Spring Boot altyapısına adapte et.
1. Maven bağımlılıklarını (`spring-boot-starter-web`, `spring-boot-starter-security` vb.) ekle.
2. Sınıfları Spring Core yapısına geçir (`@Service`, `@Repository`, `@RestController`, `@Component`).
3. Kendi yazdığımız özel kimlik doğrulama filtrelerini ve oturum yönetimini, JWT (JSON Web Token) ve `Spring Security` filtrelerine dönüştür.
4. **Gerileme Testi (Regression):** Uygulamanın frontend'i (html, css, js) eski API uç noktaları ile haberleşiyor. Spring MVC controller'larını (örn: `@GetMapping("/api/kullanicilar")`) aynen koruyarak Frontend'i BOZMADAN bağla.
5. **Doğrulama:** API'lerin tamamına HTTP istekleri atıp 200 OK yanıtı aldığını teyit et.

---

## 🛡️ ADIM 3: İLERİ DÜZEY GÜVENLİK (Defense-in-Depth)
1. **Redis ile Rate Limiting:** `AuthManager` içerisindeki `ConcurrentHashMap` tabanlı yerel brute-force korumasını, sistemin ölçeklenebilir olması için Redis tabanlı bir rate limiter algoritmasıyla değiştir.
2. **2FA Entegrasyonu:** Admin hesapları (`Admin.java`) için giriş rotasına TOTP (Time-Based One Time Password) doğrulama mekanizması kur. Kullanıcı veritabanında Adminler için `secretKey` kolonu oluştur.
3. **Doğrulama:** Rate limit'in 5 başarısız denemede çalıştığını ve 2FA kodsuz giriş yapılamadığını test ile kanıtla.

---

## 🤖 ADIM 4: GELİŞMİŞ YAPAY ZEKA MODÜLLERİ
Mevcut `GeminiClient.java` sınıfını genişlet.
1. **RAG (Retrieval-Augmented Generation):** Kitap özetlerini veya PDF dokümanlarını vektörel bir veritabanına (ChromaDB veya Pinecone) gömecek (embedding) yeni bir metod ekle. Kullanıcıların sohbet modülünde doğrudan kitap içeriklerine yönelik sorular sormasını sağla.
2. **Vision AI:** Kamera destekli barkod tarayıcı sayfasındaki yapıya, "Kitap Kapağı Tarama" butonu ekle. Yüklenen fotoğrafı Gemini Vision API'sine iletip, kitabın detaylarını otomatik getiren servisi yaz.
3. **Doğrulama:** API çağrılarında `x-goog-api-key` kullanımının ve fallback (yedek model) sistemlerinin hala çalıştığından emin ol.

---

## 🐳 ADIM 5: KONTEYNERLEŞTİRME VE CI/CD
1. Proje kök dizinine çok katmanlı (multi-stage) bir `Dockerfile` yaz (Java 17/21 temelli).
2. `docker-compose.yml` oluştur: Bu compose dosyası PostgreSQL servisini, Redis servisini ve Backend/Frontend uygulamasını tek tuşla (`docker-compose up`) ayağa kaldıracak şekilde kurgulanmalı.
3. `.github/workflows/ci.yml` oluştur ve her push işleminde `mvn clean test` koşturulacak bir CI hattı tanımla.
4. **Doğrulama:** `docker-compose up -d` komutu çalıştırıldığında veritabanı, redis ve web sunucusu sorunsuz başlatılmalıdır.

---

## 📌 YAPAY ZEKA İÇİN KESİN ÇALIŞMA KURALLARI (DO NOT IGNORE)
- **TEK TEK GİT:** Asla Adım 1'i tamamen bitirip, test edip (mvn test) doğrulamadan Adım 2'ye geçme!
- **KODU SİLME:** Kullanılan, halihazırda çalışan hiçbir metodu veya veriyi "refactoring" bahanesiyle yok etme. Taşıyorsan yerine eşdeğer/daha iyi alternatifini eksiksiz bırak.
- **HER AŞAMADA TEST ET:** Değişiklik yaptığın her dosya sonrasında konsolda Unit Test veya Integration Test çalıştır. Başarısız test varsa ilerleme, dur ve düzelt!
- **VERİ BÜTÜNLÜĞÜ:** En kritik kural budur. Kullanıcı verileri, ödünç geçmişi ve şifrelenmiş AES/SHA verileri hiçbir mimari dönüşümde silinmemelidir. Ekrana log basarak eski verilerin korunduğunu kanıtla.
