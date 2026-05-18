package ro.unibuc.semweb.hw2;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads the OWL ontology and executes every query in {@code sparql_owl.txt}.
 * The test asserts that the files parse cleanly and each query runs — it
 * does not assert specific row counts so that the queries remain valid as
 * the dataset evolves.
 */
class OntologyAndSparqlSmokeTest {

    @Test
    void ontologyLoadsAndAllQueriesExecute() throws Exception {
        Model model = ModelFactory.createDefaultModel();
        model.read(Path.of("ontology/book_ontology.ttl").toUri().toString(), "TTL");
        assertTrue(model.size() > 50, "ontology looks suspiciously small: " + model.size());

        String raw = Files.readString(Path.of("sparql_owl.txt"));
        List<String> queries = splitQueries(raw);
        assertEquals(5, queries.size(), "expected 5 queries in sparql_owl.txt");

        for (int i = 0; i < queries.size(); i++) {
            Query q = QueryFactory.create(queries.get(i));
            try (QueryExecution qe = QueryExecutionFactory.create(q, model)) {
                ResultSet rs = qe.execSelect();
                int rows = 0;
                while (rs.hasNext()) { rs.next(); rows++; }
                System.out.println("Query " + (i + 1) + ": " + rows + " row(s)");
            }
        }
    }

    /**
     * Splits the file on "# Query N" markers and returns the SPARQL block
     * from each section (comments are allowed and ignored by ARQ).
     */
    private static List<String> splitQueries(String text) {
        String[] parts = text.split("(?m)^# Query \\d+");
        List<String> out = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            int prefixIdx = part.indexOf("PREFIX");
            if (prefixIdx < 0) continue;
            int end = part.length();
            int sep = part.indexOf("\n# ---", prefixIdx);
            if (sep > 0) end = sep;
            out.add(part.substring(prefixIdx, end).trim());
        }
        return out;
    }
}
