package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Historial;

/**
 * Repositorio JPA del historial de consultas.
 */
@Repository
public interface HistorialRepository extends JpaRepository<Historial, Long> {

	/**
	 * Busca el historial de un usuario.
	 *
	 * @param idUsuario Identificador del usuario
	 * @return Registros del usuario
	 */
	List<Historial> findByIdUsuario(Long idUsuario);

	/**
	 * Busca el historial de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Registros del scraping
	 */
	List<Historial> findByIdScraping(Long idScraping);

	/**
	 * Busca el historial por estado.
	 *
	 * @param estado Estado a buscar
	 * @return Registros con ese estado
	 */
	List<Historial> findByEstado(String estado);
}
