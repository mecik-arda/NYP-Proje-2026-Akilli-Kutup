package com.akillikutup.material;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "kitap_embeddings")
@Data
@NoArgsConstructor
public class KitapEmbedding {

    @Id
    @Column(name = "kitap_id", length = 36)
    private String kitapId;

    @Column(name = "icerik_metni", columnDefinition = "TEXT")
    private String icerikMetni;

    @Column(name = "embedding")
    private float[] embedding;

    public KitapEmbedding(String kitapId, String icerikMetni, float[] embedding) {
        this.kitapId = kitapId;
        this.icerikMetni = icerikMetni;
        this.embedding = embedding;
    }
}
