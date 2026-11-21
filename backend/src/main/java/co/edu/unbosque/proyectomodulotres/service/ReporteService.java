package co.edu.unbosque.proyectomodulotres.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;

import co.edu.unbosque.proyectomodulotres.dto.ReporteDTO;
import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.entity.Reporte;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.exception.ScrapingException;
import co.edu.unbosque.proyectomodulotres.repository.ReporteRepository;
import co.edu.unbosque.proyectomodulotres.repository.ScrapingRepository;
import co.edu.unbosque.proyectomodulotres.util.GeneradorPDF;

@Service
@Transactional
public class ReporteService {

	private final ReporteRepository reporteRepository;
	private final ScrapingRepository scrapingRepository;
	private final ResultadoService resultadoService;

	@Value("${PDF_STORAGE_PATH:${app.reportes.ruta:reportes}}")
	private String rutaReportes;

	public ReporteService(ReporteRepository reporteRepository, ScrapingRepository scrapingRepository,
			ResultadoService resultadoService) {

		this.reporteRepository = reporteRepository;
		this.scrapingRepository = scrapingRepository;
		this.resultadoService = resultadoService;
	}

	public ReporteDTO generarPDF(Long idScraping) {

		if (idScraping == null || idScraping <= 0) {
			throw new DatoInvalidoException("El identificador del scraping es obligatorio y debe ser válido");
		}

		Scraping scraping = scrapingRepository.findById(idScraping)
				.orElseThrow(() -> new RecursoNoEncontradoException("Scraping no encontrado"));

		List<ResultadoDTO> resultados = resultadoService.buscarPorScraping(idScraping);

		try {

			GeneradorPDF generador = new GeneradorPDF();

			byte[] contenidoPDF = generador.generarPDF(scraping, resultados);

			Path carpeta = Paths.get(rutaReportes).toAbsolutePath().normalize();

			Files.createDirectories(carpeta);

			String nombreArchivo = "reporte_" + idScraping + "_" + UUID.randomUUID() + ".pdf";

			Path archivo = carpeta.resolve(nombreArchivo);

			Files.write(archivo, contenidoPDF);

			System.out.println("PDF GUARDADO EN: " + archivo.toAbsolutePath());

			Reporte reporte = new Reporte(nombreArchivo, archivo.toString(), idScraping);

			reporte.setEstado("GENERADO");

			return convertirDTO(reporteRepository.save(reporte));

		} catch (DocumentException | IOException e) {

			throw new ScrapingException("Error al generar el PDF: " + e.getMessage());
		}
	}

	public ReporteDTO guardar(ReporteDTO datos) {

		if (datos == null) {
			throw new DatoInvalidoException("Los datos del reporte son obligatorios");
		}

		if (datos.getNombreArchivo() == null || datos.getNombreArchivo().isBlank()) {
			throw new DatoInvalidoException("El nombre del archivo es obligatorio");
		}

		if (datos.getRutaArchivo() == null || datos.getRutaArchivo().isBlank()) {
			throw new DatoInvalidoException("La ruta del archivo es obligatoria");
		}

		if (datos.getIdScraping() == null) {
			throw new DatoInvalidoException("El identificador del scraping es obligatorio");
		}

		Reporte reporte = new Reporte(datos.getNombreArchivo(), datos.getRutaArchivo(), datos.getIdScraping());

		if (datos.getEstado() != null) {
			reporte.setEstado(datos.getEstado());
		}

		return convertirDTO(reporteRepository.save(reporte));
	}

	public ReporteDTO buscarPorId(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del reporte es inválido");
		}

		Reporte reporte = reporteRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado"));

		return convertirDTO(reporte);
	}

	public List<ReporteDTO> buscarTodos() {

		return reporteRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<ReporteDTO> buscarPorScraping(Long idScraping) {

		if (idScraping == null || idScraping <= 0) {
			throw new DatoInvalidoException("El identificador del scraping es inválido");
		}

		return reporteRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ReporteDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del reporte es obligatorio");
		}

		return reporteRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public ReporteDTO actualizar(Long id, ReporteDTO datos) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del reporte es inválido");
		}

		if (datos == null) {
			throw new DatoInvalidoException("Los datos para actualizar el reporte son obligatorios");
		}

		Reporte reporte = reporteRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado"));

		if (datos.getNombreArchivo() != null) {
			reporte.setNombreArchivo(datos.getNombreArchivo());
		}

		if (datos.getRutaArchivo() != null) {
			reporte.setRutaArchivo(datos.getRutaArchivo());
		}

		if (datos.getEstado() != null) {
			reporte.setEstado(datos.getEstado());
		}

		return convertirDTO(reporteRepository.save(reporte));
	}

	public byte[] descargarPDF(Long idReporte) {

		if (idReporte == null || idReporte <= 0) {
			throw new DatoInvalidoException("El identificador del reporte es inválido");
		}

		Reporte reporte = reporteRepository.findById(idReporte)
				.orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado"));

		try {

			Path archivo = Paths.get(reporte.getRutaArchivo());

			if (!Files.exists(archivo) || !Files.isRegularFile(archivo)) {
				throw new RecursoNoEncontradoException("El archivo PDF no existe");
			}

			return Files.readAllBytes(archivo);

		} catch (IOException e) {

			throw new ScrapingException("Error al leer el archivo PDF: " + e.getMessage());
		}
	}

	public void eliminar(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del reporte es inválido");
		}

		if (!reporteRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Reporte no encontrado");
		}

		reporteRepository.deleteById(id);
	}

	private ReporteDTO convertirDTO(Reporte reporte) {

		return new ReporteDTO(reporte.getIdReporte(), reporte.getNombreArchivo(), reporte.getRutaArchivo(),
				reporte.getFechaGeneracion(), reporte.getEstado(), reporte.getIdScraping());
	}
}