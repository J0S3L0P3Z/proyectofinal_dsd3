package co.edu.unbosque.webscraper.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Hechos del scraping (JSON del contrato F0.4) guardados como jsonb. */
@Entity
@Table(name = "scrape_results")
public class ScrapeResult {

    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> facts;

    @Column(name = "content_format")
    private String contentFormat;

    @Column(name = "content_truncated", nullable = false)
    private boolean contentTruncated;

    @Column(name = "original_chars")
    private Integer originalChars;

    @Column(name = "cleaned_chars")
    private Integer cleanedChars;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ScrapeResult() {}

    public ScrapeResult(UUID jobId, Map<String, Object> facts, String contentFormat, boolean contentTruncated,
                        Integer originalChars, Integer cleanedChars) {
        this.jobId = jobId;
        this.facts = facts;
        this.contentFormat = contentFormat;
        this.contentTruncated = contentTruncated;
        this.originalChars = originalChars;
        this.cleanedChars = cleanedChars;
    }

    public UUID getJobId() { return jobId; }
    public Map<String, Object> getFacts() { return facts; }
    public String getContentFormat() { return contentFormat; }
    public boolean isContentTruncated() { return contentTruncated; }
    public Integer getOriginalChars() { return originalChars; }
    public Integer getCleanedChars() { return cleanedChars; }
}
