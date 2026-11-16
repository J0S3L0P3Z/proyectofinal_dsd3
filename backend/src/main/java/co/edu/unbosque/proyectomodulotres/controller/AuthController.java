package co.edu.unbosque.proyectomodulotres.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.service.AuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
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
}