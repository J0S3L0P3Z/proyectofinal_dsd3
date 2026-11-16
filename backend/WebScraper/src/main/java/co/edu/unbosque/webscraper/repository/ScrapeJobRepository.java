package co.edu.unbosque.webscraper.repository;

import co.edu.unbosque.webscraper.model.JobStatus;
import co.edu.unbosque.webscraper.model.ScrapeJob;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de los trabajos de scraping, con las consultas del historial, del vigilante y de las
 * estadísticas de administración.
 */
public interface ScrapeJobRepository extends JpaRepository<ScrapeJob, UUID> {

    Page<ScrapeJob> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<ScrapeJob> findByIdAndUserId(UUID id, Long userId);

    long countByUserIdAndStatusIn(Long userId, Collection<JobStatus> statuses);

    List<ScrapeJob> findByStatusInAndUpdatedAtBefore(Collection<JobStatus> statuses, Instant before);

    List<ScrapeJob> findByStatusIn(Collection<JobStatus> statuses);

    List<ScrapeJob> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant from, Instant to, Pageable pageable);

    @Query("select j.status, count(j) from ScrapeJob j group by j.status")
    List<Object[]> countByStatus();

    @Query("select j.errorCode, count(j) from ScrapeJob j where j.errorCode is not null group by j.errorCode order by count(j) desc")
    List<Object[]> countByErrorCode();

    @Query("select j.domain, count(j) from ScrapeJob j group by j.domain order by count(j) desc")
    List<Object[]> topDomains(Pageable pageable);

    @Query("select j.userId, count(j) from ScrapeJob j where j.userId in :ids group by j.userId")
    List<Object[]> countByUsers(@Param("ids") Collection<Long> ids);

    @Query("select avg(j.loadTimeMs) from ScrapeJob j where j.loadTimeMs is not null")
    Double averageLoadTimeMs();

    long countByCreatedAtAfter(Instant since);

    @Query(value = "select cast(created_at as date) as dia, count(*) as total from scrape_jobs "
            + "where created_at >= :since group by cast(created_at as date) order by dia", nativeQuery = true)
    List<Object[]> countPerDay(@Param("since") Instant since);

    @Modifying
    @Query("delete from ScrapeJob j where j.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
