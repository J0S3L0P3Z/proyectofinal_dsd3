package co.edu.unbosque.proyectomodulotres.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.entity.User;

/**
 * Repositorio JPA de los usuarios. El username es el correo del usuario.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Busca un usuario por su correo.
	 *
	 * @param username Correo del usuario
	 * @return El usuario si existe
	 */
	Optional<User> findByUsername(String username);

	/**
	 * Indica si ya existe un usuario con ese correo.
	 *
	 * @param username Correo a revisar
	 * @return true si existe
	 */
	boolean existsByUsername(String username);

	/**
	 * Cuenta los usuarios que tienen un rol.
	 *
	 * @param role Rol a contar
	 * @return Cantidad de usuarios con ese rol
	 */
	long countByRole(Rol role);
}
