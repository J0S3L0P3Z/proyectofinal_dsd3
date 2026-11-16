package co.edu.unbosque.webscraper.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados de un scraping: PENDING, SCRAPING, ANALYZING, GENERATING_PDF, SENDING_EMAIL, COMPLETED,
 * COMPLETED_WITH_WARNINGS y FAILED.
 */
public enum JobStatus {
    PENDING, SCRAPING, ANALYZING, GENERATING_PDF, SENDING_EMAIL,
    COMPLETED, COMPLETED_WITH_WARNINGS, FAILED;

    public static final Set<JobStatus> RUNNING = EnumSet.of(SCRAPING, ANALYZING, GENERATING_PDF, SENDING_EMAIL);
    public static final Set<JobStatus> ACTIVE = EnumSet.of(PENDING, SCRAPING, ANALYZING, GENERATING_PDF, SENDING_EMAIL);

    public boolean isTerminal() {
        return this == COMPLETED || this == COMPLETED_WITH_WARNINGS || this == FAILED;
    }
}
