package co.edu.unbosque.webscraper.model;

import co.edu.unbosque.webscraper.util.EncryptedConverter;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Registro de auditoría: guarda, sin intervención del usuario, cada movimiento importante (registro,
 * inicio de sesión, scrapings, descargas de PDF y cambios de administrador) con el usuario, la fecha
 * y la IP. No tiene llaves foráneas para conservar el rastro aunque se borre el usuario.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 255)
  private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(length = 600)
  private String detail;

    /** IP desde la que se hizo la acción. Dato sensible: se guarda cifrado (AES/GCM, IV aleatorio). */
    @Convert(converter = EncryptedConverter.class)
    @Column(length = 255)
    private String ip;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {}

    public AuditLog(Long userId, String username, AuditAction action, String detail, String ip) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.detail = detail;
        this.ip = ip;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public AuditAction getAction() { return action; }
    public String getDetail() { return detail; }
    public String getIp() { return ip; }
    public Instant getCreatedAt() { return createdAt; }
}
