package co.edu.unbosque.webscraper.dto;

import java.util.Map;

/** Resultado crudo del navegador: el DOM ya renderizado con JavaScript más datos de la respuesta. */
public record RawPageDTO(
        String html,
        int httpStatus,
        String finalUrl,
        long loadTimeMs,
        Map<String, String> responseHeaders,
        Map<String, Object> jsGlobals) {}
