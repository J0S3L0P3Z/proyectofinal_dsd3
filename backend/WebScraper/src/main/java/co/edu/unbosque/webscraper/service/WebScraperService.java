package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.dto.RawPageDTO;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.exception.UrlNotScrapableException;
import co.edu.unbosque.webscraper.util.UrlValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de scraping con Selenium y Chrome sin interfaz. Lee solo la URL pedida (no recorre el
 * sitio), ya renderizada con JavaScript, y devuelve el HTML, el código HTTP, la URL final, las
 * cabeceras y algunas variables globales del navegador para detectar el framework.
 *
 * <p>Cada tarea abre su propio navegador y lo cierra siempre. Cada petición de red del navegador (y
 * por tanto cada redirección) se revalida contra la política anti-SSRF. Cuando la página no se
 * puede leer (no existe, bloquea, certificado inválido, sin respuesta) se lanza una
 * {@link UrlNotScrapableException} con el motivo; nunca se intenta evadir una protección.
 */
@Service
public class WebScraperService {

  private static final Logger log = LoggerFactory.getLogger(WebScraperService.class);

  /** Tamaño máximo del HTML que se conserva. */
  private static final int MAX_HTML_CHARS = 10_000_000;

  /** Script que lee marcas de frameworks en el navegador. */
  private static final String GLOBALS_JS =
      """
      return (() => {
        const w = window, d = document;
        const some = (sel, fn) => Array.from(d.querySelectorAll(sel)).slice(0, 60).some(fn);
        const attr = (sel, a) => { const el = d.querySelector(sel); return el ? el.getAttribute(a) : null; };
        return {
          nextData: !!w.__NEXT_DATA__,
          nuxt: !!w.__NUXT__ || !!w.$nuxt,
          gatsby: !!w.___gatsby,
          remix: !!w.__remixContext,
          sveltekit: !!d.querySelector('[data-sveltekit-preload-data]'),
          angularVersion: attr('[ng-version]', 'ng-version'),
          angularJs: !!(w.angular && w.angular.version),
          react: !!w.React || !!d.querySelector('[data-reactroot]') ||
                 some('div,main,body', e => Object.keys(e).some(k => k.startsWith('__reactContainer') || k.startsWith('_reactRootContainer'))),
          vue: !!w.__VUE__ || !!d.querySelector('[data-v-app]') || some('div', e => !!(e.__vue__ || e.__vue_app__)),
          jquery: (w.jQuery && w.jQuery.fn && w.jQuery.fn.jquery) ? String(w.jQuery.fn.jquery) : null,
          shopify: !!w.Shopify
        };
      })();
      """;

  private final AppProperties props;
  private final UrlValidator urlValidator;
  private final BrowserLauncher launcher;
  private final ObjectMapper mapper;

  /**
   * Constructor con las dependencias del servicio.
   *
   * @param props Propiedades de la aplicación
   * @param urlValidator Validador de URLs (anti-SSRF)
   * @param launcher Fábrica de navegadores
   * @param mapper Conversor JSON (para leer los eventos de red)
   */
  public WebScraperService(
      AppProperties props, UrlValidator urlValidator, BrowserLauncher launcher, ObjectMapper mapper) {
    this.props = props;
    this.urlValidator = urlValidator;
    this.launcher = launcher;
    this.mapper = mapper;
  }

  /**
   * Abre la URL en Chrome, espera a que la página se estabilice y devuelve su contenido.
   *
   * @param uri URL ya validada
   * @return Datos crudos de la página
   * @throws UrlNotScrapableException Si la página no se puede leer, con el motivo
   * @throws AppException URL_NOT_ALLOWED si una redirección apunta a una red interna, o
   *     INTERNAL_ERROR si no se pudo iniciar el navegador
   */
  public RawPageDTO scrape(URI uri) {
    ChromeDriver driver = startBrowser();
    NetworkInterceptor guard = null;
    try {
      AtomicBoolean blocked = new AtomicBoolean(false);
      guard = startGuard(driver, blocked);
      return doScrape(driver, uri, blocked);
    } finally {
      closeQuietly(guard);
      quitQuietly(driver);
    }
  }

