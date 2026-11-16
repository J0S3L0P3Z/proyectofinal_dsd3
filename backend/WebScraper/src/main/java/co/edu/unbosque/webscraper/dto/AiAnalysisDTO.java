package co.edu.unbosque.webscraper.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Respuesta (o fallo) de una de las tres IAs.
 *
 * @param provider Identificador del proveedor (ollama, gemini, openrouter)
 * @param label Nombre para mostrar
 * @param model Modelo usado
 * @param ok true si la IA respondió con un JSON válido
 * @param response Análisis con el esquema fijo (ver docs/contratos.md); nulo si falló
 * @param errorCode Código del error si falló
 * @param errorMessage Mensaje claro si falló
 * @param durationMs Duración de la llamada
 */
public record AiAnalysisDTO(
    String provider,
    String label,
    String model,
    boolean ok,
    JsonNode response,
    String errorCode,
    String errorMessage,
    Long durationMs) {}
