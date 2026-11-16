package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.AiResultDTO;
import co.edu.unbosque.webscraper.dto.CleanContentDTO;
import co.edu.unbosque.webscraper.dto.PageFactsDTO;
import co.edu.unbosque.webscraper.dto.RawPageDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.exception.UrlNotScrapableException;
import co.edu.unbosque.webscraper.exception.JobAbortedException;
import co.edu.unbosque.webscraper.model.JobStatus;
import co.edu.unbosque.webscraper.model.ScrapeJob;
import co.edu.unbosque.webscraper.repository.ScrapeJobRepository;
import co.edu.unbosque.webscraper.util.ContentCleaner;
import co.edu.unbosque.webscraper.util.FactsExtractor;
import co.edu.unbosque.webscraper.util.PageStateClassifier;
import co.edu.unbosque.webscraper.util.UrlValidator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Orquestador (F3.1). Corre en segundo plano, fuera de la petición HTTP, con un máximo de jobs simultáneos
 * (el resto espera en PENDING). Pasos: scraping → limpieza → 3 IAs → PDF → guardado → correo.
 * Cualquier fallo deja el job en FAILED con un código del catálogo y dispara el correo de error.
 */
@Service
public class ScrapePipelineService {
    private static final Logger log = LoggerFactory.getLogger(ScrapePipelineService.class);

    private final ScrapeJobRepository jobs;
    private final JobStateService state;
    private final UrlValidator urlValidator;
    private final WebScraperService scraper;
    private final FactsExtractor extractor;
    private final PageStateClassifier classifier;
    private final ContentCleaner cleaner;
    private final AiAnalysisService ai;
    private final ReportService reports;
    private final NotificationService notifier;
    private final AppProperties props;

    public ScrapePipelineService(ScrapeJobRepository jobs, JobStateService state, UrlValidator urlValidator,
                          WebScraperService scraper, FactsExtractor extractor, PageStateClassifier classifier,
                          ContentCleaner cleaner, AiAnalysisService ai, ReportService reports,
                          NotificationService notifier, AppProperties props) {
        this.jobs = jobs;
        this.state = state;
        this.urlValidator = urlValidator;
        this.scraper = scraper;
        this.extractor = extractor;
        this.classifier = classifier;
        this.cleaner = cleaner;
        this.ai = ai;
        this.reports = reports;
        this.notifier = notifier;
        this.props = props;
    }

    @Async("scrapeExecutor")
    public void run(UUID jobId) {
        MDC.put("jobId", jobId.toString());
        try {
            ScrapeJob job = jobs.findById(jobId).orElse(null);
            if (job == null) return;
            try {
                execute(job);
            } catch (JobAbortedException e) {
                log.info("Pipeline detenido: {}", e.getMessage());
            } catch (UrlNotScrapableException e) {
                log.info("URL no scrapeable [{}]: {}", e.getCode(), e.getDetail());
                if (state.fail(jobId, e)) notifier.sendFailure(jobId);
            } catch (AppException e) {
                if (state.fail(jobId, e)) notifier.sendFailure(jobId);
            } catch (Exception e) {
                log.error("Error inesperado en el pipeline", e);
                if (state.fail(jobId, ErrorCode.INTERNAL_ERROR, e.toString())) notifier.sendFailure(jobId);
            }
        } finally {
            MDC.clear();
        }
    }

    private void execute(ScrapeJob job) {
        UUID id = job.getId();

        URI uri = urlValidator.parse(job.getRequestedUrl());
        urlValidator.assertPublicHost(uri, true);

        state.step(id, JobStatus.SCRAPING);
        RawPageDTO raw = scraper.scrape(uri);
        state.saveHttpInfo(id, raw.finalUrl(), raw.httpStatus(), raw.loadTimeMs());

        Document doc = Jsoup.parse(raw.html(), raw.finalUrl());
        PageFactsDTO facts = extractor.extract(job.getRequestedUrl(), raw, doc);
        PageStateClassifier.Result classification = classifier.classify(job.getRequestedUrl(), raw, doc);
        if (classification.error() != null) {
            throw new UrlNotScrapableException(classification.error(), "HTTP " + raw.httpStatus() + " → " + raw.finalUrl());
        }
        classification.warnings().forEach(w -> state.addWarning(id, w));

        CleanContentDTO content = cleaner.clean(doc, props.ai().contentFormat(), props.ai().maxInputTokens());
        if (content.truncated()) {
            state.addWarning(id, "Contenido recortado: la página superaba el tamaño que se envía a las IAs, "
                    + "así que el análisis cubre solo la primera parte.");
        }
        state.saveResult(id, facts, content);

        state.step(id, JobStatus.ANALYZING);
        List<AiResultDTO> results = ai.analyzeAll(facts, content);
        state.saveAnalyses(id, results);
        List<AiResultDTO> failed = results.stream().filter(r -> !r.ok()).toList();
        if (failed.size() == results.size()) {
            String summary = failed.stream().map(r -> r.providerId() + "=" + r.errorCode()).collect(Collectors.joining(", "));
            throw new AppException(ErrorCode.AI_ALL_FAILED, summary);
        }
        for (AiResultDTO r : failed) {
            state.addWarning(id, "La IA " + r.providerLabel() + " no pudo analizar la página: " + r.errorMessage());
        }
        String summary = ai.summarize(results);
        if (summary != null) state.saveSummary(id, summary);

        state.step(id, JobStatus.GENERATING_PDF);
        byte[] pdf;
        try {
            pdf = reports.renderPdf(id);
        } catch (AppException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.PDF_FAILED, e.toString(), e);
        }
        String filename = reports.filename(job);
        reports.store(id, filename, pdf);

        state.step(id, JobStatus.SENDING_EMAIL);
        if (!notifier.sendResult(id, pdf, filename)) {
            state.addWarning(id, "No se pudo enviar el correo con el reporte. El resultado sigue disponible en tu historial.");
        }
        state.complete(id);
    }
}
