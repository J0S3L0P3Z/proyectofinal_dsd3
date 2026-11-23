package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.ScrapingDTO;
import co.edu.unbosque.proyectomodulotres.service.ScrapingService;

/**
 * Controlador REST de scraping. Permite ejecutar el scraping de una URL y gestionar los scrapings
 * guardados.
 */
@RestController
@RequestMapping("/scraping")
@CrossOrigin(origins = "${app.cors.origenes}")
public class ScrapingController {

	/** Servicio de scraping. */
	private final ScrapingService scrapingService;

	/**
	 * Constructor que recibe el servicio de scraping.
	 *
	 * @param scrapingService Servicio de scraping
	 */
	public ScrapingController(ScrapingService scrapingService) {
		this.scrapingService = scrapingService;
	}

	/**
	 * Ejecuta el scraping de una URL, la analiza con las IA, genera el PDF y envía el correo.
	 *
	 * @param scraping DTO con la URL y el identificador del usuario
	 * @return Resultado del scraping
	 */
	@PostMapping("/ejecutar")
	public ResponseEntity<ScrapingDTO> realizarScraping(@RequestBody ScrapingDTO scraping) {
		return ResponseEntity.ok(scrapingService.realizarScraping(scraping));
	}

	/**
	 * Registra manualmente un scraping.
	 *
	 * @param scraping Datos del scraping
	 * @return Scraping guardado
	 */
	@PostMapping
	public ResponseEntity<ScrapingDTO> guardar(@RequestBody ScrapingDTO scraping) {
		return ResponseEntity.ok(scrapingService.guardar(scraping));
	}

	/**
	 * Consulta todos los scrapings.
	 *
	 * @return Lista de scrapings
	 */
	@GetMapping
	public ResponseEntity<List<ScrapingDTO>> buscarTodos() {
		return ResponseEntity.ok(scrapingService.buscarTodos());
	}

	/**
	 * Busca un scraping por su identificador.
	 *
	 * @param id Identificador del scraping
	 * @return Datos del scraping
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ScrapingDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(scrapingService.buscarPorId(id));
	}

	/**
	 * Consulta los scrapings de un usuario.
	 *
	 * @param idUsuario Identificador del usuario
	 * @return Lista de scrapings del usuario
	 */
	@GetMapping("/usuario/{idUsuario}")
	public ResponseEntity<List<ScrapingDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
		return ResponseEntity.ok(scrapingService.buscarPorUsuario(idUsuario));
	}

	/**
	 * Consulta los scrapings por estado (COMPLETADO o ERROR).
	 *
	 * @param estado Estado a buscar
	 * @return Lista de scrapings con ese estado
	 */
	@GetMapping("/estado/{estado}")
	public ResponseEntity<List<ScrapingDTO>> buscarPorEstado(@PathVariable String estado) {
		return ResponseEntity.ok(scrapingService.buscarPorEstado(estado));
	}

	/**
	 * Actualiza un scraping.
	 *
	 * @param id Identificador del scraping
	 * @param scraping Nuevos datos
	 * @return Scraping actualizado
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ScrapingDTO> actualizar(@PathVariable Long id, @RequestBody ScrapingDTO scraping) {
		return ResponseEntity.ok(scrapingService.actualizar(id, scraping));
	}

	/**
	 * Elimina un scraping.
	 *
	 * @param id Identificador del scraping
	 * @return Respuesta sin contenido
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		scrapingService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
