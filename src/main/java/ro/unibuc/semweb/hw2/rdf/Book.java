package ro.unibuc.semweb.hw2.rdf;

import java.util.List;

public record Book(
        String id,
        String title,
        String author,
        List<String> themeLabels,
        List<String> themeIds,
        String readingLevelLabel,
        String readingLevelId
) {}
