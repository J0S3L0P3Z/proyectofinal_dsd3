package co.edu.unbosque.webscraper.controller;

import co.edu.unbosque.webscraper.dto.ScrapeCreatedDTO;
import co.edu.unbosque.webscraper.dto.ScrapeDetailDTO;
import co.edu.unbosque.webscraper.dto.ScrapeJobDTO;
import co.edu.unbosque.webscraper.dto.ScrapeRequestDTO;
import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.service.AuditService;
import co.edu.unbosque.webscraper.service.ReportService;
import co.edu.unbosque.webscraper.service.ScrapeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del scraper. Permite crear un scraping (corre en segundo plano), consultar el
 * historial propio, ver el detalle con las respuestas de las 3 IAs y descargar el PDF. Cualquier
 * usuario autenticado (USER o ADMIN) puede usarlo y solo ve sus propios scrapings. Los errores
 * responden JSON con {status, code, message}.
 */
@RestController
@RequestMapping("/scrape")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Scraping", description = "Crear un scraping, ver el historial, el detalle con las 3 IAs y descargar el PDF")
public class ScrapeController {

  /** Servicio de scraping. */
  private final ScrapeService scrapeService;

  /** Servicio de auditoría. */
  private final AuditService audit;

  /**
   * Constructor con las dependencias del controlador.
   *
   * @param scrapeService Servicio de scraping
   * @param audit Servicio de auditoría
   */
  public ScrapeController(ScrapeService scrapeService, AuditService audit) {
    this.scrapeService = scrapeService;
    this.audit = audit;
  }

