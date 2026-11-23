package co.edu.unbosque.proyectomodulotres.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Clase de utilidad para hacer peticiones HTTP a servicios externos (las API de IA) con el
 * HttpClient de Java.
 */
public class ExternalHTTPRequestHandler {

	/** Cliente HTTP compartido con 30 segundos de tiempo de conexión. */
	private static final HttpClient CLIENTE = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

	/**
	 * Respuesta de una petición externa.
	 *
	 * @param statusCode Código HTTP de la respuesta
	 * @param body Cuerpo de la respuesta
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

	/**
	 * Hace una petición POST con cuerpo JSON.
	 *
	 * @param url URL del servicio
	 * @param body Cuerpo JSON
	 * @param headers Encabezados adicionales (por ejemplo la API key)
	 * @param timeout Tiempo máximo de espera de la respuesta
	 * @return Código y cuerpo de la respuesta
	 * @throws IOException Si ocurre un error de comunicación
	 * @throws InterruptedException Si la petición es interrumpida
	 */
	public static ExternalResponse doPost(String url, String body, Map<String, String> headers, Duration timeout)
			throws IOException, InterruptedException {

		HttpRequest.Builder solicitud = HttpRequest.newBuilder().uri(URI.create(url)).timeout(timeout)
				.POST(HttpRequest.BodyPublishers.ofString(body));

		solicitud.header("Content-Type", "application/json");

		for (Map.Entry<String, String> encabezado : headers.entrySet()) {
			solicitud.header(encabezado.getKey(), encabezado.getValue());
		}

		HttpResponse<String> respuesta = CLIENTE.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());

		return new ExternalResponse(respuesta.statusCode(), respuesta.body());
	}
}
