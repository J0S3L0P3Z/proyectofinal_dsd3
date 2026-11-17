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
import co.edu.unbosque.proyectomodulotres.entity.Reporte;
import co.edu.unbosque.proyectomodulotres.entity.Resultado;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;
import co.edu.unbosque.proyectomodulotres.repository.ReporteRepository;
import co.edu.unbosque.proyectomodulotres.repository.ResultadoRepository;
import co.edu.unbosque.proyectomodulotres.repository.ScrapingRepository;
import co.edu.unbosque.proyectomodulotres.util.GeneradorPDF;

@Service
@Transactional
public class ReporteService {

	private final ReporteRepository reporteRepository;
	private final ResultadoRepository resultadoRepository;
	private final ScrapingRepository scrapingRepository;

	@Value("${app.reportes.ruta:reportes}")
	private String rutaReportes;

	public ReporteService(ReporteRepository reporteRepository, ResultadoRepository resultadoRepository,
			ScrapingRepository scrapingRepository) {

		this.reporteRepository = reporteRepository;
		this.resultadoRepository = resultadoRepository;
		this.scrapingRepository = scrapingRepository;
	}

	// Genera y guarda el PDF de un scraping.
	public ReporteDTO generarPDF(Long idScraping) {

		// Buscar el scraping.
		Scraping scraping = scrapingRepository.findById(idScraping)
				.orElseThrow(() -> new RuntimeException("Scraping no encontrado"));

		// Buscar los resultados de las IA.
		List<Resultado> resultados = resultadoRepository.findByIdScraping(idScraping);

		try {

			// Generar el documento PDF.
			GeneradorPDF generador = new GeneradorPDF();

			byte[] contenidoPDF = generador.generarPDF(scraping, resultados);

			// Crear la carpeta donde se guardarán los PDF.
			Path carpeta = Paths.get(rutaReportes).toAbsolutePath();

			Files.createDirectories(carpeta);

			// Crear un nombre único para el archivo.
			String nombreArchivo = "reporte_" + idScraping + "_" + UUID.randomUUID() + ".pdf";

			// Definir la ubicación del archivo.
			Path archivo = carpeta.resolve(nombreArchivo);

			// Guardar el PDF.
			Files.write(archivo, contenidoPDF);

			// Registrar la información del reporte.
			Reporte reporte = new Reporte(nombreArchivo, archivo.toString(), idScraping);

			reporte.setEstado("GENERADO");

			return convertirDTO(reporteRepository.save(reporte));

		} catch (DocumentException | IOException e) {

			throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
		}
	}

	// Guarda manualmente los datos de un reporte.
	public ReporteDTO guardar(ReporteDTO datos) {

		Reporte reporte = new Reporte(datos.getNombreArchivo(), datos.getRutaArchivo(), datos.getIdScraping());

		if (datos.getEstado() != null) {
			reporte.setEstado(datos.getEstado());
		}

		return convertirDTO(reporteRepository.save(reporte));
	}

	// Busca un reporte por su identificador.
	public ReporteDTO buscarPorId(Long id) {

		Reporte reporte = reporteRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

		return convertirDTO(reporte);
	}

	// Consulta todos los reportes.
	public List<ReporteDTO> buscarTodos() {

		return reporteRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	// Busca los reportes de un scraping.
	public List<ReporteDTO> buscarPorScraping(Long idScraping) {

		return reporteRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	// Busca los reportes por su estado.
	public List<ReporteDTO> buscarPorEstado(String estado) {

		return reporteRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	// Actualiza la información de un reporte.
	public ReporteDTO actualizar(Long id, ReporteDTO datos) {

		Reporte reporte = reporteRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

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

	// Descarga el contenido de un PDF guardado.
	public byte[] descargarPDF(Long idReporte) {

		Reporte reporte = reporteRepository.findById(idReporte)
				.orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

		try {

			Path archivo = Paths.get(reporte.getRutaArchivo());

			if (!Files.exists(archivo)) {
				throw new RuntimeException("El archivo PDF no existe");
			}

			return Files.readAllBytes(archivo);

		} catch (IOException e) {

			throw new RuntimeException("Error al leer el archivo PDF: " + e.getMessage(), e);
		}
	}

	// Elimina un reporte de la base de datos.
	public void eliminar(Long id) {

		if (!reporteRepository.existsById(id)) {
			throw new RuntimeException("Reporte no encontrado");
		}

		reporteRepository.deleteById(id);
	}

	// Convierte la entidad Reporte en ReporteDTO.
	private ReporteDTO convertirDTO(Reporte reporte) {

		return new ReporteDTO(reporte.getIdReporte(), reporte.getNombreArchivo(), reporte.getRutaArchivo(),
				reporte.getFechaGeneracion(), reporte.getEstado(), reporte.getIdScraping());
	}
}