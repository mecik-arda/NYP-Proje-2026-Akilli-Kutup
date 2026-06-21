package com.akillikutup.service;

import com.akillikutup.core.*;
import com.akillikutup.repository.KitapRepository;
import com.akillikutup.repository.KullaniciRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class BorrowService {

    private final KullaniciRepository kullaniciRepository;
    private final KitapRepository kitapRepository;

    public BorrowService(KullaniciRepository kullaniciRepository, KitapRepository kitapRepository) {
        this.kullaniciRepository = kullaniciRepository;
        this.kitapRepository = kitapRepository;
    }

    public Map<String, Object> borrowBook(String kullaniciId, String kitapId) {
        Kullanici user = kullaniciRepository.findById(kullaniciId)
            .orElseThrow(() -> new RuntimeException("Kullanici bulunamadi"));
        Kitap kitap = kitapRepository.findById(kitapId)
            .orElseThrow(() -> new RuntimeException("Kitap bulunamadi"));

        if (!kitap.stoktaVarMi()) {
            throw new RuntimeException("Stokta yok");
        }

        ((IOduncAlinabilir) kitap).oduncVer();
        user.materyalOduncAl(kitap.getId());

        String bugun = LocalDate.now().toString();
        String iadeGunu = LocalDate.now().plusDays(14).toString();
        user.setOduncTarihi(kitap.getId(), bugun);

        user.getBildirimler().add(new Bildirim("info", "fa-book",
            "\"" + kitap.getBaslik() + "\" ödünç alındı.", "Şimdi"));

        kitapRepository.save(kitap);
        kullaniciRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("basarili", true);
        result.put("mesaj", "Kitap ödünç verildi. İade tarihi: " + iadeGunu);
        result.put("oduncTarihi", bugun);
        result.put("iadeTarihi", iadeGunu);
        return result;
    }

    public Map<String, Object> returnBook(String kullaniciId, String kitapId) {
        Kullanici user = kullaniciRepository.findById(kullaniciId)
            .orElseThrow(() -> new RuntimeException("Kullanici bulunamadi"));
        Kitap kitap = kitapRepository.findById(kitapId)
            .orElseThrow(() -> new RuntimeException("Kitap bulunamadi"));

        ((IOduncAlinabilir) kitap).iadeEt();
        user.materyalIadeEt(kitap.getId());

        String bugun = LocalDate.now().toString();
        user.setIadeTarihi(kitap.getId(), bugun);

        double ceza = 0.0;
        String oduncTarihiStr = user.getOduncTarihi(kitap.getId());
        if (oduncTarihiStr != null) {
            LocalDate oduncTarihi = LocalDate.parse(oduncTarihiStr);
            LocalDate iadeTarihi = LocalDate.parse(bugun);
            LocalDate sonIadeGunu = oduncTarihi.plusDays(14);
            long gecikmeGunu = ChronoUnit.DAYS.between(sonIadeGunu, iadeTarihi);
            if (gecikmeGunu > 0) {
                ceza = kitap.cezaHesapla((int) gecikmeGunu);
                user.setOduncCeza(kitap.getId(), ceza);
            }
        }

        user.getBildirimler().add(new Bildirim("success", "fa-check-circle",
            "\"" + kitap.getBaslik() + "\" iade edildi."
                + (ceza > 0 ? " Gecikme cezası: " + String.format("%.2f", ceza) + " TL" : ""),
            "Şimdi"));

        kitapRepository.save(kitap);
        kullaniciRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("basarili", true);
        result.put("mesaj", "Kitap iade alındı."
            + (ceza > 0 ? " Gecikme cezası: " + String.format("%.2f", ceza) + " TL" : ""));
        result.put("ceza", ceza);
        return result;
    }

    public List<Map<String, Object>> getBorrowHistory() {
        List<Kullanici> kullanicilar = kullaniciRepository.findAll();

        Set<String> tumKitapIdleri = new HashSet<>();
        for (Kullanici k : kullanicilar) {
            tumKitapIdleri.addAll(k.getOduncAlinanMateryaller());
        }

        Map<String, Kitap> kitapMap = new HashMap<>();
        if (!tumKitapIdleri.isEmpty()) {
            List<Kitap> kitaplar = kitapRepository.findAllById(tumKitapIdleri);
            for (Kitap kitap : kitaplar) {
                kitapMap.put(kitap.getId(), kitap);
            }
        }

        List<Map<String, Object>> gecmis = new ArrayList<>();
        for (Kullanici k : kullanicilar) {
            for (String mid : k.getOduncAlinanMateryaller()) {
                Kitap kitap = kitapMap.get(mid);
                Map<String, Object> kayit = new HashMap<>();
                kayit.put("kullaniciId", k.getId());
                kayit.put("kullaniciAdi", k.getIsim());
                kayit.put("materyalId", mid);
                kayit.put("kitapAdi", kitap != null ? kitap.getBaslik() : "Bilinmeyen");
                kayit.put("oduncTarihi", k.getOduncTarihi(mid));
                kayit.put("iadeTarihi", k.getIadeTarihi(mid));
                kayit.put("ceza", k.getOduncCeza(mid));
                kayit.put("durum", k.getIadeTarihi(mid) != null ? "İade Edildi" : "Aktif");
                gecmis.add(kayit);
            }
        }
        return gecmis;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Kullanici> kullanicilar = kullaniciRepository.findAll();
        List<Kitap> kitaplar = kitapRepository.findAll();

        stats.put("toplamKitap", kitaplar.size());
        stats.put("toplamUye", kullanicilar.stream().filter(k -> !"ADMIN".equals(k.getRol())).count());

        int aktifOdunc = 0, gecikmis = 0;
        double bekleyenCeza = 0, tahsilEdilen = 0;
        LocalDate bugun = LocalDate.now();

        for (Kullanici k : kullanicilar) {
            for (String mid : k.getOduncAlinanMateryaller()) {
                aktifOdunc++;
                String tarihStr = k.getOduncTarihi(mid);
                if (tarihStr != null) {
                    try {
                        LocalDate od = LocalDate.parse(tarihStr);
                        if (bugun.isAfter(od.plusDays(14))) {
                            gecikmis++;
                            bekleyenCeza += ChronoUnit.DAYS.between(od.plusDays(14), bugun) * 5.0;
                        }
                    } catch (Exception ignored) {}
                }
            }
            for (Double c : k.getOduncCeza().values()) tahsilEdilen += c;
        }

        stats.put("aktifOdunc", aktifOdunc);
        stats.put("gecikmis", gecikmis);
        stats.put("toplamBekleyenCeza", Math.round(bekleyenCeza * 100.0) / 100.0);
        stats.put("tahsilEdilenCeza", Math.round(tahsilEdilen * 100.0) / 100.0);

        return stats;
    }
}
