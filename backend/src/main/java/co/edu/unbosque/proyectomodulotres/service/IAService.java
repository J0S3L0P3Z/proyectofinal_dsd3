package co.edu.unbosque.proyectomodulotres.service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.exception.AnalisisIAException;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;

@Service
public class IAService {

	@Value("${app.ai.gemini.base-url}")
	private String geminiBaseUrl;

	@Value("${app.ai.gemini.api-key}")
	private String geminiApiKey;

	@Value("${app.ai.groq.base-url}")
	private String groqBaseUrl;

	@Value("${app.ai.groq.api-key}")
	private String groqApiKey;

	@Value("${app.ai.cloudflare.base-url}")
	private String cloudflareBaseUrl;

	@Value("${app.ai.cloudflare.account-id}")
	private String cloudflareAccountId;

	@Value("${app.ai.cloudflare.api-token}")
	private String cloudflareApiToken;

	@Value("${app.ai.model-1}")
	private String modelo1;

	@Value("${app.ai.model-2}")
	private String modelo2;

	@Value("${app.ai.model-3}")
	private String modelo3;

	@Value("${app.ai.model-4}")
	private String modelo4;

	@Value("${app.ai.model-5}")
	private String modelo5;

	@Value("${app.ai.timeout-seconds:120}")
	private int timeoutSeconds;

	@Value("${app.ai.max-retries:3}")
	private int maxRetries;

	private final ObjectMapper objectMapper;
	private final ResultadoService resultadoService;

	private static final int TAMANO_FRAGMENTO = 6000;

	public IAService(ResultadoService resultadoService) {
		this.objectMapper = new ObjectMapper();
		this.resultadoService = resultadoService;
	}

	public Map<String, String> analizarContenido(String datosJson, String codigoHTML, String contenido) {

		Map<String, String> resultados = new LinkedHashMap<>();

		String instruccionesGenerales = obtenerInstruccionesGenerales();

		resultados.put("GEMINI_GENERAL",
				ejecutarConReintentos(() -> analizarConGemini(modelo1, instruccionesGenerales, datosJson)));

		resultados.put("GROQ_GENERAL",
				ejecutarConReintentos(() -> analizarConGroq(modelo2, instruccionesGenerales, datosJson)));

		resultados.put("CLOUDFLARE_GENERAL",
				ejecutarConReintentos(() -> analizarConCloudflare(modelo3, instruccionesGenerales, datosJson)));

		resultados.put("GEMINI_TECNOLOGIAS",
				ejecutarConReintentos(() -> analizarConGemini(modelo4, obtenerInstruccionesTecnologias(), codigoHTML)));

		resultados.put("GEMINI_PERSONAS", ejecutarConReintentos(() -> analizarPersonasConGemini(contenido)));

		return resultados;
	}

	private String ejecutarConReintentos(Supplier<String> solicitud) {

		String respuesta = "";

		for (int intento = 0; intento <= maxRetries; intento++) {

			respuesta = solicitud.get();

			if (!respuesta.startsWith("Error")) {
				return respuesta;
			}

			if (!esErrorReintentable(respuesta)) {
				return respuesta;
			}

			if (Thread.currentThread().isInterrupted()) {
				return respuesta;
			}

			if (intento < maxRetries) {

				try {
					Thread.sleep(2000L * (intento + 1));

				} catch (InterruptedException e) {

					Thread.currentThread().interrupt();
					return "Error: los reintentos fueron interrumpidos.";
				}
			}
		}

		return respuesta;
	}

	private boolean esErrorReintentable(String respuesta) {

		String error = respuesta.toLowerCase();

		return error.contains("comunicacion") || error.contains("timeout") || error.contains("timed out")
				|| error.contains("codigo http: 429") || error.contains("codigo http: 500")
				|| error.contains("codigo http: 502") || error.contains("codigo http: 503")
				|| error.contains("codigo http: 504");
	}

