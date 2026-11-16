package co.edu.unbosque.webscraper.controller;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.AdminStatsDTO;
import co.edu.unbosque.webscraper.dto.AiProviderStatusDTO;
import co.edu.unbosque.webscraper.dto.AiTestResultDTO;
import co.edu.unbosque.webscraper.dto.AuditLogDTO;
import co.edu.unbosque.webscraper.dto.UserDTO;
import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.service.AdminService;
import co.edu.unbosque.webscraper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de administración: listado y búsqueda de usuarios, estadísticas, auditoría,
 * reporte CSV y verificación de las 3 IAs. Todo /admin/** exige el rol ADMIN (regla en
 * SecurityConfig).
 */
@RestController
@RequestMapping("/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administración", description = "Estadísticas, auditoría, reporte CSV y verificación de las 3 IAs (solo ADMIN)")
@ApiResponses(
    value = {
      @ApiResponse(responseCode = "401", ref = "#/components/responses/UnauthorizedError"),
      @ApiResponse(responseCode = "403", ref = "#/components/responses/ForbiddenError")
    })
public class AdminController {

  private final AdminService adminService;
  private final UserService userService;
  private final ZoneId zone;

  /**
   * Constructor con las dependencias del controlador.
   *
   * @param adminService Servicio de administración
   * @param userService Servicio de usuarios
   * @param props Propiedades de la aplicación
   */
  public AdminController(AdminService adminService, UserService userService, AppProperties props) {
    this.adminService = adminService;
    this.userService = userService;
    this.zone = ZoneId.of(props.timezone());
  }

  /**
   * Usuarios con búsqueda y paginación.
   *
   * @param q Texto a buscar en usuario o correo (opcional)
   * @param page Número de página
   * @param size Tamaño de página
   * @return Página de usuarios con la cantidad de scrapings de cada uno
   */
  @Operation(summary = "Usuarios (búsqueda y paginación)", description = "Busca por parte del nombre de usuario o por el correo completo (el correo está cifrado en la base, así que no admite búsquedas parciales). Incluye si la cuenta está habilitada, si verificó el correo, el último acceso y cuántos scrapings ha hecho.")
  @GetMapping("/users")
  public Page<UserDTO> users(
      @Parameter(description = "Texto a buscar en usuario o correo", example = "ana") @RequestParam(required = false) String q,
      @Parameter(description = "Número de página (desde 0)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Tamaño de página (1 a 100)", example = "20") @RequestParam(defaultValue = "20") int size) {
    return userService.listForAdmin(q, PageRequest.of(Math.max(0, page), clamp(size), Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  /**
   * Estadísticas generales.
   *
   * @return Estadísticas para el panel
   */
  @Operation(summary = "Estadísticas generales", description = "Usuarios, scrapings por estado, tasa de éxito, scrapings por día (30 días), dominios más consultados, errores por código y rendimiento de cada IA.")
  @GetMapping("/stats")
  public AdminStatsDTO stats() {
    return adminService.stats();
  }

  /**
   * Auditoría paginada.
   *
   * @param userId Filtrar por id de usuario (opcional)
   * @param action Filtrar por acción (opcional)
   * @param page Número de página
   * @param size Tamaño de página
   * @return Página de registros de auditoría
   */
  @Operation(summary = "Auditoría de movimientos", description = "Registro automático de lo que hacen los usuarios (registro, login exitoso/fallido, scrapings, descargas de PDF, cambios de administrador...). Del más reciente al más antiguo.")
  @GetMapping("/audit")
  public Page<AuditLogDTO> audit(
      @Parameter(description = "Filtrar por id de usuario") @RequestParam(required = false) Long userId,
      @Parameter(description = "Filtrar por acción", example = "LOGIN_FAILED") @RequestParam(required = false) AuditAction action,
      @Parameter(description = "Número de página (desde 0)", example = "0") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Tamaño de página (1 a 100)", example = "50") @RequestParam(defaultValue = "50") int size) {
    return adminService.audit(userId, action, PageRequest.of(Math.max(0, page), clamp(size), Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  /**
   * Reporte CSV de scrapings.
   *
   * @param me Administrador autenticado
   * @param from Fecha inicial (aaaa-mm-dd), por defecto hace 30 días
   * @param to Fecha final (aaaa-mm-dd), por defecto hoy
   * @return Archivo CSV
   */
  @Operation(summary = "Reporte CSV de scrapings", description = "Descarga un CSV (abre bien en Excel) con los scrapings entre dos fechas. Por defecto, los últimos 30 días. Máximo 10 000 filas.")
  @ApiResponse(responseCode = "200", description = "Archivo CSV", content = @Content(mediaType = "text/csv"))
  @GetMapping("/jobsreport")
  public ResponseEntity<byte[]> jobsReport(
      @AuthenticationPrincipal User me,
      @Parameter(description = "Fecha inicial (aaaa-mm-dd)", example = "2026-11-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @Parameter(description = "Fecha final (aaaa-mm-dd)", example = "2026-11-30") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    LocalDate end = to != null ? to : LocalDate.now(zone);
    LocalDate start = from != null ? from : end.minusDays(30);
    Instant fromInstant = start.atStartOfDay(zone).toInstant();
    Instant toInstant = end.plusDays(1).atStartOfDay(zone).toInstant();
    byte[] csv = adminService.jobsCsv(me, fromInstant, toInstant);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("scrapings-" + start + "_" + end + ".csv").build().toString())
        .body(csv);
  }

  /**
   * Estado de configuración de las 3 IAs.
   *
   * @return Una entrada por IA (sin claves)
   */
  @Operation(
      summary = "Estado de las 3 IAs (¿tienen clave?)",
      description =
          """
            Muestra, para cada IA, el modelo, la URL base y si **ya tiene su clave configurada**. Nunca devuelve la clave.

            Si una IA aparece con `configured: false`, pega su clave en el archivo `.env` en la variable
            indicada en `keyVariable` (`AI_OLLAMA_API_KEY`, `AI_GEMINI_API_KEY` o `AI_OPENROUTER_API_KEY`) y
            **reinicia el back**.
        """)
  @ApiResponse(
      responseCode = "200",
      description = "Estado de cada IA",
      content = @Content(examples = @ExampleObject(value = "[{\"provider\": \"ollama\", \"label\": \"Ollama Cloud\", \"model\": \"gemma4:31b\", \"baseUrl\": \"https://ollama.com/v1\", \"configured\": false, \"keyVariable\": \"AI_OLLAMA_API_KEY\"}]")))
  @GetMapping("/ai/status")
  public List<AiProviderStatusDTO> aiStatus() {
    return adminService.aiStatus();
  }

  /**
   * Prueba real de las 3 IAs.
   *
   * @return Un resultado por IA
   */
  @Operation(
      summary = "Probar las 3 IAs",
      description =
          "Hace una llamada mínima (\"responde OK\") a cada IA para confirmar que la clave, la URL y el modelo funcionan. Gasta una petición mínima de cada cuota gratuita. Puede tardar hasta ~75 segundos si alguna IA está lenta.")
  @ApiResponse(responseCode = "200", description = "Resultado por IA (ok, duración y error si falló)")
  @PostMapping("/ai/test")
  public List<AiTestResultDTO> aiTest() {
    return adminService.aiTest();
  }

  private static int clamp(int size) {
    return Math.max(1, Math.min(size, 100));
  }
}
