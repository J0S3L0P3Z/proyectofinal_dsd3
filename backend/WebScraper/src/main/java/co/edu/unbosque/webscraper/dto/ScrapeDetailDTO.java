package co.edu.unbosque.webscraper.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/**
 * Detalle de un scraping. "facts" y "analyses" llegan vacíos mientras el job no ha llegado a esa
 * etapa.
 *
 * @param job Resumen y estado actual
 * @param pdfAvailable true si ya se puede descargar el PDF
 * @param facts Datos medidos por el código (imágenes, enlaces, tecnologías...)
 * @param contentTruncated true si el contenido enviado a las IAs se recortó por tamaño
 * @param analyses Una entrada por IA
 */
public record ScrapeDetailDTO(
    ScrapeJobDTO job,
    boolean pdfAvailable,
    JsonNode facts,
    boolean contentTruncated,
    List<AiAnalysisDTO> analyses,
    String aiSummary) {}
