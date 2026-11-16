package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.springframework.stereotype.Component;

/**
 * Crea instancias de Chrome sin interfaz (headless) controladas con Selenium. Se usa tanto para
 * scrapear una página como para convertir el reporte HTML en PDF. Selenium Manager descarga solo el
 * chromedriver adecuado si no se configura una ruta propia.
 */
@Component
public class BrowserLauncher {

  /** Propiedades de la aplicación. */
  private final AppProperties props;

  /**
   * Constructor con las propiedades de la aplicación.
   *
   * @param props Propiedades de la aplicación
   */
  public BrowserLauncher(AppProperties props) {
    this.props = props;
  }

  /**
   * Lanza una instancia nueva de Chrome. Quien la pide es responsable de cerrarla con quit().
   *
   * @param blockImages true para no descargar imágenes (el conteo de etiquetas img no cambia)
   * @param javascriptEnabled false para desactivar JavaScript (se usa al generar el PDF)
   * @param performanceLogs true para registrar los eventos de red (código HTTP y cabeceras)
   * @return El navegador listo para usar
   */
  public ChromeDriver launch(boolean blockImages, boolean javascriptEnabled, boolean performanceLogs) {
    ChromeOptions options = new ChromeOptions();
    options.addArguments(
        "--headless=new",
        "--disable-gpu",
        "--disable-dev-shm-usage",
        "--window-size=1366,900",
        "--hide-scrollbars",
        "--mute-audio",
        "--disable-extensions",
        "--no-first-run",
        "--lang=es-CO",
        "--user-agent=" + props.scraping().userAgent());
    if (props.scraping().chromiumNoSandbox()) {
      options.addArguments("--no-sandbox");
    }
    String binary = props.scraping().chromeBinary();
    if (binary != null && !binary.isBlank()) {
      options.setBinary(binary.trim());
    }
    options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
    options.setAcceptInsecureCerts(false);

    Map<String, Object> prefs = new HashMap<>();
    if (blockImages) {
      prefs.put("profile.managed_default_content_settings.images", 2);
    }
    if (!javascriptEnabled) {
      prefs.put("profile.managed_default_content_settings.javascript", 2);
    }
    prefs.put("profile.default_content_setting_values.notifications", 2);
    options.setExperimentalOption("prefs", prefs);

    if (performanceLogs) {
      LoggingPreferences logging = new LoggingPreferences();
      logging.enable(LogType.PERFORMANCE, Level.ALL);
      options.setCapability("goog:loggingPrefs", logging);
      Map<String, Object> perf = new HashMap<>();
      perf.put("enableNetwork", true);
      perf.put("enablePage", false);
      options.setExperimentalOption("perfLoggingPrefs", perf);
    }

    String driverPath = props.scraping().chromedriverPath();
    if (driverPath != null && !driverPath.isBlank()) {
      ChromeDriverService service =
          new ChromeDriverService.Builder().usingDriverExecutable(new File(driverPath.trim())).build();
      return new ChromeDriver(service, options);
    }
    return new ChromeDriver(options);
  }
}
