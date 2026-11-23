package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.exception.UsuarioYaExisteException;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

/**
 * Servicio de gestión de usuarios: consultar, actualizar, eliminar y contar.
 */
@Service
@Transactional
public class UserService {

	/** Repositorio de usuarios. */
	private final UserRepository userRepository;

	/** Repositorio de roles. */
	private final RolRepository rolRepository;

	/**
	 * Constructor que recibe los repositorios.
	 *
	 * @param userRepository Repositorio de usuarios
	 * @param rolRepository Repositorio de roles
	 */
	public UserService(UserRepository userRepository, RolRepository rolRepository) {
		this.userRepository = userRepository;
		this.rolRepository = rolRepository;
	}

	/**
	 * Busca un usuario por su identificador.
	 *
	 * @param id Identificador del usuario
	 * @return DTO del usuario
	 * @throws DatoInvalidoException Si el identificador es nulo o no es positivo
	 * @throws RecursoNoEncontradoException Si el usuario no existe
	 */
	public UserDTO buscarPorId(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del usuario es inválido");
		}

		User user = userRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

		return convertirDTO(user);
	}

	/**
	 * Busca un usuario por su correo.
	 *
	 * @param username Correo del usuario
	 * @return DTO del usuario
	 * @throws DatoInvalidoException Si el correo está vacío
	 * @throws RecursoNoEncontradoException Si el usuario no existe
	 */
	public UserDTO buscarPorUsername(String username) {

		if (username == null || username.isBlank()) {
			throw new DatoInvalidoException("El correo del usuario es obligatorio");
		}

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

		return convertirDTO(user);
	}

	/**
	 * Consulta todos los usuarios.
	 *
	 * @return Lista de usuarios
	 */
	public List<UserDTO> buscarTodos() {

		return userRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Actualiza un usuario. Solo cambia los campos que llegan con valor.
	 *
	 * @param id Identificador del usuario
	 * @param datos Nuevos datos del usuario
	 * @return DTO del usuario actualizado
	 * @throws DatoInvalidoException Si el identificador o los datos no son válidos
	 * @throws RecursoNoEncontradoException Si el usuario o el rol no existen
	 * @throws UsuarioYaExisteException Si el nuevo correo ya está registrado
	 */
	public UserDTO actualizar(Long id, UserDTO datos) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del usuario es inválido");
		}

		if (datos == null) {
			throw new DatoInvalidoException("Los datos del usuario son obligatorios");
		}

		User user = userRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

		if (datos.getUsername() != null && !datos.getUsername().equals(user.getUsername())) {

			if (datos.getUsername().isBlank()) {
				throw new DatoInvalidoException("El correo no puede estar vacío");
			}

			if (userRepository.existsByUsername(datos.getUsername())) {
				throw new UsuarioYaExisteException("El correo ya está registrado");
			}

			user.setUsername(datos.getUsername());
		}

		if (datos.getNombre() != null) {
			user.setNombre(datos.getNombre());
		}

		if (datos.getApellido() != null) {
			user.setApellido(datos.getApellido());
		}

		if (datos.getRole() != null) {

			Rol rol = rolRepository.findByNombreIgnoreCase(datos.getRole())
					.orElseThrow(() -> new RecursoNoEncontradoException("El rol no existe"));

			user.setRole(rol);
		}

		if (datos.getEnabled() != null) {
			user.setEnabled(datos.getEnabled());
		}

		if (datos.getActivo() != null) {
			user.setActivo(datos.getActivo());
		}

		return convertirDTO(userRepository.save(user));
	}

	/**
	 * Elimina un usuario.
	 *
	 * @param id Identificador del usuario
	 * @throws DatoInvalidoException Si el identificador no es válido
	 * @throws RecursoNoEncontradoException Si el usuario no existe
	 */
	public void eliminar(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del usuario es inválido");
		}

		if (!userRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Usuario no encontrado");
		}

		userRepository.deleteById(id);
	}

	/**
	 * Cuenta todos los usuarios.
	 *
	 * @return Cantidad de usuarios
	 */
	public long contar() {
		return userRepository.count();
	}

	/**
	 * Cuenta los usuarios con rol ADMIN.
	 *
	 * @return Cantidad de administradores
	 * @throws RecursoNoEncontradoException Si no existe el rol ADMIN
	 */
	public long contarAdministradores() {

		Rol rolAdmin = rolRepository.findByNombreIgnoreCase("ADMIN")
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol ADMIN"));

		return userRepository.countByRole(rolAdmin);
	}

	/**
	 * Cuenta los usuarios con rol USER.
	 *
	 * @return Cantidad de usuarios con rol USER
	 * @throws RecursoNoEncontradoException Si no existe el rol USER
	 */
	public long contarUsuarios() {

		Rol rolUsuario = rolRepository.findByNombreIgnoreCase("USER")
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol USER"));

		return userRepository.countByRole(rolUsuario);
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