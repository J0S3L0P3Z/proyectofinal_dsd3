package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.service.UserService;

/**
 * Controlador REST de gestión de usuarios: consultar, contar, actualizar y eliminar.
 */
@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "${app.cors.origenes}")
public class UserController {

	/** Servicio de usuarios. */
	private final UserService userService;

	/**
	 * Constructor que recibe el servicio de usuarios.
	 *
	 * @param userService Servicio de usuarios
	 */
	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Consulta todos los usuarios.
	 *
	 * @return Lista de usuarios
	 */
	@GetMapping
	public ResponseEntity<List<UserDTO>> buscarTodos() {
		return ResponseEntity.ok(userService.buscarTodos());
	}

	/**
	 * Busca un usuario por su identificador.
	 *
	 * @param id Identificador del usuario
	 * @return Datos del usuario
	 */
	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(userService.buscarPorId(id));
	}

	/**
	 * Busca un usuario por su correo.
	 *
	 * @param username Correo del usuario
	 * @return Datos del usuario
	 */
	@GetMapping("/username/{username}")
	public ResponseEntity<UserDTO> buscarPorUsername(@PathVariable String username) {
		return ResponseEntity.ok(userService.buscarPorUsername(username));
	}

	/**
	 * Cuenta todos los usuarios.
	 *
	 * @return Cantidad de usuarios
	 */
	@GetMapping("/contar")
	public ResponseEntity<Long> contar() {
		return ResponseEntity.ok(userService.contar());
	}

	/**
	 * Cuenta los usuarios con rol ADMIN.
	 *
	 * @return Cantidad de administradores
	 */
	@GetMapping("/contar/administradores")
	public ResponseEntity<Long> contarAdministradores() {
		return ResponseEntity.ok(userService.contarAdministradores());
	}

	/**
	 * Cuenta los usuarios con rol USER.
	 *
	 * @return Cantidad de usuarios con rol USER
	 */
	@GetMapping("/contar/usuarios")
	public ResponseEntity<Long> contarUsuarios() {
		return ResponseEntity.ok(userService.contarUsuarios());
	}

	/**
	 * Actualiza un usuario. Solo cambia los campos que llegan con valor.
	 *
	 * @param id Identificador del usuario
	 * @param user Nuevos datos
	 * @return Datos actualizados del usuario
	 */
	@PutMapping("/{id}")
	public ResponseEntity<UserDTO> actualizar(@PathVariable Long id, @RequestBody UserDTO user) {
		return ResponseEntity.ok(userService.actualizar(id, user));
	}

	/**
	 * Elimina un usuario.
	 *
	 * @param id Identificador del usuario
	 * @return Respuesta sin contenido
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		userService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
