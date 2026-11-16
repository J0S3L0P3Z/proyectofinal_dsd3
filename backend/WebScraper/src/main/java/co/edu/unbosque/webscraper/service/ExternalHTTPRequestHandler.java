package co.edu.unbosque.webscraper.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Clase para manejar peticiones HTTP a servicios externos (basada en ExternalHTTPRequestHandler del
 * proyecto de referencia SpringFirstAppJWT). Usa el cliente HTTP de Java ({@link HttpClient}) con
 * HTTP/2 y un tiempo máximo de conexión de 10 segundos. Se usa para consumir las APIs de las 3 IAs
 * (Ollama Cloud, Google Gemini y OpenRouter).
 *
 * <p>Diferencias con la referencia: los errores de red no se imprimen y se ignoran, sino que se
 * lanzan al que llama (para convertirlos en un error claro del catálogo), cada petición tiene su
 * tiempo máximo de respuesta y las cabeceras (como la clave de la API) se reciben como parámetro en
 * lugar de estar escritas en el código.
 */
public class ExternalHTTPRequestHandler {

  /**
   * Cliente HTTP compartido para realizar peticiones. Configurado con HTTP/2 y un tiempo de espera
   * de conexión de 10 segundos. Es seguro usarlo desde varios hilos a la vez.
   */
  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_2)
          .connectTimeout(Duration.ofSeconds(10))
          .build();

  /**
   * Respuesta de una petición externa.
   *
   * @param statusCode Código de estado HTTP
   * @param body Cuerpo de la respuesta como texto
   */
  public record ExternalResponse(int statusCode, String body) {

    /**
     * Indica si la respuesta fue exitosa (código 2xx).
     *
     * @return true si el código está entre 200 y 299
     */
    public boolean isSuccess() {
      return statusCode >= 200 && statusCode < 300;
    }
  }

  /** Clase utilitaria: no se instancia. */
  private ExternalHTTPRequestHandler() {}

  /**
   * Realiza una petición GET.
   *
   * @param url URL completa del servicio
   * @param headers Cabeceras adicionales (por ejemplo Authorization)
   * @param timeout Tiempo máximo de espera de la respuesta
   * @return Código de estado y cuerpo de la respuesta
   * @throws IOException Si no se pudo conectar o se agotó el tiempo (HttpTimeoutException)
   * @throws InterruptedException Si el hilo fue interrumpido mientras esperaba
   */
  public static ExternalResponse doGet(String url, Map<String, String> headers, Duration timeout)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder().GET().uri(URI.create(url)).timeout(timeout);
    addHeaders(builder, headers);
    return send(builder.build());
  }

  /**
   * Realiza una petición POST con un cuerpo JSON.
   *
   * @param url URL completa del servicio
   * @param json Cuerpo de la petición en formato JSON
   * @param headers Cabeceras adicionales (por ejemplo Authorization)
   * @param timeout Tiempo máximo de espera de la respuesta
   * @return Código de estado y cuerpo de la respuesta
   * @throws IOException Si no se pudo conectar o se agotó el tiempo (HttpTimeoutException)
   * @throws InterruptedException Si el hilo fue interrumpido mientras esperaba
   */
  public static ExternalResponse doPost(
      String url, String json, Map<String, String> headers, Duration timeout)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .uri(URI.create(url))
            .timeout(timeout)
            .header("Content-Type", "application/json");
    addHeaders(builder, headers);
    return send(builder.build());
  }

  /** Agrega las cabeceras recibidas a la petición. */
  private static void addHeaders(HttpRequest.Builder builder, Map<String, String> headers) {
    if (headers == null) return;
    headers.forEach(builder::setHeader);
  }

  /** Envía la petición y devuelve el código y el cuerpo como texto (UTF-8). */
  private static ExternalResponse send(HttpRequest request)
      throws IOException, InterruptedException {
    HttpResponse<String> response =
        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    return new ExternalResponse(response.statusCode(), response.body());
  }
}
