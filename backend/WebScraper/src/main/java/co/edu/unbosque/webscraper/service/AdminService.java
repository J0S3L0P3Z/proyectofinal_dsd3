package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.dto.AdminStatsDTO;
import co.edu.unbosque.webscraper.dto.AdminStatsDTO.DayCount;
import co.edu.unbosque.webscraper.dto.AdminStatsDTO.NameCount;
import co.edu.unbosque.webscraper.dto.AdminStatsDTO.ProviderStat;
import co.edu.unbosque.webscraper.dto.AiProviderStatusDTO;
import co.edu.unbosque.webscraper.dto.AiTestResultDTO;
import co.edu.unbosque.webscraper.dto.AuditLogDTO;
import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.AuditLog;
import co.edu.unbosque.webscraper.model.JobStatus;
import co.edu.unbosque.webscraper.model.ScrapeJob;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.repository.AiAnalysisRepository;
import co.edu.unbosque.webscraper.repository.AuditLogRepository;
import co.edu.unbosque.webscraper.repository.ScrapeJobRepository;
import co.edu.unbosque.webscraper.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de administración: estadísticas, auditoría, reporte CSV y verificación de las 3 IAs. Los
 * endpoints que lo usan exigen el rol ADMIN.
 */
@Service
public class AdminService {

  private final UserRepository users;
  private final ScrapeJobRepository jobs;
  private final AiAnalysisRepository analyses;
  private final AuditLogRepository auditRepo;
  private final AuditService audit;
  private final AiAnalysisService aiService;

  /**
   * Constructor con las dependencias del servicio.
   *
   * @param users Repositorio de usuarios
   * @param jobs Repositorio de jobs
   * @param analyses Repositorio de análisis de IA
   * @param auditRepo Repositorio de auditoría
   * @param audit Servicio de auditoría
   * @param aiService Servicio de las 3 IAs
   */
  public AdminService(
      UserRepository users,
      ScrapeJobRepository jobs,
      AiAnalysisRepository analyses,
      AuditLogRepository auditRepo,
      AuditService audit,
      AiAnalysisService aiService) {
    this.users = users;
    this.jobs = jobs;
    this.analyses = analyses;
    this.auditRepo = auditRepo;
    this.audit = audit;
    this.aiService = aiService;
  }

  /**
   * Estadísticas generales del sistema.
   *
   * @return Estadísticas listas para el panel
   */
  @Transactional(readOnly = true)
  public AdminStatsDTO stats() {
    Instant now = Instant.now();

    Map<String, Long> byStatus = new LinkedHashMap<>();
    for (JobStatus s : JobStatus.values()) byStatus.put(s.name(), 0L);
    for (Object[] row : jobs.countByStatus()) byStatus.put(String.valueOf(row[0]), (Long) row[1]);
    long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
    long ok = byStatus.get("COMPLETED") + byStatus.get("COMPLETED_WITH_WARNINGS");
    long finished = ok + byStatus.get("FAILED");
    double rate = finished == 0 ? 0 : Math.round(ok * 1000.0 / finished) / 10.0;

    List<DayCount> perDay = new ArrayList<>();
    for (Object[] row : jobs.countPerDay(now.minus(30, ChronoUnit.DAYS))) {
      perDay.add(new DayCount(String.valueOf(row[0]), ((Number) row[1]).longValue()));
    }
    List<NameCount> domains = new ArrayList<>();
    for (Object[] row : jobs.topDomains(PageRequest.of(0, 10))) {
      domains.add(new NameCount(String.valueOf(row[0]), (Long) row[1]));
    }
    List<NameCount> errors = new ArrayList<>();
    for (Object[] row : jobs.countByErrorCode()) {
      errors.add(new NameCount(String.valueOf(row[0]), (Long) row[1]));
    }

    return new AdminStatsDTO(
        users.count(),
        users.countByEnabledTrue(),
        users.countByCreatedAtAfter(now.minus(7, ChronoUnit.DAYS)),
        total,
        jobs.countByCreatedAtAfter(now.minus(24, ChronoUnit.HOURS)),
        byStatus,
        rate,
        jobs.averageLoadTimeMs(),
        perDay,
        domains,
        errors,
        providerStats());
  }

