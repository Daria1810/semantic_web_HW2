package ro.unibuc.semweb.hw2.rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public final class BR {
    public static final String NS = "http://www.semanticweb.org/hw2/book-recommendation#";

    public static final Resource Book = ResourceFactory.createResource(NS + "Book");
    public static final Resource User = ResourceFactory.createResource(NS + "User");
    public static final Resource Theme = ResourceFactory.createResource(NS + "Theme");
    public static final Resource ReadingLevel = ResourceFactory.createResource(NS + "ReadingLevel");

    public static final Property title = ResourceFactory.createProperty(NS, "title");
    public static final Property author = ResourceFactory.createProperty(NS, "author");
    public static final Property hasTheme = ResourceFactory.createProperty(NS, "hasTheme");
    public static final Property suitableReadingLevel = ResourceFactory.createProperty(NS, "suitableReadingLevel");
    public static final Property name = ResourceFactory.createProperty(NS, "name");
    public static final Property readingLevel = ResourceFactory.createProperty(NS, "readingLevel");
    public static final Property preferredTheme = ResourceFactory.createProperty(NS, "preferredTheme");

    private BR() {}
}
