package co.edu.unbosque.proyectomodulotres.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación Swagger (OpenAPI). Agrega el esquema de autenticación con token
 * JWT para poder probar los endpoints protegidos desde Swagger.
 */
@Configuration
public class OpenApiConfig {

	/**
	 * Crea la configuración de OpenAPI con el título, la descripción y el esquema Bearer.
	 *
	 * @return Configuración de OpenAPI
	 */
	@Bean
	public OpenAPI customOpenAPI() {

		return new OpenAPI()
				.info(new Info().title("PROYECTOMODULOTRES").version("1.0")
						.description("API para scraping y analisis de paginas web mediante inteligencia artificial."))

				.components(new Components().addSecuritySchemes("bearerAuth",
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
								.description("Ingresa el token JWT para autenticarte.")))

				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
	}
}
