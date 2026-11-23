package co.edu.unbosque.proyectomodulotres.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.edu.unbosque.proyectomodulotres.dto.ScrapingDTO;
import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.dto.ReporteDTO;
import co.edu.unbosque.proyectomodulotres.dto.HistorialDTO;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.repository.ScrapingRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.exception.ScrapingException;
import co.edu.unbosque.proyectomodulotres.util.ExtractorHTML;

/**
 * Servicio de scraping. Abre la página con Selenium (o la descarga sin navegador si el servidor no
 * tiene Edge), extrae su contenido, la analiza con las IA, guarda el historial, genera el reporte PDF
 * y envía el correo al usuario.
 */
@Service
@Transactional
public class ScrapingService {

	/** Logger para registrar avisos y errores del proceso. */
	private static final Logger logger = LoggerFactory.getLogger(ScrapingService.class);

	/** Repositorio de scrapings. */
	private final ScrapingRepository scrapingRepository;

	/** Proveedor de navegadores (uno nuevo por scraping). */
	private final ObjectProvider<WebDriver> webDriverProvider;

	/** Servicio de las IA. */
	private final IAService iaService;

	/** Servicio de reportes PDF. */
	private final ReporteService reporteService;

	/** Repositorio de usuarios. */
	private final UserRepository userRepository;

	/** Servicio de correo. */
	private final NotificacionEmailService notificacionEmailService;

	/** Servicio del historial. */
	private final HistorialService historialService;

	/** Identificación de navegador que se envía al descargar sin Selenium (muchos sitios bloquean Java). */
	private static final String AGENTE_NAVEGADOR = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
			+ "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0";

	/** Cliente HTTP para descargar las páginas cuando no hay navegador. Sigue las redirecciones. */
	private final HttpClient clienteHttp = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
			.connectTimeout(Duration.ofSeconds(20)).build();

	/** Indica si el navegador (Edge) se pudo abrir. Pasa a false la primera vez que no se puede. */
	private volatile boolean navegadorDisponible = true;

	/**
	 * Constructor que recibe las dependencias del servicio.
	 *
	 * @param scrapingRepository Repositorio de scrapings
	 * @param webDriverProvider Proveedor de navegadores
	 * @param iaService Servicio de las IA
	 * @param reporteService Servicio de reportes PDF
	 * @param userRepository Repositorio de usuarios
	 * @param notificacionEmailService Servicio de correo
	 * @param historialService Servicio del historial
	 */
	public ScrapingService(ScrapingRepository scrapingRepository, ObjectProvider<WebDriver> webDriverProvider,
			IAService iaService, ReporteService reporteService, UserRepository userRepository,
			NotificacionEmailService notificacionEmailService, HistorialService historialService) {

		this.scrapingRepository = scrapingRepository;
		this.webDriverProvider = webDriverProvider;
		this.iaService = iaService;
		this.reporteService = reporteService;
		this.userRepository = userRepository;
		this.notificacionEmailService = notificacionEmailService;
		this.historialService = historialService;
	}

