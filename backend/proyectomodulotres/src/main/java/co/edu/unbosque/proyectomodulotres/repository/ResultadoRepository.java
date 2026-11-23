package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Resultado;

/**
 * Repositorio JPA de los resultados de las IA.
 */
@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

	/**
	 * Busca los resultados de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Resultados del scraping
	 */
	List<Resultado> findByIdScraping(Long idScraping);

	/**
	 * Busca los resultados de una IA.
	 *
	 * @param nombreIA Nombre de la IA
	 * @return Resultados de esa IA
	 */
	List<Resultado> findByNombreIA(String nombreIA);

	/**
	 * Busca los resultados por estado.
	 *
	 * @param estado Estado a buscar
	 * @return Resultados con ese estado
	 */
	List<Resultado> findByEstado(String estado);
}
