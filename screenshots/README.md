# Screenshots — exercises 5 & 6

Drop the following PNGs in this folder before submitting:

## Exercise 5 — ontology visualization
- `ontology-graph.png` — graph view of `ontology/book_ontology.ttl` exported from **GraphDB Workbench** (or screenshot of OntoGraf / VOWL in **Protégé**).

  How to get it in GraphDB:
  1. Go to https://graphdb.ontotext.com → start the workbench.
  2. Create a new repository (anything, defaults are fine).
  3. *Import → User data → Upload file* → `ontology/book_ontology.ttl`.
  4. *Explore → Visual graph* → search "Book" → click "Show graph"; expand a couple of levels; screenshot.

## Exercise 6 — SPARQL query results
For each of the five queries in `sparql_owl.txt`, run it in **GraphDB's SPARQL tab** (or Protégé's SPARQL Query tab) and save the result screenshot here:

- `sparql/query1.png`
- `sparql/query2.png`
- `sparql/query3.png`
- `sparql/query4.png`
- `sparql/query5.png`

Tip: enable the OWL2-RL reasoner in GraphDB (or HermiT in Protégé) before running query 2/3 so the defined classes like `:BookRecommendedForAlice` are populated by inference — although these specific queries work without a reasoner because they hit the asserted triples directly.
