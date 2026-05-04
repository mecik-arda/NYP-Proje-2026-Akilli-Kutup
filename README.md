# NYP-Proje-2026-Akilli-Kutup
Akıllı Kütüphane ve Dijital Varlık Yönetim Sistemi

EKİP GÖREV DAĞILIMI

Backend & Core Architect - Ahmet Güler:
Projenin nesneye yönelik tasarım hiyerarşisini ve iş mantığını (Business Logic) kurgular. Sistemdeki tüm nesnelerin atası olan Abstract (Soyut) sınıfları ve ortak davranışları belirleyen Interface (Arayüz) yapılarını tasarlar. Kalıtım (Inheritance) mekanizması ile materyal çeşitliliğini yönetirken; kredi puanı hesaplama, dinamik ceza sistemi ve stok kontrolü gibi çekirdek algoritmaları kodlar. Ayrıca, sınıflar arası ilişkilerin (Composition/Aggregation) sağlam bir mimaride yürümesini sağlayarak projenin genişletilebilir olmasını garanti altına alır.

Database & Data Persistence Manager - Arda Meçik: Sistemin veri kalıcılığı katmanını tasarlar ve yönetir. Verileri SQL yerine C++ veya Java kullanarak dosya tabanlı (JSON/TXT/Binary) bir yapıda saklayacak olan "Database Engine" mekanizmasını kurar. Nesnelerin diske yazılması (Save) ve açılışta tekrar belleğe yüklenmesi (Load) süreçlerini yürütür. Ayrıca, dosya okuma/yazma sırasında oluşabilecek tüm senaryolar için Hata Yönetimi (Exception Handling) mimarisini ve veritabanı güvenliğini (Backup/Sync) sağlar.

UI/UX Developer - Göktuğ Berke Kuzucu:
Sistemin kullanıcı ile temas eden tüm görsel arayüzlerini ve etkileşim senaryolarını tasarlar. JavaFX, Qt veya Swing gibi teknolojileri kullanarak, karmaşık kütüphane işlemlerini (materyal arama, ödünç alma, kullanıcı kaydı) son kullanıcı için basit ve sezgisel bir deneyime dönüştürür. Görsel hiyerarşi, renk paleti ve tipografi seçimleriyle kullanıcı deneyimini (UX) iyileştirirken; Backend'den gelen verileri dinamik grafikler, tablolar ve uyarı pencereleriyle görselleştirir. Ayrıca, arayüzün sistem mantığıyla entegrasyonunu sağlayarak akıcı bir navigasyon yapısı oluşturur.

Security & Integration Specialist - Eren Gider:
Sistemin güvenlik altyapısını ve proje entegrasyon süreçlerini yönetir. Kullanıcı kayıt ve giriş işlemlerinde güvenli yetkilendirme (Authentication & Authorization) mekanizmalarını kurar. Hassas verilerin korunması için Hashing (SHA-256 vb.) algoritmalarını kullanarak şifreleme katmanını oluşturur. Ayrıca, projenin profesyonel bir portfolyo öğesi haline gelmesi için GitHub depo yönetimi, kapsamlı README dokümantasyonu ve teknik raporlama süreçlerini yürütür. Kodun farklı branch’ler üzerinden tutarlı bir şekilde birleştirilmesini (Merge/Integration) sağlayarak ekip içi teknik koordinasyonu denetler.
