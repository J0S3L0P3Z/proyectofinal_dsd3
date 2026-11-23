package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.ReporteDTO;
import co.edu.unbosque.proyectomodulotres.service.ReporteService;

/**
 * Controlador REST de reportes PDF. Permite generar, descargar y gestionar los reportes de cada
 * scraping.
 */
@RestController
@RequestMapping("/reporte")
@CrossOrigin(origins = "${app.cors.origenes}")
public class ReporteController {

	/** Servicio de reportes. */
	private final ReporteService reporteService;

	/**
	 * Constructor que recibe el servicio de reportes.
	 *
	 * @param reporteService Servicio de reportes
	 */
	public ReporteController(ReporteService reporteService) {
		this.reporteService = reporteService;
	}

	/**
	 * Registra manualmente los datos de un reporte.
	 *
	 * @param reporte Datos del reporte
	 * @return Reporte guardado
	 */
	@PostMapping
	public ResponseEntity<ReporteDTO> guardar(@RequestBody ReporteDTO reporte) {
		return ResponseEntity.ok(reporteService.guardar(reporte));
	}

	/**
	 * Genera el PDF de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Reporte generado
	 */
	@PostMapping("/generar/{idScraping}")
	public ResponseEntity<ReporteDTO> generarPDF(@PathVariable Long idScraping) {
		return ResponseEntity.ok(reporteService.generarPDF(idScraping));
	}

	/**
	 * Descarga el PDF de un reporte como archivo adjunto.
	 *
	 * @param id Identificador del reporte
	 * @return Archivo PDF
	 */
	@GetMapping("/descargar/{id}")
	public ResponseEntity<ByteArrayResource> descargarPDF(@PathVariable Long id) {

		byte[] archivo = reporteService.descargarPDF(id);

		ReporteDTO reporte = reporteService.buscarPorId(id);

		ByteArrayResource recurso = new ByteArrayResource(archivo);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reporte.getNombreArchivo() + "\"")
				.contentLength(archivo.length).body(recurso);
	}

	/**
	 * Consulta todos los reportes.
	 *
	 * @return Lista de reportes
	 */
	@GetMapping
	public ResponseEntity<List<ReporteDTO>> buscarTodos() {
		return ResponseEntity.ok(reporteService.buscarTodos());
	}

	/**
	 * Busca un reporte por su identificador.
	 *
	 * @param id Identificador del reporte
	 * @return Datos del reporte
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ReporteDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(reporteService.buscarPorId(id));
	}

	/**
	 * Consulta los reportes de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Lista de reportes del scraping
	 */
	@GetMapping("/scraping/{idScraping}")
	public ResponseEntity<List<ReporteDTO>> buscarPorScraping(@PathVariable Long idScraping) {
		return ResponseEntity.ok(reporteService.buscarPorScraping(idScraping));
	}

	/**
	 * Consulta los reportes por estado.
	 *
	 * @param estado Estado a buscar
	 * @return Lista de reportes con ese estado
	 */
	@GetMapping("/estado/{estado}")
	public ResponseEntity<List<ReporteDTO>> buscarPorEstado(@PathVariable String estado) {
		return ResponseEntity.ok(reporteService.buscarPorEstado(estado));
	}

	/**
	 * Actualiza un reporte.
	 *
	 * @param id Identificador del reporte
	 * @param reporte Nuevos datos
	 * @return Reporte actualizado
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ReporteDTO> actualizar(@PathVariable Long id, @RequestBody ReporteDTO reporte) {

		return ResponseEntity.ok(reporteService.actualizar(id, reporte));
	}

	/**
	 * Elimina un reporte.
	 *
	 * @param id Identificador del reporte
	 * @return Respuesta sin contenido
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		reporteService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
