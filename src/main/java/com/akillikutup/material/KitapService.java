package com.akillikutup.material;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class KitapService {

    private final KitapRepository kitapRepository;

    public KitapService(KitapRepository kitapRepository) {
        this.kitapRepository = kitapRepository;
    }

    public List<Kitap> findAll() {
        return kitapRepository.findAll();
    }

    public Optional<Kitap> findById(String id) {
        return kitapRepository.findById(id);
    }

    public Kitap save(Kitap kitap) {
        return kitapRepository.save(kitap);
    }

    public List<Kitap> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        List<Kitap> byTitle = kitapRepository.findByBaslikContainingIgnoreCase(query);
        List<Kitap> byAuthor = kitapRepository.findByYazarContainingIgnoreCase(query);
        byTitle.addAll(byAuthor.stream()
            .filter(b -> !byTitle.contains(b))
            .toList());
        return byTitle;
    }

    public List<Kitap> findByKategori(String kategori) {
        return kitapRepository.findByKategori(kategori);
    }

    public void deleteById(String id) {
        kitapRepository.deleteById(id);
    }

    public long count() {
        return kitapRepository.count();
    }

    public void borrowBook(String kitapId) {
        Kitap kitap = kitapRepository.findById(kitapId)
            .orElseThrow(() -> new RuntimeException("Kitap bulunamadi"));
        if (kitap.stoktaVarMi()) {
            ((IOduncAlinabilir) kitap).oduncVer();
            kitapRepository.save(kitap);
        } else {
            throw new RuntimeException("Stokta yok");
        }
    }

    public void returnBook(String kitapId) {
        Kitap kitap = kitapRepository.findById(kitapId)
            .orElseThrow(() -> new RuntimeException("Kitap bulunamadi"));
        ((IOduncAlinabilir) kitap).iadeEt();
        kitapRepository.save(kitap);
    }
}
