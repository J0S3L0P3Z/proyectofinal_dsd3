package co.edu.unbosque.webscraper.dto;

import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.service.AiProvider;

import com.fasterxml.jackson.databind.JsonNode;

/** Resultado de una IA para un análisis: correcto (response) o fallido (errorCode + mensaje). */
public record AiResultDTO(
        String providerId,
        String providerLabel,
        String model,
        boolean ok,
        JsonNode response,
        ErrorCode errorCode,
        String errorMessage,
        String errorDetail,
        long durationMs) {

    public static AiResultDTO success(AiProvider p, JsonNode response, long ms) {
        return new AiResultDTO(p.id(), p.label(), p.model(), true, response, null, null, null, ms);
    }

    public static AiResultDTO failure(AiProvider p, ErrorCode code, String detail, long ms) {
        return new AiResultDTO(p.id(), p.label(), p.model(), false, null, code, code.getMessage(), detail, ms);
    }
}
