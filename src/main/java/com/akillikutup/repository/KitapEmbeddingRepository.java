package com.akillikutup.repository;

import com.akillikutup.core.KitapEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KitapEmbeddingRepository extends JpaRepository<KitapEmbedding, String> {

    @Query(value = "SELECT ke.*, 1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity " +
           "FROM kitap_embeddings ke " +
           "ORDER BY embedding <=> CAST(:queryVector AS vector) " +
           "LIMIT :limit",
           nativeQuery = true)
    List<Object[]> findNearest(@Param("queryVector") String queryVector, @Param("limit") int limit);
}
