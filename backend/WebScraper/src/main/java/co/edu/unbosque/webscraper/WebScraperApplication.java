package co.edu.unbosque.webscraper;

import co.edu.unbosque.webscraper.util.UrlValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de la aplicación Spring Boot del Web Scraper con análisis de 3 IAs. Sirve como
 * punto de entrada y configura los beans globales del contexto de la aplicación.
 *
 * @author Universidad El Bosque
 * @version 0.1
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@EnableScheduling
public class WebScraperApplication {

  /**
   * Método principal que inicia la aplicación Spring Boot.
   *
   * @param args Argumentos de línea de comandos pasados a la aplicación
   */
  public static void main(String[] args) {
    SpringApplication.run(WebScraperApplication.class, args);
  }

  /**
   * Crea el validador de URLs (formato y protección anti-SSRF). Es una clase sin dependencias de
   * Spring, por eso se registra aquí como bean, igual que otros utilitarios globales.
   *
   * @return Instancia del validador de URLs
   */
  @Bean
  public UrlValidator getUrlValidator() {
    return new UrlValidator();
  }
}
