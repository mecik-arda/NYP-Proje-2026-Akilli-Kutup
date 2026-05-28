# UML Sinif Diyagrami

Akilli Kutuphane ve Guvenli Dijital Varlik Yonetim Sistemi icin sinif diyagrami.

```mermaid
classDiagram
    direction TB

    class IMateryal {
        <<interface>>
        +getId() String
        +getBaslik() String
        +getTur() String
        +bilgiGetir() String
    }

    class Materyal {
        <<abstract>>
        #id : String
        #baslik : String
        #tur : String
        #eklemeTarihi : String
        +Materyal(id, baslik, tur)
        +getId() String
        +getBaslik() String
        +getTur() String
        +bilgiGetir() String*
        +toJson() JSONObject
    }

    class Kitap {
        -yazar : String
        -isbn : String
        -sayfaSayisi : int
        -kategori : String
        -durum : String
        -oduncAlan : String
        +Kitap(id, baslik, yazar, isbn, sayfaSayisi, kategori)
        +getYazar() String
        +getIsbn() String
        +getDurum() String
        +setDurum(durum) void
        +oduncVer(kullaniciId) void
        +iadeAl() void
        +bilgiGetir() String
        +toJson() JSONObject
    }

    class DijitalMedya {
        -format : String
        -boyutMB : double
        -url : String
        +DijitalMedya(id, baslik, format, boyutMB, url)
        +getFormat() String
        +getBoyutMB() double
        +getUrl() String
        +bilgiGetir() String
        +toJson() JSONObject
    }

    class Kullanici {
        <<abstract>>
        #id : String
        #ad : String
        #soyad : String
        #tcKimlikNo : String
        #sifreHash : String
        #rol : String
        +Kullanici(id, ad, soyad, tcKimlikNo, sifreHash, rol)
        +getId() String
        +getAd() String
        +getSoyad() String
        +getTcKimlikNo() String
        +getSifreHash() String
        +getRol() String
        +toJson() JSONObject
    }

    class Admin {
        +Admin(id, ad, soyad, tcKimlikNo, sifreHash)
        +kitapEkle(kitap) void
        +kitapSil(kitapId) void
        +kullaniciYonet() void
        +istatistikGoruntule() void
    }

    class Uye {
        -oduncListesi : List~String~
        -maxOdunc : int
        +Uye(id, ad, soyad, tcKimlikNo, sifreHash)
        +kitapOduncAl(kitapId) boolean
        +kitapIadeEt(kitapId) boolean
        +getOduncListesi() List~String~
        +toJson() JSONObject
    }

    class DatabaseManager {
        -instance : DatabaseManager$
        -veritabaniYolu : String
        -kitaplar : List~Kitap~
        -kullanicilar : List~Kullanici~
        -DatabaseManager()
        +getInstance() DatabaseManager$
        +kitapEkle(kitap) void
        +kitapSil(kitapId) boolean
        +kitapBul(kitapId) Kitap
        +kitapAra(sorgu) List~Kitap~
        +tumKitaplar() List~Kitap~
        +kullaniciEkle(kullanici) void
        +kullaniciBul(tcKimlikNo) Kullanici
        +tumKullanicilar() List~Kullanici~
        +kaydet() void
        +yukle() void
    }

    class AuthManager {
        -dbManager : DatabaseManager
        +AuthManager(dbManager)
        +girisYap(tcKimlikNo, sifreHash) Kullanici
        +hashSifre(sifre) String
        +dogrula(tcKimlikNo, sifreHash) boolean
    }

    class ApiServer {
        -port : int
        -server : HttpServer
        -dbManager : DatabaseManager
        -authManager : AuthManager
        +ApiServer(port)
        +baslat() void
        +durdur() void
        -handleKitaplar(exchange) void
        -handleKullanicilar(exchange) void
        -handleGiris(exchange) void
        -handleOdunc(exchange) void
        -handleIade(exchange) void
        -handleIstatistikler(exchange) void
        -sendResponse(exchange, code, body) void
    }

    IMateryal <|.. Materyal
    Materyal <|-- Kitap
    Materyal <|-- DijitalMedya
    Kullanici <|-- Admin
    Kullanici <|-- Uye
    DatabaseManager --> Kitap : yonetir
    DatabaseManager --> Kullanici : yonetir
    AuthManager --> DatabaseManager : kullanir
    ApiServer --> DatabaseManager : kullanir
    ApiServer --> AuthManager : kullanir
    Uye --> Kitap : odunc alir
    Admin --> Kitap : ekler/siler
```

## Iliskiler

| Iliski | Aciklama |
|--------|----------|
| `IMateryal <\|.. Materyal` | Materyal sinifi IMateryal arayuzunu uygular |
| `Materyal <\|-- Kitap` | Kitap sinifi Materyal soyut sinifini genisletir |
| `Materyal <\|-- DijitalMedya` | DijitalMedya sinifi Materyal soyut sinifini genisletir |
| `Kullanici <\|-- Admin` | Admin sinifi Kullanici soyut sinifini genisletir |
| `Kullanici <\|-- Uye` | Uye sinifi Kullanici soyut sinifini genisletir |
| `DatabaseManager --> Kitap` | DatabaseManager kitaplari yonetir (composition) |
| `DatabaseManager --> Kullanici` | DatabaseManager kullanicilari yonetir (composition) |
| `AuthManager --> DatabaseManager` | AuthManager veritabani erisimi icin DatabaseManager kullanir |
| `ApiServer --> DatabaseManager` | ApiServer veri islemleri icin DatabaseManager kullanir |
| `ApiServer --> AuthManager` | ApiServer kimlik dogrulama icin AuthManager kullanir |

## Tasarim Desenleri

- **Singleton**: `DatabaseManager` tek ornek olarak calisir
- **Template Method**: `Materyal` soyut sinifinda `bilgiGetir()` metodu
- **Strategy**: Farkli materyal turleri icin farkli `bilgiGetir()` uygulamalari
