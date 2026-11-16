package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.PageFactsDTO;
import co.edu.unbosque.webscraper.dto.ReportViewDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.model.AiAnalysis;
import co.edu.unbosque.webscraper.model.JobStatus;
import co.edu.unbosque.webscraper.model.ReportPdf;
import co.edu.unbosque.webscraper.model.ScrapeJob;
import co.edu.unbosque.webscraper.model.ScrapeResult;
import co.edu.unbosque.webscraper.repository.AiAnalysisRepository;
import co.edu.unbosque.webscraper.repository.ReportPdfRepository;
import co.edu.unbosque.webscraper.repository.ScrapeJobRepository;
import co.edu.unbosque.webscraper.repository.ScrapeResultRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Arma el modelo del reporte desde lo guardado en la base de datos y entrega/genera el PDF. */
@Service
public class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    public record PdfFile(String filename, byte[] content) {}

    private final ScrapeJobRepository jobs;
    private final ScrapeResultRepository results;
    private final AiAnalysisRepository analyses;
    private final ReportPdfRepository pdfs;
    private final PdfRendererService renderer;
    private final ObjectMapper mapper;
    private final ZoneId zone;
    private final Map<String, String> providerLabels = new HashMap<>();

    public ReportService(ScrapeJobRepository jobs, ScrapeResultRepository results, AiAnalysisRepository analyses,
                         ReportPdfRepository pdfs, PdfRendererService renderer, ObjectMapper mapper,
                         AiProviderRegistry registry, AppProperties props) {
        this.jobs = jobs;
        this.results = results;
        this.analyses = analyses;
        this.pdfs = pdfs;
        this.renderer = renderer;
        this.mapper = mapper;
        this.zone = ZoneId.of(props.timezone());
        for (AiProvider p : registry.providers()) providerLabels.put(p.id(), p.label());
    }

    public ReportViewDTO buildView(UUID jobId) {
        ScrapeJob job = jobs.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        ScrapeResult result = results.findById(jobId).orElse(null);
        boolean hasFacts = result != null && result.getFacts() != null;
        PageFactsDTO f = hasFacts
                ? mapper.convertValue(result.getFacts(), PageFactsDTO.class)
                : emptyFacts(job);

        List<ReportViewDTO.AiSection> sections = new ArrayList<>();
        for (AiAnalysis a : analyses.findByJobIdOrderByIdAsc(jobId)) sections.add(section(a));
        int ok = (int) sections.stream().filter(ReportViewDTO.AiSection::ok).count();

        boolean failed = job.getStatus() == JobStatus.FAILED;
        boolean hasWarnings = !job.getWarnings().isEmpty();
        String status = failed ? "Fallido" : (hasWarnings ? "Completado con advertencias" : "Completado");
        String date = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm", Locale.forLanguageTag("es-CO"))
                .withZone(zone).format(job.getCreatedAt());

        List<String> headings = f.headings() == null ? List.of() : f.headings().stream().limit(12).toList();
        return new ReportViewDTO(job.getRequestedUrl(), f.finalUrl(), job.getDomain(), date, status, hasWarnings,
                nz(f.title()), nz(f.language()), nz(f.metaDescription()), f.httpStatus(), f.loadTimeMs(),
                f.imageCount(), f.internalLinkCount(), f.externalLinkCount(), f.formCount(), f.scriptCount(),
                f.technologies() == null ? List.of() : f.technologies(), headings,
                List.copyOf(job.getWarnings()), sections, ok,
                failed, nz(job.getErrorCode()), nz(job.getErrorMessage()), hint(job.getErrorCode()),
                hasFacts, nz(job.getAiSummary()));
    }

    /**
     * Genera y guarda el PDF de un job sin lanzar excepciones (se usa para el reporte de los
     * scrapings fallidos, que también llevan su PDF con la URL y el motivo del fallo).
     *
     * @param job Job terminado
     * @return El PDF, o vacío si no se pudo generar
     */
    public Optional<PdfFile> tryRenderAndStore(ScrapeJob job) {
        try {
            byte[] bytes = renderPdf(job.getId());
            String name = filename(job);
            store(job.getId(), name, bytes);
            return Optional.of(new PdfFile(name, bytes));
        } catch (RuntimeException e) {
            log.warn("No se pudo generar el PDF del job {}: {}", job.getId(), e.toString());
            return Optional.empty();
        }
    }

    private static String hint(String errorCode) {
        if (errorCode == null) return "";
        try {
            return ErrorCode.valueOf(errorCode).getHint();
        } catch (IllegalArgumentException e) {
            return "";
        }
    }

    private PageFactsDTO emptyFacts(ScrapeJob job) {
        ObjectNode n = mapper.createObjectNode();
        n.put("requestedUrl", job.getRequestedUrl());
        n.put("finalUrl", job.getFinalUrl() == null ? job.getRequestedUrl() : job.getFinalUrl());
        n.put("httpStatus", job.getHttpStatus() == null ? 0 : job.getHttpStatus());
        n.put("loadTimeMs", job.getLoadTimeMs() == null ? 0 : job.getLoadTimeMs());
        return mapper.convertValue(n, PageFactsDTO.class);
    }

    public byte[] renderPdf(UUID jobId) {
        return renderer.render(buildView(jobId));
    }

    public void store(UUID jobId, String filename, byte[] bytes) {
        pdfs.save(new ReportPdf(jobId, filename, bytes));
    }

    public String filename(ScrapeJob job) {
        String domain = job.getDomain().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.-]", "_");
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(zone).format(job.getCreatedAt());
        return "reporte-" + domain + "-" + date + ".pdf";
    }

    /** Entrega el PDF guardado; si falta, lo regenera desde el JSON guardado (útil si falló la generación). */
    public PdfFile getOrGenerate(ScrapeJob job) {
        Optional<ReportPdf> stored = pdfs.findById(job.getId());
        if (stored.isPresent()) return new PdfFile(stored.get().getFilename(), stored.get().getContent());

        if (!job.getStatus().isTerminal()) throw new AppException(ErrorCode.PDF_NOT_AVAILABLE);

        byte[] bytes;
        try {
            bytes = renderPdf(job.getId());
        } catch (AppException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.PDF_FAILED, e.toString(), e);
        }
        String name = filename(job);
        store(job.getId(), name, bytes);
        return new PdfFile(name, bytes);
    }

    private ReportViewDTO.AiSection section(AiAnalysis a) {
        String label = providerLabels.getOrDefault(a.getProvider(), a.getProvider());
        if (!a.isOk() || a.getResponse() == null) {
            return new ReportViewDTO.AiSection(label, nz(a.getModel()), false,
                    nz(a.getErrorMessage()).isEmpty() ? "Esta IA no pudo completar el análisis." : a.getErrorMessage(),
                    "", List.of(), List.of(), "", "", "", List.of());
        }
        JsonNode r = mapper.valueToTree(a.getResponse());
        List<String> people = new ArrayList<>();
        for (JsonNode p : r.path("personasMencionadas")) {
            String name = p.path("nombre").asText("");
            String ctx = p.path("contexto").asText("");
            if (!name.isBlank()) people.add(ctx.isBlank() ? name : name + " — " + ctx);
        }
        return new ReportViewDTO.AiSection(label, nz(a.getModel()), true, null,
                r.path("deQueTrata").asText(""), strings(r.path("informacionPrincipal")), people,
                r.path("imagenes").path("comentario").asText(""),
                r.path("tecnologia").path("nombre").asText("no determinado"),
                r.path("tecnologia").path("evidencia").asText(""),
                strings(r.path("observaciones")));
    }

    private static List<String> strings(JsonNode arr) {
        List<String> out = new ArrayList<>();
        for (JsonNode n : arr) if (n.isTextual() && !n.asText().isBlank()) out.add(n.asText());
        return out;
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
