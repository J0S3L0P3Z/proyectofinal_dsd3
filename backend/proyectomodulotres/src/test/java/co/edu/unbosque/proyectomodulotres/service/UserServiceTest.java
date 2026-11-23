package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.exception.UsuarioYaExisteException;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

/**
 * Pruebas unitarias de {@link UserService} con los repositorios simulados.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	/** Repositorio de usuarios simulado. */
	@Mock
	private UserRepository userRepository;

	/** Repositorio de roles simulado. */
	@Mock
	private RolRepository rolRepository;

	/** Servicio que se prueba. */
	@InjectMocks
	private UserService userService;

	/**
	 * Crea un usuario de prueba con id 5 y rol USER.
	 *
	 * @return Usuario de prueba
	 */
	private User crearUsuario() {

		User usuario = new User("ana@correo.com", "hash", new Rol("USER", "Usuario"), "Ana", "Gomez");
		usuario.setId(5L);

		return usuario;
	}

	/** Buscar por id devuelve el DTO del usuario. */
	@Test
	void buscarPorIdDevuelveElUsuario() {

		when(userRepository.findById(5L)).thenReturn(Optional.of(crearUsuario()));

		UserDTO usuario = userService.buscarPorId(5L);

		assertEquals("ana@correo.com", usuario.getUsername());
		assertEquals("USER", usuario.getRole());
	}

	/** Un id inválido no se acepta. */
	@Test
	void buscarPorIdInvalidoLanzaError() {
		assertThrows(DatoInvalidoException.class, () -> userService.buscarPorId(0L));
	}

	/** Un usuario que no existe lanza recurso no encontrado. */
	@Test
	void buscarUsuarioInexistenteLanzaError() {

		when(userRepository.findById(9L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> userService.buscarPorId(9L));
	}

	/** Actualizar solo cambia los campos enviados. */
	@Test
	void actualizarCambiaSoloLosCamposEnviados() {

		User usuario = crearUsuario();

		when(userRepository.findById(5L)).thenReturn(Optional.of(usuario));
		when(userRepository.save(any(User.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		UserDTO datos = new UserDTO(null, null, "Ana Maria", null, null, null, null, null);

		UserDTO actualizado = userService.actualizar(5L, datos);

		assertEquals("Ana Maria", actualizado.getNombre());
		assertEquals("Gomez", actualizado.getApellido());
		assertEquals("ana@correo.com", actualizado.getUsername());
	}

	/** No se puede cambiar el correo por uno que ya existe. */
	@Test
	void actualizarConCorreoRepetidoLanzaError() {

		when(userRepository.findById(5L)).thenReturn(Optional.of(crearUsuario()));
		when(userRepository.existsByUsername("otro@correo.com")).thenReturn(true);

		UserDTO datos = new UserDTO(null, "otro@correo.com", null, null, null, LocalDate.now(), null, null);

		assertThrows(UsuarioYaExisteException.class, () -> userService.actualizar(5L, datos));

		verify(userRepository, never()).save(any(User.class));
	}

	/** No se puede eliminar un usuario que no existe. */
	@Test
	void eliminarUsuarioInexistenteLanzaError() {

		when(userRepository.existsById(9L)).thenReturn(false);

		assertThrows(RecursoNoEncontradoException.class, () -> userService.eliminar(9L));
	}

	/** Contar administradores usa el rol ADMIN. */
	@Test
	void contarAdministradoresUsaElRolAdmin() {

		Rol admin = new Rol("ADMIN", "Administrador");

		when(rolRepository.findByNombreIgnoreCase("ADMIN")).thenReturn(Optional.of(admin));
		when(userRepository.countByRole(admin)).thenReturn(2L);

		assertEquals(2L, userService.contarAdministradores());
	}
}
