package ro.unibuc.semweb.hw2.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.unibuc.semweb.hw2.rdf.Book;
import ro.unibuc.semweb.hw2.rdf.BookRdfService;

import java.util.List;

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

    // ---- Exercise 3: add / modify ----

    @GetMapping("/books/new")
    public String newForm(Model m) {
        m.addAttribute("themes", books.listThemes());
        m.addAttribute("levels", books.listReadingLevels());
        m.addAttribute("chatContext", "add-book");
        m.addAttribute("chatContextLabel", "the add-book form");
        return "books/form";
    }

    @PostMapping("/books")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String author,
                         @RequestParam(name = "themes", required = false) List<String> themeIds,
                         @RequestParam("readingLevel") String readingLevelId,
                         RedirectAttributes ra) {
        try {
            String id = books.addBook(null, title, author, themeIds, readingLevelId);
            ra.addFlashAttribute("flash", "Added book \"" + title + "\".");
            return "redirect:/books/" + id;
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("flashError", ex.getMessage());
            return "redirect:/books/new";
        }
    }

    @GetMapping("/books/{id}/edit-level")
    public String editLevelForm(@PathVariable String id, Model m) {
        Book b = books.getBook(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
        m.addAttribute("book", b);
        m.addAttribute("levels", books.listReadingLevels());
        m.addAttribute("chatContext", "edit-level:" + id);
        m.addAttribute("chatContextLabel", b.title());
        return "books/edit-level";
    }

    @PostMapping("/books/{id}/reading-level")
    public String updateLevel(@PathVariable String id,
                              @RequestParam("readingLevel") String readingLevelId,
                              RedirectAttributes ra) {
        try {
            books.updateReadingLevel(id, readingLevelId);
            ra.addFlashAttribute("flash", "Reading level updated.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/books/" + id;
    }
}
