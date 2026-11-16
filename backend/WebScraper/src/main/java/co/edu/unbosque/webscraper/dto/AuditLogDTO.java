package co.edu.unbosque.webscraper.dto;

import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.AuditLog;
import java.time.Instant;

/**
 * Registro de auditoría para el panel de administración.
 *
 * @param id Identificador del registro
 * @param userId Id del usuario (nulo si el intento no pertenece a ninguna cuenta)
 * @param username Nombre de usuario o identificador que se intentó
 * @param action Acción registrada
 * @param detail Detalle breve
 * @param ip Dirección IP de origen
 * @param createdAt Fecha y hora
 */
public record AuditLogDTO(
    Long id, Long userId, String username, AuditAction action, String detail, String ip, Instant createdAt) {

  /**
   * Convierte la entidad en su DTO.
   *
   * @param a Registro de auditoría
   * @return DTO listo para la API
   */
  public static AuditLogDTO from(AuditLog a) {
    return new AuditLogDTO(
        a.getId(), a.getUserId(), a.getUsername(), a.getAction(), a.getDetail(), a.getIp(), a.getCreatedAt());
  }
}
