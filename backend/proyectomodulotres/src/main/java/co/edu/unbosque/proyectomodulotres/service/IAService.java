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

/**
 * Servicio que consume las API de inteligencia artificial (Gemini, Groq y Cloudflare Workers AI).
 * Hace tres análisis generales de la página, identifica las tecnologías del HTML y los nombres de
 * personas del texto, reintenta los errores temporales y guarda cada respuesta como resultado.
 */
@Service
public class IAService {

	/** URL base del API de Gemini. */
	@Value("${app.ai.gemini.base-url}")
	private String geminiBaseUrl;

	/** API key de Gemini. */
	@Value("${app.ai.gemini.api-key}")
	private String geminiApiKey;

	/** URL base del API de Groq. */
	@Value("${app.ai.groq.base-url}")
	private String groqBaseUrl;

	/** API key de Groq. */
	@Value("${app.ai.groq.api-key}")
	private String groqApiKey;

	/** URL base del API de Cloudflare. */
	@Value("${app.ai.cloudflare.base-url}")
	private String cloudflareBaseUrl;

	/** Identificador de la cuenta de Cloudflare. */
	@Value("${app.ai.cloudflare.account-id}")
	private String cloudflareAccountId;

	/** Token del API de Cloudflare. */
	@Value("${app.ai.cloudflare.api-token}")
	private String cloudflareApiToken;

	/** Modelo 1: Gemini, análisis general. */
	@Value("${app.ai.model-1}")
	private String modelo1;

	/** Modelo 2: Groq, análisis general. */
	@Value("${app.ai.model-2}")
	private String modelo2;

	/** Modelo 3: Cloudflare, análisis general. */
	@Value("${app.ai.model-3}")
	private String modelo3;

	/** Modelo 4: Gemini, identificación de tecnologías. */
	@Value("${app.ai.model-4}")
	private String modelo4;

	/** Modelo 5: Gemini, identificación de nombres de personas. */
	@Value("${app.ai.model-5}")
	private String modelo5;

	/** Tiempo máximo de espera de cada solicitud, en segundos. */
	@Value("${app.ai.timeout-seconds:120}")
	private int timeoutSeconds;

	/** Cantidad de reintentos cuando una solicitud falla por un error temporal. */
	@Value("${app.ai.max-retries:3}")
	private int maxRetries;

	/** Convierte las solicitudes y respuestas entre JSON y objetos. */
	private final ObjectMapper objectMapper;

	/** Servicio que guarda los resultados de las IA. */
	private final ResultadoService resultadoService;

	/** Cantidad máxima de caracteres de cada fragmento de texto que se envía a Gemini. */
	private static final int TAMANO_FRAGMENTO = 6000;

	/**
	 * Constructor que recibe el servicio de resultados.
	 *
	 * @param resultadoService Servicio de resultados
	 */
	public IAService(ResultadoService resultadoService) {
		this.objectMapper = new ObjectMapper();
		this.resultadoService = resultadoService;
	}

	/**
	 * Envía el contenido de la página a los cinco análisis de IA.
	 *
	 * @param datosJson Datos estructurados de la página (para los análisis generales)
	 * @param codigoHTML Código HTML de la página (para identificar tecnologías)
	 * @param contenido Texto de la página (para identificar personas)
	 * @return Mapa con el nombre de cada análisis y su respuesta (las respuestas con error empiezan
	 *     por "Error")
	 */
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

	/**
	 * Ejecuta una solicitud a una IA y la repite si falla por un error temporal, esperando más tiempo
	 * en cada intento.
	 *
	 * @param solicitud Solicitud que devuelve la respuesta del modelo
	 * @return Respuesta del modelo o el último mensaje de error
	 */
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

