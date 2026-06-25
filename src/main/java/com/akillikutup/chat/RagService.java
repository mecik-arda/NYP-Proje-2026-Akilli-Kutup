package com.akillikutup.chat;

import com.akillikutup.material.Kitap;
import com.akillikutup.material.KitapEmbedding;
import com.akillikutup.material.KitapEmbeddingRepository;
import com.akillikutup.material.KitapRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class RagService {

    private final KitapRepository kitapRepository;
    private final KitapEmbeddingRepository embeddingRepository;

    public RagService(KitapRepository kitapRepository,
                       KitapEmbeddingRepository embeddingRepository) {
        this.kitapRepository = kitapRepository;
        this.embeddingRepository = embeddingRepository;
    }

    public int indexAllBooks() {
        List<Kitap> kitaplar = kitapRepository.findAll();
        int indexed = 0;
        for (Kitap kitap : kitaplar) {
            try {
                if (embeddingRepository.existsById(kitap.getId())) continue;
                String content = buildBookContent(kitap);
                float[] embedding = GeminiClient.generateEmbedding(content, null);
                if (embedding != null) {
                    embeddingRepository.save(new KitapEmbedding(kitap.getId(), content, embedding));
                    indexed++;
                }
            } catch (Exception e) {
                System.err.println("RAG indeksleme hatasi (" + kitap.getBaslik() + "): " + e.getMessage());
            }
        }
        return indexed;
    }

    public List<SearchResult> search(String query, int topK) {
        float[] queryEmbedding = GeminiClient.generateEmbedding(query, null);
        if (queryEmbedding == null) return Collections.emptyList();

        String vectorStr = arrayToPgVector(queryEmbedding);
        List<Object[]> rows = embeddingRepository.findNearest(vectorStr, topK);

        List<SearchResult> results = new ArrayList<>();
        for (Object[] row : rows) {
            KitapEmbedding ke = (KitapEmbedding) row[0];
            Double similarity = (Double) row[1];
            results.add(new SearchResult(ke.getKitapId(), similarity, ke.getIcerikMetni()));
        }
        return results;
    }

    public String askWithContext(String question) {
        List<SearchResult> relevant = search(question, 3);
        if (relevant.isEmpty()) {
            List<Kitap> fallback = kitapRepository.findAll();
            if (fallback.isEmpty()) return "Kutuphanede kitap bulunamadi.";
            StringBuilder ctx = new StringBuilder("Kutuphanedeki tum kitaplara dayanarak yanitla:\n");
            for (Kitap k : fallback.stream().limit(5).toList()) {
                ctx.append("- ").append(k.getBaslik()).append(" | ").append(k.getYazar()).append("\n");
            }
            ctx.append("\nSoru: ").append(question);
            return GeminiClient.askQuestion(ctx.toString(), null);
        }
        StringBuilder context = new StringBuilder("Su kutuphane kitaplarina dayanarak yanitla:\n\n");
        for (int i = 0; i < relevant.size(); i++) {
            context.append("--- Kitap ").append(i + 1).append(" ---\n");
            context.append(relevant.get(i).content()).append("\n\n");
        }
        context.append("Soru: ").append(question);
        context.append("\n\nYanitin Turkce olsun ve hangi kitaptan bilgi aldigini belirt.");
        return GeminiClient.askQuestion(context.toString(), null);
    }

    private String buildBookContent(Kitap kitap) {
        return String.format("Kitap: %s | Yazar: %s | Kategori: %s | ISBN: %s",
            kitap.getBaslik(),
            kitap.getYazar() != null ? kitap.getYazar() : "Bilinmiyor",
            kitap.getKategori() != null ? kitap.getKategori() : "Diger",
            kitap.getIsbn());
    }

    private String arrayToPgVector(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    public record SearchResult(String bookId, double similarity, String content) {}
}
