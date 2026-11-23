package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Scraping;

/**
 * Repositorio JPA de los scrapings.
 */
@Repository
public interface ScrapingRepository extends JpaRepository<Scraping, Long> {

	/**
	 * Busca los scrapings de un usuario.
	 *
	 * @param idUsuario Identificador del usuario
	 * @return Scrapings del usuario
	 */
	List<Scraping> findByIdUsuario(Long idUsuario);

	/**
	 * Busca los scrapings por estado.
	 *
	 * @param estado Estado a buscar (COMPLETADO o FALLIDO)
	 * @return Scrapings con ese estado
	 */
	List<Scraping> findByEstado(String estado);
}
