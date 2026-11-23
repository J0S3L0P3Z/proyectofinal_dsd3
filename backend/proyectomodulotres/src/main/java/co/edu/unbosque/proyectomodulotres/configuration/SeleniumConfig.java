package co.edu.unbosque.proyectomodulotres.configuration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Configuración de Selenium. Crea el navegador Microsoft Edge sin interfaz gráfica que usa el
 * servicio de scraping.
 */
@Configuration
public class SeleniumConfig {

	/**
	 * Crea un navegador Edge en modo headless. Es prototype, así que cada scraping recibe un navegador
	 * nuevo.
	 *
	 * @return Navegador listo para usar
	 */
	@Bean
	@Scope("prototype")
	public WebDriver webDriver() {

		EdgeOptions opciones = new EdgeOptions();

		opciones.addArguments("--headless");
		opciones.addArguments("--no-sandbox");
		opciones.addArguments("--disable-dev-shm-usage");

		return new EdgeDriver(opciones);
	}
}
