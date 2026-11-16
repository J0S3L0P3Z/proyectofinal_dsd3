package co.edu.unbosque.webscraper.dto;

import co.edu.unbosque.webscraper.model.JobStatus;
import java.util.UUID;

/**
 * Respuesta inmediata al crear un scraping: el análisis corre en segundo plano y se consulta con
 * su id.
 *
 * @param id Identificador del job
 * @param status Estado inicial (PENDING)
 * @param requestedUrl URL normalizada que se va a analizar
 */
public record ScrapeCreatedDTO(UUID id, JobStatus status, String requestedUrl) {}
