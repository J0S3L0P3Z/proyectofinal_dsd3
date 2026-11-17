package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.ScrapingDTO;
import co.edu.unbosque.proyectomodulotres.service.ScrapingService;

@RestController
@RequestMapping("/scraping")
@CrossOrigin(origins = "http://localhost:4200")
public class ScrapingController {

	private final ScrapingService scrapingService;

	public ScrapingController(ScrapingService scrapingService) {
		this.scrapingService = scrapingService;
	}

	// EJECUTAR SCRAPING
	@PostMapping("/ejecutar")
	public ResponseEntity<ScrapingDTO> realizarScraping(@RequestBody ScrapingDTO scraping) {
		return ResponseEntity.ok(scrapingService.realizarScraping(scraping));
	}

	// REGISTRAR
	@PostMapping
	public ResponseEntity<ScrapingDTO> guardar(@RequestBody ScrapingDTO scraping) {
		return ResponseEntity.ok(scrapingService.guardar(scraping));
	}

	// BUSCAR TODOS
	@GetMapping
	public ResponseEntity<List<ScrapingDTO>> buscarTodos() {
		return ResponseEntity.ok(scrapingService.buscarTodos());
	}

	// BUSCAR POR ID
	@GetMapping("/{id}")
	public ResponseEntity<ScrapingDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(scrapingService.buscarPorId(id));
	}

	// BUSCAR POR USUARIO
	@GetMapping("/usuario/{idUsuario}")
	public ResponseEntity<List<ScrapingDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
		return ResponseEntity.ok(scrapingService.buscarPorUsuario(idUsuario));
	}

	// BUSCAR POR ESTADO
	@GetMapping("/estado/{estado}")
	public ResponseEntity<List<ScrapingDTO>> buscarPorEstado(@PathVariable String estado) {
		return ResponseEntity.ok(scrapingService.buscarPorEstado(estado));
	}

	// ACTUALIZAR
	@PutMapping("/{id}")
	public ResponseEntity<ScrapingDTO> actualizar(@PathVariable Long id, @RequestBody ScrapingDTO scraping) {
		return ResponseEntity.ok(scrapingService.actualizar(id, scraping));
	}

	// ELIMINAR
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		scrapingService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
