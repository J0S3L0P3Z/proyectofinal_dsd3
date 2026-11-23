package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Reporte;

/**
 * Repositorio JPA de los reportes PDF.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

	/**
	 * Busca los reportes de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Reportes del scraping
	 */
	List<Reporte> findByIdScraping(Long idScraping);

	/**
	 * Busca los reportes por estado.
	 *
	 * @param estado Estado a buscar
	 * @return Reportes con ese estado
	 */
	List<Reporte> findByEstado(String estado);
}
