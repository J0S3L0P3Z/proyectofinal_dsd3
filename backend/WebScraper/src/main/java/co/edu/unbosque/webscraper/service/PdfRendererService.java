package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.dto.ReportViewDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Convierte el reporte (plantilla HTML/CSS de Thymeleaf) en PDF usando Chrome sin interfaz y el
 * comando Page.printToPDF de DevTools, con número de página en el pie. La página del PDF se abre con
 * JavaScript desactivado y Thymeleaf escapa todo el texto, así que lo que dijo una IA o traía la
 * página scrapeada nunca se interpreta como HTML.
 */
@Component
public class PdfRendererService {

  private static final String FOOTER =
      "<div style=\"font-size:9px;color:#7a8499;width:100%;text-align:center;font-family:Arial,sans-serif;\">"
          + "Página <span class=\"pageNumber\"></span> de <span class=\"totalPages\"></span></div>";

  private final SpringTemplateEngine templates;
  private final BrowserLauncher launcher;

  /** Como máximo 2 PDFs a la vez, porque Chrome consume mucha memoria. */
  private final Semaphore permits = new Semaphore(2);

  /**
   * Constructor con las dependencias del servicio.
   *
   * @param templates Motor de plantillas Thymeleaf
   * @param launcher Fábrica de navegadores
   */
  public PdfRendererService(SpringTemplateEngine templates, BrowserLauncher launcher) {
    this.templates = templates;
    this.launcher = launcher;
  }

  /**
   * Genera el PDF del reporte.
   *
   * @param view Datos del reporte
   * @return Bytes del PDF
   * @throws AppException PDF_FAILED si no se pudo generar
   */
  public byte[] render(ReportViewDTO view) {
    Context ctx = new Context(Locale.forLanguageTag("es-CO"));
    ctx.setVariable("r", view);
    String html = templates.process("pdf/report", ctx);

    boolean acquired = false;
    Path file = null;
    ChromeDriver driver = null;
    try {
      permits.acquire();
      acquired = true;
      file = Files.createTempFile("reporte-", ".html");
      Files.writeString(file, html, StandardCharsets.UTF_8);

      driver = launcher.launch(false, false, false);
      driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
      driver.get(file.toUri().toString());

      Map<String, Object> params = new HashMap<>();
      params.put("printBackground", true);
      params.put("displayHeaderFooter", true);
      params.put("headerTemplate", "<span></span>");
      params.put("footerTemplate", FOOTER);
      params.put("paperWidth", 8.27);
      params.put("paperHeight", 11.69);
      params.put("marginTop", 0.63);
      params.put("marginBottom", 0.71);
      params.put("marginLeft", 0.55);
      params.put("marginRight", 0.55);
      params.put("preferCSSPageSize", false);

      Map<String, Object> result = driver.executeCdpCommand("Page.printToPDF", params);
      Object data = result.get("data");
      if (!(data instanceof String base64) || base64.isEmpty()) {
        throw new AppException(ErrorCode.PDF_FAILED, "Chrome no devolvió el PDF");
      }
      return Base64.getDecoder().decode(base64);
    } catch (WebDriverException | IOException e) {
      throw new AppException(ErrorCode.PDF_FAILED, e.getMessage(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AppException(ErrorCode.PDF_FAILED, "interrumpido", e);
    } finally {
      if (driver != null) {
        try {
          driver.quit();
        } catch (Exception ignored) {
          driver = null;
        }
      }
      if (file != null) {
        try {
          Files.deleteIfExists(file);
        } catch (IOException ignored) {
          file = null;
        }
      }
      if (acquired) permits.release();
    }
  }
}
