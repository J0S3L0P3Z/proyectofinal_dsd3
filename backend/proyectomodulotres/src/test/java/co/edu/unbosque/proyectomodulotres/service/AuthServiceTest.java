package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.exception.AccesoNoAutorizadoException;
import co.edu.unbosque.proyectomodulotres.exception.CredencialesInvalidasException;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.UsuarioYaExisteException;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;
import co.edu.unbosque.proyectomodulotres.security.JwtUtil;

/**
 * Pruebas unitarias de {@link AuthService} con los repositorios, el codificador y el JWT simulados.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	/** Repositorio de usuarios simulado. */
	@Mock
	private UserRepository userRepository;

	/** Repositorio de roles simulado. */
	@Mock
	private RolRepository rolRepository;

	/** Codificador de contraseñas simulado. */
	@Mock
	private PasswordEncoder passwordEncoder;

	/** Administrador de autenticación simulado. */
	@Mock
	private AuthenticationManager authenticationManager;

	/** Utilidad JWT simulada. */
	@Mock
	private JwtUtil jwtUtil;

	/** Servicio que se prueba. */
	@InjectMocks
	private AuthService authService;

	/**
	 * Crea un usuario de prueba.
	 *
	 * @return Usuario con correo, contraseña, nombre y apellido
	 */
	private User crearUsuario() {
		return new User("ana@correo.com", "Clave12345", null, "Ana", "Gomez");
	}

	/** Un registro válido codifica la contraseña y asigna el rol USER. */
	@Test
	void registrarGuardaElUsuarioConRolUser() {

		Rol rolUser = new Rol("USER", "Usuario");

		when(userRepository.existsByUsername("ana@correo.com")).thenReturn(false);
		when(rolRepository.findByNombreIgnoreCase("USER")).thenReturn(Optional.of(rolUser));
		when(passwordEncoder.encode("Clave12345")).thenReturn("hash");
		when(userRepository.save(any(User.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		Map<String, Object> respuesta = authService.registrar(crearUsuario());

		UserDTO usuario = (UserDTO) respuesta.get("usuario");

		assertEquals("EXITO", respuesta.get("estado"));
		assertEquals("USER", usuario.getRole());
		assertEquals("ana@correo.com", usuario.getUsername());
	}

	/** No se puede registrar un correo que ya existe. */
	@Test
	void registrarCorreoRepetidoLanzaError() {

		when(userRepository.existsByUsername("ana@correo.com")).thenReturn(true);

		assertThrows(UsuarioYaExisteException.class, () -> authService.registrar(crearUsuario()));

		verify(userRepository, never()).save(any(User.class));
	}

	/** Una contraseña de menos de 8 caracteres no se acepta. */
	@Test
	void registrarConContrasenaCortaLanzaError() {

		User usuario = crearUsuario();
		usuario.setPassword("123");

		assertThrows(DatoInvalidoException.class, () -> authService.registrar(usuario));
	}

	/** Un registro sin nombre no se acepta. */
	@Test
	void registrarSinNombreLanzaError() {

		User usuario = crearUsuario();
		usuario.setNombre(" ");

		assertThrows(DatoInvalidoException.class, () -> authService.registrar(usuario));
	}

	/** Con credenciales correctas el login devuelve el token. */
	@Test
	void iniciarSesionDevuelveElToken() {

		User usuario = crearUsuario();
		usuario.setRole(new Rol("USER", "Usuario"));

		when(userRepository.findByUsername("ana@correo.com")).thenReturn(Optional.of(usuario));
		when(jwtUtil.generateToken(usuario)).thenReturn("token-de-prueba");

		Map<String, Object> respuesta = authService.iniciarSesion("ana@correo.com", "Clave12345");

		assertEquals("token-de-prueba", respuesta.get("token"));
		assertEquals("EXITO", respuesta.get("estado"));
	}

	/** Con contraseña incorrecta el login lanza credenciales inválidas. */
	@Test
	void iniciarSesionConClaveIncorrectaLanzaError() {

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("malas credenciales"));

		assertThrows(CredencialesInvalidasException.class,
				() -> authService.iniciarSesion("ana@correo.com", "incorrecta"));
	}

	/** Una cuenta deshabilitada no puede iniciar sesión. */
	@Test
	void iniciarSesionConCuentaDeshabilitadaLanzaError() {

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new DisabledException("deshabilitada"));

		assertThrows(AccesoNoAutorizadoException.class,
				() -> authService.iniciarSesion("ana@correo.com", "Clave12345"));
	}

	/** El login sin contraseña no se acepta. */
	@Test
	void iniciarSesionSinContrasenaLanzaError() {
		assertThrows(DatoInvalidoException.class, () -> authService.iniciarSesion("ana@correo.com", ""));
	}
}
