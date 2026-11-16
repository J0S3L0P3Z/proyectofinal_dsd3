package co.edu.unbosque.webscraper.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Respuesta (o fallo) de una IA para un job. */
@Entity
@Table(name = "ai_analyses")
public class AiAnalysis {
    public static final String OK = "OK";
    public static final String FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(nullable = false)
    private String provider;

    private String model;

    @Column(nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> response;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AiAnalysis() {}

    public AiAnalysis(UUID jobId, String provider, String model, String status, Map<String, Object> response,
                            String errorCode, String errorMessage, Long durationMs) {
        this.jobId = jobId;
        this.provider = provider;
        this.model = model;
        this.status = status;
        this.response = response;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
    }

    public Long getId() { return id; }
    public UUID getJobId() { return jobId; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public String getStatus() { return status; }
    public boolean isOk() { return OK.equals(status); }
    public Map<String, Object> getResponse() { return response; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public Long getDurationMs() { return durationMs; }
}
