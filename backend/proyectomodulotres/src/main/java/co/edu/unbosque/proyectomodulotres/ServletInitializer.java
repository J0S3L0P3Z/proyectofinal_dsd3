package co.edu.unbosque.proyectomodulotres;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Inicializador que permite desplegar la aplicación como WAR en un servidor Tomcat externo.
 */
public class ServletInitializer extends SpringBootServletInitializer {

	/**
	 * Indica la clase principal de la aplicación al desplegarla en Tomcat.
	 *
	 * @param application Constructor de la aplicación
	 * @return Constructor configurado
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ProyectomodulotresApplication.class);
	}
}
