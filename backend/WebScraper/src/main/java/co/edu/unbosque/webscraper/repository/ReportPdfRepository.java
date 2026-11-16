package co.edu.unbosque.webscraper.repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import co.edu.unbosque.webscraper.model.ReportPdf;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio de los PDF guardados en el historial de cada usuario.
 */
public interface ReportPdfRepository extends JpaRepository<ReportPdf, UUID> {

    @Modifying
    @Query("delete from ReportPdf p where p.jobId in (select j.id from ScrapeJob j where j.userId = :userId)")
    void deleteByUserId(@Param("userId") Long userId);
}