  /**
   * Crea un scraping. Responde 202 de inmediato con el id; el análisis corre en segundo plano.
   *
   * @param me Usuario autenticado
   * @param request URL a analizar
   * @return Id y estado inicial del job
   */
  @Operation(
      summary = "Crear un scraping",
      description =
          """
            Valida la URL y crea el trabajo. **Responde de inmediato (202)** con un `id`; el análisis
            corre en segundo plano:

            `PENDING → SCRAPING → ANALYZING → GENERATING_PDF → SENDING_EMAIL → COMPLETED | COMPLETED_WITH_WARNINGS | FAILED`

            Consulta el avance con `GET /scrape/getbyid/{id}`.

            **URLs rechazadas al instante (400):** formato inválido (`INVALID_URL`) o direcciones internas
            como `localhost`, `10.x`, `192.168.x`, `169.254.169.254` (`URL_NOT_ALLOWED`).

            **URLs que no se pueden scrapear** (la página no existe, captcha o anti-bots, login obligatorio,
            403/429, dominio inexistente, certificado inválido, sin respuesta): se controlan como una excepción y
            el job termina en `FAILED` con el mensaje *"No es posible scrapear esta URL. Motivo: ..."*
            (`PAGE_NOT_FOUND`, `BLOCKED_BY_SITE`, `LOGIN_REQUIRED`, `ACCESS_FORBIDDEN`, `NETWORK_ERROR`,
            `SSL_ERROR`, `TIMEOUT`...). El mismo mensaje llega por correo. No se intenta saltar ninguna protección.

            Cada usuario puede tener como máximo `SCRAPE_MAX_ACTIVE_PER_USER` análisis en curso (429 si se pasa).
        """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "202", description = "Scraping creado", content = @Content(schema = @Schema(implementation = ScrapeCreatedDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "URL inválida (INVALID_URL) o no permitida (URL_NOT_ALLOWED)",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                "{\"status\": 400, \"code\": \"URL_NOT_ALLOWED\", \"message\": \"Esa dirección no está permitida: apunta a una red interna o privada y, por seguridad, no se puede analizar.\"}"))),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @ApiResponse(responseCode = "429", description = "Demasiados análisis en curso (TOO_MANY_ACTIVE_JOBS)"),
        @ApiResponse(responseCode = "503", description = "Cola llena (QUEUE_FULL)")
      })
  @PostMapping("/create")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ScrapeCreatedDTO create(
      @AuthenticationPrincipal User me,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "URL de la página a analizar",
              required = true,
              content =
                  @Content(
                      schema = @Schema(implementation = ScrapeRequestDTO.class),
                      examples = {
                        @ExampleObject(name = "1. Página simple", value = "{\"url\": \"https://example.com\"}"),
                        @ExampleObject(name = "2. Tienda de prueba (muchas imágenes)", value = "{\"url\": \"https://books.toscrape.com\"}"),
                        @ExampleObject(name = "3. Redirección http a https (da advertencia)", value = "{\"url\": \"http://wikipedia.org\"}"),
                        @ExampleObject(name = "4. Página inexistente (404)", value = "{\"url\": \"https://httpbin.org/status/404\"}"),
                        @ExampleObject(name = "5. Acceso prohibido (403)", value = "{\"url\": \"https://httpbin.org/status/403\"}"),
                        @ExampleObject(name = "6. Dominio que no existe", value = "{\"url\": \"https://este-dominio-no-existe-12345.com\"}"),
                        @ExampleObject(name = "7. Sitio con protección anti-bots / login", value = "{\"url\": \"https://www.netflix.com\"}"),
                        @ExampleObject(name = "8. URL interna (rechazada, SSRF)", value = "{\"url\": \"http://localhost:8080\"}"),
                        @ExampleObject(name = "9. Metadatos de nube (rechazada, SSRF)", value = "{\"url\": \"http://169.254.169.254/latest/meta-data/\"}"),
                        @ExampleObject(name = "10. Esquema inválido (rechazada)", value = "{\"url\": \"ftp://example.com\"}")
                      }))
          @Valid
          @RequestBody
          ScrapeRequestDTO request) {
    return scrapeService.create(me, request.url());
  }

  /**
   * Historial del usuario autenticado, paginado y del más reciente al más antiguo.
   *
   * @param me Usuario autenticado
   * @param page Número de página (desde 0)
   * @param size Tamaño de página (1 a 50)
   * @return Página de resúmenes
   */
  @Operation(
      summary = "Mi historial de scrapings",
      description = "Lista paginada (del más reciente al más antiguo) solo con los scrapings del usuario autenticado. Cada fila trae el estado, el error (si lo hubo) y las advertencias.")
  @ApiResponse(responseCode = "200", description = "Página de resúmenes")
  @GetMapping("/getall")
  public Page<ScrapeJobDTO> history(
      @AuthenticationPrincipal User me,
      @Parameter(description = "Número de página (desde 0)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Tamaño de página (1 a 50)", example = "10") @RequestParam(defaultValue = "10") int size) {
    int safeSize = Math.max(1, Math.min(size, 50));
    return scrapeService.history(me.getId(), PageRequest.of(Math.max(0, page), safeSize));
  }

  /**
   * Detalle de un scraping: se consulta repetidamente hasta que "finished" sea true.
   *
   * @param me Usuario autenticado
   * @param id Id del job
   * @return Detalle del job
   */
  @Operation(
      summary = "Detalle y estado de un scraping",
      description =
          """
            Devuelve el estado actual (`job.status`), las advertencias, el error si falló y, cuando ya existen,
            los **datos medidos** (`facts`: imágenes, enlaces, tecnologías...) y las **respuestas de las 3 IAs**
            (`analyses`, una por IA, con `ok: true/false`).

            Mientras `job.finished` sea `false`, vuelve a consultar cada 2-3 segundos.
        """)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Detalle del scraping", content = @Content(schema = @Schema(implementation = ScrapeDetailDTO.class))),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
        @ApiResponse(responseCode = "404", description = "No existe o no es tuyo")
      })
  @GetMapping("/getbyid/{id}")
  public ScrapeDetailDTO detail(
      @AuthenticationPrincipal User me,
      @Parameter(description = "Id devuelto por /scrape/create", example = "6f1c2b7e-3a1d-4c55-9d52-0b6a1e8f7c10") @PathVariable UUID id) {
    return scrapeService.detail(me.getId(), id);
  }

  /**
   * Descarga el PDF del reporte (una sección por IA).
   *
   * @param me Usuario autenticado
   * @param id Id del job
   * @return El PDF como archivo adjunto
   */
  @Operation(
      summary = "Descargar el PDF del reporte",
      description = "Descarga el PDF (portada, datos medidos y una sección por IA). Si el PDF no se guardó, se genera de nuevo a partir de lo guardado. En Swagger usa el botón **Download file** de la respuesta.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "PDF", content = @Content(mediaType = "application/pdf")),
        @ApiResponse(responseCode = "404", description = "No existe o no es tuyo"),
        @ApiResponse(responseCode = "409", description = "El análisis sigue en curso (PDF_NOT_AVAILABLE)")
      })
  @GetMapping("/downloadpdf/{id}")
  public ResponseEntity<byte[]> downloadPdf(
      @AuthenticationPrincipal User me,
      @Parameter(description = "Id del scraping", example = "6f1c2b7e-3a1d-4c55-9d52-0b6a1e8f7c10") @PathVariable UUID id) {
    ReportService.PdfFile file = scrapeService.pdf(me.getId(), id);
    audit.log(me.getId(), me.getUsername(), AuditAction.PDF_DOWNLOADED, id.toString());
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
        .body(file.content());
  }
}
