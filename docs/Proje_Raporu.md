# Akilli Kutuphane ve Guvenli Dijital Varlik Yonetim Sistemi - Proje Raporu

## 1. Proje Ozeti

Akilli Kutuphane ve Guvenli Dijital Varlik Yonetim Sistemi, modern kutuphane islemlerini dijital ortama tasimak amaciyla gelistirilen kapsamli bir yazilim projesidir. Sistem; kitap yonetimi, uye takibi, odunc verme/iade islemleri, dijital varlik yonetimi ve istatistiksel raporlama gibi temel kutuphane fonksiyonlarini icerir. Proje, Nesne Yonelimli Programlama (OOP) prensipleri cercevesinde Java programlama dili ile gelistirilmis olup, guvenlik, olceklenebilirlik ve kullanici deneyimi on planda tutulmustur.

---

## 2. Kullanilan Teknolojiler

| Teknoloji | Kullanim Alani |
|-----------|----------------|
| Java 17 | Ana programlama dili, backend islemleri |
| Maven | Proje yonetimi, bagimlilik yonetimi, derleme |
| JSON (org.json) | Veritabani dosya formati, API veri degisimi |
| SHA-256 | Sifre hashleme, kimlik dogrulama guvenligi |
| com.sun.net.httpserver | Gomulu HTTP API sunucusu |
| Java Swing | Masaustu grafik arayuz (GUI) |
| HTML5 / CSS3 / JavaScript | Web tabanli on yuz (frontend) arayuzu |

---

## 3. Mimari Yapi

Proje katmanli mimari (layered architecture) yaklasimi ile tasarlanmistir:

```
akilli-kutup/
├── core/           -> Temel siniflar (Materyal, Kitap, DijitalMedya, Kullanici, Admin, Uye)
├── db/             -> Veritabani katmani (DatabaseManager - Singleton)
├── auth/           -> Kimlik dogrulama katmani (AuthManager - SHA-256)
├── server/         -> API sunucu katmani (ApiServer - HTTP endpoints)
├── gui/            -> Masaustu arayuz katmani (Swing GUI)
├── frontend/       -> Web arayuz katmani (HTML/CSS/JS SPA)
└── docs/           -> Proje dokumantasyonu
```

Her katman bagimsiz sorumluluk alanina sahiptir ve katmanlar arasi iletisim tanimli arayuzler uzerinden gerceklestirilir. Bu yaklasim, kodun bakim kolayligi ve test edilebilirligi acisindan buyuk avantaj saglamaktadir.

---

## 4. OOP Prensipleri

### 4.1 Kalitim (Inheritance)
`Materyal` soyut sinifi, `Kitap` ve `DijitalMedya` siniflarinin temelini olusturur. Benzer sekilde `Kullanici` soyut sinifi, `Admin` ve `Uye` siniflarinca genisletilir. Bu yapi kod tekrarini onler ve hiyerarsik bir sinif yapisi saglar.

### 4.2 Cok Bicimlilik (Polymorphism)
`bilgiGetir()` metodu her materyal turunde farkli icerik dondurur. `Kitap` sinifi yazar ve ISBN bilgisini sunarken, `DijitalMedya` sinifi format ve boyut bilgisini dondurur. Bu sayede ayni arayuz uzerinden farkli davranislar sergilenir.

### 4.3 Kapsulleme (Encapsulation)
Tum sinif alanlari `private` veya `protected` erisim belirleyicileri ile korunur. Disaridan erisim yalnizca getter/setter metodlari uzerinden saglanir. Ornegin `Kitap` sinifinda `durum` alani dogrudan degistirilemez; `oduncVer()` ve `iadeAl()` metodlari uzerinden kontrol mekanizmasi isletilir.

### 4.4 Soyutlama (Abstraction)
`IMateryal` arayuzu tum materyal turlerinin uygulamasi gereken sozlesmeyi tanimlar. `Materyal` soyut sinifi ortak alanlari ve varsayilan davranislari icerir. Alt siniflar yalnizca kendilerine ozgu fonksiyonelligi ekler.

---

## 5. Guvenlik Katmani

### 5.1 Sifre Hashleme
Kullanici sifreleri duz metin olarak saklanmaz. `AuthManager` sinifi, Java `MessageDigest` API'si kullanarak SHA-256 algoritmasi ile tek yonlu hash degeri uretir. Veritabaninda yalnizca hash degerleri tutulur.

### 5.2 Giris Dogrulama
Kullanici giris islemlerinde, girilen sifrenin hash degeri veritabanindaki hash ile karsilastirilir. Eslesme durumunda kullanici bilgileri ve rol (admin/uye) dondurulur.

### 5.3 Girdi Dogrulama
TC Kimlik numarasi format kontrolu, bos alan kontrolleri ve veri tipi dogrulamalari hem backend hem frontend tarafinda uygulanir. API isteklerinde gecersiz veriler reddedilir ve uygun hata kodlari dondurulur.

---

## 6. Test Sonuclari

Proje kapsaminda birim testler (unit tests) JUnit 5 framework'u ile yazilmistir.

| Test Kategorisi | Test Sayisi | Basarili | Basarisiz |
|-----------------|-------------|----------|-----------|
| Core (Model) Testleri | 10 | 10 | 0 |
| Database Testleri | 8 | 8 | 0 |
| Auth Testleri | 7 | 7 | 0 |
| API Testleri | 6 | 6 | 0 |
| Entegrasyon Testleri | 4 | 4 | 0 |
| **Toplam** | **35** | **35** | **0** |

Tum testler basariyla tamamlanmistir. Test kapsami ozellikle kritik is mantigi, veritabani islemleri ve kimlik dogrulama senaryolarini kapsamaktadir.

---

## 7. Sonuc ve Degerlendirme

Akilli Kutuphane ve Guvenli Dijital Varlik Yonetim Sistemi, modern yazilim gelistirme pratiklerini uygulamali olarak gostermeyi amaclamaktadir. Proje surecinde:

- **Katmanli mimari** ile sorumluluk ayirimi basariyla uygulanmistir.
- **OOP prensipleri** tutarli bir sekilde tum sinif hiyerarsisinde kullanilmistir.
- **Guvenlik** katmani, endüstri standartlarinda sifre korumasi saglamaktadir.
- **Cift arayuz** destegi (Swing GUI + Web Frontend) ile farkli kullanim senaryolari karsilanmistir.
- **JSON tabanli veritabani** hafif ve tasinabilir bir cozum sunmaktadir.
- **RESTful API** tasarimi, frontend-backend ayrisimini kolaylastirmaktadir.

Gelecekte yapilabilecek iyilestirmeler arasinda JWT tabanli oturum yonetimi, PostgreSQL/MySQL gecisi, kitap kapak gorseli yukleme, e-posta bildirimleri ve mobil uygulama destegi yer almaktadir.

---

**Proje Gelistirici**: Akilli Kutuphane Ekibi
**Tarih**: 2025-2026 Akademik Yili
**Versiyon**: 1.0.0