  private List<ProviderStat> providerStats() {
    Map<String, long[]> counts = new LinkedHashMap<>();
    Map<String, Double> avg = new HashMap<>();
    for (Object[] row : analyses.statsByProvider()) {
      String provider = String.valueOf(row[0]);
      boolean okRow = "OK".equals(row[1]);
      long n = (Long) row[2];
      long[] c = counts.computeIfAbsent(provider, k -> new long[2]);
      c[okRow ? 0 : 1] += n;
      if (okRow && row[3] != null) avg.put(provider, ((Number) row[3]).doubleValue());
    }
    return counts.entrySet().stream()
        .map(e -> new ProviderStat(e.getKey(), e.getValue()[0], e.getValue()[1], avg.get(e.getKey())))
        .toList();
  }

  /**
   * Auditoría paginada, con filtros opcionales por usuario y por acción.
   *
   * @param userId Filtrar por id de usuario (opcional)
   * @param action Filtrar por acción (opcional)
   * @param pageable Paginación
   * @return Página de registros
   */
  @Transactional(readOnly = true)
  public Page<AuditLogDTO> audit(Long userId, AuditAction action, Pageable pageable) {
    Specification<AuditLog> spec =
        (root, query, cb) -> {
          List<Predicate> predicates = new ArrayList<>();
          if (userId != null) predicates.add(cb.equal(root.get("userId"), userId));
          if (action != null) predicates.add(cb.equal(root.get("action"), action));
          return cb.and(predicates.toArray(new Predicate[0]));
        };
    return auditRepo.findAll(spec, pageable).map(AuditLogDTO::from);
  }

  /**
   * Reporte CSV de los scrapings entre dos fechas (máximo 10 000 filas). Abre bien en Excel (UTF-8
   * con BOM).
   *
   * @param admin Administrador que lo exporta (queda en la auditoría)
   * @param from Inicio del rango
   * @param to Fin del rango
   * @return Bytes del CSV
   */
  @Transactional(readOnly = true)
  public byte[] jobsCsv(User admin, Instant from, Instant to) {
    List<ScrapeJob> list = jobs.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, PageRequest.of(0, 10_000));
    Set<Long> ids = list.stream().map(ScrapeJob::getUserId).collect(Collectors.toSet());
    Map<Long, String> names = users.findAllById(ids).stream().collect(Collectors.toMap(User::getId, User::getUsername));

    StringBuilder sb = new StringBuilder("\uFEFF");
    sb.append("id,usuario,url_pedida,url_final,dominio,estado,codigo_error,http,carga_ms,advertencias,creado,terminado\n");
    for (ScrapeJob j : list) {
      sb.append(
              String.join(
                  ",",
                  csv(j.getId().toString()),
                  csv(names.getOrDefault(j.getUserId(), "")),
                  csv(j.getRequestedUrl()),
                  csv(j.getFinalUrl()),
                  csv(j.getDomain()),
                  csv(j.getStatus().name()),
                  csv(j.getErrorCode()),
                  csv(j.getHttpStatus()),
                  csv(j.getLoadTimeMs()),
                  csv(j.getWarnings().size()),
                  csv(j.getCreatedAt()),
                  csv(j.getFinishedAt())))
          .append('\n');
    }
    audit.log(admin.getId(), admin.getUsername(), AuditAction.ADMIN_REPORT_EXPORTED, list.size() + " filas, " + from + " a " + to);
    return sb.toString().getBytes(StandardCharsets.UTF_8);
  }

  /** Escapa comillas y neutraliza fórmulas (=, +, -, @) para evitar inyección de CSV en Excel. */
  static String csv(Object value) {
    if (value == null) return "";
    String s = String.valueOf(value);
    if (!s.isEmpty() && "=+-@\t\r".indexOf(s.charAt(0)) >= 0) s = "'" + s;
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  /**
   * Estado de configuración de las 3 IAs.
   *
   * @return Una entrada por IA (sin claves)
   */
  public List<AiProviderStatusDTO> aiStatus() {
    return aiService.status();
  }

  /**
   * Llamada de prueba a cada IA.
   *
   * @return Un resultado por IA
   */
  public List<AiTestResultDTO> aiTest() {
    return aiService.test();
  }
}
