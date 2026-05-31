# Akıllı Kütüphane ve Güvenli Dijital Varlık Yönetim Sistemi V2 - Teknik Proje Raporu

## 1. Proje Özeti
Bu proje, geleneksel kütüphane otomasyonlarını modern web teknolojileri, Yapay Zeka (AI) ve derinlemesine savunma (Defense-in-Depth) prensipleriyle birleştiren, Java tabanlı bir dijital varlık yönetim sistemidir. Sistem; fiziksel kitaplar, dijital medyalar ve süreli yayınlar gibi farklı materyalleri Nesneye Yönelik Programlama (OOP) standartlarıyla tek bir merkezden yönetir. Klasik ödev projelerinden farklı olarak, Java bir web sunucusu (Backend) gibi konumlandırılmış ve istemci (Frontend) ile haberleşmesi güvenli bir REST mimarisi üzerine inşa edilmiştir. V2 sürümü ile birlikte Google Gemini 1.5 altyapısı entegre edilerek yapay zeka destekli akıllı asistan yetenekleri kazandırılmıştır.

## 2. Çekirdek Mimari ve OOP Uygulamaları
Projenin temel iskeleti, yazılım mühendisliği standartlarına uygun olarak tasarlanmıştır:
*   Kalıtım (Inheritance) ve Soyutlama (Abstraction): IMateryal arayüzü ve Materyal soyut sınıfı üzerinden Kitap ve DijitalMedya gibi alt sınıflar türetilerek, genişletilebilir (Scalable) bir yapı kurulmuştur.
*   Çok Biçimlilik (Polymorphism): Her materyal türünün ceza hesaplama veya ödünç verilme mantığı çalışma zamanında (Runtime) dinamik olarak belirlenir.
*   Kapsülleme (Encapsulation): Kritik iş mantığı, ceza puanları ve sistemin iç durumu dış müdahalelere kapatılarak nesne bütünlüğü korunmuştur.

## 3. Siber Güvenlik Katmanı (Cybersecurity Framework)
Proje, kullanıcı verilerini ve sunucu bütünlüğünü korumak amacıyla gelişmiş güvenlik mekanizmaları içerir:
*   Kriptografik Şifreleme (Hashing & Salting): Kullanıcı parolaları veritabanında kesinlikle açık metin (plaintext) olarak saklanmaz. Parolalar, güvenlik standartlarına uygun olarak hash algoritmaları (SHA-256) kullanılarak şifrelenir. Ayrıca hassas konfigürasyon verileri AES-256 algoritmasıyla uçtan uca korunur.
*   Kimlik Doğrulama ve Yetkilendirme (Auth & Authorization): Frontend ile Backend arasındaki API iletişiminde yetkisiz erişimleri engellemek için güvenlik mekanizmaları devrededir. Sistemde En Az Ayrıcalık (Least Privilege) prensibi uygulanır; sıradan bir Uye sadece okuma yapabilirken, CRUD operasyonlarını yalnızca Admin yetkisine sahip kullanıcılar gerçekleştirebilir.
*   Girdi Denetimi ve Sanitizasyon (Input Validation): İstemciden (Web arayüzünden) gelen her türlü veri, Backend tarafında işlenmeden önce süzgeçten geçirilir. Bu sayede JSON Injection ve XSS gibi saldırı vektörleri engellenir.
*   Dosya Yolu Güvenliği (Path Traversal Protection): Sistem, yerel JSON dosyalarını kullandığından dışarıdan manipüle edilmiş dosya yolu isteklerine karşı sıkı bir dizin denetimi uygular. Dosya okuma/yazma işlemleri data/ klasörü dışına çıkamaz.

## 4. Veri Kalıcılığı ve Hata Yönetimi (Database & Persistence)
*   Sistem, SQL kullanmak yerine kendi özel Dosya Tabanlı Veritabanı Motorunu (File-Based Database Engine) Java ile sıfırdan yönetir.
*   Veriler JSON formatında serialize/deserialize edilerek saklanır.
*   Gelişmiş Hata Yakalama (Exception Handling) blokları sayesinde; dosya bulunamaması veya JSON formatının bozulması gibi durumlarda sistem çökmez. Ayrıca her kritik işlemden önce zaman damgalı otomatik JSON yedekleri oluşturularak veri kaybı tamamen önlenir.

## 5. Modern Web Arayüzü ve AI Entegrasyonu (Frontend Integration)
*   Java backend'i, gömülü bir HTTP sunucusu modülü (ApiServer) barındırarak ağ üzerinden gelen istekleri dinler.
*   Kullanıcılar sisteme kurumsal tasarıma sahip, karanlık tema destekli, asenkron (Fetch API) ve kullanıcı dostu bir web paneli üzerinden erişim sağlar.
*   Sisteme entegre edilen GeminiClient modülü sayesinde kullanıcılara okuma geçmişlerine uygun materyal önerileri, akıllı asistan hizmetleri ve katalog analizleri sağlanır.