	/**
	 * Indica si un error vale la pena reintentarlo: fallas de comunicación, tiempo agotado o códigos
	 * HTTP 429, 500, 502, 503 y 504.
	 *
	 * @param respuesta Mensaje de error
	 * @return true si el error es temporal
	 */
	private boolean esErrorReintentable(String respuesta) {

		String error = respuesta.toLowerCase();

		return error.contains("comunicacion") || error.contains("timeout") || error.contains("timed out")
				|| error.contains("codigo http: 429") || error.contains("codigo http: 500")
				|| error.contains("codigo http: 502") || error.contains("codigo http: 503")
				|| error.contains("codigo http: 504");
	}

	/**
	 * Devuelve las instrucciones del análisis general de la página.
	 *
	 * @return Instrucciones para el modelo
	 */
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

	/**
	 * Devuelve las instrucciones para identificar las tecnologías del código HTML.
	 *
	 * @return Instrucciones para el modelo
	 */
	private String obtenerInstruccionesTecnologias() {

		return "Identifica las tecnologias, frameworks, " + "bibliotecas y lenguajes de programacion que puedas "
				+ "reconocer en el codigo HTML proporcionado. " + "Puedes incluir tecnologias que consideres probables "
				+ "aunque no puedas confirmarlas completamente. "
				+ "Devuelve solamente sus nombres separados por comas. " + "No incluyas explicaciones. "
				+ "Si no identificas ninguna, responde exactamente: "
				+ "No se pudieron identificar las tecnologías utilizadas.";
	}

	/**
	 * Devuelve las instrucciones para extraer los nombres de personas del texto.
	 *
	 * @return Instrucciones para el modelo
	 */
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

	/**
	 * Identifica los nombres de personas del texto. Divide el texto en fragmentos, envía cada uno a
	 * Gemini y junta los nombres sin repetir.
	 *
	 * @param contenido Texto de la página
	 * @return Nombres separados por comas, el mensaje de que no hay personas o un mensaje de error
	 */
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

	/**
	 * Divide el texto en fragmentos de máximo {@value #TAMANO_FRAGMENTO} caracteres, cortando en un
	 * salto de línea cuando es posible.
	 *
	 * @param contenido Texto a dividir
	 * @return Lista de fragmentos sin vacíos
	 */
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

	/**
	 * Agrega al conjunto los nombres de la respuesta del modelo, limpiando viñetas y números y sin
	 * repetir nombres (sin importar mayúsculas).
	 *
	 * @param personasEncontradas Conjunto donde se acumulan los nombres
	 * @param respuesta Respuesta del modelo con nombres separados por comas
	 */
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

	/**
	 * Hace una solicitud al API de Gemini (generateContent).
	 *
	 * @param modelo Modelo de Gemini
	 * @param instrucciones Instrucciones del sistema
	 * @param datos Contenido que se envía al modelo
	 * @return Texto de la respuesta, o un mensaje que empieza por "Error" si algo falló
	 */
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

	/**
	 * Hace una solicitud al API de chat de Groq (compatible con OpenAI).
	 *
	 * @param modelo Modelo de Groq
	 * @param instrucciones Mensaje de sistema con las instrucciones
	 * @param datos Contenido que se envía al modelo
	 * @return Texto de la respuesta, o un mensaje que empieza por "Error" si algo falló
	 */
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

	/**
	 * Hace una solicitud a Cloudflare Workers AI.
	 *
	 * @param modelo Modelo de Cloudflare
	 * @param instrucciones Mensaje de sistema con las instrucciones
	 * @param datos Contenido que se envía al modelo
	 * @return Texto de la respuesta, o un mensaje que empieza por "Error" si algo falló
	 */
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

	/**
	 * Analiza el contenido con las IA y guarda cada respuesta como un resultado del scraping, con
	 * estado COMPLETADO o ERROR.
	 *
	 * @param idScraping Identificador del scraping
	 * @param datosJson Datos estructurados de la página
	 * @param codigoHTML Código HTML de la página
	 * @param contenido Texto de la página
	 * @return Lista con los resultados guardados
	 * @throws DatoInvalidoException Si el identificador del scraping es nulo
	 */
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