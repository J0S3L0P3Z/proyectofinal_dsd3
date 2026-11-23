package co.edu.unbosque.proyectomodulotres.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Rol;

/**
 * Repositorio JPA de los roles.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

	/**
	 * Busca un rol por su nombre sin importar mayúsculas.
	 *
	 * @param nombre Nombre del rol
	 * @return El rol si existe
	 */
	Optional<Rol> findByNombreIgnoreCase(String nombre);

	/**
	 * Indica si existe un rol con ese nombre sin importar mayúsculas.
	 *
	 * @param nombre Nombre del rol
	 * @return true si existe
	 */
	boolean existsByNombreIgnoreCase(String nombre);
}
