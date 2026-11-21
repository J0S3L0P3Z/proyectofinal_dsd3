package co.edu.unbosque.proyectomodulotres.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.service.AuthService;
import co.edu.unbosque.proyectomodulotres.service.RecuperacionPasswordService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

	private final AuthService authService;
	private final RecuperacionPasswordService recuperacionPasswordService;

	public AuthController(AuthService authService, RecuperacionPasswordService recuperacionPasswordService) {

		this.authService = authService;
		this.recuperacionPasswordService = recuperacionPasswordService;
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> registrar(@RequestBody User user) {
		return ResponseEntity.ok(authService.registrar(user));
	}

	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> iniciarSesion(@RequestBody Map<String, String> datos) {

		String username = datos.get("username");
		String password = datos.get("password");

		return ResponseEntity.ok(authService.iniciarSesion(username, password));
	}

	@PostMapping("/recuperar-password")
	public ResponseEntity<Map<String, Object>> solicitarRecuperacion(@RequestBody Map<String, String> datos) {

		return ResponseEntity.ok(recuperacionPasswordService.solicitarRecuperacion(datos.get("correo")));
	}

	@PostMapping("/confirmar-recuperacion")
	public ResponseEntity<Map<String, Object>> confirmarRecuperacion(@RequestBody Map<String, String> datos) {

		return ResponseEntity.ok(recuperacionPasswordService.confirmarRecuperacion(datos.get("correo"),
				datos.get("codigo"), datos.get("nuevaPassword")));
	}
}
