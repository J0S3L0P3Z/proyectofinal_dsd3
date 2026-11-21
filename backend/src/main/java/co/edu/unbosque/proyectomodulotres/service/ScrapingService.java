package co.edu.unbosque.proyectomodulotres.service;

import java.io.File;
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

@Service
@Transactional
public class ScrapingService {

	private static final Logger logger = LoggerFactory.getLogger(ScrapingService.class);

	private final ScrapingRepository scrapingRepository;
	private final ObjectProvider<WebDriver> webDriverProvider;
	private final IAService iaService;
	private final ReporteService reporteService;
	private final UserRepository userRepository;
	private final NotificacionEmailService notificacionEmailService;
	private final HistorialService historialService;

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

		WebDriver navegador = null;

		try {

			navegador = webDriverProvider.getObject();

			navegador.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));

			try {
				navegador.get(datos.getUrl());
			} catch (TimeoutException e) {
				logger.warn("La pagina excedio el tiempo de carga: {}", datos.getUrl());
			}

			esperarCarga(navegador);

			cargarContenidoAdicional(navegador);

			String titulo = navegador.getTitle();
			String contenido = obtenerTextoVisible(navegador);
			String codigoHTML = navegador.getPageSource();

			int cantidadImagenes = navegador.findElements(By.tagName("img")).size();
			int cantidadEnlaces = navegador.findElements(By.tagName("a")).size();

			int cantidadPalabras = 0;

			if (contenido != null && !contenido.isBlank()) {
				cantidadPalabras = contenido.trim().split("\\s+").length;
			}

			navegador.quit();
			navegador = null;

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

		} finally {

			if (navegador != null) {
				navegador.quit();
			}
		}
	}

	// Identificar mensajes de error devueltos como contenido de la pagina.
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

	private void esperarCarga(WebDriver navegador) {

		try {

			WebDriverWait espera = new WebDriverWait(navegador, Duration.ofSeconds(15));

			espera.until(driver -> {

				String estado = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState");

				return "complete".equals(estado) || "interactive".equals(estado);
			});

		} catch (TimeoutException e) {
		}
	}

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

	public ScrapingDTO buscarPorId(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		Scraping scraping = scrapingRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Scraping no encontrado."));

		return convertirDTO(scraping);
	}

	public List<ScrapingDTO> buscarTodos() {

		return scrapingRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<ScrapingDTO> buscarPorUsuario(Long idUsuario) {

		if (idUsuario == null) {
			throw new DatoInvalidoException("El ID del usuario es obligatorio.");
		}

		return scrapingRepository.findByIdUsuario(idUsuario).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ScrapingDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del scraping es obligatorio.");
		}

		return scrapingRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

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

	public void eliminar(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		if (!scrapingRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Scraping no encontrado.");
		}

		scrapingRepository.deleteById(id);
	}

	private ScrapingDTO convertirDTO(Scraping scraping) {

		return new ScrapingDTO(scraping.getIdScraping(), scraping.getUrl(), scraping.getTituloPagina(),
				scraping.getContenidoExtraido(), scraping.getDatosJson(), scraping.getCantidadImagenes(),
				scraping.getCantidadEnlaces(), scraping.getCantidadPalabras(), scraping.getNombresPersonas(),
				scraping.getTecnologiasIdentificadas(), scraping.getFechaScraping(), scraping.getEstado(),
				scraping.getIdUsuario());
	}

	private String escaparJSON(String texto) {

		if (texto == null) {
			return "";
		}

		return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t",
				"\\t");
	}
}