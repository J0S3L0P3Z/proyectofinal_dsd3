package co.edu.unbosque.proyectomodulotres.configuration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SeleniumConfig {

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