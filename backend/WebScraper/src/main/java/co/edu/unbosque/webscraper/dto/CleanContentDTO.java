package co.edu.unbosque.webscraper.dto;

/** Contenido listo para enviar a las IAs. {@code truncated} = se recortó por el presupuesto de tokens. */
public record CleanContentDTO(String text, String format, boolean truncated, int originalChars, int cleanedChars, int estimatedTokens) {}
