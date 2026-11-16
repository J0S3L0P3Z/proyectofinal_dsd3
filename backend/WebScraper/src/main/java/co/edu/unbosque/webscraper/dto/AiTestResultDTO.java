package co.edu.unbosque.webscraper.dto;

/**
 * Resultado de una llamada de prueba a una IA ("responde OK").
 *
 * @param provider Identificador del proveedor
 * @param label Nombre para mostrar
 * @param model Modelo probado
 * @param ok true si respondió
 * @param durationMs Duración de la llamada
 * @param reply Inicio de la respuesta recibida (si respondió)
 * @param errorCode Código del catálogo de errores (si falló)
 * @param errorMessage Mensaje claro (si falló)
 */
public record AiTestResultDTO(
    String provider,
    String label,
    String model,
    boolean ok,
    long durationMs,
    String reply,
    String errorCode,
    String errorMessage) {}
