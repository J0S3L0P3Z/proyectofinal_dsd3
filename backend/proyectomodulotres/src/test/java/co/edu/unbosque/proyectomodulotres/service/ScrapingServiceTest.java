package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.ObjectProvider;

import co.edu.unbosque.proyectomodulotres.dto.ScrapingDTO;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.exception.ScrapingException;
import co.edu.unbosque.proyectomodulotres.repository.ScrapingRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

/**
 * Pruebas unitarias de las validaciones de {@link ScrapingService}. No abre ningún navegador: se
 * prueban los casos que fallan antes de usar Selenium y la gestión de scrapings guardados.
 */
@ExtendWith(MockitoExtension.class)
class ScrapingServiceTest {

	/** Repositorio de scrapings simulado. */
	@Mock
	private ScrapingRepository scrapingRepository;

	/** Proveedor de navegadores simulado. */
	@Mock
	private ObjectProvider<WebDriver> webDriverProvider;

	/** Servicio de IA simulado. */
	@Mock
	private IAService iaService;

	/** Servicio de reportes simulado. */
	@Mock
	private ReporteService reporteService;

	/** Repositorio de usuarios simulado. */
	@Mock
	private UserRepository userRepository;

	/** Servicio de correo simulado. */
	@Mock
	private NotificacionEmailService notificacionEmailService;

	/** Servicio del historial simulado. */
	@Mock
	private HistorialService historialService;

	/** Servicio que se prueba. */
	@InjectMocks
	private ScrapingService scrapingService;

	/**
	 * Crea un DTO con la URL y el usuario.
	 *
	 * @param url URL a analizar
	 * @param idUsuario Identificador del usuario
	 * @return DTO de scraping
	 */
	private ScrapingDTO crearDatos(String url, Long idUsuario) {

		ScrapingDTO datos = new ScrapingDTO();
		datos.setUrl(url);
		datos.setIdUsuario(idUsuario);

		return datos;
	}

	/** Un scraping sin URL no se acepta y no abre el navegador. */
	@Test
	void scrapingSinUrlLanzaError() {

		assertThrows(DatoInvalidoException.class, () -> scrapingService.realizarScraping(crearDatos(" ", 1L)));

		verify(webDriverProvider, never()).getObject();
	}

	/** Una URL que no empieza por http o https no se acepta. */
	@Test
	void scrapingConUrlSinHttpLanzaError() {
		assertThrows(DatoInvalidoException.class,
				() -> scrapingService.realizarScraping(crearDatos("ftp://pagina.com", 1L)));
	}

	/** Un scraping sin usuario no se acepta. */
	@Test
	void scrapingSinUsuarioLanzaError() {
		assertThrows(DatoInvalidoException.class,
				() -> scrapingService.realizarScraping(crearDatos("https://pagina.com", null)));
	}

	/** Un scraping para un usuario que no existe lanza recurso no encontrado. */
	@Test
	void scrapingConUsuarioInexistenteLanzaError() {

		when(userRepository.existsById(99L)).thenReturn(false);

		assertThrows(RecursoNoEncontradoException.class,
				() -> scrapingService.realizarScraping(crearDatos("https://pagina.com", 99L)));

		verify(webDriverProvider, never()).getObject();
	}

