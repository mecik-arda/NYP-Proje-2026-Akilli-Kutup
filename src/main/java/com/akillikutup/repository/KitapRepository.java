package com.akillikutup.repository;

import com.akillikutup.core.Kitap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitapRepository extends JpaRepository<Kitap, String> {

    List<Kitap> findByBaslikContainingIgnoreCase(String baslik);

    List<Kitap> findByYazarContainingIgnoreCase(String yazar);

    List<Kitap> findByKategori(String kategori);

    List<Kitap> findByIsbn(String isbn);

    List<Kitap> findByStokAdediGreaterThan(int minStok);
}