	/**
	 * Realiza el scraping de una URL. Si la página responde con un mensaje de error (403, 404, etc.)
	 * el scraping queda en estado ERROR y se avisa por correo; si no, se analiza con las IA, se
	 * genera el PDF y se envía el correo con el reporte.
	 *
	 * @param datos DTO con la URL y el identificador del usuario
	 * @return DTO con el resultado del scraping
	 * @throws DatoInvalidoException Si faltan datos o la URL no empieza por http:// o https://
	 * @throws RecursoNoEncontradoException Si el usuario no existe
	 * @throws ScrapingException Si ocurre un error durante el proceso
	 */
	public ScrapingDTO realizarScraping(ScrapingDTO datos) {

		if (datos == null) {
			throw new DatoInvalidoException("Debe ingresar los datos del scraping.");
		}

		if (datos.getUrl() == null || datos.getUrl().isBlank()) {
			throw new DatoInvalidoException("Debe ingresar una URL.");
		}

		if (!datos.getUrl().startsWith("http://") && !datos.getUrl().startsWith("https://")) {
			throw new DatoInvalidoException("La URL debe comenzar con http:// o https://.");
		}

		if (datos.getIdUsuario() == null) {
			throw new DatoInvalidoException("Debe ingresar el ID del usuario.");
		}

		if (!userRepository.existsById(datos.getIdUsuario())) {
			throw new RecursoNoEncontradoException("No se encontro el usuario indicado.");
		}

		try {

			PaginaExtraida pagina = extraerPagina(datos.getUrl());

			String titulo = pagina.titulo();
			String contenido = pagina.contenido();
			String codigoHTML = pagina.codigoHTML();

			int cantidadImagenes = pagina.cantidadImagenes();
			int cantidadEnlaces = pagina.cantidadEnlaces();

			int cantidadPalabras = 0;

			if (contenido != null && !contenido.isBlank()) {
				cantidadPalabras = contenido.trim().split("\\s+").length;
			}

			Scraping scraping = new Scraping(datos.getUrl(), datos.getIdUsuario());

			scraping.setTituloPagina(titulo);
			scraping.setContenidoExtraido(contenido);
			scraping.setCantidadImagenes(cantidadImagenes);
			scraping.setCantidadEnlaces(cantidadEnlaces);
			scraping.setCantidadPalabras(cantidadPalabras);

			scraping.setTecnologiasIdentificadas("No se pudieron identificar las tecnologías utilizadas.");
			scraping.setNombresPersonas("No se identificaron nombres de personas.");

			String datosJson = "{" + "\"titulo\":\"" + escaparJSON(titulo) + "\"," + "\"url\":\""
					+ escaparJSON(datos.getUrl()) + "\"," + "\"contenido\":\"" + escaparJSON(contenido) + "\","
					+ "\"cantidadImagenes\":" + cantidadImagenes + "," + "\"cantidadEnlaces\":" + cantidadEnlaces + ","
					+ "\"cantidadPalabras\":" + cantidadPalabras + "}";

			scraping.setDatosJson(datosJson);

			if (esContenidoError(contenido)) {

				scraping.setEstado("ERROR");

				Scraping guardado = scrapingRepository.save(scraping);

				HistorialDTO historial = new HistorialDTO(null, null,
						"Scraping fallido: el sitio web rechazo el acceso. URL: " + guardado.getUrl(),
						guardado.getEstado(), guardado.getIdUsuario(), guardado.getIdScraping());

				historialService.guardar(historial);

				try {

					User usuario = userRepository.findById(guardado.getIdUsuario()).orElseThrow(
							() -> new RecursoNoEncontradoException("No se encontro el usuario del scraping."));

					String nombreUsuario = usuario.getNombre() + " " + usuario.getApellido();

					notificacionEmailService.enviarNotificacion(usuario.getUsername(), nombreUsuario, guardado.getUrl(),
							"ERROR",
							"No fue posible realizar el scraping porque el sitio web rechazo la solicitud de acceso. "
									+ "El servidor devolvio una respuesta 403 Forbidden. "
									+ "Por este motivo, no se realizaron los analisis de inteligencia artificial "
									+ "ni se genero el reporte PDF.",
							null);

				} catch (Exception e) {

					logger.error("El scraping fallo y no se pudo enviar el correo informativo.", e);
				}

				logger.warn("El sitio web rechazo el acceso durante el scraping: {}", guardado.getUrl());

				return convertirDTO(guardado);
			}

			scraping.setEstado("COMPLETADO");

			Scraping guardado = scrapingRepository.save(scraping);

			if (datosJson != null && !datosJson.isBlank()) {

				List<ResultadoDTO> resultadosIA = iaService.analizarYGuardar(guardado.getIdScraping(), datosJson,
						codigoHTML, contenido);

				for (ResultadoDTO resultado : resultadosIA) {

					if (resultado.getRespuesta() != null && !resultado.getRespuesta().startsWith("Error")) {

						if ("GEMINI_TECNOLOGIAS".equals(resultado.getNombreIA())) {
							guardado.setTecnologiasIdentificadas(resultado.getRespuesta());
						}

						if ("GEMINI_PERSONAS".equals(resultado.getNombreIA())) {
							guardado.setNombresPersonas(resultado.getRespuesta());
						}
					}
				}

				guardado = scrapingRepository.save(guardado);
			}

			HistorialDTO historial = new HistorialDTO(null, null, "Scraping realizado: " + guardado.getUrl(),
					guardado.getEstado(), guardado.getIdUsuario(), guardado.getIdScraping());

			historialService.guardar(historial);

			ReporteDTO reporte = reporteService.generarPDF(guardado.getIdScraping());

			try {

				User usuario = userRepository.findById(guardado.getIdUsuario())
						.orElseThrow(() -> new RecursoNoEncontradoException("No se encontro el usuario del scraping."));

				File archivoPDF = Paths.get(reporte.getRutaArchivo()).toFile();

				String nombreUsuario = usuario.getNombre() + " " + usuario.getApellido();

				notificacionEmailService.enviarNotificacion(usuario.getUsername(), nombreUsuario, guardado.getUrl(),
						guardado.getEstado(),
						"El scraping, el analisis de inteligencia artificial y la generacion del reporte finalizaron correctamente.",
						archivoPDF);

			} catch (Exception e) {

				logger.error("El scraping termino correctamente, pero no se pudo enviar el correo.", e);
			}

			return convertirDTO(guardado);

		} catch (ScrapingException e) {

			throw e;

		} catch (Exception e) {

			throw new ScrapingException("Error al realizar el scraping: " + e.getMessage());
		}
	}

