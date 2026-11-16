package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.UserDTO;
import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final RolRepository rolRepository;

	public UserService(UserRepository userRepository, RolRepository rolRepository) {
		this.userRepository = userRepository;
		this.rolRepository = rolRepository;
	}

	public UserDTO buscarPorId(Long id) {

		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		return convertirDTO(user);
	}

	public UserDTO buscarPorUsername(String username) {

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		return convertirDTO(user);
	}

	public List<UserDTO> buscarTodos() {

		return userRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public UserDTO actualizar(Long id, UserDTO datos) {

		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		if (datos.getUsername() != null && !datos.getUsername().equals(user.getUsername())) {

			if (userRepository.existsByUsername(datos.getUsername())) {
				throw new RuntimeException("El correo ya está registrado");
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
					.orElseThrow(() -> new RuntimeException("El rol no existe"));

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

	public void eliminar(Long id) {

		if (!userRepository.existsById(id)) {
			throw new RuntimeException("Usuario no encontrado");
		}

		userRepository.deleteById(id);
	}

	public long contar() {
		return userRepository.count();
	}

	public long contarAdministradores() {

		Rol rolAdmin = rolRepository.findByNombreIgnoreCase("ADMIN")
				.orElseThrow(() -> new RuntimeException("No existe el rol ADMIN"));

		return userRepository.countByRole(rolAdmin);
	}

	public long contarUsuarios() {

		Rol rolUsuario = rolRepository.findByNombreIgnoreCase("USER")
				.orElseThrow(() -> new RuntimeException("No existe el rol USER"));

		return userRepository.countByRole(rolUsuario);
	}

	private UserDTO convertirDTO(User user) {

		return new UserDTO(user.getId(), user.getUsername(), user.getNombre(), user.getApellido(),
				user.getRole().getNombre(), user.getFechaRegistro(), user.isEnabled(), user.isActivo());
	}
}
