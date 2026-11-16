package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.dto.ChatMessageDTO;

import java.util.List;

/**
 * Proveedor de IA intercambiable (F2.1). Agregar uno nuevo no obliga a tocar el resto del pipeline:
 * basta otra implementación registrada en {@link AiProviderRegistry}.
 */
public interface AiProvider {
    String id();

    String label();

    String model();

    /** Máximo de llamadas simultáneas que admite el plan del proveedor. */
    int maxConcurrent();

    /** URL base de la API (por ejemplo, https://openrouter.ai/api/v1). */
    String baseUrl();

    /** Modelo de respaldo (puede ser vacío). */
    String fallbackModel();

    /** true si tiene clave, URL base y modelo configurados. */
    boolean isConfigured();

    /** Devuelve el texto de la respuesta. Lanza AppException con código AI_* si algo falla. */
    String complete(List<ChatMessageDTO> messages);
}