	private String obtenerInstruccionesGenerales() {

		return "Eres un analista especializado en paginas web. "
				+ "Analiza detalladamente la informacion proporcionada y presenta un informe organizado. "
				+ "Debes incluir los siguientes apartados: " + "1. Tema principal: explica de que trata la pagina web. "
				+ "2. Informacion que contiene: describe los temas, datos y contenidos mas importantes encontrados en la pagina. "
				+ "3. Cantidad de imagenes: indica cuantas imagenes contiene la pagina utilizando los datos proporcionados. "
				+ "4. Conclusion: presenta una conclusion general que resuma el contenido y la finalidad de la pagina. "
				+ "IMPORTANTE: la respuesta debe estar escrita exclusivamente en texto normal. Utiliza titulos sencillos y parrafos. "
				+ "ESTA PROHIBIDO utilizar tablas de cualquier tipo. "
				+ "No utilices tablas Markdown, barras verticales, separadores de columnas, filas, guiones para crear tablas "
				+ "ni estructuras que representen informacion en filas y columnas. "
				+ "Tampoco utilices bloques de codigo ni formato Markdown. "
				+ "Cuando necesites comparar o enumerar informacion, explicala mediante oraciones y parrafos normales. "
				+ "Utiliza un lenguaje claro, profesional y detallado. "
				+ "No inventes informacion ni agregues datos externos. "
				+ "Si algun dato no se encuentra disponible, indicalo claramente.";
	}

	private String obtenerInstruccionesTecnologias() {

		return "Identifica las tecnologias, frameworks, " + "bibliotecas y lenguajes de programacion que puedas "
				+ "reconocer en el codigo HTML proporcionado. " + "Puedes incluir tecnologias que consideres probables "
				+ "aunque no puedas confirmarlas completamente. "
				+ "Devuelve solamente sus nombres separados por comas. " + "No incluyas explicaciones. "
				+ "Si no identificas ninguna, responde exactamente: "
				+ "No se pudieron identificar las tecnologías utilizadas.";
	}

	private String obtenerInstruccionesPersonas() {

		return "Eres un especialista en reconocimiento de nombres propios de personas. "
				+ "Analiza TODO el fragmento de texto proporcionado. "
				+ "Extrae los nombres de todas las personas que aparezcan mencionadas, "
				+ "incluyendo nombres y apellidos, nombres completos, nombres parciales "
				+ "cuando sea posible reconocer que corresponden a una persona. "
				+ "Incluye personajes historicos, politicos, cientificos, investigadores, "
				+ "escritores, autores, fundadores, artistas, deportistas y cualquier otra persona. "
				+ "Revisa todas las partes del texto, incluyendo titulos, parrafos, "
				+ "ejemplos, referencias, articulos relacionados, preguntas frecuentes " + "y cualquier otra seccion. "
				+ "No excluyas personas por ser personajes historicos o por pertenecer " + "a una epoca antigua. "
				+ "No incluyas empresas, marcas, organizaciones, instituciones, " + "lugares, paises ni tecnologias. "
				+ "No inventes nombres ni agregues personas que no aparezcan en el texto. "
				+ "Devuelve exclusivamente los nombres de las personas identificadas, " + "separados por comas. "
				+ "No incluyas explicaciones, introducciones, conclusiones ni encabezados. "
				+ "Si no encuentras ningun nombre de persona, responde exactamente: "
				+ "No se identificaron nombres de personas.";
	}

	private String analizarPersonasConGemini(String contenido) {

		if (contenido == null || contenido.trim().isEmpty()) {
			return "No se identificaron nombres de personas.";
		}

		List<String> fragmentos = dividirContenido(contenido);

		Set<String> personasEncontradas = new LinkedHashSet<>();

		for (int i = 0; i < fragmentos.size(); i++) {

			String fragmento = fragmentos.get(i);

			String respuesta = ejecutarConReintentos(
					() -> analizarConGemini(modelo5, obtenerInstruccionesPersonas(), fragmento));

			if (respuesta.startsWith("Error")) {
				return "Error: no se pudo analizar el fragmento " + (i + 1) + " del contenido. " + respuesta;
			}

			if (respuesta.trim().equalsIgnoreCase("No disponible")) {
				return "Error: Gemini devolvio una respuesta no valida al identificar personas.";
			}

			agregarPersonas(personasEncontradas, respuesta);
		}

		if (personasEncontradas.isEmpty()) {
			return "No se identificaron nombres de personas.";
		}

		return String.join(", ", personasEncontradas);
	}

	private List<String> dividirContenido(String contenido) {

		List<String> fragmentos = new ArrayList<>();

		int inicio = 0;

		while (inicio < contenido.length()) {

			int fin = Math.min(inicio + TAMANO_FRAGMENTO, contenido.length());

			if (fin < contenido.length()) {

				int ultimoSalto = contenido.lastIndexOf('\n', fin);

				if (ultimoSalto > inicio) {
					fin = ultimoSalto;
				}
			}

			if (fin <= inicio) {
				fin = Math.min(inicio + TAMANO_FRAGMENTO, contenido.length());
			}

			String fragmento = contenido.substring(inicio, fin).trim();

			if (!fragmento.isEmpty()) {
				fragmentos.add(fragmento);
			}

			inicio = fin;
		}

		return fragmentos;
	}

