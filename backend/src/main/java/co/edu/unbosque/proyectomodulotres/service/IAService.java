package co.edu.unbosque.proyectomodulotres.service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;

@Service
public class IAService {

	// Direccion base de OpenRouter.
	@Value("${app.ai.openrouter.base-url}")
	private String baseUrl;

	// API Key de OpenRouter.
	@Value("${app.ai.openrouter.api-key}")
	private String apiKey;

	// Modelos que vamos a utilizar.
	@Value("${app.ai.openrouter.model-nvidia}")
	private String modeloNvidia;

	@Value("${app.ai.openrouter.model-qwen}")
	private String modeloQwen;

	@Value("${app.ai.openrouter.model-cohere}")
	private String modeloCohere;

	// Tiempo maximo de espera.
	@Value("${app.ai.timeout-seconds:60}")
	private int timeoutSeconds;

	// Objeto para construir y leer JSON.
	private final ObjectMapper objectMapper;

	// Servicio encargado de guardar los resultados.
	private final ResultadoService resultadoService;

	// Constructor para recibir las dependencias.
	public IAService(ResultadoService resultadoService) {
		this.objectMapper = new ObjectMapper();
		this.resultadoService = resultadoService;
	}

	// Ejecuta el analisis con los tres modelos.
	public Map<String, String> analizarContenido(String contenido) {

		Map<String, String> resultados = new LinkedHashMap<>();

		resultados.put("NVIDIA", analizarConModelo(modeloNvidia, contenido));

		resultados.put("BAIDU", analizarConModelo(modeloQwen, contenido));

		resultados.put("COHERE", analizarConModelo(modeloCohere, contenido));

		return resultados;
	}

	// Envia el contenido a un modelo especifico.
	private String analizarConModelo(String modelo, String contenido) {

		try {

			// Construimos el cuerpo de la solicitud.
			ObjectNode solicitud = objectMapper.createObjectNode();

			solicitud.put("model", modelo);
			solicitud.put("temperature", 0.2);

			// Creamos los mensajes.
			ArrayNode mensajes = objectMapper.createArrayNode();

			ObjectNode mensajeSistema = objectMapper.createObjectNode();

			mensajeSistema.put("role", "system");

			mensajeSistema.put("content",
					"Eres un analista especializado en analizar contenido de paginas web. "
							+ "Identifica el tema principal, resume la informacion, "
							+ "extrae los puntos importantes y presenta conclusiones. "
							+ "No inventes informacion que no aparezca en el contenido.");

			mensajes.add(mensajeSistema);

			ObjectNode mensajeUsuario = objectMapper.createObjectNode();

			mensajeUsuario.put("role", "user");

			mensajeUsuario.put("content", "Analiza el siguiente contenido extraido de una pagina web:\n\n" + contenido);

			mensajes.add(mensajeUsuario);

			solicitud.set("messages", mensajes);

			// Convertimos la solicitud a JSON.
			String cuerpo = objectMapper.writeValueAsString(solicitud);

			// Configuramos la autorizacion.
			Map<String, String> encabezados = new LinkedHashMap<>();

			encabezados.put("Authorization", "Bearer " + apiKey);

			// Enviamos la solicitud a OpenRouter.
			ExternalHTTPRequestHandler.ExternalResponse respuesta = ExternalHTTPRequestHandler
					.doPost(baseUrl + "/chat/completions", cuerpo, encabezados, Duration.ofSeconds(timeoutSeconds));

			// Verificamos si la solicitud fue exitosa.
			if (!respuesta.isSuccess()) {

				return "Error del modelo. Codigo HTTP: " + respuesta.statusCode() + ". Respuesta: " + respuesta.body();
			}

			// Leemos la respuesta JSON.
			JsonNode jsonRespuesta = objectMapper.readTree(respuesta.body());

			JsonNode contenidoRespuesta = jsonRespuesta.path("choices").path(0).path("message").path("content");

			if (contenidoRespuesta.isMissingNode() || contenidoRespuesta.isNull()) {

				return "Error: el modelo no devolvio una respuesta valida.";
			}

			return contenidoRespuesta.asText();

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();

			return "Error: la solicitud al modelo fue interrumpida.";

		} catch (IOException e) {

			return "Error de comunicacion con el modelo: " + e.getMessage();

		} catch (Exception e) {

			return "Error al procesar la respuesta del modelo: " + e.getMessage();
		}
	}

	// Analiza el contenido con las tres IA y guarda sus resultados.
	public List<ResultadoDTO> analizarYGuardar(Long idScraping, String contenido) {

		List<ResultadoDTO> resultados = new ArrayList<>();

		// Ejecutamos el analisis con los tres modelos.
		Map<String, String> respuestas = analizarContenido(contenido);

		// Recorremos las respuestas obtenidas.
		for (Map.Entry<String, String> entrada : respuestas.entrySet()) {

			ResultadoDTO resultado = new ResultadoDTO();

			resultado.setNombreIA(entrada.getKey());

			resultado.setRespuesta(entrada.getValue());

			resultado.setIdScraping(idScraping);

			// Identificamos el modelo utilizado.
			if (entrada.getKey().equals("NVIDIA")) {

				resultado.setModelo(modeloNvidia);

			} else if (entrada.getKey().equals("BAIDU")) {

				resultado.setModelo(modeloQwen);

			} else if (entrada.getKey().equals("COHERE")) {

				resultado.setModelo(modeloCohere);
			}

			// Determinamos si el analisis fue exitoso.
			if (entrada.getValue().startsWith("Error")) {

				resultado.setEstado("ERROR");

			} else {

				resultado.setEstado("COMPLETADO");
			}

			// Guardamos el resultado en MySQL.
			ResultadoDTO resultadoGuardado = resultadoService.guardar(resultado);

			resultados.add(resultadoGuardado);
		}

		return resultados;
	}
}