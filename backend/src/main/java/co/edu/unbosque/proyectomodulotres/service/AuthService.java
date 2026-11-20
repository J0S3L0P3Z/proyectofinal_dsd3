package co.edu.unbosque.proyectomodulotres.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;
import co.edu.unbosque.proyectomodulotres.security.JwtUtil;

@Service
@Transactional
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtUtil jwtUtil) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	public Map<String, Object> registrar(User user) {

		if (user.getUsername() == null || user.getUsername().isBlank()) {
			throw new RuntimeException("El correo es obligatorio");
		}

		if (user.getNombre() == null || user.getNombre().isBlank()) {
			throw new RuntimeException("El nombre es obligatorio");
		}

		if (user.getApellido() == null || user.getApellido().isBlank()) {
			throw new RuntimeException("El apellido es obligatorio");
		}

		if (user.getPassword() == null || user.getPassword().length() < 8) {
			throw new RuntimeException("La contraseña debe tener mínimo 8 caracteres");
		}

		if (userRepository.existsByUsername(user.getUsername())) {
			throw new RuntimeException("Este correo ya está registrado");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(User.Role.USER);
		user.setEnabled(true);
		user.setActivo(true);

		User usuarioGuardado = userRepository.save(user);

		Map<String, Object> respuesta = new HashMap<>();

		respuesta.put("estado", "EXITO");
		respuesta.put("mensaje", "Usuario registrado correctamente");
		respuesta.put("usuario", convertirDTO(usuarioGuardado));

		return respuesta;
	}

	public Map<String, Object> iniciarSesion(String username, String password) {

		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		String token = jwtUtil.generateToken(user);

		Map<String, Object> respuesta = new HashMap<>();

		respuesta.put("estado", "EXITO");
		respuesta.put("mensaje", "Inicio de sesión exitoso");
		respuesta.put("token", token);
		respuesta.put("usuario", convertirDTO(user));

		return respuesta;
	}

	private UserDTO convertirDTO(User user) {

		return new UserDTO(user.getId(), user.getUsername(), user.getNombre(), user.getApellido(),
				user.getRole().name(), user.getFechaRegistro(), user.isEnabled(), user.isActivo());
	}
}
