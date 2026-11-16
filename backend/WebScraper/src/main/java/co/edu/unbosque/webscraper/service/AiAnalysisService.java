package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.AiResultDTO;
import co.edu.unbosque.webscraper.dto.AiProviderStatusDTO;
import co.edu.unbosque.webscraper.dto.AiTestResultDTO;
import co.edu.unbosque.webscraper.dto.ChatMessageDTO;
import co.edu.unbosque.webscraper.dto.CleanContentDTO;
import co.edu.unbosque.webscraper.dto.PageFactsDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.util.AiResponseParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Llama a las tres IAs en paralelo (F2.7), cada una con su tiempo máximo y sus reintentos de formato (F2.6).
 * Una IA que falla no tumba a las otras: su resultado queda marcado como fallido con la causa.
 */
@Service
public class AiAnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    private final List<AiProvider> providers;
    private final PromptBuilder promptBuilder;
    private final AiResponseParser parser;
    private final ThreadPoolTaskExecutor aiExecutor;
    private final AppProperties props;
    private final Map<String, Semaphore> permits = new HashMap<>();

    public AiAnalysisService(AiProviderRegistry registry, PromptBuilder promptBuilder, ObjectMapper mapper,
                             @Qualifier("aiExecutor") ThreadPoolTaskExecutor aiExecutor, AppProperties props) {
        this.providers = registry.providers();
        this.promptBuilder = promptBuilder;
        this.parser = new AiResponseParser(mapper);
        this.aiExecutor = aiExecutor;
        this.props = props;
        for (AiProvider p : providers) permits.put(p.id(), new Semaphore(p.maxConcurrent(), true));
    }

    public List<AiResultDTO> analyzeAll(PageFactsDTO facts, CleanContentDTO content) {
        List<ChatMessageDTO> base = promptBuilder.build(facts, content);
        long timeoutSeconds = props.ai().timeoutSeconds();

        List<CompletableFuture<AiResultDTO>> futures = new ArrayList<>();
        for (AiProvider p : providers) {
            CompletableFuture<AiResultDTO> f = CompletableFuture
                    .supplyAsync(() -> analyzeOne(p, base, facts.imageCount()), aiExecutor)
                    .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        boolean timeout = ex instanceof TimeoutException || ex.getCause() instanceof TimeoutException;
                        log.warn("{} falló fuera de la llamada: {}", p.id(), ex.toString());
                        return AiResultDTO.failure(p, timeout ? ErrorCode.AI_TIMEOUT : ErrorCode.AI_UNAVAILABLE,
                                ex.toString(), timeoutSeconds * 1000);
                    });
            futures.add(f);
        }
        return futures.stream().map(CompletableFuture::join).toList();
    }

    /** Nunca lanza excepciones: siempre devuelve un resultado (correcto o fallido). */
    AiResultDTO analyzeOne(AiProvider p, List<ChatMessageDTO> base, int imageCount) {
        long start = System.nanoTime();
        Semaphore permit = permits.get(p.id());
        boolean acquired = false;
        try {
            permit.acquire();
            acquired = true;

            List<ChatMessageDTO> messages = new ArrayList<>(base);
            AppException last = null;
            for (int attempt = 0; attempt <= props.ai().maxRetries(); attempt++) {
                String raw = p.complete(messages);
                try {
                    JsonNode parsed = parser.parseAndValidate(raw, imageCount);
                    return AiResultDTO.success(p, parsed, elapsedMs(start));
                } catch (AppException e) {
                    last = e;
                    log.info("{} intento {}: respuesta inválida ({})", p.id(), attempt + 1, e.getDetail());
                    messages.add(ChatMessageDTO.assistant(raw));
                    messages.add(ChatMessageDTO.user("Tu respuesta anterior no es válida: " + e.getDetail()
                            + ". Responde SOLO con el objeto JSON corregido, siguiendo el formato indicado."));
                }
            }
            throw last;
        } catch (AppException e) {
            log.warn("{} falló: {} ({})", p.id(), e.getCode(), e.getDetail());
            return AiResultDTO.failure(p, e.getCode(), e.getDetail(), elapsedMs(start));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AiResultDTO.failure(p, ErrorCode.AI_UNAVAILABLE, "interrumpido", elapsedMs(start));
        } catch (RuntimeException e) {
            log.error("{} falló con un error inesperado", p.id(), e);
            return AiResultDTO.failure(p, ErrorCode.AI_UNAVAILABLE, e.toString(), elapsedMs(start));
        } finally {
            if (acquired) permit.release();
        }
    }

    /**
     * Resumen final (opcional): le pide a una IA que sí respondió que combine los análisis válidos en
     * un solo párrafo. Si falla, simplemente no hay resumen; nunca hace fallar el job.
     *
     * @param results Resultados de las 3 IAs
     * @return El resumen, o null si está desactivado o ninguna IA pudo hacerlo
     */
    public String summarize(List<AiResultDTO> results) {
        if (!props.ai().finalSummary()) return null;
        List<AiResultDTO> ok = results.stream().filter(AiResultDTO::ok).toList();
        if (ok.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (AiResultDTO r : ok) {
            sb.append("ANÁLISIS DE ").append(r.providerLabel()).append(":\n").append(r.response()).append("\n\n");
        }
        List<ChatMessageDTO> messages = List.of(
                ChatMessageDTO.system("Eres un editor. Recibirás varios análisis (en JSON) de la MISMA página web hechos por "
                        + "distintas IAs. Escribe en español un resumen final de 4 a 6 frases: de qué trata la página, la "
                        + "información más importante, en qué coinciden y en qué difieren los análisis. No inventes nada que "
                        + "no esté en los análisis. Responde solo con el texto del resumen, sin títulos, sin JSON y sin Markdown."),
                ChatMessageDTO.user(sb.toString()));

        for (AiResultDTO r : ok) {
            AiProvider p = providers.stream().filter(x -> x.id().equals(r.providerId())).findFirst().orElse(null);
            if (p == null) continue;
            Semaphore permit = permits.get(p.id());
            boolean acquired = false;
            try {
                permit.acquire();
                acquired = true;
                String text = p.complete(messages).strip();
                if (!text.isBlank()) return text;
            } catch (AppException e) {
                log.info("{} no pudo hacer el resumen final: {}", p.id(), e.getDetail());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (RuntimeException e) {
                log.warn("{} falló haciendo el resumen final: {}", p.id(), e.toString());
            } finally {
                if (acquired) permit.release();
            }
        }
        return null;
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    /**
     * Estado de configuración de las 3 IAs (para revisar desde Swagger que las claves quedaron en
     * el archivo .env). Nunca devuelve la clave.
     *
     * @return Una entrada por IA
     */
    public List<AiProviderStatusDTO> status() {
        return providers.stream()
                .map(p -> new AiProviderStatusDTO(p.id(), p.label(), p.model(), p.fallbackModel(), p.baseUrl(),
                        p.isConfigured(), "AI_" + p.id().toUpperCase() + "_API_KEY"))
                .toList();
    }

    /**
     * Hace una llamada mínima a cada IA configurada ("responde OK") para confirmar que la clave, la
     * URL y el modelo funcionan. Las IAs sin clave se reportan como no configuradas.
     *
     * @return Un resultado por IA
     */
    public List<AiTestResultDTO> test() {
        long timeoutSeconds = props.ai().httpTimeoutSeconds() + 15L;
        List<CompletableFuture<AiTestResultDTO>> futures = new ArrayList<>();
        for (AiProvider p : providers) {
            futures.add(CompletableFuture
                    .supplyAsync(() -> testOne(p), aiExecutor)
                    .orTimeout(timeoutSeconds, TimeUnit.SECONDS)
                    .exceptionally(ex -> new AiTestResultDTO(p.id(), p.label(), p.model(), false, timeoutSeconds * 1000,
                            null, ErrorCode.AI_TIMEOUT.name(), ErrorCode.AI_TIMEOUT.getMessage())));
        }
        return futures.stream().map(CompletableFuture::join).toList();
    }

    private AiTestResultDTO testOne(AiProvider p) {
        long start = System.nanoTime();
        Semaphore permit = permits.get(p.id());
        boolean acquired = false;
        try {
            permit.acquire();
            acquired = true;
            String reply = p.complete(List.of(ChatMessageDTO.user("Responde únicamente con la palabra OK.")));
            String shortReply = reply.strip();
            if (shortReply.length() > 80) shortReply = shortReply.substring(0, 80);
            return new AiTestResultDTO(p.id(), p.label(), p.model(), true, elapsedMs(start), shortReply, null, null);
        } catch (AppException e) {
            return new AiTestResultDTO(p.id(), p.label(), p.model(), false, elapsedMs(start), null,
                    e.getCode().name(), e.getCode().getMessage() + (e.getCode() == ErrorCode.AI_NOT_CONFIGURED && e.getDetail() != null ? " " + e.getDetail() : ""));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new AiTestResultDTO(p.id(), p.label(), p.model(), false, elapsedMs(start), null,
                    ErrorCode.AI_UNAVAILABLE.name(), "Interrumpido");
        } finally {
            if (acquired) permit.release();
        }
    }
}
