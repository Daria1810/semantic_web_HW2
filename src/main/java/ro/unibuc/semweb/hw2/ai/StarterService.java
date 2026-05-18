package ro.unibuc.semweb.hw2.ai;

import org.springframework.stereotype.Service;
import ro.unibuc.semweb.hw2.rdf.Book;
import ro.unibuc.semweb.hw2.rdf.BookRdfService;

import java.util.List;

/**
 * Builds three context-aware conversation starters for the chat widget.
 * The page context is set by each controller (e.g. "book:Dune" on the
 * Dune detail page, "book-list" on the catalog, "upload" on the upload
 * page) and we tailor the suggestions accordingly — per ex. 7 part 2.
 */
@Service
public class StarterService {

    private final BookRdfService books;

    public StarterService(BookRdfService books) {
        this.books = books;
    }

    public List<String> startersFor(String context) {
        if (context == null || context.isBlank()) return defaultStarters();

        if (context.startsWith("book:")) {
            String id = context.substring("book:".length());
            return books.getBook(id)
                    .map(this::startersForBook)
                    .orElseGet(this::defaultStarters);
        }
        if (context.startsWith("edit-level:")) {
            String id = context.substring("edit-level:".length());
            return books.getBook(id)
                    .map(b -> List.of(
                            "What is the current reading level of " + b.title() + "?",
                            "Which reading levels exist in the system?",
                            "What audience does " + b.title() + " best suit?"
                    ))
                    .orElseGet(this::defaultStarters);
        }
        return switch (context) {
            case "book-list" -> List.of(
                    "What is a book I am most likely to enjoy from this list?",
                    "Show me books for Beginner readers.",
                    "Which books match the Mystery theme?"
            );
            case "add-book" -> List.of(
                    "What themes are available in the library?",
                    "What reading levels can I pick?",
                    "Suggest a popular Science Fiction book."
            );
            case "upload", "upload-result" -> List.of(
                    "Explain the book recommendation rule.",
                    "What does the sample dataset contain?",
                    "What is RDF used for in this project?"
            );
            default -> defaultStarters();
        };
    }

    private List<String> startersForBook(Book b) {
        String t = b.title();
        return List.of(
                "Tell me about " + t + ".",
                "Who wrote " + t + "?",
                "Which other books are similar to " + t + "?"
        );
    }

    private List<String> defaultStarters() {
        return List.of(
                "What books are in the library?",
                "Recommend a book for a Beginner reader.",
                "Which themes are available?"
        );
    }
}
