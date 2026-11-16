package co.edu.unbosque.webscraper.repository;

import co.edu.unbosque.webscraper.model.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio para la entidad User. Proporciona operaciones CRUD básicas heredadas de
 * JpaRepository y métodos personalizados para buscar y eliminar usuarios por nombre de usuario o
 * por correo electrónico.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Busca un usuario por su nombre de usuario.
   *
   * @param username El nombre de usuario a buscar
   * @return Un Optional que contiene el usuario si existe, o vacío si no existe
   */
  public Optional<User> findByUsername(String username);

  /**
   * Busca un usuario por su correo electrónico (guardado en minúsculas).
   *
   * @param email El correo a buscar
   * @return Un Optional que contiene el usuario si existe, o vacío si no existe
   */
  public Optional<User> findByEmail(String email);

  /**
   * Elimina un usuario por su nombre de usuario.
   *
   * @param username El nombre de usuario del usuario a eliminar
   */
  public void deleteByUsername(String username);

  /**
   * Indica si ya existe un usuario con ese nombre de usuario.
   *
   * @param username Nombre de usuario
   * @return true si existe
   */
  public boolean existsByUsername(String username);

  /**
   * Indica si ya existe un usuario con ese correo.
   *
   * @param email Correo en minúsculas
   * @return true si existe
   */
  public boolean existsByEmail(String email);

  /**
   * Búsqueda paginada por parte del nombre de usuario o por el correo exacto. El correo está
   * cifrado en la base, por eso solo se puede buscar completo (no por una parte).
   *
   * @param username Texto a buscar en el nombre de usuario
   * @param email Correo completo (en minúsculas)
   * @param pageable Paginación
   * @return Página de usuarios
   */
  public Page<User> findByUsernameContainingIgnoreCaseOrEmail(
      String username, String email, Pageable pageable);

  /**
   * Cuenta los usuarios habilitados.
   *
   * @return Cantidad de usuarios habilitados
   */
  public long countByEnabledTrue();

  /**
   * Cuenta los usuarios creados después de una fecha.
   *
   * @param since Fecha de corte
   * @return Cantidad de usuarios nuevos
   */
  public long countByCreatedAtAfter(Instant since);
}
