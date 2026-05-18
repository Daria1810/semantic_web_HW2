package ro.unibuc.semweb.hw2.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import ro.unibuc.semweb.hw2.rdf.Book;
import ro.unibuc.semweb.hw2.rdf.BookRdfService;

@Controller
public class BookController {

    private final BookRdfService books;

    public BookController(BookRdfService books) {
        this.books = books;
    }

    @GetMapping({"/", "/books"})
    public String list(Model m) {
        m.addAttribute("books", books.listBooks());
        m.addAttribute("chatContext", "book-list");
        m.addAttribute("chatContextLabel", "the book list");
        return "books/list";
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable String id, Model m) {
        Book b = books.getBook(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
        m.addAttribute("book", b);
        m.addAttribute("chatContext", "book:" + id);
        m.addAttribute("chatContextLabel", b.title());
        return "books/detail";
    }
}
