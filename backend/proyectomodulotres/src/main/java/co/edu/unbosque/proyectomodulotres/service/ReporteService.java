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

/**
 * Servicio de reportes PDF. Genera el PDF de cada scraping, lo guarda en disco y permite
 * descargarlo. La carpeta se toma de la variable PDF_STORAGE_PATH o, si no existe, de
 * {@code app.reportes.ruta}.
 */
@Service
@Transactional
public class ReporteService {

	/** Repositorio de reportes. */
	private final ReporteRepository reporteRepository;

	/** Repositorio de scrapings. */
	private final ScrapingRepository scrapingRepository;

	/** Servicio de resultados de las IA. */
	private final ResultadoService resultadoService;

	/** Carpeta donde se guardan los PDF. */
	@Value("${PDF_STORAGE_PATH:${app.reportes.ruta:reportes}}")
	private String rutaReportes;

	/**
	 * Constructor que recibe las dependencias del servicio.
	 *
	 * @param reporteRepository Repositorio de reportes
	 * @param scrapingRepository Repositorio de scrapings
	 * @param resultadoService Servicio de resultados de las IA
	 */
	public ReporteService(ReporteRepository reporteRepository, ScrapingRepository scrapingRepository,
			ResultadoService resultadoService) {

		this.reporteRepository = reporteRepository;
		this.scrapingRepository = scrapingRepository;
		this.resultadoService = resultadoService;
	}

	/**
	 * Genera el PDF de un scraping con sus resultados de IA, lo guarda en disco y registra el reporte.
	 *
	 * @param idScraping Identificador del scraping
	 * @return DTO del reporte generado
	 * @throws DatoInvalidoException Si el identificador no es válido
	 * @throws RecursoNoEncontradoException Si el scraping no existe
	 * @throws ScrapingException Si no se pudo crear o guardar el PDF
	 */
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

	/**
	 * Registra manualmente los datos de un reporte.
	 *
	 * @param datos Datos del reporte (nombre, ruta y scraping obligatorios)
	 * @return DTO del reporte guardado
	 * @throws DatoInvalidoException Si falta algún dato obligatorio
	 */
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

	/**
	 * Busca un reporte por su identificador.
	 *
	 * @param id Identificador del reporte
	 * @return DTO del reporte
	 * @throws DatoInvalidoException Si el identificador no es válido
	 * @throws RecursoNoEncontradoException Si el reporte no existe
	 */
	public ReporteDTO buscarPorId(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del reporte es inválido");
		}

		Reporte reporte = reporteRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Reporte no encontrado"));

		return convertirDTO(reporte);
	}

	/**
	 * Consulta todos los reportes.
	 *
	 * @return Lista de reportes
	 */
	public List<ReporteDTO> buscarTodos() {

		return reporteRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Consulta los reportes de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Lista de reportes del scraping
	 * @throws DatoInvalidoException Si el identificador no es válido
	 */
	public List<ReporteDTO> buscarPorScraping(Long idScraping) {

		if (idScraping == null || idScraping <= 0) {
			throw new DatoInvalidoException("El identificador del scraping es inválido");
		}

		return reporteRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Consulta los reportes por estado.
	 *
	 * @param estado Estado a buscar
	 * @return Lista de reportes con ese estado
	 * @throws DatoInvalidoException Si el estado está vacío
	 */
	public List<ReporteDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del reporte es obligatorio");
		}

		return reporteRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Actualiza un reporte. Solo cambia los campos que llegan con valor.
	 *
	 * @param id Identificador del reporte
	 * @param datos Nuevos datos
	 * @return DTO del reporte actualizado
	 * @throws DatoInvalidoException Si el identificador o los datos no son válidos
	 * @throws RecursoNoEncontradoException Si el reporte no existe
	 */
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

	/**
	 * Lee el contenido del PDF de un reporte.
	 *
	 * @param idReporte Identificador del reporte
	 * @return Bytes del PDF
	 * @throws DatoInvalidoException Si el identificador no es válido
	 * @throws RecursoNoEncontradoException Si el reporte o el archivo no existen
	 * @throws ScrapingException Si no se pudo leer el archivo
	 */
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

	/**
	 * Elimina el registro de un reporte.
	 *
	 * @param id Identificador del reporte
	 * @throws DatoInvalidoException Si el identificador no es válido
	 * @throws RecursoNoEncontradoException Si el reporte no existe
	 */
	public void eliminar(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del reporte es inválido");
		}

		if (!reporteRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Reporte no encontrado");
		}

		reporteRepository.deleteById(id);
	}

	/**
	 * Convierte la entidad Reporte en ReporteDTO.
	 *
	 * @param reporte Reporte a convertir
	 * @return DTO del reporte
	 */
	private ReporteDTO convertirDTO(Reporte reporte) {

		return new ReporteDTO(reporte.getIdReporte(), reporte.getNombreArchivo(), reporte.getRutaArchivo(),
				reporte.getFechaGeneracion(), reporte.getEstado(), reporte.getIdScraping());
	}
}