  private RawPageDTO doScrape(ChromeDriver driver, URI uri, AtomicBoolean blocked) {
    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(props.scraping().timeoutSeconds()));
    driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(15));

    long start = System.nanoTime();
    try {
      driver.get(uri.toString());
    } catch (TimeoutException e) {
      throw new UrlNotScrapableException(
          ErrorCode.TIMEOUT, "la navegación superó " + props.scraping().timeoutSeconds() + " s", e);
    } catch (WebDriverException e) {
      throw translate(e, blocked.get());
    }

    waitForSettle(driver, props.scraping().settleSeconds());

    String finalUrl = driver.getCurrentUrl();
    if (finalUrl == null || finalUrl.startsWith("chrome-error:")) {
      throw new UrlNotScrapableException(ErrorCode.NETWORK_ERROR, "el navegador mostró una página de error");
    }
    assertFinalUrlIsPublic(finalUrl);

    String html = driver.getPageSource();
    if (html == null) html = "";
    if (html.length() > MAX_HTML_CHARS) html = html.substring(0, MAX_HTML_CHARS);
    long loadMs = (System.nanoTime() - start) / 1_000_000;

    MainResponse main = readMainResponse(driver, finalUrl);
    return new RawPageDTO(html, main.status(), finalUrl, loadMs, main.headers(), readGlobals(driver));
  }

  private record MainResponse(int status, Map<String, String> headers) {}

  private void assertFinalUrlIsPublic(String finalUrl) {
    try {
      URI finalUri = new URI(finalUrl);
      String scheme = finalUri.getScheme();
      if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
        urlValidator.assertPublicHost(finalUri, false);
      }
    } catch (java.net.URISyntaxException e) {
      throw new UrlNotScrapableException(ErrorCode.NETWORK_ERROR, "URL final inválida: " + finalUrl, e);
    }
  }

  private void waitForSettle(ChromeDriver driver, int settleSeconds) {
    try {
      new WebDriverWait(driver, Duration.ofSeconds(Math.max(settleSeconds, 1)))
          .until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    } catch (WebDriverException e) {
      log.debug("La página no llegó a 'complete' en {} s; se continúa con el DOM actual", settleSeconds);
    }
    if (settleSeconds <= 0) return;
    long deadline = System.currentTimeMillis() + settleSeconds * 1000L;
    int last = -1;
    int stable = 0;
    try {
      while (System.currentTimeMillis() < deadline && stable < 2) {
        Object len = ((JavascriptExecutor) driver).executeScript("return document.body ? document.body.innerText.length : 0");
        int current = len instanceof Number n ? n.intValue() : 0;
        if (current == last) {
          stable++;
        } else {
          stable = 0;
          last = current;
        }
        Thread.sleep(500);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (WebDriverException e) {
      log.debug("No se pudo medir la estabilidad de la página: {}", e.getMessage());
    }
  }

  private MainResponse readMainResponse(ChromeDriver driver, String finalUrl) {
    int fallbackStatus = 0;
    Map<String, String> fallbackHeaders = Map.of();
    int matchedStatus = -1;
    Map<String, String> matchedHeaders = Map.of();
    String target = stripFragment(finalUrl);
    try {
      List<LogEntry> entries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
      for (LogEntry entry : entries) {
        JsonNode message = mapper.readTree(entry.getMessage()).path("message");
        if (!"Network.responseReceived".equals(message.path("method").asText())) continue;
        JsonNode params = message.path("params");
        if (!"Document".equals(params.path("type").asText())) continue;
        JsonNode response = params.path("response");
        int status = response.path("status").asInt(0);
        Map<String, String> headers = new HashMap<>();
        response.path("headers").fields().forEachRemaining(
            e -> headers.put(e.getKey().toLowerCase(Locale.ROOT), e.getValue().asText()));
        fallbackStatus = status;
        fallbackHeaders = headers;
        if (stripFragment(response.path("url").asText()).equals(target)) {
          matchedStatus = status;
          matchedHeaders = headers;
        }
      }
    } catch (Exception e) {
      log.debug("No se pudieron leer los eventos de red: {}", e.getMessage());
    }
    return matchedStatus >= 0 ? new MainResponse(matchedStatus, matchedHeaders) : new MainResponse(fallbackStatus, fallbackHeaders);
  }

  private static String stripFragment(String url) {
    int i = url.indexOf('#');
    return i < 0 ? url : url.substring(0, i);
  }

  private Map<String, Object> readGlobals(ChromeDriver driver) {
    Map<String, Object> out = new HashMap<>();
    try {
      Object result = ((JavascriptExecutor) driver).executeScript(GLOBALS_JS);
      if (result instanceof Map<?, ?> m) {
        m.forEach((k, v) -> out.put(String.valueOf(k), v));
      }
    } catch (WebDriverException e) {
      log.debug("No se pudieron leer las variables globales: {}", e.getMessage());
    }
    return out;
  }

  private ChromeDriver startBrowser() {
    try {
      return launcher.launch(props.scraping().blockResources(), true, true);
    } catch (WebDriverException e) {
      throw new AppException(
          ErrorCode.INTERNAL_ERROR,
          "No se pudo iniciar Chrome (¿está instalado y accesible?): " + firstLine(e.getMessage()),
          e);
    }
  }

  private NetworkInterceptor startGuard(ChromeDriver driver, AtomicBoolean blocked) {
    if (!props.scraping().networkGuard()) {
      return null;
    }
    Map<String, Boolean> cache = new ConcurrentHashMap<>();
    try {
      return new NetworkInterceptor(
          driver,
          Route.matching(
                  req -> {
                    boolean allowed = hostAllowed(req.getUri(), cache);
                    if (!allowed) {
                      blocked.set(true);
                      log.warn("Petición bloqueada por política anti-SSRF: {}", req.getUri());
                    }
                    return !allowed;
                  })
              .to(() -> req -> new HttpResponse().setStatus(403)));
    } catch (RuntimeException e) {
      throw new AppException(
          ErrorCode.INTERNAL_ERROR,
          "No se pudo activar la protección de red (DevTools). Con SCRAPE_NETWORK_GUARD=false se desactiva solo para desarrollo: "
              + firstLine(e.getMessage()),
          e);
    }
  }

  private boolean hostAllowed(String url, Map<String, Boolean> cache) {
    try {
      URI u = new URI(url);
      String scheme = u.getScheme();
      if (scheme == null || !(scheme.startsWith("http") || scheme.startsWith("ws"))) return true;
      String host = u.getHost();
      if (host == null) return false;
      return cache.computeIfAbsent(
          host.toLowerCase(Locale.ROOT),
          h -> {
            try {
              urlValidator.assertPublicHost(u, false);
              return true;
            } catch (AppException e) {
              return false;
            }
          });
    } catch (Exception e) {
      return false;
    }
  }

  private void closeQuietly(NetworkInterceptor guard) {
    if (guard == null) return;
    try {
      guard.close();
    } catch (Exception e) {
      log.debug("No se pudo cerrar el interceptor de red: {}", e.getMessage());
    }
  }

  private void quitQuietly(ChromeDriver driver) {
    try {
      driver.quit();
    } catch (Exception e) {
      log.debug("No se pudo cerrar Chrome: {}", e.getMessage());
    }
  }

  /**
   * Convierte los errores de navegación de Chrome en excepciones del catálogo.
   *
   * @param e Error original
   * @param ssrfBlocked true si se bloqueó una petición hacia una red interna
   * @return La excepción con el motivo para el usuario
   */
  static AppException translate(WebDriverException e, boolean ssrfBlocked) {
    if (ssrfBlocked) {
      return new AppException(ErrorCode.URL_NOT_ALLOWED, "redirección a destino no permitido", e);
    }
    String m = e.getMessage() == null ? "" : e.getMessage();
    if (m.contains("session not created")
        || m.contains("chrome not reachable")
        || m.contains("invalid session id")
        || m.contains("disconnected: not connected to DevTools")) {
      return new AppException(ErrorCode.INTERNAL_ERROR, firstLine(m), e);
    }
    if (m.contains("ERR_CERT_") || m.contains("ERR_SSL_") || m.contains("SSL_ERROR")) {
      return new UrlNotScrapableException(ErrorCode.SSL_ERROR, firstLine(m), e);
    }
    if (m.contains("timeout") || m.contains("Timed out")) {
      return new UrlNotScrapableException(ErrorCode.TIMEOUT, firstLine(m), e);
    }
    return new UrlNotScrapableException(ErrorCode.NETWORK_ERROR, firstLine(m), e);
  }

  private static String firstLine(String s) {
    if (s == null) return "";
    int i = s.indexOf('\n');
    String line = i < 0 ? s : s.substring(0, i);
    return line.length() > 300 ? line.substring(0, 300) : line;
  }
}
