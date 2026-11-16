package co.edu.unbosque.webscraper.dto;

import co.edu.unbosque.webscraper.model.JobStatus;
import co.edu.unbosque.webscraper.model.ScrapeJob;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Resumen de un scraping (una fila del historial).
 *
 * @param id Identificador del job
 * @param requestedUrl URL que pidió el usuario
 * @param finalUrl URL final tras redirecciones (si ya se conoce)
 * @param domain Dominio de la página
 * @param status Estado actual
 * @param finished true si el job ya terminó (con éxito o con error)
 * @param errorCode Código del catálogo de errores (si falló)
 * @param errorMessage Mensaje claro para el usuario (si falló)
 * @param warnings Advertencias (por ejemplo, redirección o una IA que falló)
 * @param createdAt Fecha de creación
 * @param finishedAt Fecha de término
 */
public record ScrapeJobDTO(
    UUID id,
    String requestedUrl,
    String finalUrl,
    String domain,
    JobStatus status,
    boolean finished,
    String errorCode,
    String errorMessage,
    List<String> warnings,
    Instant createdAt,
    Instant finishedAt) {

  /**
   * Convierte la entidad en su resumen.
   *
   * @param j Entidad del job
   * @return Resumen listo para la API
   */
  public static ScrapeJobDTO from(ScrapeJob j) {
    return new ScrapeJobDTO(
        j.getId(),
        j.getRequestedUrl(),
        j.getFinalUrl(),
        j.getDomain(),
        j.getStatus(),
        j.getStatus().isTerminal(),
        j.getErrorCode(),
        j.getErrorMessage(),
        j.getWarnings(),
        j.getCreatedAt(),
        j.getFinishedAt());
  }
}
