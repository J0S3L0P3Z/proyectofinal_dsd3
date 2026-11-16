package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.ReportViewDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.model.ScrapeJob;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.repository.ScrapeJobRepository;
import co.edu.unbosque.webscraper.repository.UserRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Servicio de correos. Envía los correos de cuenta (verificación y recuperación de contraseña, en
 * segundo plano) y el correo automático al terminar cada scraping: éxito con el PDF adjunto, éxito
 * con advertencias o fallo con la causa. Un error de correo nunca tumba un job ni una petición.
 */
@Service
public class NotificationService {
  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final EmailSender sender;
  private final SpringTemplateEngine templates;
  private final ScrapeJobRepository jobs;
  private final UserRepository users;
  private final ReportService reports;
  private final AppProperties props;

  /**
   * Constructor con las dependencias del servicio.
   *
   * @param sender Implementación de envío de correo (SMTP)
   * @param templates Motor de plantillas Thymeleaf
   * @param jobs Repositorio de jobs
   * @param users Repositorio de usuarios
   * @param reports Servicio de reportes
   * @param props Propiedades de la aplicación
   */
  public NotificationService(
      EmailSender sender,
      SpringTemplateEngine templates,
      ScrapeJobRepository jobs,
      UserRepository users,
      ReportService reports,
      AppProperties props) {
    this.sender = sender;
    this.templates = templates;
    this.jobs = jobs;
    this.users = users;
    this.reports = reports;
    this.props = props;
  }

  /**
   * Envía el correo de verificación (en segundo plano).
   *
   * @param to Correo destino
   * @param username Nombre del usuario
   * @param link Enlace de verificación
   * @param validHours Horas de vigencia del enlace
   */
  @Async("mailExecutor")
  public void sendVerification(String to, String username, String link, int validHours) {
    Context ctx = new Context();
    ctx.setVariable("name", username);
    ctx.setVariable("link", link);
    ctx.setVariable("validFor", validHours + " horas");
    sendQuietly(to, "Verifica tu correo — Web Scraper IA", templates.process("email/verify-email", ctx));
  }

  /**
   * Envía el correo de recuperación de contraseña (en segundo plano).
   *
   * @param to Correo destino
   * @param username Nombre del usuario
   * @param link Enlace para crear la nueva contraseña
   * @param validMinutes Minutos de vigencia del enlace
   */
  @Async("mailExecutor")
  public void sendPasswordReset(String to, String username, String link, int validMinutes) {
    Context ctx = new Context();
    ctx.setVariable("name", username);
    ctx.setVariable("link", link);
    ctx.setVariable("validFor", validMinutes + " minutos");
    sendQuietly(to, "Recupera tu contraseña — Web Scraper IA", templates.process("email/reset-password", ctx));
  }

  private void sendQuietly(String to, String subject, String html) {
    try {
      sender.send(to, subject, html, null);
    } catch (AppException e) {
      log.warn("No se pudo enviar el correo de cuenta: {}", e.getDetail());
    }
  }

  /**
   * Envía el correo de resultado (éxito o éxito con advertencias) con el PDF adjunto.
   *
   * @param jobId Id del job
   * @param pdf Bytes del PDF
   * @param filename Nombre del archivo adjunto
   * @return true si el correo salió (o no había a quién enviarlo); false si falló
   */
  public boolean sendResult(UUID jobId, byte[] pdf, String filename) {
    try {
      ScrapeJob job = jobs.findById(jobId).orElseThrow();
      User user = users.findById(job.getUserId()).orElseThrow();
      if (user.getEmail() == null) {
        log.info("El usuario {} no tiene correo; no se envía el reporte", user.getUsername());
        return true;
      }
      ReportViewDTO view = reports.buildView(jobId);
      Context ctx = base(user, job);
      ctx.setVariable("r", view);
      String subject =
          (view.hasWarnings() ? "⚠️ Análisis listo con advertencias: " : "✅ Análisis listo: ") + job.getDomain();
      String html = templates.process("email/job-result", ctx);
      return sendWithRetries(user.getEmail(), subject, html, new EmailSender.Attachment(filename, pdf, "application/pdf"));
    } catch (RuntimeException e) {
      log.warn("No se pudo preparar el correo de resultado: {}", e.toString());
      return false;
    }
  }

  /**
   * Envía el correo de fallo con la causa en lenguaje claro.
   *
   * @param jobId Id del job fallido
   */
  public void sendFailure(UUID jobId) {
    try {
      ScrapeJob job = jobs.findById(jobId).orElseThrow();
      User user = users.findById(job.getUserId()).orElseThrow();
      if (user.getEmail() == null) return;

      ErrorCode code;
      try {
        code = ErrorCode.valueOf(job.getErrorCode());
      } catch (RuntimeException e) {
        code = ErrorCode.INTERNAL_ERROR;
      }
      Context ctx = base(user, job);
      ctx.setVariable("cause", job.getErrorMessage() != null ? job.getErrorMessage() : code.getMessage());
      ctx.setVariable("hint", code.getHint());
      ctx.setVariable("code", code.name());
      EmailSender.Attachment attachment =
          reports
              .tryRenderAndStore(job)
              .map(f -> new EmailSender.Attachment(f.filename(), f.content(), "application/pdf"))
              .orElse(null);
      ctx.setVariable("hasPdf", attachment != null);
      String html = templates.process("email/job-failure", ctx);
      sendWithRetries(user.getEmail(), "❌ No se pudo analizar: " + job.getDomain(), html, attachment);
    } catch (RuntimeException e) {
      log.warn("No se pudo preparar el correo de fallo: {}", e.toString());
    }
  }

  private Context base(User user, ScrapeJob job) {
    Context ctx = new Context();
    String front = props.frontendUrl();
    if (front.endsWith("/")) front = front.substring(0, front.length() - 1);
    ctx.setVariable("name", user.getUsername());
    ctx.setVariable("url", job.getRequestedUrl());
    ctx.setVariable("domain", job.getDomain());
    ctx.setVariable("historyUrl", front + props.frontendHistoryPath());
    return ctx;
  }

  private boolean sendWithRetries(String to, String subject, String html, EmailSender.Attachment attachment) {
    int attempts = 1 + props.mail().retries();
    for (int i = 1; i <= attempts; i++) {
      try {
        sender.send(to, subject, html, attachment);
        return true;
      } catch (AppException e) {
        log.warn("Envío de correo falló (intento {}/{}): {}", i, attempts, e.getDetail());
        if (i < attempts) {
          try {
            Thread.sleep(2000L * i);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
          }
        }
      }
    }
    return false;
  }
}
