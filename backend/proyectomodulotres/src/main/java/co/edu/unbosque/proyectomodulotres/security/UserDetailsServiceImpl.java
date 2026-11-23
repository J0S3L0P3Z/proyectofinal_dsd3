package co.edu.unbosque.proyectomodulotres.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

/**
 * Implementación de {@link UserDetailsService} que carga los usuarios desde la base de datos usando
 * su correo.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	/** Repositorio de usuarios. */
	private final UserRepository userRepository;

	/**
	 * Constructor que recibe el repositorio de usuarios.
	 *
	 * @param userRepository Repositorio de usuarios
	 */
	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Busca un usuario por su correo.
	 *
	 * @param username Correo del usuario
	 * @return Usuario encontrado
	 * @throws UsernameNotFoundException Si no existe un usuario con ese correo
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		return userRepository.findByUsername(username).orElseThrow(
				() -> new UsernameNotFoundException("No se encontró un usuario con el correo: " + username));
	}
}
