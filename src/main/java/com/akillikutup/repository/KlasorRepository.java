package com.akillikutup.repository;

import com.akillikutup.core.Klasor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KlasorRepository extends JpaRepository<Klasor, String> {

    List<Klasor> findByBaslikContainingIgnoreCase(String baslik);
}
