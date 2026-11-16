package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.AiAnalysisDTO;
import co.edu.unbosque.webscraper.dto.ScrapeCreatedDTO;
import co.edu.unbosque.webscraper.dto.ScrapeDetailDTO;
import co.edu.unbosque.webscraper.dto.ScrapeJobDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.JobStatus;
import co.edu.unbosque.webscraper.model.ScrapeJob;
import co.edu.unbosque.webscraper.model.ScrapeResult;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.repository.AiAnalysisRepository;
import co.edu.unbosque.webscraper.repository.ScrapeJobRepository;
import co.edu.unbosque.webscraper.repository.ScrapeResultRepository;
import co.edu.unbosque.webscraper.util.UrlValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Servicio de scraping para los usuarios: crea los jobs (el trabajo corre en segundo plano) y
 * consulta el historial, el detalle y el PDF. Todo se filtra por usuario: ver el job de otra persona
 * responde 404, igual que si no existiera.
 */
@Service
public class ScrapeService {

  private final UrlValidator urlValidator;
  private final JobStateService state;
  private final ScrapeJobRepository jobs;
  private final ScrapeResultRepository results;
  private final AiAnalysisRepository analyses;
  private final ScrapePipelineService pipeline;
  private final ReportService reports;
  private final AiProviderRegistry registry;
  private final AuditService audit;
  private final AppProperties props;
  private final ObjectMapper mapper;

  /**
   * Constructor con las dependencias del servicio.
   *
   * @param urlValidator Validador de URLs (anti-SSRF)
   * @param state Servicio que cambia el estado de los jobs
   * @param jobs Repositorio de jobs
   * @param results Repositorio de resultados
   * @param analyses Repositorio de análisis de IA
   * @param pipeline Orquestador que corre en segundo plano
   * @param reports Servicio de reportes y PDF
   * @param registry Registro de las 3 IAs
   * @param audit Servicio de auditoría
   * @param props Propiedades de la aplicación
   * @param mapper Conversor JSON
   */
  public ScrapeService(
      UrlValidator urlValidator,
      JobStateService state,
      ScrapeJobRepository jobs,
      ScrapeResultRepository results,
      AiAnalysisRepository analyses,
      ScrapePipelineService pipeline,
      ReportService reports,
      AiProviderRegistry registry,
      AuditService audit,
      AppProperties props,
      ObjectMapper mapper) {
    this.urlValidator = urlValidator;
    this.state = state;
    this.jobs = jobs;
    this.results = results;
    this.analyses = analyses;
    this.pipeline = pipeline;
    this.reports = reports;
    this.registry = registry;
    this.audit = audit;
    this.props = props;
    this.mapper = mapper;
  }

  /**
   * Crea el job en PENDING y responde de inmediato; el trabajo corre en segundo plano. URLs
   * inválidas o internas (anti-SSRF) se rechazan aquí con 400 y el código del catálogo.
   *
   * @param user Usuario autenticado
   * @param rawUrl URL escrita por el usuario
   * @return Id y estado inicial del job
   * @throws AppException Si la URL no es válida o permitida, o hay demasiados análisis en curso
   */
  public ScrapeCreatedDTO create(User user, String rawUrl) {
    URI uri;
    try {
      uri = urlValidator.validate(rawUrl);
    } catch (AppException e) {
      audit.log(user.getId(), user.getUsername(), AuditAction.SCRAPE_REJECTED, e.getCode() + ": " + rawUrl);
      throw e;
    }
    if (jobs.countByUserIdAndStatusIn(user.getId(), JobStatus.ACTIVE) >= props.scraping().maxActivePerUser()) {
      throw new AppException(ErrorCode.TOO_MANY_ACTIVE_JOBS);
    }

    ScrapeJob job = state.create(user.getId(), uri.toString(), uri.getHost().toLowerCase());
    audit.log(user.getId(), user.getUsername(), AuditAction.SCRAPE_REQUESTED, uri.toString());
    try {
      pipeline.run(job.getId());
    } catch (TaskRejectedException e) {
      state.fail(job.getId(), ErrorCode.QUEUE_FULL, "cola de scraping llena");
      throw new AppException(ErrorCode.QUEUE_FULL, "cola llena");
    }
    return new ScrapeCreatedDTO(job.getId(), JobStatus.PENDING, uri.toString());
  }

  /**
   * Historial del usuario, paginado y del más reciente al más antiguo.
   *
   * @param userId Id del usuario
   * @param pageable Paginación
   * @return Página de resúmenes
   */
  public Page<ScrapeJobDTO> history(Long userId, Pageable pageable) {
    return jobs.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(ScrapeJobDTO::from);
  }

  /**
   * Detalle de un scraping: estado, advertencias, error y (si ya existen) los hechos y las
   * respuestas de las IAs.
   *
   * @param userId Id del usuario dueño
   * @param id Id del job
   * @return Detalle del job
   * @throws AppException NOT_FOUND si no existe o no es del usuario
   */
  public ScrapeDetailDTO detail(Long userId, UUID id) {
    ScrapeJob job = owned(userId, id);
    ScrapeResult result = results.findById(id).orElse(null);

    Map<String, String> labels = new HashMap<>();
    registry.providers().forEach(p -> labels.put(p.id(), p.label()));
    List<AiAnalysisDTO> views =
        analyses.findByJobIdOrderByIdAsc(id).stream()
            .map(
                a ->
                    new AiAnalysisDTO(
                        a.getProvider(),
                        labels.getOrDefault(a.getProvider(), a.getProvider()),
                        a.getModel(),
                        a.isOk(),
                        a.getResponse() == null ? null : mapper.valueToTree(a.getResponse()),
                        a.getErrorCode(),
                        a.getErrorMessage(),
                        a.getDurationMs()))
            .toList();
    JsonNode facts = result == null ? null : mapper.valueToTree(result.getFacts());
    boolean pdf = job.getStatus().isTerminal();
    return new ScrapeDetailDTO(
        ScrapeJobDTO.from(job), pdf, facts, result != null && result.isContentTruncated(), views, job.getAiSummary());
  }

  /**
   * Entrega el PDF de un scraping (si falta, se regenera desde lo guardado).
   *
   * @param userId Id del usuario dueño
   * @param id Id del job
   * @return Nombre y bytes del PDF
   * @throws AppException NOT_FOUND si no es del usuario; PDF_NOT_AVAILABLE si el análisis no terminó
   */
  public ReportService.PdfFile pdf(Long userId, UUID id) {
    return reports.getOrGenerate(owned(userId, id));
  }

  private ScrapeJob owned(Long userId, UUID id) {
    return jobs.findByIdAndUserId(id, userId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
  }
}
