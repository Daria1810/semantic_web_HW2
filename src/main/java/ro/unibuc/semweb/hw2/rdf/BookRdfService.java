package ro.unibuc.semweb.hw2.rdf;

import jakarta.annotation.PostConstruct;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class BookRdfService {

    private final org.springframework.core.io.Resource booksResource;
    private Model model;

    public BookRdfService(@Value("${app.rdf.books-path}") org.springframework.core.io.Resource booksResource) {
        this.booksResource = booksResource;
    }

    @PostConstruct
    void load() throws IOException {
        model = ModelFactory.createDefaultModel();
        try (InputStream is = booksResource.getInputStream()) {
            model.read(is, BR.NS, "RDF/XML");
        }
    }

    public synchronized Model getModel() {
        return model;
    }

    public synchronized List<Book> listBooks() {
        List<Book> out = new ArrayList<>();
        ResIterator it = model.listSubjectsWithProperty(RDF.type, BR.Book);
        while (it.hasNext()) {
            out.add(toBook(it.next()));
        }
        out.sort(Comparator.comparing(Book::title, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    public synchronized Optional<Book> getBook(String id) {
        Resource r = model.getResource(BR.NS + id);
        if (!model.contains(r, RDF.type, BR.Book)) {
            return Optional.empty();
        }
        return Optional.of(toBook(r));
    }

    public synchronized List<Vocab> listReadingLevels() {
        return listIndividuals(BR.ReadingLevel);
    }

    public synchronized List<Vocab> listThemes() {
        return listIndividuals(BR.Theme);
    }

    private List<Vocab> listIndividuals(Resource type) {
        List<Vocab> out = new ArrayList<>();
        ResIterator it = model.listSubjectsWithProperty(RDF.type, type);
        while (it.hasNext()) {
            Resource r = it.next();
            out.add(new Vocab(r.getLocalName(), labelOrLocalName(r)));
        }
        out.sort(Comparator.comparing(Vocab::label, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    /**
     * Add a new book. Generates a local name from the title if {@code id} is blank.
     * Themes / reading level identifiers must reference existing individuals.
     */
    public synchronized String addBook(String id, String title, String author,
                                       List<String> themeIds, String readingLevelId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (readingLevelId == null || readingLevelId.isBlank()) {
            throw new IllegalArgumentException("readingLevel is required");
        }
        String localName = (id == null || id.isBlank()) ? slug(title) : id;
        Resource book = model.createResource(BR.NS + localName);
        if (model.contains(book, RDF.type, BR.Book)) {
            throw new IllegalStateException("book already exists: " + localName);
        }
        book.addProperty(RDF.type, BR.Book);
        book.addProperty(BR.title, title);
        if (author != null && !author.isBlank()) {
            book.addProperty(BR.author, author);
        }
        if (themeIds != null) {
            for (String t : themeIds) {
                if (t == null || t.isBlank()) continue;
                book.addProperty(BR.hasTheme, model.getResource(BR.NS + t));
            }
        }
        book.addProperty(BR.suitableReadingLevel, model.getResource(BR.NS + readingLevelId));
        return localName;
    }

    public synchronized void updateReadingLevel(String bookId, String newLevelId) {
        Resource book = model.getResource(BR.NS + bookId);
        if (!model.contains(book, RDF.type, BR.Book)) {
            throw new IllegalArgumentException("book not found: " + bookId);
        }
        model.removeAll(book, BR.suitableReadingLevel, null);
        book.addProperty(BR.suitableReadingLevel, model.getResource(BR.NS + newLevelId));
    }

    private Book toBook(Resource r) {
        String title = r.hasProperty(BR.title) ? r.getProperty(BR.title).getString() : r.getLocalName();
        String author = r.hasProperty(BR.author) ? r.getProperty(BR.author).getString() : null;

        List<String> themeLabels = new ArrayList<>();
        List<String> themeIds = new ArrayList<>();
        StmtIterator themeIt = r.listProperties(BR.hasTheme);
        while (themeIt.hasNext()) {
            Resource t = themeIt.next().getResource();
            themeLabels.add(labelOrLocalName(t));
            themeIds.add(t.getLocalName());
        }

        String levelLabel = null;
        String levelId = null;
        if (r.hasProperty(BR.suitableReadingLevel)) {
            Resource l = r.getProperty(BR.suitableReadingLevel).getResource();
            levelLabel = labelOrLocalName(l);
            levelId = l.getLocalName();
        }
        return new Book(r.getLocalName(), title, author, themeLabels, themeIds, levelLabel, levelId);
    }

    private String labelOrLocalName(Resource r) {
        if (r.hasProperty(RDFS.label)) {
            return r.getProperty(RDFS.label).getString();
        }
        return r.getLocalName();
    }

    private static String slug(String title) {
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char c : title.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            } else {
                upper = true;
            }
        }
        if (sb.length() == 0) {
            sb.append("Book").append(System.currentTimeMillis());
        }
        return sb.toString();
    }
}
