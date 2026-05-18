package ro.unibuc.semweb.hw2.rdf;

import java.util.List;

public record GraphData(List<Node> nodes, List<Edge> edges) {
    public record Node(String id, String label, String group) {}
    public record Edge(String from, String to, String label) {}
}
