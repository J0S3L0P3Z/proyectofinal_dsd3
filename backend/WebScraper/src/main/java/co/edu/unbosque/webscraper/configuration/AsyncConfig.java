package co.edu.unbosque.webscraper.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configuración de los pools de hilos: uno para los scrapings (cantidad máxima simultánea y cola; si
 * la cola se llena la API responde 503), uno para las llamadas en paralelo a las 3 IAs y uno para
 * los correos de cuenta.
 */
@Configuration
public class AsyncConfig {

    /**
     * Pipeline de scraping. Chromium consume mucha RAM: el máximo de scrapings simultáneos es fijo
     * (SCRAPE_MAX_CONCURRENT) y el resto espera en la cola (jobs en PENDING). Si la cola se llena,
     * el executor rechaza la tarea y la API responde 503 QUEUE_FULL.
     */
    @Bean(name = "scrapeExecutor")
    public ThreadPoolTaskExecutor scrapeExecutor(AppProperties props) {
        var s = props.scraping();
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(s.maxConcurrent());
        ex.setMaxPoolSize(s.maxConcurrent());
        ex.setQueueCapacity(s.queueCapacity());
        ex.setThreadNamePrefix("scrape-");
        return ex;
    }

    /** Llamadas a las 3 IAs en paralelo. */
    @Bean(name = "aiExecutor")
    public ThreadPoolTaskExecutor aiExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(9);
        ex.setMaxPoolSize(9);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("ai-");
        return ex;
    }

    /** Correos de cuenta (verificación / recuperación) sin bloquear la petición. */
    @Bean(name = "mailExecutor")
    public ThreadPoolTaskExecutor mailExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(2);
        ex.setMaxPoolSize(2);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("mail-");
        return ex;
    }
}
