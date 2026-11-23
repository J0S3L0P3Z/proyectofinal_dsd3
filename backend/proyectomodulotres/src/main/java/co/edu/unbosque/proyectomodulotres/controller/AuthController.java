package co.edu.unbosque.proyectomodulotres.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.service.AuthService;
import co.edu.unbosque.proyectomodulotres.service.RecuperacionPasswordService;

/**
 * Controlador REST de autenticación. Permite registrarse, iniciar sesión y recuperar la contraseña.
 * Todos sus endpoints son públicos.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "${app.cors.origenes}")
public class AuthController {

	/** Servicio de autenticación. */
	private final AuthService authService;

	/** Servicio de recuperación de contraseña. */
	private final RecuperacionPasswordService recuperacionPasswordService;

	/**
	 * Constructor que recibe los servicios del controlador.
	 *
	 * @param authService Servicio de autenticación
	 * @param recuperacionPasswordService Servicio de recuperación de contraseña
	 */
	public AuthController(AuthService authService, RecuperacionPasswordService recuperacionPasswordService) {

		this.authService = authService;
		this.recuperacionPasswordService = recuperacionPasswordService;
	}

	/**
	 * Registra un usuario nuevo con el rol USER.
	 *
	 * @param user Datos del usuario (username, password, nombre y apellido)
	 * @return Mapa con el estado, el mensaje y los datos del usuario registrado
	 */
	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> registrar(@RequestBody User user) {
		return ResponseEntity.ok(authService.registrar(user));
	}

	/**
	 * Inicia sesión y devuelve el token JWT.
	 *
	 * @param datos Mapa con "username" (correo) y "password"
	 * @return Mapa con el estado, el mensaje, el token y los datos del usuario
	 */
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> iniciarSesion(@RequestBody Map<String, String> datos) {

		String username = datos.get("username");
		String password = datos.get("password");

		return ResponseEntity.ok(authService.iniciarSesion(username, password));
	}

	/**
	 * Envía al correo un código de 6 dígitos para recuperar la contraseña. Responde lo mismo exista o
	 * no el correo.
	 *
	 * @param datos Mapa con "correo"
	 * @return Mapa con el estado y el mensaje
	 */
	@PostMapping("/recuperar-password")
	public ResponseEntity<Map<String, Object>> solicitarRecuperacion(@RequestBody Map<String, String> datos) {

		return ResponseEntity.ok(recuperacionPasswordService.solicitarRecuperacion(datos.get("correo")));
	}

	/**
	 * Cambia la contraseña usando el código enviado al correo.
	 *
	 * @param datos Mapa con "correo", "codigo" y "nuevaPassword"
	 * @return Mapa con el estado y el mensaje
	 */
	@PostMapping("/confirmar-recuperacion")
	public ResponseEntity<Map<String, Object>> confirmarRecuperacion(@RequestBody Map<String, String> datos) {

		return ResponseEntity.ok(recuperacionPasswordService.confirmarRecuperacion(datos.get("correo"),
				datos.get("codigo"), datos.get("nuevaPassword")));
	}
}
