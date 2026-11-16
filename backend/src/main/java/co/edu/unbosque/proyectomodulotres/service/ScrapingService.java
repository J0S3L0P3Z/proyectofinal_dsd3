package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.ScrapingDTO;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;
import co.edu.unbosque.proyectomodulotres.repository.ScrapingRepository;

@Service
@Transactional
public class ScrapingService {

	private final ScrapingRepository scrapingRepository;

	public ScrapingService(ScrapingRepository scrapingRepository) {
		this.scrapingRepository = scrapingRepository;
	}

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

	public ScrapingDTO buscarPorId(Long id) {

		Scraping scraping = scrapingRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Scraping no encontrado"));

		return convertirDTO(scraping);
	}

	public List<ScrapingDTO> buscarTodos() {

		return scrapingRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<ScrapingDTO> buscarPorUsuario(Long idUsuario) {

		return scrapingRepository.findByIdUsuario(idUsuario).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ScrapingDTO> buscarPorEstado(String estado) {

		return scrapingRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

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

	public void eliminar(Long id) {

		if (!scrapingRepository.existsById(id)) {
			throw new RuntimeException("Scraping no encontrado");
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
}
