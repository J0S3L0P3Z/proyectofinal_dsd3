package co.edu.unbosque.proyectomodulotres.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.exception.AccesoNoAutorizadoException;
import co.edu.unbosque.proyectomodulotres.exception.CredencialesInvalidasException;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.exception.UsuarioYaExisteException;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;
import co.edu.unbosque.proyectomodulotres.security.JwtUtil;

@Service
@Transactional
public class AuthService {

	private final UserRepository userRepository;
	private final RolRepository rolRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public AuthService(UserRepository userRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtUtil jwtUtil) {

		this.userRepository = userRepository;
		this.rolRepository = rolRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	public Map<String, Object> registrar(User user) {

		if (user == null) {
			throw new DatoInvalidoException("Los datos del usuario son obligatorios");
		}

		if (user.getUsername() == null || user.getUsername().isBlank()) {
			throw new DatoInvalidoException("El correo es obligatorio");
		}

		if (user.getNombre() == null || user.getNombre().isBlank()) {
			throw new DatoInvalidoException("El nombre es obligatorio");
		}

		if (user.getApellido() == null || user.getApellido().isBlank()) {
			throw new DatoInvalidoException("El apellido es obligatorio");
		}

		if (user.getPassword() == null || user.getPassword().length() < 8) {
			throw new DatoInvalidoException("La contraseña debe tener mínimo 8 caracteres");
		}

		if (userRepository.existsByUsername(user.getUsername())) {
			throw new UsuarioYaExisteException("Este correo ya está registrado");
		}

		Rol rolUsuario = rolRepository.findByNombreIgnoreCase("USER")
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol USER"));

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole(rolUsuario);
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

		if (username == null || username.isBlank()) {
			throw new DatoInvalidoException("El correo es obligatorio");
		}

		if (password == null || password.isBlank()) {
			throw new DatoInvalidoException("La contraseña es obligatoria");
		}

		try {

			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

		} catch (BadCredentialsException e) {

			throw new CredencialesInvalidasException("Correo o contraseña incorrectos");

		} catch (DisabledException | LockedException e) {

			throw new AccesoNoAutorizadoException("La cuenta está deshabilitada o bloqueada");

		} catch (AuthenticationException e) {

			throw new CredencialesInvalidasException("No fue posible autenticar al usuario");
		}

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

		if (!user.isEnabled() || !user.isActivo()) {
			throw new AccesoNoAutorizadoException("La cuenta está deshabilitada o inactiva");
		}

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
				user.getRole().getNombre(), user.getFechaRegistro(), user.isEnabled(), user.isActivo());
	}
}