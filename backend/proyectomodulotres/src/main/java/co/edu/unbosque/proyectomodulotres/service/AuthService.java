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

/**
 * Servicio de autenticación. Registra usuarios nuevos con el rol USER e inicia sesión generando el
 * token JWT.
 */
@Service
@Transactional
public class AuthService {

	/** Repositorio de usuarios. */
	private final UserRepository userRepository;

	/** Repositorio de roles. */
	private final RolRepository rolRepository;

	/** Codificador de contraseñas (BCrypt). */
	private final PasswordEncoder passwordEncoder;

	/** Administrador de autenticación de Spring Security. */
	private final AuthenticationManager authenticationManager;

	/** Utilidad para generar los tokens JWT. */
	private final JwtUtil jwtUtil;

	/**
	 * Constructor que recibe las dependencias del servicio.
	 *
	 * @param userRepository Repositorio de usuarios
	 * @param rolRepository Repositorio de roles
	 * @param passwordEncoder Codificador de contraseñas
	 * @param authenticationManager Administrador de autenticación
	 * @param jwtUtil Utilidad de tokens JWT
	 */
	public AuthService(UserRepository userRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtUtil jwtUtil) {

		this.userRepository = userRepository;
		this.rolRepository = rolRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	/**
	 * Registra un usuario nuevo con el rol USER. La contraseña se guarda con BCrypt.
	 *
	 * @param user Datos del usuario (correo, contraseña, nombre y apellido)
	 * @return Mapa con el estado, el mensaje y los datos del usuario registrado
	 * @throws DatoInvalidoException Si falta algún dato o la contraseña tiene menos de 8 caracteres
	 * @throws UsuarioYaExisteException Si el correo ya está registrado
	 * @throws RecursoNoEncontradoException Si no existe el rol USER
	 */
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

	/**
	 * Valida las credenciales del usuario y genera su token JWT.
	 *
	 * @param username Correo del usuario
	 * @param password Contraseña del usuario
	 * @return Mapa con el estado, el mensaje, el token y los datos del usuario
	 * @throws DatoInvalidoException Si el correo o la contraseña están vacíos
	 * @throws CredencialesInvalidasException Si el correo o la contraseña son incorrectos
	 * @throws AccesoNoAutorizadoException Si la cuenta está deshabilitada, bloqueada o inactiva
	 */
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

	/**
	 * Convierte la entidad User en UserDTO (sin la contraseña).
	 *
	 * @param user Usuario a convertir
	 * @return DTO del usuario
	 */
	private UserDTO convertirDTO(User user) {

		return new UserDTO(user.getId(), user.getUsername(), user.getNombre(), user.getApellido(),
				user.getRole().getNombre(), user.getFechaRegistro(), user.isEnabled(), user.isActivo());
	}
}