package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.service.UserService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public ResponseEntity<List<UserDTO>> buscarTodos() {
		return ResponseEntity.ok(userService.buscarTodos());
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(userService.buscarPorId(id));
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<UserDTO> buscarPorUsername(@PathVariable String username) {
		return ResponseEntity.ok(userService.buscarPorUsername(username));
	}

	@GetMapping("/contar")
	public ResponseEntity<Long> contar() {
		return ResponseEntity.ok(userService.contar());
	}

	@GetMapping("/contar/administradores")
	public ResponseEntity<Long> contarAdministradores() {
		return ResponseEntity.ok(userService.contarAdministradores());
	}

	@GetMapping("/contar/usuarios")
	public ResponseEntity<Long> contarUsuarios() {
		return ResponseEntity.ok(userService.contarUsuarios());
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserDTO> actualizar(@PathVariable Long id, @RequestBody UserDTO user) {
		return ResponseEntity.ok(userService.actualizar(id, user));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		userService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}