	/** Guardar manualmente un scraping conserva sus datos. */
	@Test
	void guardarScrapingManual() {

		when(scrapingRepository.save(any(Scraping.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		ScrapingDTO datos = crearDatos("https://pagina.com", 1L);
		datos.setTituloPagina("Mi página");
		datos.setEstado("COMPLETADO");

		ScrapingDTO guardado = scrapingService.guardar(datos);

		assertEquals("Mi página", guardado.getTituloPagina());
		assertEquals("COMPLETADO", guardado.getEstado());
	}

	/** Buscar un scraping que no existe lanza recurso no encontrado. */
	@Test
	void buscarScrapingInexistenteLanzaError() {

		when(scrapingRepository.findById(8L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> scrapingService.buscarPorId(8L));
	}

	/** Buscar por estado vacío no se acepta. */
	@Test
	void buscarPorEstadoVacioLanzaError() {
		assertThrows(DatoInvalidoException.class, () -> scrapingService.buscarPorEstado(""));
	}

	/**
	 * Levanta un servidor web local que siempre responde el código y el HTML indicados.
	 *
	 * @param codigo Código HTTP de la respuesta
	 * @param html HTML que devuelve
	 * @return Servidor iniciado en un puerto libre
	 * @throws IOException Si no se puede iniciar el servidor
	 */
	private HttpServer iniciarServidor(int codigo, String html) throws IOException {

		HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

		servidor.createContext("/", intercambio -> {
			byte[] cuerpo = html.getBytes(StandardCharsets.UTF_8);
			intercambio.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
			intercambio.sendResponseHeaders(codigo, cuerpo.length);
			try (OutputStream salida = intercambio.getResponseBody()) {
				salida.write(cuerpo);
			}
		});

		servidor.start();

		return servidor;
	}

	/**
	 * Hace que el repositorio devuelva lo que se guarda, asignándole el id 10.
	 */
	private void guardarConId() {

		when(scrapingRepository.save(any(Scraping.class))).thenAnswer(invocacion -> {
			Scraping scraping = invocacion.getArgument(0);
			scraping.setIdScraping(10L);
			return scraping;
		});
	}

	/**
	 * Si el servidor no tiene Edge, la página se descarga sin navegador, se analiza con las IA y el
	 * scraping queda COMPLETADO. En el segundo scraping ya no se intenta abrir el navegador.
	 */
	@Test
	void sinEdgeDescargaLaPaginaSinNavegador() throws IOException {

		HttpServer servidor = iniciarServidor(200, "<html><head><title>Sitio de prueba</title></head><body>"
				+ "<h1>Hola mundo</h1><p>Texto de la pagina</p><img src='a.png'><a href='/x'>Ir</a></body></html>");

		try {

			String url = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/";

			when(userRepository.existsById(1L)).thenReturn(true);
			when(webDriverProvider.getObject()).thenThrow(new IllegalStateException("Could not start a new session"));
			when(iaService.analizarYGuardar(any(), any(), any(), any())).thenReturn(List.of());
			guardarConId();

			ScrapingDTO resultado = scrapingService.realizarScraping(crearDatos(url, 1L));

			assertEquals("COMPLETADO", resultado.getEstado());
			assertEquals("Sitio de prueba", resultado.getTituloPagina());
			assertEquals(1, resultado.getCantidadImagenes());
			assertEquals(1, resultado.getCantidadEnlaces());
			assertTrue(resultado.getContenidoExtraido().contains("Hola mundo"));
			verify(iaService).analizarYGuardar(any(), any(), any(), any());

			scrapingService.realizarScraping(crearDatos(url, 1L));

			verify(webDriverProvider, times(1)).getObject();

		} finally {
			servidor.stop(0);
		}
	}

	/** Si el sitio responde 403 sin navegador, el scraping queda en ERROR y no se llama a las IA. */
	@Test
	void sinEdgeUnSitioQueRespondeErrorQuedaEnError() throws IOException {

		HttpServer servidor = iniciarServidor(403, "<html><body>Acceso prohibido</body></html>");

		try {

			String url = "http://127.0.0.1:" + servidor.getAddress().getPort() + "/";

			when(userRepository.existsById(1L)).thenReturn(true);
			when(webDriverProvider.getObject()).thenThrow(new IllegalStateException("Could not start a new session"));
			guardarConId();

			ScrapingDTO resultado = scrapingService.realizarScraping(crearDatos(url, 1L));

			assertEquals("ERROR", resultado.getEstado());
			verify(iaService, never()).analizarYGuardar(any(), any(), any(), any());

		} finally {
			servidor.stop(0);
		}
	}

	/** Si sin navegador la página no se puede descargar, se informa un error de scraping. */
	@Test
	void sinEdgeYSitioCaidoLanzaErrorDeScraping() throws IOException {

		HttpServer servidor = iniciarServidor(200, "");
		int puertoCerrado = servidor.getAddress().getPort();
		servidor.stop(0);

		when(userRepository.existsById(1L)).thenReturn(true);
		when(webDriverProvider.getObject()).thenThrow(new IllegalStateException("Could not start a new session"));

		assertThrows(ScrapingException.class,
				() -> scrapingService.realizarScraping(crearDatos("http://127.0.0.1:" + puertoCerrado + "/", 1L)));
	}
}