	/**
	 * Obtiene el contenido de la página. Primero intenta con el navegador (Selenium con Edge); si el
	 * servidor no tiene Edge o el navegador falla, descarga la página directamente con HttpClient.
	 * Cuando el navegador no se puede abrir, se recuerda para no volver a intentarlo en los siguientes
	 * scrapings.
	 *
	 * @param url URL de la página
	 * @return Datos extraídos de la página
	 * @throws ScrapingException Si la página no se puede obtener de ninguna de las dos formas
	 */
	private PaginaExtraida extraerPagina(String url) {

		if (!navegadorDisponible) {
			return extraerSinNavegador(url);
		}

		WebDriver navegador;

		try {

			navegador = webDriverProvider.getObject();

		} catch (Exception e) {

			navegadorDisponible = false;
			logger.warn("No se pudo abrir el navegador (Edge). Las paginas se descargaran sin navegador. Motivo: {}",
					e.getMessage());

			return extraerSinNavegador(url);
		}

		try {

			return extraerConNavegador(navegador, url);

		} catch (ScrapingException e) {

			throw e;

		} catch (Exception e) {

			logger.warn("El navegador fallo al abrir {}. Se intenta sin navegador. Motivo: {}", url, e.getMessage());

			return extraerSinNavegador(url);

		} finally {

			try {
				navegador.quit();
			} catch (Exception e) {
				logger.warn("No se pudo cerrar el navegador.", e);
			}
		}
	}

