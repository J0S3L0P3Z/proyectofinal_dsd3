package co.edu.unbosque.webscraper.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Cuerpo JSON de los errores de la API de scraping y administración: fecha, estado HTTP, código del
 * catálogo, mensaje claro para el usuario y errores por campo cuando hay validaciones.
 */
public record ApiError(Instant timestamp, int status, String code, String message, Map<String, String> fieldErrors) {
    public static ApiError of(ErrorCode code) {
        return of(code, code.getMessage());
    }

    public static ApiError of(ErrorCode code, String message) {
        return new ApiError(Instant.now(), code.getStatus(), code.name(), message, null);
    }
}
