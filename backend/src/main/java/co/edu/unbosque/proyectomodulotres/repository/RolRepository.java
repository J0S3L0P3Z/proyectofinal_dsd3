package co.edu.unbosque.proyectomodulotres.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

	Optional<Rol> findByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCase(String nombre);
}