	/**
	 * Abre la página en el navegador, espera a que cargue, baja para cargar el contenido adicional y
	 * extrae título, texto, HTML, imágenes y enlaces.
	 *
	 * @param navegador Navegador abierto
	 * @param url URL de la página
	 * @return Datos extraídos de la página
	 */
	private PaginaExtraida extraerConNavegador(WebDriver navegador, String url) {

		navegador.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));

		try {
			navegador.get(url);
		} catch (TimeoutException e) {
			logger.warn("La pagina excedio el tiempo de carga: {}", url);
		}

		esperarCarga(navegador);

		cargarContenidoAdicional(navegador);

		return new PaginaExtraida(navegador.getTitle(), obtenerTextoVisible(navegador), navegador.getPageSource(),
				navegador.findElements(By.tagName("img")).size(), navegador.findElements(By.tagName("a")).size());
	}

	/**
	 * Descarga la página con HttpClient (sin navegador) y extrae título, texto, imágenes y enlaces del
	 * HTML. Si el sitio responde con un código de error (403, 404, 500...), el contenido queda como el
	 * mensaje de ese error para que el scraping se marque como ERROR.
	 *
	 * @param url URL de la página
	 * @return Datos extraídos de la página
	 * @throws ScrapingException Si la página no se puede descargar
	 */
	private PaginaExtraida extraerSinNavegador(String url) {

		try {

			HttpRequest peticion = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(40))
					.header("User-Agent", AGENTE_NAVEGADOR)
					.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
					.header("Accept-Language", "es-CO,es;q=0.9,en;q=0.8").GET().build();

			HttpResponse<String> respuesta = clienteHttp.send(peticion, HttpResponse.BodyHandlers.ofString());

			String html = respuesta.body() != null ? respuesta.body() : "";

			if (respuesta.statusCode() >= 400) {

				String mensaje = mensajeDeError(respuesta.statusCode());

				logger.warn("El sitio respondio {} al descargar {}", respuesta.statusCode(), url);

				return new PaginaExtraida(mensaje, mensaje, html, 0, 0);
			}

			return new PaginaExtraida(ExtractorHTML.obtenerTitulo(html), ExtractorHTML.obtenerTexto(html), html,
					ExtractorHTML.contarEtiquetas(html, "img"), ExtractorHTML.contarEtiquetas(html, "a"));

		} catch (IllegalArgumentException e) {

			throw new ScrapingException("La URL no es valida: " + url);

		} catch (IOException e) {

			throw new ScrapingException("No fue posible descargar la pagina: " + e.getMessage());

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
			throw new ScrapingException("Se interrumpio la descarga de la pagina.");
		}
	}

	/**
	 * Convierte un código HTTP de error en el mensaje que reconoce {@link #esContenidoError(String)}.
	 *
	 * @param codigo Código HTTP de la respuesta
	 * @return Mensaje del error
	 */
	private String mensajeDeError(int codigo) {

		return switch (codigo) {
		case 403 -> "403 Forbidden";
		case 404 -> "404 Not Found";
		case 500 -> "500 Internal Server Error";
		case 502 -> "502 Bad Gateway";
		case 503 -> "503 Service Unavailable";
		case 504 -> "504 Gateway Timeout";
		default -> "Access denied";
		};
	}

	/**
	 * Datos que se extraen de una página, ya sea con el navegador o sin él.
	 *
	 * @param titulo Título de la página
	 * @param contenido Texto visible
	 * @param codigoHTML Código HTML completo
	 * @param cantidadImagenes Cantidad de imágenes
	 * @param cantidadEnlaces Cantidad de enlaces
	 */
	private record PaginaExtraida(String titulo, String contenido, String codigoHTML, int cantidadImagenes,
			int cantidadEnlaces) {
	}

	/**
	 * Indica si el contenido de la página es solo un mensaje de error del servidor (por ejemplo "403
	 * Forbidden").
	 *
	 * @param contenido Texto de la página
	 * @return true si el contenido es un mensaje de error
	 */
	private boolean esContenidoError(String contenido) {

		if (contenido == null || contenido.isBlank()) {
			return false;
		}

		String texto = contenido.trim().toLowerCase();

		return texto.equals("403 forbidden") || texto.equals("404 not found")
				|| texto.equals("500 internal server error") || texto.equals("502 bad gateway")
				|| texto.equals("503 service unavailable") || texto.equals("504 gateway timeout")
				|| texto.equals("access denied") || texto.equals("forbidden");
	}

	/**
	 * Espera hasta 15 segundos a que el documento termine de cargar.
	 *
	 * @param navegador Navegador con la página abierta
	 */
	private void esperarCarga(WebDriver navegador) {

		try {

			WebDriverWait espera = new WebDriverWait(navegador, Duration.ofSeconds(15));

			espera.until(driver -> {

				String estado = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState");

				return "complete".equals(estado) || "interactive".equals(estado);
			});

		} catch (TimeoutException e) {
			// Si no termina de cargar se continúa con lo que alcanzó a cargar.
		}
	}

	/**
	 * Hace scroll por la página para que cargue el contenido que aparece al bajar.
	 *
	 * @param navegador Navegador con la página abierta
	 * @throws ScrapingException Si la espera es interrumpida
	 */
	private void cargarContenidoAdicional(WebDriver navegador) {

		try {

			JavascriptExecutor javascript = (JavascriptExecutor) navegador;

			for (int i = 0; i < 8; i++) {

				javascript.executeScript("window.scrollBy(0, Math.max(window.innerHeight * 0.8, 500));");

				Thread.sleep(700);
			}

			javascript.executeScript("window.scrollTo(0, 0);");

			Thread.sleep(500);

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
			throw new ScrapingException("Se interrumpio la carga del contenido de la pagina.");
		}
	}

	/**
	 * Obtiene el texto visible de la página. Primero intenta con todo el body, luego con las secciones
	 * principales y por último con títulos, párrafos y listas.
	 *
	 * @param navegador Navegador con la página abierta
	 * @return Texto visible de la página (vacío si no hay)
	 */
	private String obtenerTextoVisible(WebDriver navegador) {

		JavascriptExecutor javascript = (JavascriptExecutor) navegador;

		String contenido = (String) javascript.executeScript("return document.body ? document.body.innerText : '';");

		if (contenido != null && !contenido.isBlank()) {
			return contenido.trim();
		}

		String scriptSecciones = "var elementos = document.querySelectorAll('main, article, [role=\"main\"]');"
				+ "for (var i = 0; i < elementos.length; i++) {" + "  var elemento = elementos[i];"
				+ "  if (elemento.getClientRects().length > 0 && elemento.innerText.trim() !== '') {"
				+ "    return elemento.innerText;" + "  }" + "}" + "return '';";

		contenido = (String) javascript.executeScript(scriptSecciones);

		if (contenido != null && !contenido.isBlank()) {
			return contenido.trim();
		}

		String scriptElementos = "var elementos = document.querySelectorAll("
				+ "'h1, h2, h3, h4, h5, h6, p, li, blockquote');" + "var textos = [];"
				+ "for (var i = 0; i < elementos.length; i++) {" + "  var elemento = elementos[i];"
				+ "  var texto = elemento.innerText.trim();"
				+ "  if (elemento.getClientRects().length > 0 && texto !== '') {" + "    textos.push(texto);" + "  }"
				+ "}" + "return textos.join('\\n');";

		contenido = (String) javascript.executeScript(scriptElementos);

		if (contenido != null) {
			return contenido.trim();
		}

		return "";
	}

	/**
	 * Registra manualmente un scraping.
	 *
	 * @param datos Datos del scraping (URL y usuario obligatorios)
	 * @return DTO del scraping guardado
	 * @throws DatoInvalidoException Si falta algún dato obligatorio
	 */
	public ScrapingDTO guardar(ScrapingDTO datos) {

		if (datos == null) {
			throw new DatoInvalidoException("Debe ingresar los datos del scraping.");
		}

		if (datos.getUrl() == null || datos.getUrl().isBlank()) {
			throw new DatoInvalidoException("La URL del scraping es obligatoria.");
		}

		if (datos.getIdUsuario() == null) {
			throw new DatoInvalidoException("El ID del usuario es obligatorio.");
		}

		Scraping scraping = new Scraping(datos.getUrl(), datos.getIdUsuario());

		scraping.setTituloPagina(datos.getTituloPagina());
		scraping.setContenidoExtraido(datos.getContenidoExtraido());
		scraping.setDatosJson(datos.getDatosJson());
		scraping.setCantidadImagenes(datos.getCantidadImagenes());
		scraping.setCantidadEnlaces(datos.getCantidadEnlaces());
		scraping.setCantidadPalabras(datos.getCantidadPalabras());
		scraping.setNombresPersonas(datos.getNombresPersonas());
		scraping.setTecnologiasIdentificadas(datos.getTecnologiasIdentificadas());

		if (datos.getEstado() != null) {
			scraping.setEstado(datos.getEstado());
		}

		return convertirDTO(scrapingRepository.save(scraping));
	}

	/**
	 * Busca un scraping por su identificador.
	 *
	 * @param id Identificador del scraping
	 * @return DTO del scraping
	 * @throws DatoInvalidoException Si el identificador es nulo
	 * @throws RecursoNoEncontradoException Si el scraping no existe
	 */
	public ScrapingDTO buscarPorId(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		Scraping scraping = scrapingRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Scraping no encontrado."));

		return convertirDTO(scraping);
	}

	/**
	 * Consulta todos los scrapings.
	 *
	 * @return Lista de scrapings
	 */
	public List<ScrapingDTO> buscarTodos() {

		return scrapingRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Consulta los scrapings de un usuario.
	 *
	 * @param idUsuario Identificador del usuario
	 * @return Lista de scrapings del usuario
	 * @throws DatoInvalidoException Si el identificador es nulo
	 */
	public List<ScrapingDTO> buscarPorUsuario(Long idUsuario) {

		if (idUsuario == null) {
			throw new DatoInvalidoException("El ID del usuario es obligatorio.");
		}

		return scrapingRepository.findByIdUsuario(idUsuario).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Consulta los scrapings por estado.
	 *
	 * @param estado Estado a buscar (COMPLETADO o ERROR)
	 * @return Lista de scrapings con ese estado
	 * @throws DatoInvalidoException Si el estado está vacío
	 */
	public List<ScrapingDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del scraping es obligatorio.");
		}

		return scrapingRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Actualiza un scraping. Los textos solo se cambian si llegan con valor; las cantidades siempre se
	 * reemplazan.
	 *
	 * @param id Identificador del scraping
	 * @param datos Nuevos datos
	 * @return DTO del scraping actualizado
	 * @throws DatoInvalidoException Si el identificador o los datos son nulos
	 * @throws RecursoNoEncontradoException Si el scraping no existe
	 */
	public ScrapingDTO actualizar(Long id, ScrapingDTO datos) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		if (datos == null) {
			throw new DatoInvalidoException("Debe ingresar los datos para actualizar el scraping.");
		}

		Scraping scraping = scrapingRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Scraping no encontrado."));

		if (datos.getUrl() != null) {
			scraping.setUrl(datos.getUrl());
		}

		if (datos.getTituloPagina() != null) {
			scraping.setTituloPagina(datos.getTituloPagina());
		}

		if (datos.getContenidoExtraido() != null) {
			scraping.setContenidoExtraido(datos.getContenidoExtraido());
		}

		if (datos.getDatosJson() != null) {
			scraping.setDatosJson(datos.getDatosJson());
		}

		scraping.setCantidadImagenes(datos.getCantidadImagenes());
		scraping.setCantidadEnlaces(datos.getCantidadEnlaces());
		scraping.setCantidadPalabras(datos.getCantidadPalabras());

		if (datos.getNombresPersonas() != null) {
			scraping.setNombresPersonas(datos.getNombresPersonas());
		}

		if (datos.getTecnologiasIdentificadas() != null) {
			scraping.setTecnologiasIdentificadas(datos.getTecnologiasIdentificadas());
		}

		if (datos.getEstado() != null) {
			scraping.setEstado(datos.getEstado());
		}

		return convertirDTO(scrapingRepository.save(scraping));
	}

	/**
	 * Elimina un scraping.
	 *
	 * @param id Identificador del scraping
	 * @throws DatoInvalidoException Si el identificador es nulo
	 * @throws RecursoNoEncontradoException Si el scraping no existe
	 */
	public void eliminar(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		if (!scrapingRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Scraping no encontrado.");
		}

		scrapingRepository.deleteById(id);
	}

	/**
	 * Convierte la entidad Scraping en ScrapingDTO.
	 *
	 * @param scraping Scraping a convertir
	 * @return DTO del scraping
	 */
	private ScrapingDTO convertirDTO(Scraping scraping) {

		return new ScrapingDTO(scraping.getIdScraping(), scraping.getUrl(), scraping.getTituloPagina(),
				scraping.getContenidoExtraido(), scraping.getDatosJson(), scraping.getCantidadImagenes(),
				scraping.getCantidadEnlaces(), scraping.getCantidadPalabras(), scraping.getNombresPersonas(),
				scraping.getTecnologiasIdentificadas(), scraping.getFechaScraping(), scraping.getEstado(),
				scraping.getIdUsuario());
	}

	/**
	 * Escapa un texto para poder ponerlo dentro de un JSON (comillas, barras y saltos de línea).
	 *
	 * @param texto Texto a escapar (puede ser null)
	 * @return Texto escapado, o vacío si era null
	 */
	private String escaparJSON(String texto) {

		if (texto == null) {
			return "";
		}

		return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}
}