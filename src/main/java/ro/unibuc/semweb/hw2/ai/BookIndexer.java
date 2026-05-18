package ro.unibuc.semweb.hw2.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ro.unibuc.semweb.hw2.rdf.Book;
import ro.unibuc.semweb.hw2.rdf.BookRdfService;
import ro.unibuc.semweb.hw2.rdf.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds short, self-contained natural-language descriptions for every Book
 * and User in the RDF model and stores their Gemini embeddings in
 * {@link VectorStore}. The descriptions become the only source of truth the
 * RAG chatbot has — even if Gemini "knows" J. K. Rowling wrote Harry Potter,
 * an answer about it must come from the chunks we index here.
 *
 * Runs once on {@link ApplicationReadyEvent} (after the web server is up).
 * Failures are caught and logged so the rest of the app keeps working.
 */
@Service
public class BookIndexer {

    private static final Logger log = LoggerFactory.getLogger(BookIndexer.class);

    private final BookRdfService books;
    private final GeminiClient gemini;
    private final VectorStore store;

    public BookIndexer(BookRdfService books, GeminiClient gemini, VectorStore store) {
        this.books = books;
        this.gemini = gemini;
        this.store = store;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void indexOnStartup() {
        if (!gemini.isConfigured()) {
            log.warn("Gemini API key not configured — chatbot will return a stub. Set app.gemini.api-key or GEMINI_API_KEY.");
            return;
        }
        try {
            reindex();
        } catch (Exception ex) {
            log.error("Indexing failed; chatbot will return errors. Cause: {}", ex.getMessage(), ex);
        }
    }

    public synchronized void reindex() {
        store.clear();
        List<String> docs = buildDocuments();
        log.info("Embedding {} documents with Gemini ({})…", docs.size(), "text-embedding-004");
        for (int i = 0; i < docs.size(); i++) {
            String text = docs.get(i);
            float[] emb = gemini.embed(text, "RETRIEVAL_DOCUMENT");
            store.add(new BookDocument("doc-" + i, text, emb));
        }
        log.info("Vector store ready: {} documents", store.size());
    }

    /** Each entry becomes one chunk in the vector DB. */
    private List<String> buildDocuments() {
        List<String> out = new ArrayList<>();

        for (Book b : books.listBooks()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Book titled \"").append(b.title()).append("\"");
            if (b.author() != null && !b.author().isBlank()) {
                sb.append(" was written by ").append(b.author());
            }
            sb.append(".");
            if (!b.themeLabels().isEmpty()) {
                sb.append(" Themes: ").append(String.join(", ", b.themeLabels())).append(".");
            }
            if (b.readingLevelLabel() != null) {
                sb.append(" Suitable reading level: ").append(b.readingLevelLabel()).append(".");
            }
            sb.append(" Identifier: ").append(b.id()).append(".");
            out.add(sb.toString());
        }

        for (UserProfile u : books.listUsers()) {
            StringBuilder sb = new StringBuilder();
            sb.append("User ").append(u.name()).append(" has a reading level of ").append(u.readingLevel())
              .append(" and prefers books with the ").append(u.preferredTheme()).append(" theme.");
            sb.append(" Identifier: ").append(u.id()).append(".");
            out.add(sb.toString());
        }

        out.add("Recommendation rule: a book is recommended for a user when one of the book's themes matches the user's preferred theme AND the book's suitable reading level equals the user's reading level.");
        out.add("Available reading levels in this library: Beginner, Intermediate, Advanced.");
        out.add("Available themes in this library: Science Fiction, Fantasy, Mystery, Murder.");

        return out;
    }
}