	private void agregarPersonas(Set<String> personasEncontradas, String respuesta) {

		String respuestaLimpia = respuesta.trim();

		if (respuestaLimpia.isEmpty() || respuestaLimpia.equalsIgnoreCase("No se identificaron nombres de personas.")
				|| respuestaLimpia.equalsIgnoreCase("No se identificaron personas.")) {
			return;
		}

		String[] nombres = respuestaLimpia.split("[,;\\n]+");

		for (String nombre : nombres) {

			String nombreLimpio = nombre.trim();

			nombreLimpio = nombreLimpio.replaceAll("^[\\s\\-•*\\d.)]+", "");
			nombreLimpio = nombreLimpio.replaceAll("[\\s.]+$", "");

			if (nombreLimpio.isEmpty()) {
				continue;
			}

			if (nombreLimpio.equalsIgnoreCase("No se identificaron nombres de personas.")
					|| nombreLimpio.equalsIgnoreCase("No se identificaron personas.")) {
				continue;
			}

			boolean existe = false;

			for (String persona : personasEncontradas) {
				if (persona.equalsIgnoreCase(nombreLimpio)) {
					existe = true;
					break;
				}
			}

			if (!existe) {
				personasEncontradas.add(nombreLimpio);
			}
		}
	}

	private String analizarConGemini(String modelo, String instrucciones, String datos) {

		try {

			ObjectNode solicitud = objectMapper.createObjectNode();

			ObjectNode instruccionesSistema = objectMapper.createObjectNode();
			ArrayNode partesSistema = objectMapper.createArrayNode();

			ObjectNode parteSistema = objectMapper.createObjectNode();
			parteSistema.put("text", instrucciones);

			partesSistema.add(parteSistema);
			instruccionesSistema.set("parts", partesSistema);

			solicitud.set("systemInstruction", instruccionesSistema);

			ArrayNode contenidos = objectMapper.createArrayNode();

			ObjectNode contenidoUsuario = objectMapper.createObjectNode();
			contenidoUsuario.put("role", "user");

			ArrayNode partesUsuario = objectMapper.createArrayNode();

			ObjectNode parteUsuario = objectMapper.createObjectNode();
			parteUsuario.put("text", datos == null ? "" : datos);

			partesUsuario.add(parteUsuario);
			contenidoUsuario.set("parts", partesUsuario);

			contenidos.add(contenidoUsuario);
			solicitud.set("contents", contenidos);

			ObjectNode configuracion = objectMapper.createObjectNode();
			configuracion.put("temperature", 0.2);

			solicitud.set("generationConfig", configuracion);

			String cuerpo = objectMapper.writeValueAsString(solicitud);

			Map<String, String> encabezados = new LinkedHashMap<>();
			encabezados.put("x-goog-api-key", geminiApiKey);

			String url = geminiBaseUrl + "/models/" + modelo + ":generateContent";

			ExternalHTTPRequestHandler.ExternalResponse respuesta = ExternalHTTPRequestHandler.doPost(url, cuerpo,
					encabezados, Duration.ofSeconds(timeoutSeconds));

			if (!respuesta.isSuccess()) {
				throw new AnalisisIAException(
						"Error de Gemini. Codigo HTTP: " + respuesta.statusCode() + ". Respuesta: " + respuesta.body());
			}

			JsonNode jsonRespuesta = objectMapper.readTree(respuesta.body());

			JsonNode partes = jsonRespuesta.path("candidates").path(0).path("content").path("parts");

			StringBuilder texto = new StringBuilder();

			for (JsonNode parte : partes) {
				if (parte.has("text")) {
					texto.append(parte.path("text").asText());
				}
			}

			if (texto.length() == 0) {
				throw new AnalisisIAException("Gemini no devolvio una respuesta valida.");
			}

			return texto.toString();

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
			return "Error: la solicitud a Gemini fue interrumpida.";

		} catch (AnalisisIAException e) {

			return "Error: " + e.getMessage();

		} catch (IOException e) {

			return "Error de comunicacion con Gemini: " + e.getMessage();

		} catch (Exception e) {

			return "Error al procesar la respuesta de Gemini: " + e.getMessage();
		}
	}

