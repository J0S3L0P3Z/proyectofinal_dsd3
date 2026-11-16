package co.edu.unbosque.webscraper.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Petición para scrapear una página.
 *
 * @param url Dirección de la página (http o https; si falta el esquema se asume https)
 */
public record ScrapeRequestDTO(@NotBlank @Size(max = 2048) String url) {}
