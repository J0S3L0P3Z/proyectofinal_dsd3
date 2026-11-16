package co.edu.unbosque.webscraper.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import co.edu.unbosque.webscraper.model.ScrapeResult;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio de los resultados del scraping (datos medidos de la página).
 */
public interface ScrapeResultRepository extends JpaRepository<ScrapeResult, UUID> {

    @Modifying
    @Query("delete from ScrapeResult r where r.jobId in (select j.id from ScrapeJob j where j.userId = :userId)")
    void deleteByUserId(@Param("userId") Long userId);
}
