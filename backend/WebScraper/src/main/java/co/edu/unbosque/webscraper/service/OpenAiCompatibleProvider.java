package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.ChatMessageDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente de una IA compatible con el formato de chat de OpenAI ({@code POST
 * {BASE_URL}/chat/completions} con {@code Authorization: Bearer {API_KEY}}). Las 3 IAs del proyecto
 * (Ollama Cloud, Google Gemini y OpenRouter) aceptan este formato, así que hay una instancia de esta
 * clase por proveedor y solo cambian la URL base, la clave y el modelo (variables AI_*).
 *
 * <p>Las peticiones se hacen con {@link ExternalHTTPRequestHandler}, igual que el consumo de APIs
 * externas del proyecto de referencia. Los errores HTTP se convierten en errores del catálogo: 429
 * es cuota agotada, 401/403 es clave rechazada y el resto es IA no disponible. Si el modelo
 * principal responde 429 o no está disponible y hay modelo de respaldo, se intenta con el de
 * respaldo.
 */
public class OpenAiCompatibleProvider implements AiProvider {

  /** Logger para registrar los cambios al modelo de respaldo. */
  private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleProvider.class);

  /** Conversor JSON para armar la petición y leer la respuesta. */
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Identificador del proveedor (ollama, gemini u openrouter). */
  private final String id;

  /** Nombre para mostrar del proveedor. */
  private final String label;

  /** Configuración del proveedor (URL base, clave, modelo, modelo de respaldo). */
  private final AppProperties.Ai.Provider cfg;

  /** URL completa del endpoint de chat. */
  private final String endpoint;

  /** Tiempo máximo de espera de cada respuesta. */
  private final Duration timeout;

  /**
   * Crea el cliente de un proveedor.
   *
   * @param id Identificador del proveedor
   * @param label Nombre para mostrar
   * @param cfg Configuración del proveedor
   * @param httpTimeoutSeconds Tiempo máximo de espera de cada respuesta, en segundos
   */
  public OpenAiCompatibleProvider(
      String id, String label, AppProperties.Ai.Provider cfg, int httpTimeoutSeconds) {
    this.id = id;
    this.label = label;
    this.cfg = cfg;
    String base = cfg.baseUrl() == null ? "" : cfg.baseUrl().trim();
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
    this.endpoint = base + "/chat/completions";
    this.timeout = Duration.ofSeconds(httpTimeoutSeconds);
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String label() {
    return label;
  }

  @Override
  public String model() {
    return cfg.model();
  }

  @Override
  public int maxConcurrent() {
    return Math.max(1, cfg.maxConcurrent());
  }

  @Override
  public String baseUrl() {
    return cfg.baseUrl();
  }

  @Override
  public String fallbackModel() {
    return cfg.fallbackModel() == null ? "" : cfg.fallbackModel();
  }

  @Override
  public boolean isConfigured() {
    return !(isBlank(cfg.apiKey()) || isBlank(cfg.baseUrl()) || isBlank(cfg.model()));
  }

  /**
   * Envía la conversación a la IA y devuelve el texto de su respuesta.
   *
   * @param messages Mensajes de la conversación (system/user/assistant)
   * @return Texto de la respuesta de la IA
   * @throws AppException Con el código del catálogo si la IA no está configurada o falla
   */
  @Override
  public String complete(List<ChatMessageDTO> messages) {
    if (!isConfigured()) {
      throw new AppException(
          ErrorCode.AI_NOT_CONFIGURED,
          id + ": falta la clave AI_" + id.toUpperCase() + "_API_KEY en el archivo .env");
    }
    try {
      return call(cfg.model(), messages);
    } catch (AppException e) {
      boolean retryable =
          e.getCode() == ErrorCode.AI_QUOTA_EXCEEDED || e.getCode() == ErrorCode.AI_UNAVAILABLE;
      if (retryable && !isBlank(cfg.fallbackModel())) {
        log.warn(
            "{}: el modelo {} falló ({}); se prueba el de respaldo {}",
            id, cfg.model(), e.getCode(), cfg.fallbackModel());
        return call(cfg.fallbackModel(), messages);
      }
      throw e;
    }
  }

  /** Hace la llamada HTTP a la IA con el modelo indicado y extrae el texto de la respuesta. */
  private String call(String model, List<ChatMessageDTO> messages) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", model);
    body.put("messages", messages);
    body.put("temperature", 0.2);

    ExternalHTTPRequestHandler.ExternalResponse response;
    try {
      response =
          ExternalHTTPRequestHandler.doPost(
              endpoint,
              MAPPER.writeValueAsString(body),
              Map.of("Authorization", "Bearer " + cfg.apiKey().trim(), "Accept", "application/json"),
              timeout);
    } catch (HttpTimeoutException e) {
      throw new AppException(ErrorCode.AI_TIMEOUT, id + ": tiempo de espera agotado", e);
    } catch (IOException e) {
      throw new AppException(ErrorCode.AI_UNAVAILABLE, id + ": sin conexión (" + e + ")", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AppException(ErrorCode.AI_UNAVAILABLE, id + ": llamada interrumpida", e);
    }

    if (!response.isSuccess()) {
      throw mapHttpError(response);
    }

    String content;
    try {
      JsonNode json = MAPPER.readTree(response.body());
      content = json.path("choices").path(0).path("message").path("content").asText("");
    } catch (JsonProcessingException e) {
      throw new AppException(ErrorCode.AI_INVALID_RESPONSE, id + ": la respuesta no es JSON", e);
    }
    if (content.isBlank()) {
      throw new AppException(ErrorCode.AI_INVALID_RESPONSE, id + ": respuesta sin contenido");
    }
    return content;
  }

  /** Convierte un código HTTP de error en un error del catálogo. */
  private AppException mapHttpError(ExternalHTTPRequestHandler.ExternalResponse response) {
    int status = response.statusCode();
    String body = response.body() == null ? "" : response.body();
    String detail = id + " HTTP " + status + ": " + (body.length() > 300 ? body.substring(0, 300) : body);
    ErrorCode code =
        switch (status) {
          case 429 -> ErrorCode.AI_QUOTA_EXCEEDED;
          case 401, 403 -> ErrorCode.AI_AUTH_FAILED;
          default -> ErrorCode.AI_UNAVAILABLE;
        };
    return new AppException(code, detail);
  }

  /** Indica si un texto es null o está vacío. */
  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
