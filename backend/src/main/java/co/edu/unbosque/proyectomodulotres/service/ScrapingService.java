package co.edu.unbosque.proyectomodulotres.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.ScrapingDTO;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;
import co.edu.unbosque.proyectomodulotres.repository.ScrapingRepository;

@Service
@Transactional
public class ScrapingService {

	private final ScrapingRepository scrapingRepository;
	private final ObjectProvider<WebDriver> webDriverProvider;

	public ScrapingService(ScrapingRepository scrapingRepository, ObjectProvider<WebDriver> webDriverProvider) {

		this.scrapingRepository = scrapingRepository;
		this.webDriverProvider = webDriverProvider;
	}

	// REALIZAR SCRAPING Y GUARDAR LA INFORMACION
	public ScrapingDTO realizarScraping(ScrapingDTO datos) {

		if (datos.getUrl() == null || datos.getUrl().isBlank()) {
			throw new RuntimeException("Debe ingresar una URL");
		}

		if (!datos.getUrl().startsWith("http://") && !datos.getUrl().startsWith("https://")) {
			throw new RuntimeException("La URL debe comenzar con http:// o https://");
		}

		if (datos.getIdUsuario() == null) {
			throw new RuntimeException("Debe ingresar el ID del usuario");
		}

		WebDriver navegador = null;

		try {

			// Crear una nueva instancia de Microsoft Edge
			navegador = webDriverProvider.getObject();

			navegador.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(40));

			// Abrir la página web
			navegador.get(datos.getUrl());

			// Obtener el título de la página
			String titulo = navegador.getTitle();

			// Obtener todo el texto visible de la página
			WebElement cuerpo = navegador.findElement(By.tagName("body"));
			String contenido = cuerpo.getText();

			// Contar imágenes
			int cantidadImagenes = navegador.findElements(By.tagName("img")).size();

			// Contar enlaces
			int cantidadEnlaces = navegador.findElements(By.tagName("a")).size();

			// Contar palabras
			int cantidadPalabras = 0;

			if (contenido != null && !contenido.isBlank()) {
				cantidadPalabras = contenido.trim().split("\\s+").length;
			}

			// Crear el registro del scraping
			Scraping scraping = new Scraping(datos.getUrl(), datos.getIdUsuario());

			scraping.setTituloPagina(titulo);
			scraping.setContenidoExtraido(contenido);
			scraping.setCantidadImagenes(cantidadImagenes);
			scraping.setCantidadEnlaces(cantidadEnlaces);
			scraping.setCantidadPalabras(cantidadPalabras);

			// Guardar información adicional en formato JSON
			String datosJson = "{" + "\"titulo\":\"" + escaparJSON(titulo) + "\"," + "\"url\":\""
					+ escaparJSON(datos.getUrl()) + "\"," + "\"cantidadImagenes\":" + cantidadImagenes + ","
					+ "\"cantidadEnlaces\":" + cantidadEnlaces + "," + "\"cantidadPalabras\":" + cantidadPalabras + "}";

			scraping.setDatosJson(datosJson);

			// Marcar el scraping como completado
			scraping.setEstado("COMPLETADO");

			// Guardar en la base de datos
			Scraping guardado = scrapingRepository.save(scraping);

			return convertirDTO(guardado);

		} catch (Exception e) {

			throw new RuntimeException("Error al realizar el scraping: " + e.getMessage(), e);

		} finally {

			// Cerrar Microsoft Edge
			if (navegador != null) {
				navegador.quit();
			}
		}
	}

	// REGISTRAR UN SCRAPING MANUALMENTE
	public ScrapingDTO guardar(ScrapingDTO datos) {

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

	// BUSCAR POR ID
	public ScrapingDTO buscarPorId(Long id) {

		Scraping scraping = scrapingRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Scraping no encontrado"));

		return convertirDTO(scraping);
	}

	// BUSCAR TODOS
	public List<ScrapingDTO> buscarTodos() {

		return scrapingRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	// BUSCAR POR USUARIO
	public List<ScrapingDTO> buscarPorUsuario(Long idUsuario) {

		return scrapingRepository.findByIdUsuario(idUsuario).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	// BUSCAR POR ESTADO
	public List<ScrapingDTO> buscarPorEstado(String estado) {

		return scrapingRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	// ACTUALIZAR
	public ScrapingDTO actualizar(Long id, ScrapingDTO datos) {

		Scraping scraping = scrapingRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Scraping no encontrado"));

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

	// ELIMINAR
	public void eliminar(Long id) {

		if (!scrapingRepository.existsById(id)) {
			throw new RuntimeException("Scraping no encontrado");
		}

		scrapingRepository.deleteById(id);
	}

	// CONVERTIR ENTIDAD A DTO
	private ScrapingDTO convertirDTO(Scraping scraping) {

		return new ScrapingDTO(scraping.getIdScraping(), scraping.getUrl(), scraping.getTituloPagina(),
				scraping.getContenidoExtraido(), scraping.getDatosJson(), scraping.getCantidadImagenes(),
				scraping.getCantidadEnlaces(), scraping.getCantidadPalabras(), scraping.getNombresPersonas(),
				scraping.getTecnologiasIdentificadas(), scraping.getFechaScraping(), scraping.getEstado(),
				scraping.getIdUsuario());
	}

	// ESCAPAR CARACTERES PARA GUARDAR JSON
	private String escaparJSON(String texto) {
		if (texto == null) {
			return "";
		}
		return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t","\\t");
	}
}
