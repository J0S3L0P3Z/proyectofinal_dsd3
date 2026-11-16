package co.edu.unbosque.webscraper.dto;

/**
 * Estado de configuración de una de las 3 IAs (nunca incluye la clave).
 *
 * @param provider Identificador (ollama, gemini, openrouter)
 * @param label Nombre para mostrar
 * @param model Modelo configurado
 * @param fallbackModel Modelo de respaldo (vacío si no hay)
 * @param baseUrl URL base de la API
 * @param configured true si tiene clave, URL y modelo
 * @param keyVariable Variable del archivo .env donde se pega la clave
 */
public record AiProviderStatusDTO(
    String provider,
    String label,
    String model,
    String fallbackModel,
    String baseUrl,
    boolean configured,
    String keyVariable) {}
