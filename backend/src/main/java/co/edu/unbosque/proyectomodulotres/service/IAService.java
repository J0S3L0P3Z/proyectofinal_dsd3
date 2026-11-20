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

	@Value("${app.ai.openrouter.base-url}")
	private String baseUrl;

	@Value("${app.ai.openrouter.api-key}")
	private String apiKey;

	@Value("${app.ai.openrouter.model-nvidia}")
	private String modeloNvidia;

	@Value("${app.ai.openrouter.model-dots}")
	private String modeloDots;

	@Value("${app.ai.openrouter.model-cohere}")
	private String modeloCohere;

	@Value("${app.ai.openrouter.model-particle}")
	private String modeloParticle;

	@Value("${app.ai.openrouter.model-ling}")
	private String modeloLing;

	@Value("${app.ai.timeout-seconds:60}")
	private int timeoutSeconds;

	private final ObjectMapper objectMapper;
	private final ResultadoService resultadoService;

	public IAService(ResultadoService resultadoService) {
		this.objectMapper = new ObjectMapper();
		this.resultadoService = resultadoService;
	}

	// EJECUTAR LAS CINCO IA SECUENCIALMENTE
	public Map<String, String> analizarContenido(String datosJson, String codigoHTML, String contenido) {

		Map<String, String> resultados = new LinkedHashMap<>();

		// Tres IA para el analisis general.
		resultados.put("NVIDIA", analizarConModelo(modeloNvidia, datosJson));
		resultados.put("DOTS", analizarConModelo(modeloDots, datosJson));
		resultados.put("COHERE", analizarConModelo(modeloCohere, datosJson));

		// IA especializada en tecnologias.
		resultados.put("PARTICLE", analizarConModelo(modeloParticle,
				"Identifica las tecnologias, frameworks, " + "bibliotecas y lenguajes de programacion que puedas "
						+ "reconocer en el codigo HTML proporcionado. "
						+ "Puedes incluir tecnologias que consideres probables "
						+ "aunque no puedas confirmarlas completamente. "
						+ "Devuelve solamente sus nombres separados por comas. " + "No incluyas explicaciones. "
						+ "Si no identificas ninguna, responde exactamente: "
						+ "No se pudieron identificar las tecnologías utilizadas.",
				codigoHTML));

		// IA especializada en nombres de personas.
		resultados.put("LING",
				analizarConModelo(modeloLing, "Eres un especialista en reconocimiento de nombres propios "
						+ "de personas dentro de textos y paginas web. "
						+ "Analiza cuidadosamente TODO el contenido proporcionado "
						+ "y extrae los nombres de todas las personas mencionadas. "

						+ "Debes identificar nombres y apellidos de personas, "
						+ "cientificos, investigadores, escritores, autores, "
						+ "fundadores, personajes historicos y cualquier otra " + "persona mencionada en el texto. "

						+ "No limites la busqueda a la seccion de autores. "
						+ "Revisa tambien los parrafos, ejemplos, referencias " + "y explicaciones del contenido. "

						+ "Si aparece solamente un nombre o un apellido y "
						+ "puedes reconocer que corresponde a una persona, " + "tambien debes incluirlo. "

						+ "No incluyas empresas, marcas, organizaciones, "
						+ "instituciones, lugares ni nombres de tecnologias. "

						+ "No inventes personas que no aparezcan mencionadas. "

						+ "Devuelve exclusivamente los nombres identificados, "
						+ "separados por comas, sin explicaciones, descripciones " + "ni texto adicional. "

						+ "Si no encuentras ningun nombre de persona, " + "responde exactamente: "
						+ "No se identificaron nombres de personas.", contenido));

		return resultados;
	}

	// ENVIAR INFORMACION A UN MODELO ESPECIFICO
	private String analizarConModelo(String modelo, String datos) {
		return analizarConModelo(modelo, "Eres un analista especializado en paginas web. "
				+ "Analiza la informacion proporcionada sin inventar datos.", datos);
	}

	// ENVIAR INFORMACION CON UN PROMPT ESPECIFICO
	private String analizarConModelo(String modelo, String instrucciones, String datos) {

		try {

			ObjectNode solicitud = objectMapper.createObjectNode();

			solicitud.put("model", modelo);
			solicitud.put("temperature", 0.2);

			ArrayNode mensajes = objectMapper.createArrayNode();

			ObjectNode mensajeSistema = objectMapper.createObjectNode();
			mensajeSistema.put("role", "system");
			mensajeSistema.put("content", instrucciones);
			mensajes.add(mensajeSistema);

			ObjectNode mensajeUsuario = objectMapper.createObjectNode();
			mensajeUsuario.put("role", "user");
			mensajeUsuario.put("content", datos == null ? "" : datos);
			mensajes.add(mensajeUsuario);

			solicitud.set("messages", mensajes);

			String cuerpo = objectMapper.writeValueAsString(solicitud);

			Map<String, String> encabezados = new LinkedHashMap<>();
			encabezados.put("Authorization", "Bearer " + apiKey);

			ExternalHTTPRequestHandler.ExternalResponse respuesta = ExternalHTTPRequestHandler
					.doPost(baseUrl + "/chat/completions", cuerpo, encabezados, Duration.ofSeconds(timeoutSeconds));

			if (!respuesta.isSuccess()) {
				return "Error del modelo. Codigo HTTP: " + respuesta.statusCode() + ". Respuesta: " + respuesta.body();
			}

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

	// ANALIZAR CON LAS CINCO IA Y GUARDAR LOS RESULTADOS
	public List<ResultadoDTO> analizarYGuardar(Long idScraping, String datosJson, String codigoHTML, String contenido) {

		List<ResultadoDTO> resultados = new ArrayList<>();

		Map<String, String> respuestas = analizarContenido(datosJson, codigoHTML, contenido);

		for (Map.Entry<String, String> entrada : respuestas.entrySet()) {

			ResultadoDTO resultado = new ResultadoDTO();

			resultado.setNombreIA(entrada.getKey());
			resultado.setRespuesta(entrada.getValue());
			resultado.setIdScraping(idScraping);

			if (entrada.getKey().equals("NVIDIA")) {
				resultado.setModelo(modeloNvidia);

			} else if (entrada.getKey().equals("DOTS")) {
				resultado.setModelo(modeloDots);

			} else if (entrada.getKey().equals("COHERE")) {
				resultado.setModelo(modeloCohere);

			} else if (entrada.getKey().equals("PARTICLE")) {
				resultado.setModelo(modeloParticle);

			} else if (entrada.getKey().equals("LING")) {
				resultado.setModelo(modeloLing);
			}

			if (entrada.getValue().startsWith("Error")) {
				resultado.setEstado("ERROR");
			} else {
				resultado.setEstado("COMPLETADO");
			}

			ResultadoDTO resultadoGuardado = resultadoService.guardar(resultado);

			resultados.add(resultadoGuardado);
		}

		return resultados;
	}
}
