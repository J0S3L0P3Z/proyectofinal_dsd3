package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.RolDTO;
import co.edu.unbosque.proyectomodulotres.service.RolService;

@RestController
@RequestMapping("/rol")
@CrossOrigin(origins = "http://localhost:4200")
public class RolController {

	private final RolService rolService;

	public RolController(RolService rolService) {
		this.rolService = rolService;
	}

	@PostMapping("/crear")
	public ResponseEntity<RolDTO> crear(@RequestBody RolDTO rol) {
		return ResponseEntity.ok(rolService.crear(rol));
	}

	@PutMapping("/actualizar/{id}")
	public ResponseEntity<RolDTO> actualizar(@PathVariable Long id, @RequestBody RolDTO rol) {
		return ResponseEntity.ok(rolService.actualizar(id, rol));
	}

	@DeleteMapping("/eliminar/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		rolService.eliminar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/buscar/{id}")
	public ResponseEntity<RolDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(rolService.buscarPorId(id));
	}

	@GetMapping("/buscarTodos")
	public ResponseEntity<List<RolDTO>> buscarTodos() {
		return ResponseEntity.ok(rolService.buscarTodos());
	}

	@GetMapping("/contar")
	public ResponseEntity<Long> contar() {
		return ResponseEntity.ok(rolService.contar());
	}
}