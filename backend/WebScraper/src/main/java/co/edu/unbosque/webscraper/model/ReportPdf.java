package co.edu.unbosque.webscraper.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * PDF de un scraping guardado en el historial del usuario. Se genera al terminar el análisis, se
 * adjunta al correo de notificación y se puede descargar después desde el historial. Si por algún
 * motivo falta, se vuelve a generar desde el JSON guardado al pedir la descarga.
 */
@Entity
@Table(name = "report_pdfs")
public class ReportPdf {

    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, length = 33_554_432)
    private byte[] content;

    @Column(name = "size_bytes", nullable = false)
    private int sizeBytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ReportPdf() {}

    public ReportPdf(UUID jobId, String filename, byte[] content) {
        this.jobId = jobId;
        this.filename = filename;
        this.content = content;
        this.sizeBytes = content.length;
    }

    public UUID getJobId() { return jobId; }
    public String getFilename() { return filename; }
    public byte[] getContent() { return content; }
    public int getSizeBytes() { return sizeBytes; }
}
