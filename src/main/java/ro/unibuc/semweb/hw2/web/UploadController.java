package ro.unibuc.semweb.hw2.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.unibuc.semweb.hw2.rdf.GraphData;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class UploadController {

    private final ObjectMapper json = new ObjectMapper();

    @GetMapping("/upload")
    public String form(Model m) {
        m.addAttribute("chatContext", "upload");
        m.addAttribute("chatContextLabel", "the RDF upload page");
        return "upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         Model m,
                         RedirectAttributes ra) {
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("flashError", "Pick an RDF/XML or Turtle file first.");
            return "redirect:/upload";
        }

        org.apache.jena.rdf.model.Model rdf = ModelFactory.createDefaultModel();
        String lang = guessLang(file.getOriginalFilename());
        try (InputStream is = file.getInputStream()) {
            rdf.read(is, "urn:uploaded:", lang);
        } catch (RiotException | java.io.IOException ex) {
            ra.addFlashAttribute("flashError", "Could not parse the file as " + lang + ": " + ex.getMessage());
            return "redirect:/upload";
        }

        GraphData data = toGraphData(rdf);
        try {
            m.addAttribute("graphJson", json.writeValueAsString(data));
        } catch (Exception ex) {
            throw new IllegalStateException("could not serialize graph", ex);
        }
        m.addAttribute("filename", file.getOriginalFilename());
        m.addAttribute("triples", rdf.size());
        m.addAttribute("nodeCount", data.nodes().size());
        m.addAttribute("edgeCount", data.edges().size());
        m.addAttribute("chatContext", "upload-result");
        m.addAttribute("chatContextLabel", "the uploaded RDF graph");
        return "graph";
    }

    private static String guessLang(String filename) {
        if (filename == null) return "RDF/XML";
        String f = filename.toLowerCase(Locale.ROOT);
        if (f.endsWith(".ttl") || f.endsWith(".turtle")) return "TURTLE";
        if (f.endsWith(".n3")) return "N3";
        if (f.endsWith(".nt")) return "N-TRIPLES";
        if (f.endsWith(".jsonld") || f.endsWith(".json")) return "JSON-LD";
        return "RDF/XML";
    }

    private static GraphData toGraphData(org.apache.jena.rdf.model.Model rdf) {
        List<GraphData.Node> nodes = new ArrayList<>();
        List<GraphData.Edge> edges = new ArrayList<>();
        Map<String, Integer> nodeIdx = new HashMap<>();
        int literalCounter = 0;

        StmtIterator it = rdf.listStatements();
        while (it.hasNext()) {
            Statement s = it.next();
            Resource subj = s.getSubject();
            RDFNode  obj  = s.getObject();
            String predLabel = labelFor(s.getPredicate());

            String subjId = nodeId(subj);
            String subjLabel = labelFor(subj);
            String subjGroup = groupFor(subj, rdf);
            addNode(nodes, nodeIdx, subjId, subjLabel, subjGroup);

            if (obj.isResource()) {
                Resource objR = obj.asResource();
                String objId = nodeId(objR);
                String objLabel = labelFor(objR);
                String objGroup = groupFor(objR, rdf);
                addNode(nodes, nodeIdx, objId, objLabel, objGroup);
                edges.add(new GraphData.Edge(subjId, objId, predLabel));
            } else {
                // Literal — give it a synthetic id so identical literal values don't collapse.
                String litId = "lit-" + (++literalCounter);
                String litLabel = obj.asLiteral().getLexicalForm();
                if (litLabel.length() > 40) litLabel = litLabel.substring(0, 37) + "…";
                addNode(nodes, nodeIdx, litId, litLabel, "literal");
                edges.add(new GraphData.Edge(subjId, litId, predLabel));
            }
        }
        return new GraphData(nodes, edges);
    }

    private static void addNode(List<GraphData.Node> nodes, Map<String, Integer> idx,
                                String id, String label, String group) {
        if (idx.containsKey(id)) return;
        idx.put(id, nodes.size());
        nodes.add(new GraphData.Node(id, label, group));
    }

    private static String nodeId(Resource r) {
        if (r.isAnon()) return "_:" + r.getId().getLabelString();
        return r.getURI();
    }

    private static String labelFor(Resource r) {
        if (r.isAnon()) return "_:" + r.getId().getLabelString();
        if (r.hasProperty(RDFS.label)) {
            return r.getProperty(RDFS.label).getString();
        }
        String local = r.getLocalName();
        return (local == null || local.isEmpty()) ? r.getURI() : local;
    }

    /** Cheap colouring: bucket nodes by their first rdf:type local name, or "Resource" / "literal". */
    private static String groupFor(Resource r, org.apache.jena.rdf.model.Model m) {
        if (r.isAnon()) return "blank";
        StmtIterator types = m.listStatements(r, org.apache.jena.vocabulary.RDF.type, (RDFNode) null);
        while (types.hasNext()) {
            Resource t = types.next().getObject().asResource();
            String local = t.getLocalName();
            if (local != null && !local.isEmpty()) return local;
        }
        return "Resource";
    }
}
