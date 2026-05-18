# Semantic Web — Homework 2

Book recommendation web application with RDF/OWL data, a Jena-backed Spring Boot UI, and a Gemini-powered RAG chatbot.

**Repository:** https://github.com/Daria1810/semantic_web_HW2

## Team (group 1241EA)

| Name | Contribution |
|------|--------------|
| **TOMA Daria-Maria** | Spring Boot scaffold, Jena integration, book list / detail pages, add & modify book forms, RDF upload + graph visualization, RAG chatbot (Gemini embeddings + vector store + chat endpoint), floating chat widget. |
| **SOLOMON Miruna-Maria** | RDF/XML data model (`book_recommendation.rdf`), OWL ontology (`book_ontology.owl`) with GraphDB visualization, five SPARQL queries (`sparql_owl.txt`) with result screenshots. |

## Stack
- Java 17, Maven, Spring Boot 3, Thymeleaf
- Apache Jena 5.x (RDF, SPARQL, OWL)
- Google Gemini API (chat + embeddings) for the RAG chatbot
- vis-network (browser-side RDF graph visualization)

## Running
```bash
mvn spring-boot:run
```
Then open http://localhost:8080.

For the chatbot, create `src/main/resources/application-local.properties` with:
```
app.gemini.api-key=YOUR_KEY_HERE
```
(get a free key at https://aistudio.google.com/apikey). This file is gitignored.

## Repository layout
- `src/main/java/ro/unibuc/semweb/hw2/` — Spring Boot application code
- `src/main/resources/data/` — RDF/OWL data files
- `src/main/resources/templates/` — Thymeleaf views
- `src/main/resources/static/` — CSS / JS for the chat widget + graph viz
- `screenshots/` — GraphDB / Protégé / SPARQL screenshots required by the rubric
- `sparql_owl.txt` — five SPARQL queries (exercise 6)
