package co.edu.unbosque.webscraper.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trabajo de scraping de un usuario: la URL pedida, la URL final, el estado actual, el error (si
 * falló), las advertencias y las fechas. El estado avanza desde PENDING hasta COMPLETED,
 * COMPLETED_WITH_WARNINGS o FAILED.
 */
@Entity
@Table(name = "scrape_jobs")
public class ScrapeJob {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "requested_url", nullable = false, length = 2048)
    private String requestedUrl;

    @Column(name = "final_url", length = 2048)
    private String finalUrl;

    @Column(nullable = false)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private List<String> warnings = new ArrayList<>();

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "load_time_ms")
    private Long loadTimeMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    /** Resumen final hecho por IA a partir de los análisis que sí respondieron (opcional). */
    @Column(name = "ai_summary", length = 4000)
    private String aiSummary;

    protected ScrapeJob() {}

    public ScrapeJob(Long userId, String requestedUrl, String domain) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.requestedUrl = requestedUrl;
        this.domain = domain;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /** Se reemplaza la lista completa para que Hibernate detecte el cambio del JSON. */
    public void addWarning(String warning) {
        List<String> copy = new ArrayList<>(warnings);
        if (!copy.contains(warning)) copy.add(warning);
        this.warnings = copy;
    }

    public UUID getId() { return id; }
    public Long getUserId() { return userId; }
    public String getRequestedUrl() { return requestedUrl; }
    public String getFinalUrl() { return finalUrl; }
    public void setFinalUrl(String finalUrl) { this.finalUrl = finalUrl; }
    public String getDomain() { return domain; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public List<String> getWarnings() { return warnings; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public Long getLoadTimeMs() { return loadTimeMs; }
    public void setLoadTimeMs(Long loadTimeMs) { this.loadTimeMs = loadTimeMs; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
}
