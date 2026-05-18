# Semantic Web — Homework 2

Book recommendation web application backed by RDF and an OWL ontology, with a Jena-powered Spring Boot UI and a Gemini-grounded RAG chatbot.

**Repository:** https://github.com/Daria1810/semantic_web_HW2
**Group:** 1241EA · **Deadline:** 2026-05-18 23:59 (soft) / 2026-05-20 14:00 (hard).

---

## Team & contributions

| Name | Contribution |
|------|--------------|
| **TOMA Daria-Maria** | Spring Boot scaffold, Jena `BookRdfService`, book list / detail pages, add-book & change-reading-level forms, RDF/XML upload + vis-network graph visualization, RAG chatbot (Gemini embeddings + in-memory vector store + chat REST API + floating widget JS), end-to-end smoke tests. |
| **SOLOMON Miruna-Maria** | RDF/XML data (`src/main/resources/data/books.rdf`), OWL ontology (`ontology/book_ontology.ttl`) with defined classes (`ScienceFictionBook`, `BookRecommendedForAlice`, …), GraphDB visualization screenshots, five SPARQL queries (`sparql_owl.txt`) with result screenshots. |

---

## What each exercise maps to in the repo

| # | Deliverable | Location |
|---|---|---|
| 1 | RDF/XML for the scenario (Alice/Bob + Dune/Silent Patient/Hunger Games) | `src/main/resources/data/books.rdf` |
| 2 | Upload RDF/XML and visualize the graph | `UploadController`, `templates/upload.html`, `templates/graph.html`, `static/js/graph.js` |
| 3 | Add/modify books via Jena (test: add *Harry Potter*, modify *Hunger Games* level) | `BookController` (`/books/new`, `/books/{id}/edit-level`), `templates/books/form.html`, `templates/books/edit-level.html`, `BookRdfService.addBook` / `updateReadingLevel` |
| 4 | List books + per-book page (Jena) | `BookController` (`/books`, `/books/{id}`), `templates/books/list.html`, `templates/books/detail.html` |
| 5 | OWL ontology + visualization | `ontology/book_ontology.ttl`, `screenshots/ontology-graph.png` |
| 6 | Five SPARQL queries + results | `sparql_owl.txt`, `screenshots/sparql/query{1..5}.png` |
| 7 | Floating RAG chatbot (Gemini) | `ro.unibuc.semweb.hw2.ai.*`, `ChatController`, `templates/fragments/chat-widget.html`, `static/js/chat-widget.js` |

---

## Stack

- Java 17, Maven, Spring Boot 3.3, Thymeleaf
- Apache Jena 5.1 (RDF, SPARQL, OWL)
- vis-network 9.x (browser-side RDF graph)
- Google Gemini (`text-embedding-004` + `gemini-2.0-flash`) — embeddings + chat

---

## Running

```bash
mvn spring-boot:run
```

Then open http://localhost:8080.

### Enabling the chatbot (exercise 7)

The chatbot requires a free Gemini API key (https://aistudio.google.com/apikey). Either:

```bash
# option A — env variable
GEMINI_API_KEY=YOUR_KEY mvn spring-boot:run

# option B — create src/main/resources/application-local.properties (gitignored)
echo "app.gemini.api-key=YOUR_KEY" > src/main/resources/application-local.properties
mvn spring-boot:run
```

Without a key, the rest of the app still works; the chat widget shows a clear "not configured" message instead of crashing.

---

## How the RAG chatbot satisfies the rubric

- **Floats on every page** — `templates/fragments/chat-widget.html` is included in every Thymeleaf view via `th:replace`.
- **3 context-aware starters** — each controller writes a `chatContext` attribute (`book-list`, `book:Dune`, `add-book`, `upload`, …). The widget passes it to `GET /api/chat/starters?context=…` and `StarterService` returns starters tailored to the page (e.g. *"Who wrote Dune?"* on the Dune detail page, *"What is a book I am most likely to enjoy from this list?"* on the catalog).
- **Grounded responses, not whole-XML prompts** — `BookIndexer` runs once on startup and embeds one chunk per book + one per user + a few schema chunks via `text-embedding-004`. The chunks live in `VectorStore` (in-memory cosine similarity). For each question, `RagChatService` embeds the query, retrieves the top-6 most similar chunks, and builds a prompt that *forbids outside knowledge* ("Answer using ONLY the CONTEXT. If the answer is not present, reply *I don't have that information…*"). That's why the bot would answer "Gigel" if the XML claimed Gigel wrote Harry Potter, exactly as the rubric demands.
- **Find by theme + author** — try *"What book has the author Frank Herbert and the theme Science Fiction?"* — retrieval surfaces the Dune chunk, the prompt grounds the answer, the bot answers "Dune".

---

## Demo script for the lab presentation

1. `mvn spring-boot:run` with the Gemini key configured.
2. **Ex 4** — open `/books`; click "Dune" to show the detail page.
3. **Ex 3** — open *Add book*, add **Harry Potter** (Fantasy, Intermediate); show it in the list; from the list click *Change level* on Hunger Games → Intermediate; refresh to confirm.
4. **Ex 2** — open *Upload RDF*, pick `src/main/resources/data/books.rdf`; show the interactive vis-network graph; repeat with `ontology/book_ontology.ttl` to show it also handles Turtle/OWL.
5. **Ex 5** — switch to GraphDB / Protégé and show the ontology visualization (`screenshots/ontology-graph.png`).
6. **Ex 6** — paste each of the five queries from `sparql_owl.txt` into the SPARQL tab and run them (or just show the result screenshots).
7. **Ex 7** — back in the browser, open the floating chat widget. Verify:
   - the 3 starters change with the page
   - ask *"Who wrote Dune?"* → answers from the vector DB (Frank Herbert)
   - ask *"What book has the author Frank Herbert and the theme Science Fiction?"* → "Dune"
   - ask *"What is Alice's preferred theme?"* → Science Fiction (because Alice is in the vector DB too)

---

## Repository layout

```
HW2/
├── README.md                                 ← this file
├── pom.xml                                   Maven, Spring Boot 3 + Jena
├── ontology/book_ontology.ttl                Ex 5 — OWL ontology (Turtle)
├── sparql_owl.txt                            Ex 6 — five SPARQL queries
├── screenshots/                              Ex 5 + 6 — Protégé/GraphDB PNGs
├── src/main/resources/
│   ├── application.properties
│   ├── data/books.rdf                        Ex 1 — RDF/XML
│   ├── static/{css,js}/
│   └── templates/
│       ├── books/{list,detail,form,edit-level}.html
│       ├── fragments/{layout,chat-widget}.html
│       ├── upload.html
│       └── graph.html
└── src/main/java/ro/unibuc/semweb/hw2/
    ├── Hw2Application.java
    ├── rdf/    BR, Book, UserProfile, Vocab, GraphData, BookRdfService
    ├── ai/     GeminiClient, BookDocument, VectorStore, BookIndexer,
    │           RagChatService, StarterService
    └── web/    BookController, UploadController, ChatController
```