	private String analizarConGroq(String modelo, String instrucciones, String datos) {

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
			encabezados.put("Authorization", "Bearer " + groqApiKey);

			ExternalHTTPRequestHandler.ExternalResponse respuesta = ExternalHTTPRequestHandler
					.doPost(groqBaseUrl + "/chat/completions", cuerpo, encabezados, Duration.ofSeconds(timeoutSeconds));

			if (!respuesta.isSuccess()) {
				throw new AnalisisIAException(
						"Error de Groq. Codigo HTTP: " + respuesta.statusCode() + ". Respuesta: " + respuesta.body());
			}

			JsonNode jsonRespuesta = objectMapper.readTree(respuesta.body());

			JsonNode contenidoRespuesta = jsonRespuesta.path("choices").path(0).path("message").path("content");

			if (contenidoRespuesta.isMissingNode() || contenidoRespuesta.isNull()
					|| contenidoRespuesta.asText().trim().isEmpty()) {
				throw new AnalisisIAException("Groq no devolvio una respuesta valida.");
			}

			return contenidoRespuesta.asText();

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
			return "Error: la solicitud a Groq fue interrumpida.";

		} catch (AnalisisIAException e) {

			return "Error: " + e.getMessage();

		} catch (IOException e) {

			return "Error de comunicacion con Groq: " + e.getMessage();

		} catch (Exception e) {

			return "Error al procesar la respuesta de Groq: " + e.getMessage();
		}
	}

	private String analizarConCloudflare(String modelo, String instrucciones, String datos) {

		try {

			ObjectNode solicitud = objectMapper.createObjectNode();

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
			encabezados.put("Authorization", "Bearer " + cloudflareApiToken);

			String url = cloudflareBaseUrl + "/" + cloudflareAccountId + "/ai/run/" + modelo;

			ExternalHTTPRequestHandler.ExternalResponse respuesta = ExternalHTTPRequestHandler.doPost(url, cuerpo,
					encabezados, Duration.ofSeconds(timeoutSeconds));

			if (!respuesta.isSuccess()) {
				throw new AnalisisIAException("Error de Cloudflare. Codigo HTTP: " + respuesta.statusCode()
						+ ". Respuesta: " + respuesta.body());
			}

			JsonNode jsonRespuesta = objectMapper.readTree(respuesta.body());

			if (!jsonRespuesta.path("success").asBoolean(false)) {
				throw new AnalisisIAException(
						"Cloudflare no pudo procesar la solicitud: " + jsonRespuesta.path("errors").toString());
			}

			JsonNode contenidoRespuesta = jsonRespuesta.path("result").path("response");

			if (contenidoRespuesta.isMissingNode() || contenidoRespuesta.isNull()) {
				contenidoRespuesta = jsonRespuesta.path("result").path("choices").path(0).path("message")
						.path("content");
			}

			if (contenidoRespuesta.isMissingNode() || contenidoRespuesta.isNull()) {
				throw new AnalisisIAException("Cloudflare no devolvio una respuesta valida.");
			}

			return contenidoRespuesta.asText();

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
			return "Error: la solicitud a Cloudflare fue interrumpida.";

		} catch (AnalisisIAException e) {

			return "Error: " + e.getMessage();

		} catch (IOException e) {

			return "Error de comunicacion con Cloudflare: " + e.getMessage();

		} catch (Exception e) {

			return "Error al procesar la respuesta de Cloudflare: " + e.getMessage();
		}
	}

	public List<ResultadoDTO> analizarYGuardar(Long idScraping, String datosJson, String codigoHTML, String contenido) {

		if (idScraping == null) {
			throw new DatoInvalidoException("El identificador del scraping es obligatorio.");
		}

		List<ResultadoDTO> resultados = new ArrayList<>();

		Map<String, String> respuestas = analizarContenido(datosJson, codigoHTML, contenido);

		for (Map.Entry<String, String> entrada : respuestas.entrySet()) {

			ResultadoDTO resultado = new ResultadoDTO();

			resultado.setNombreIA(entrada.getKey());
			resultado.setRespuesta(entrada.getValue());
			resultado.setIdScraping(idScraping);

			if (entrada.getKey().equals("GEMINI_GENERAL")) {
				resultado.setModelo(modelo1);

			} else if (entrada.getKey().equals("GROQ_GENERAL")) {
				resultado.setModelo(modelo2);

			} else if (entrada.getKey().equals("CLOUDFLARE_GENERAL")) {
				resultado.setModelo(modelo3);

			} else if (entrada.getKey().equals("GEMINI_TECNOLOGIAS")) {
				resultado.setModelo(modelo4);

			} else if (entrada.getKey().equals("GEMINI_PERSONAS")) {
				resultado.setModelo(modelo5);
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