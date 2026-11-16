package co.edu.unbosque.webscraper.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/** Toda la configuración sale de variables de entorno (ver application.properties y .env.example). */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @NotBlank String frontendUrl,
        @NotBlank String frontendHistoryPath,
        @NotBlank String frontendVerifyEmailPath,
        @NotBlank String frontendResetPasswordPath,
        String publicUrl,
        @NotBlank String corsAllowedOrigins,
        @NotBlank String timezone,
        @Valid Auth auth,
        Seed seed,
        Mail mail,
        @Valid Scraping scraping,
        @Valid Ai ai) {

    public record Auth(boolean requireEmailVerification, @Min(1) int resetTokenMinutes, @Min(1) int verificationTokenHours) {}

    /** Usuarios que se crean al arrancar si no existen (para pruebas con Swagger). Todos opcionales. */
    public record Seed(String adminUsername, String adminEmail, String adminPassword,
                       String demoUsername, String demoEmail, String demoPassword) {}

    public record Mail(String from, boolean enabled, @Min(0) int retries) {}

    public record Scraping(
            @Min(5) int timeoutSeconds,
            @Min(0) int settleSeconds,
            @Min(1) int maxConcurrent,
            @Min(1) int queueCapacity,
            @Min(1) int globalTimeoutMinutes,
            boolean blockResources,
            boolean chromiumNoSandbox,
            boolean networkGuard,
            String chromeBinary,
            String chromedriverPath,
            @NotBlank String userAgent,
            @Min(1) int maxActivePerUser) {}

    public record Ai(
            @Min(500) int maxInputTokens,
            @NotBlank String contentFormat,
            @Min(0) int maxRetries,
            @Min(10) int timeoutSeconds,
            @Min(5) int httpTimeoutSeconds,
            boolean finalSummary,
            Map<String, Provider> providers) {

        public record Provider(String baseUrl, String apiKey, String model, String fallbackModel, @Min(1) int maxConcurrent) {}
    }
}
