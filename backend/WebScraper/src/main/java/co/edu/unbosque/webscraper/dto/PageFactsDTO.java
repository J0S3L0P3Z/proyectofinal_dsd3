package co.edu.unbosque.webscraper.dto;

import java.util.List;
import java.util.Map;

/**
 * "Hechos" calculados por código (no por la IA): el JSON del contrato F0.4.
 * Los LLM se equivocan al contar, así que números como imageCount salen de aquí.
 */
public record PageFactsDTO(
        String requestedUrl,
        String finalUrl,
        int httpStatus,
        long loadTimeMs,
        String title,
        String language,
        String metaDescription,
        Map<String, String> metaTags,
        List<String> headings,
        int htmlSizeChars,
        int textLength,
        int imageCount,
        List<String> imageSources,
        int internalLinkCount,
        int externalLinkCount,
        int otherLinkCount,
        List<String> internalLinkSamples,
        List<String> externalLinkSamples,
        int formCount,
        int scriptCount,
        List<String> scriptSources,
        List<Technology> technologies) {

    /** Tecnología detectada con su evidencia. Si no hay evidencia clara, la lista queda vacía ("no determinado"). */
    public record Technology(String name, String category, String evidence) {}
}