## 6. Sistemin Temel Özellikleri (Features)
Projeye V2 sürümü ile birlikte kazandırılan ve sistemin temelini oluşturan tüm fonksiyonel yetenekler aşağıda detaylandırılmıştır:
*   Kapsamlı Materyal Yönetimi: Admin kullanıcıları sisteme Kitap veya Dijital Medya gibi farklı özelliklere sahip materyaller ekleyebilir, güncelleyebilir, stok durumlarını kontrol edebilir ve silebilir.
*   Rol Tabanlı Gelişmiş Kimlik Doğrulama: Admin ve standart Üye rolleri birbirinden ayrılmıştır. SHA-256 ile şifrelenen hesaplara giriş yapıldığında, yetkilendirmeye göre sadece ilgili butonlar ve işlemler (CRUD) açılır.
*   Ödünç Alma ve İade Süreçleri: Kullanıcılar kütüphane materyallerini ödünç alabilir. Ödünç alma limitleri (Admin için sınırsız, Üye için belirli limitler) nesneye yönelik programlama kurallarıyla dinamik olarak yönetilir.
*   Dinamik Ceza Puanı ve Kredi Sistemi: Zamanında iade edilmeyen materyaller için sisteme entegre algoritma sayesinde dinamik ceza puanı hesaplanır. Kredisi eksilere düşen üyeler otomatik olarak kısıtlanır.
*   Google Gemini Yapay Zeka Desteği: Sisteme AES-256 ile şifrelenerek entegre edilen Gemini 1.5 altyapısı sayesinde; kullanıcılara akıllı kitap/medya önerileri yapılır ve genel okuma alışkanlıkları analiz edilir.
*   Finansal Analiz ve Otomatik PDF Raporlama: Sistemin finansal durumu, kesilen cezalar ve genel istatistikler admin paneli üzerinden anlık olarak PDF formatında raporlanabilir.
*   Simüle Edilmiş Barkod/Hızlı Tarama Sistemi: Ön yüz (Dashboard) üzerinde yer alan hızlı işlemler menüsü ile kütüphane barkod sistemi simüle edilerek tek tıkla en çok okunanlara erişim imkanı tanınır.
*   Asenkron Çalışan Arka Plan Sunucusu: Java tabanlı ApiServer sayesinde tüm arayüz (Frontend) işlemleri sayfayı yenilemeden arka planda hızlı ve güvenli bir şekilde sunucu ile haberleşir.
*   Felaket Kurtarma ve Otomatik Yedekleme: Sistemde yapılan her kritik okuma/yazma işlemi öncesinde DatabaseManager modülü tüm veritabanı (JSON) dosyalarının tam yedeğini alır.

## 7. Ekip Görev Dağılımı
Backend & Core Architect - Ahmet Güler:
Projenin nesneye yönelik tasarım hiyerarşisini ve iş mantığını kurgular. Sistemdeki tüm nesnelerin atası olan Abstract sınıfları ve ortak davranışları belirleyen Interface yapılarını tasarlar. Kalıtım mekanizması ile materyal çeşitliliğini yönetirken; kredi puanı hesaplama, dinamik ceza sistemi ve stok kontrolü gibi çekirdek algoritmaları kodlar. Ayrıca, sınıflar arası ilişkilerin sağlam bir mimaride yürümesini sağlayarak projenin genişletilebilir olmasını garanti altına alır.

Database & Data Persistence Manager / Penetration Tester - Arda Meçik:
Sistemin veri kalıcılığı katmanını tasarlar ve yönetir. Verileri SQL yerine Java kullanarak dosya tabanlı bir yapıda saklayacak olan Database Engine mekanizmasını kurar. Nesnelerin diske yazılması ve açılışta tekrar belleğe yüklenmesi süreçlerini yürütür. Ayrıca, dosya okuma/yazma sırasında oluşabilecek tüm senaryolar için Hata Yönetimi mimarisini, otomatik yedekleme süreçlerini kurar. Projenin canlıya alınma durumunda host penetrasyon işlemini yapar.

UI/UX Developer - Göktuğ Berke Kuzucu:
Sistemin kullanıcı ile temas eden tüm görsel arayüzlerini ve etkileşim senaryolarını tasarlar. Web teknolojilerini kullanarak karmaşık kütüphane işlemlerini son kullanıcı için basit bir deneyime dönüştürür. Kurumsal koyu tema, görsel hiyerarşi, renk paleti ve tipografi seçimleriyle kullanıcı deneyimini iyileştirirken; Backend'den gelen verileri dinamik grafikler, tablolar ve uyarı pencereleriyle görselleştirir.

Security & Integration Specialist - Eren Gider:
Sistemin güvenlik altyapısını ve iletişim ağını kurar. AES-256 ve SHA-256 şifreleme sistemleri üzerinden güvenli veri ve konfigürasyon depolama mimarilerini kurar. ApiServer ile Java backend - web frontend haberleşmesini sağlar. V2 güncellemesi ile Gemini AI entegrasyonunu gerçekleştirerek sisteme yapay zeka özelliklerini kazandırır. Kapsamlı README dokümantasyonunu yönetir.

## 8. Geliştirme Metodolojisi ve Süreç Yönetimi
Proje, "Çevik Yazılım Geliştirme" (Agile) prensiplerine uygun olarak ve iteratif bir yaklaşımla hayata geçirilmiştir. Modern yazılım mühendisliği süreçlerinde verimliliği ve kod kalitesini artırmak amacıyla yeni nesil yapay zeka (AI) destekli kodlama asistanlarından endüstri standartlarına uygun şekilde faydalanılmıştır.
*   AI Destekli Hızlı Prototipleme: Tasarım aşamasında (özellikle HTML/CSS arayüz iskeletlerinin oluşturulmasında ve standart JavaScript DOM işlemlerinde) AI asistan eklentileri bir geliştirme yardımcısı (pair-programmer) olarak kullanılarak prototipleme süresi kısaltılmıştır. Bu yaklaşım, sistemin çekirdek Java algoritmalarına ve karmaşık güvenlik mimarisine odaklanılması için önemli bir zaman tasarrufu sağlamıştır.
*   Manuel Kod İnceleme (Code-Review): AI araçları tarafından sunulan kod kalıpları körü körüne projeye dahil edilmemiş; tüm kod blokları nesne yönelimli programlama (OOP) prensipleri, veritabanı kararlılığı ve derinlemesine savunma stratejileri bizzat geliştirici tarafından gözetilerek titiz bir manuel denetimden (code-review) geçirildikten sonra ana mimariye entegre edilmiştir.
