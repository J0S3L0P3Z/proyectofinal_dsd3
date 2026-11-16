package co.edu.unbosque.webscraper.repository;

import co.edu.unbosque.webscraper.model.AiAnalysis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de las respuestas de las IAs, con consultas de estadísticas por proveedor y borrado
 * por usuario.
 */
public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, Long> {

    List<AiAnalysis> findByJobIdOrderByIdAsc(UUID jobId);

    @Modifying
    @Query("delete from AiAnalysis a where a.jobId = :jobId")
    void deleteByJobId(@Param("jobId") UUID jobId);

    @Query("select a.provider, a.status, count(a), avg(a.durationMs) from AiAnalysis a group by a.provider, a.status")
    List<Object[]> statsByProvider();

    @Modifying
    @Query("delete from AiAnalysis a where a.jobId in (select j.id from ScrapeJob j where j.userId = :userId)")
    void deleteByUserId(@Param("userId") Long userId);
}
