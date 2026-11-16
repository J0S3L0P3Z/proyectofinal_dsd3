package co.edu.unbosque.webscraper.security;

import co.edu.unbosque.webscraper.repository.UserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de detalles de usuario para la autenticación. Carga los datos del
 * usuario desde el repositorio durante el proceso de autenticación. Se puede iniciar sesión con el
 * nombre de usuario o con el correo electrónico (autenticación por correo).
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  /** Repositorio de usuarios utilizado para buscar información de usuarios. */
  private final UserRepository userRepository;

  /**
   * Constructor que inicializa el repositorio de usuarios.
   *
   * @param userRepository El repositorio de usuarios a utilizar para las consultas
   */
  public UserDetailsServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Carga los detalles del usuario por su nombre de usuario o por su correo electrónico.
   *
   * @param username Nombre de usuario o correo electrónico
   * @return Los detalles del usuario encontrado
   * @throws UsernameNotFoundException Si no se encuentra el usuario
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String identifier = username == null ? "" : username.trim();
    return userRepository
        .findByUsername(identifier)
        .or(() -> userRepository.findByEmail(identifier.toLowerCase(Locale.ROOT)))
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + identifier));
  }
}
