package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.dto.AiResultDTO;
import co.edu.unbosque.webscraper.dto.CleanContentDTO;
import co.edu.unbosque.webscraper.dto.PageFactsDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.exception.JobAbortedException;
import co.edu.unbosque.webscraper.model.AiAnalysis;
import co.edu.unbosque.webscraper.model.JobStatus;
import co.edu.unbosque.webscraper.model.ScrapeJob;
import co.edu.unbosque.webscraper.model.ScrapeResult;
import co.edu.unbosque.webscraper.repository.AiAnalysisRepository;
import co.edu.unbosque.webscraper.repository.ScrapeJobRepository;
import co.edu.unbosque.webscraper.repository.ScrapeResultRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Único lugar que cambia el estado de un job. Cada método es una transacción corta, así el avance
 * se ve desde la API mientras el pipeline sigue corriendo en segundo plano.
 */
@Service
public class JobStateService {
    private static final Logger log = LoggerFactory.getLogger(JobStateService.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ScrapeJobRepository jobs;
    private final ScrapeResultRepository results;
    private final AiAnalysisRepository analyses;
    private final ObjectMapper mapper;

    public JobStateService(ScrapeJobRepository jobs, ScrapeResultRepository results,
                           AiAnalysisRepository analyses, ObjectMapper mapper) {
        this.jobs = jobs;
        this.results = results;
        this.analyses = analyses;
        this.mapper = mapper;
    }

    @Transactional
    public ScrapeJob create(Long userId, String requestedUrl, String domain) {
        return jobs.save(new ScrapeJob(userId, requestedUrl, domain));
    }

    /** Pasa el job al siguiente paso. Si ya estaba terminado (p. ej. por el vigilante), lanza JobAbortedException. */
    @Transactional
    public void step(UUID id, JobStatus status) {
        ScrapeJob j = jobs.findById(id).orElseThrow(() -> new JobAbortedException("job inexistente"));
        if (j.getStatus().isTerminal()) throw new JobAbortedException("el job ya estaba en " + j.getStatus());
        j.setStatus(status);
        MDC.put("step", status.name());
        log.info("Paso: {}", status);
    }

    @Transactional
    public void saveHttpInfo(UUID id, String finalUrl, int httpStatus, long loadMs) {
        ScrapeJob j = jobs.findById(id).orElseThrow(() -> new JobAbortedException("job inexistente"));
        j.setFinalUrl(finalUrl);
        j.setHttpStatus(httpStatus);
        j.setLoadTimeMs(loadMs);
    }

    @Transactional
    public void saveResult(UUID id, PageFactsDTO facts, CleanContentDTO content) {
        results.save(new ScrapeResult(id, mapper.convertValue(facts, MAP_TYPE), content.format(),
                content.truncated(), content.originalChars(), content.cleanedChars()));
    }

    @Transactional
    public void saveAnalyses(UUID id, List<AiResultDTO> aiResults) {
        analyses.deleteByJobId(id);
        List<AiAnalysis> rows = new ArrayList<>();
        for (AiResultDTO r : aiResults) {
            rows.add(new AiAnalysis(id, r.providerId(), r.model(),
                    r.ok() ? AiAnalysis.OK : AiAnalysis.FAILED,
                    r.ok() ? mapper.convertValue(r.response(), MAP_TYPE) : null,
                    r.errorCode() == null ? null : r.errorCode().name(),
                    r.errorMessage(), r.durationMs()));
        }
        analyses.saveAll(rows);
    }

    /**
     * Guarda el resumen final hecho por IA.
     *
     * @param id Id del job
     * @param summary Resumen (se recorta a 4000 caracteres)
     */
    @Transactional
    public void saveSummary(UUID id, String summary) {
        String s = summary.length() > 4000 ? summary.substring(0, 4000) : summary;
        jobs.findById(id).ifPresent(j -> j.setAiSummary(s));
    }

    @Transactional
    public void addWarning(UUID id, String warning) {
        jobs.findById(id).ifPresent(j -> j.addWarning(warning));
    }

    /** COMPLETED si no hubo advertencias; COMPLETED_WITH_WARNINGS si las hubo. */
    @Transactional
    public void complete(UUID id) {
        ScrapeJob j = jobs.findById(id).orElseThrow(() -> new JobAbortedException("job inexistente"));
        if (j.getStatus().isTerminal()) return;
        j.setStatus(j.getWarnings().isEmpty() ? JobStatus.COMPLETED : JobStatus.COMPLETED_WITH_WARNINGS);
        j.setFinishedAt(Instant.now());
        log.info("Job terminado: {}", j.getStatus());
    }

    /** @return true si realmente pasó a FAILED (false si ya estaba terminado). El detalle técnico solo va al log. */
    @Transactional
    public boolean fail(UUID id, ErrorCode code, String detail) {
        return markFailed(id, code, code.getMessage(), detail);
    }

    /**
     * Marca el job como fallido con el mensaje para el usuario de la excepción (para una URL que no se
     * puede scrapear incluye "No es posible scrapear esta URL" y el motivo).
     *
     * @param id Id del job
     * @param e Excepción que causó el fallo
     * @return true si realmente pasó a FAILED (false si ya estaba terminado)
     */
    @Transactional
    public boolean fail(UUID id, AppException e) {
        return markFailed(id, e.getCode(), e.getUserMessage(), e.getDetail());
    }

    private boolean markFailed(UUID id, ErrorCode code, String userMessage, String detail) {
        ScrapeJob j = jobs.findById(id).orElse(null);
        if (j == null || j.getStatus().isTerminal()) return false;
        j.setStatus(JobStatus.FAILED);
        j.setErrorCode(code.name());
        j.setErrorMessage(userMessage);
        j.setFinishedAt(Instant.now());
        log.warn("Job FAILED [{}]: {}", code, detail);
        return true;
    }

    /** Cierra como fallidos los jobs que llevan demasiado tiempo en esos estados. Devuelve los que cerró. */
    @Transactional
    public List<UUID> failStale(Collection<JobStatus> statuses, Instant before, ErrorCode code, String detail) {
        List<UUID> failed = new ArrayList<>();
        for (ScrapeJob j : jobs.findByStatusInAndUpdatedAtBefore(statuses, before)) {
            j.setStatus(JobStatus.FAILED);
            j.setErrorCode(code.name());
            j.setErrorMessage(code.getMessage());
            j.setFinishedAt(Instant.now());
            failed.add(j.getId());
            log.warn("Job {} cerrado por el vigilante [{}]: {}", j.getId(), code, detail);
        }
        return failed;
    }

    /** Al arrancar: lo que quedó a medias por un reinicio no puede seguir corriendo. */
    @Transactional
    public List<UUID> failAllActive(ErrorCode code, String detail) {
        List<UUID> failed = new ArrayList<>();
        for (ScrapeJob j : jobs.findByStatusIn(JobStatus.ACTIVE)) {
            j.setStatus(JobStatus.FAILED);
            j.setErrorCode(code.name());
            j.setErrorMessage(code.getMessage());
            j.setFinishedAt(Instant.now());
            failed.add(j.getId());
        }
        if (!failed.isEmpty()) log.warn("{} job(s) quedaron a medias y se marcaron FAILED: {}", failed.size(), detail);
        return failed;
    }
}
