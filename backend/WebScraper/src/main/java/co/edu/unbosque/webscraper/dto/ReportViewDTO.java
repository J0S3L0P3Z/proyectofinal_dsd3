package co.edu.unbosque.webscraper.dto;

import java.util.List;

/** Modelo plano para las plantillas del PDF y del correo (las plantillas llaman r.campo()). */
public record ReportViewDTO(
        String url,
        String finalUrl,
        String domain,
        String generatedAt,
        String statusLabel,
        boolean hasWarnings,
        String title,
        String language,
        String metaDescription,
        int httpStatus,
        long loadTimeMs,
        int imageCount,
        int internalLinks,
        int externalLinks,
        int forms,
        int scripts,
        List<PageFactsDTO.Technology> technologies,
        List<String> headings,
        List<String> warnings,
        List<AiSection> analyses,
        int okCount,
        boolean failed,
        String errorCode,
        String errorMessage,
        String errorHint,
        boolean hasFacts,
        String aiSummary) {

    public record AiSection(
            String provider,
            String model,
            boolean ok,
            String error,
            String summary,
            List<String> information,
            List<String> people,
            String imageComment,
            String technology,
            String technologyEvidence,
            List<String> notes) {}
}
