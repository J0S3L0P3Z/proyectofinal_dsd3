package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.RolDTO;
import co.edu.unbosque.proyectomodulotres.service.RolService;

/**
 * Controlador REST de roles.
 */
@RestController
@RequestMapping("/rol")
@CrossOrigin(origins = "${app.cors.origenes}")
public class RolController {

	/** Servicio de roles. */
	private final RolService rolService;

	/**
	 * Constructor que recibe el servicio de roles.
	 *
	 * @param rolService Servicio de roles
	 */
	public RolController(RolService rolService) {
		this.rolService = rolService;
	}

	/**
	 * Crea un rol.
	 *
	 * @param rol Datos del rol
	 * @return Rol creado
	 */
	@PostMapping("/crear")
	public ResponseEntity<RolDTO> crear(@RequestBody RolDTO rol) {
		return ResponseEntity.ok(rolService.crear(rol));
	}

	/**
	 * Actualiza un rol.
	 *
	 * @param id Identificador del rol
	 * @param rol Nuevos datos
	 * @return Rol actualizado
	 */
	@PutMapping("/actualizar/{id}")
	public ResponseEntity<RolDTO> actualizar(@PathVariable Long id, @RequestBody RolDTO rol) {
		return ResponseEntity.ok(rolService.actualizar(id, rol));
	}

	/**
	 * Elimina un rol.
	 *
	 * @param id Identificador del rol
	 * @return Respuesta sin contenido
	 */
	@DeleteMapping("/eliminar/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		rolService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Busca un rol por su identificador.
	 *
	 * @param id Identificador del rol
	 * @return Datos del rol
	 */
	@GetMapping("/buscar/{id}")
	public ResponseEntity<RolDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(rolService.buscarPorId(id));
	}

	/**
	 * Consulta todos los roles.
	 *
	 * @return Lista de roles
	 */
	@GetMapping("/buscarTodos")
	public ResponseEntity<List<RolDTO>> buscarTodos() {
		return ResponseEntity.ok(rolService.buscarTodos());
	}

	/**
	 * Cuenta los roles registrados.
	 *
	 * @return Cantidad de roles
	 */
	@GetMapping("/contar")
	public ResponseEntity<Long> contar() {
		return ResponseEntity.ok(rolService.contar());
	}
}
