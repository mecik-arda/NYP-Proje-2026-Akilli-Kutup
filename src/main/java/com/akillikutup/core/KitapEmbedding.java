package com.akillikutup.core;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "kitap_embeddings")
public class KitapEmbedding {

    @Id
    @Column(name = "kitap_id", length = 36)
    private String kitapId;

    @Column(name = "icerik_metni", columnDefinition = "TEXT")
    private String icerikMetni;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;

    protected KitapEmbedding() {}

    public KitapEmbedding(String kitapId, String icerikMetni, float[] embedding) {
        this.kitapId = kitapId;
        this.icerikMetni = icerikMetni;
        this.embedding = embedding;
    }

    public String getKitapId() { return kitapId; }
    public void setKitapId(String kitapId) { this.kitapId = kitapId; }
    public String getIcerikMetni() { return icerikMetni; }
    public void setIcerikMetni(String icerikMetni) { this.icerikMetni = icerikMetni; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}
