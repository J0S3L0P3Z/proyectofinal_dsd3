package co.edu.unbosque.proyectomodulotres.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class ExternalHTTPRequestHandler {

	private static final HttpClient CLIENTE = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();

	public record ExternalResponse(int statusCode, String body) {

		public boolean isSuccess() {
			return statusCode >= 200 && statusCode < 300;
		}
	}

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