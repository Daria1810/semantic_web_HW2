package ro.unibuc.semweb.hw2.ai;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tiny in-memory vector database. Holds {@link BookDocument}s and ranks them
 * by cosine similarity against a query embedding. Good enough for the
 * 5-10 book corpus this homework needs; would not scale beyond a few
 * thousand documents.
 */
@Service
public class VectorStore {

    private final List<BookDocument> docs = new CopyOnWriteArrayList<>();

    public void add(BookDocument doc) {
        docs.add(doc);
    }

    public void clear() {
        docs.clear();
    }

    public int size() {
        return docs.size();
    }

    public List<BookDocument> search(float[] queryEmbedding, int k) {
        if (docs.isEmpty()) return List.of();
        record Scored(BookDocument doc, double score) {}
        List<Scored> scored = new ArrayList<>(docs.size());
        for (BookDocument d : docs) {
            scored.add(new Scored(d, cosineSimilarity(queryEmbedding, d.embedding())));
        }
        scored.sort(Comparator.comparingDouble(Scored::score).reversed());
        List<BookDocument> out = new ArrayList<>(Math.min(k, scored.size()));
        for (int i = 0; i < Math.min(k, scored.size()); i++) out.add(scored.get(i).doc());
        return out;
    }

    static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("dim mismatch: " + a.length + " vs " + b.length);
        }
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na  += a[i] * a[i];
            nb  += b[i] * b[i];
        }
        double denom = Math.sqrt(na) * Math.sqrt(nb);
        return denom == 0 ? 0 : dot / denom;
    }
}
