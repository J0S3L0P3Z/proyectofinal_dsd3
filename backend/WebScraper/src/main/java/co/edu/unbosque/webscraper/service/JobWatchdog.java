package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.model.JobStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Tiempo máximo global (F3.1): ningún job queda colgado para siempre. */
@Component
public class JobWatchdog {
    private static final Logger log = LoggerFactory.getLogger(JobWatchdog.class);
    private static final Duration MAX_QUEUE_WAIT = Duration.ofMinutes(45);

    private final JobStateService state;
    private final NotificationService notifier;
    private final AppProperties props;

    public JobWatchdog(JobStateService state, NotificationService notifier, AppProperties props) {
        this.state = state;
        this.notifier = notifier;
        this.props = props;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        List<UUID> failed = state.failAllActive(ErrorCode.INTERNAL_ERROR, "el servidor se reinició mientras corría el job");
        failed.forEach(notifier::sendFailure);
    }

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
    public void sweep() {
        Instant now = Instant.now();
        Instant runningLimit = now.minus(Duration.ofMinutes(props.scraping().globalTimeoutMinutes()));
        List<UUID> failed = state.failStale(JobStatus.RUNNING, runningLimit, ErrorCode.JOB_TIMEOUT,
                "superó " + props.scraping().globalTimeoutMinutes() + " min sin avanzar");
        failed.addAll(state.failStale(List.of(JobStatus.PENDING), now.minus(MAX_QUEUE_WAIT), ErrorCode.JOB_TIMEOUT,
                "esperó en la cola más de " + MAX_QUEUE_WAIT.toMinutes() + " min"));
        if (!failed.isEmpty()) log.warn("El vigilante cerró {} job(s)", failed.size());
        failed.forEach(notifier::sendFailure);
    }
}
