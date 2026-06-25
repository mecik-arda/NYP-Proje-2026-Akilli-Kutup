package com.akillikutup.material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KlasorRepository extends JpaRepository<Klasor, String> {
    List<Klasor> findByBaslikContainingIgnoreCase(String baslik);
}
