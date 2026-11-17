package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.ReporteDTO;
import co.edu.unbosque.proyectomodulotres.service.ReporteService;

@RestController
@RequestMapping("/reporte")
@CrossOrigin(origins = "http://localhost:4200")
public class ReporteController {

	private final ReporteService reporteService;

	public ReporteController(ReporteService reporteService) {
		this.reporteService = reporteService;
	}

	@PostMapping
	public ResponseEntity<ReporteDTO> guardar(@RequestBody ReporteDTO reporte) {
		return ResponseEntity.ok(reporteService.guardar(reporte));
	}

	// Generar PDF de un scraping.
	@PostMapping("/generar/{idScraping}")
	public ResponseEntity<ReporteDTO> generarPDF(@PathVariable Long idScraping) {
		return ResponseEntity.ok(reporteService.generarPDF(idScraping));
	}

	// Descargar PDF generado.
	@GetMapping("/descargar/{id}")
	public ResponseEntity<ByteArrayResource> descargarPDF(@PathVariable Long id) {

		byte[] archivo = reporteService.descargarPDF(id);

		ReporteDTO reporte = reporteService.buscarPorId(id);

		ByteArrayResource recurso = new ByteArrayResource(archivo);

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + reporte.getNombreArchivo() + "\"")
				.contentLength(archivo.length).body(recurso);
	}

	@GetMapping
	public ResponseEntity<List<ReporteDTO>> buscarTodos() {
		return ResponseEntity.ok(reporteService.buscarTodos());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ReporteDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(reporteService.buscarPorId(id));
	}

	@GetMapping("/scraping/{idScraping}")
	public ResponseEntity<List<ReporteDTO>> buscarPorScraping(@PathVariable Long idScraping) {
		return ResponseEntity.ok(reporteService.buscarPorScraping(idScraping));
	}

	@GetMapping("/estado/{estado}")
	public ResponseEntity<List<ReporteDTO>> buscarPorEstado(@PathVariable String estado) {
		return ResponseEntity.ok(reporteService.buscarPorEstado(estado));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ReporteDTO> actualizar(@PathVariable Long id, @RequestBody ReporteDTO reporte) {

		return ResponseEntity.ok(reporteService.actualizar(id, reporte));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		reporteService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
