package com.akillikutup.material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DijitalMedyaRepository extends JpaRepository<DijitalMedya, String> {
    List<DijitalMedya> findByTur(String tur);
    List<DijitalMedya> findByDosyaFormati(String dosyaFormati);
    List<DijitalMedya> findByBaslikContainingIgnoreCase(String baslik);
}
