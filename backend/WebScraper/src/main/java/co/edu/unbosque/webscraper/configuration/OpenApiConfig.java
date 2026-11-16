package co.edu.unbosque.webscraper.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI (Swagger) para documentar y PROBAR la API REST antes de conectar el
 * frontend. Define la guía de uso, el esquema de seguridad JWT (botón "Authorize"), el orden de
 * los grupos de endpoints y respuestas de error reutilizables.
 *
 * <p>Swagger queda disponible en la raíz: http://localhost:8080/
 */
@Configuration
public class OpenApiConfig {

  /**
   * Define la información general de la API, el esquema de seguridad y los componentes
   * reutilizables de la documentación.
   *
   * @return Configuración personalizada de OpenAPI
   */
  @Bean
  public OpenAPI customOpenAPI(AppProperties props) {
    String mainDescription =
        "<h2>API del Web Scraper con análisis de 3 IAs</h2>"
            + "<p>Recibe la URL de <b>una página</b>, la scrapea con un navegador real, la analiza con"
            + " <b>tres IAs</b> en paralelo, genera un <b>PDF</b> y envía un <b>correo</b>. Esta"
            + " pantalla sirve para probar todo el back antes de conectar el frontend.</p>"
            + "<h3>Prueba rápida (5 pasos)</h3><ol>"
            + "<li>Inicia sesión con <code>POST /auth/login</code> (usuarios de prueba en el"
            + " archivo <code>.env</code>: <code>SEED_ADMIN_*</code> y <code>SEED_DEMO_*</code>)."
            + " También puedes crear una cuenta con <code>POST /auth/register</code>.</li>"
            + "<li>Copia el valor de <code>token</code> de la respuesta.</li>"
            + "<li>Pulsa el botón <b>Authorize</b> (arriba a la derecha), pega SOLO el token y"
            + " confirma. Swagger lo recuerda al recargar.</li>"
            + "<li>Crea un scraping con <code>POST /scrape/create</code> (hay ejemplos de URLs"
            + " buenas y malas). Responde 202 con un <code>id</code>.</li>"
            + "<li>Consulta el avance con <code>GET /scrape/getbyid/{id}</code> hasta que"
            + " <code>finished</code> sea true y descarga el PDF con"
            + " <code>GET /scrape/downloadpdf/{id}</code>. Los correos se ven en Mailpit"
            + " (<code>http://localhost:8025</code>).</li></ol>"
            + "<h3>Antes de probar con las IAs</h3>"
            + "<p>Pega las claves en el archivo <code>.env</code> (<code>AI_OLLAMA_API_KEY</code>,"
            + " <code>AI_GEMINI_API_KEY</code>, <code>AI_OPENROUTER_API_KEY</code>), reinicia el back"
            + " y usa como administrador <code>GET /admin/ai/status</code> y"
            + " <code>POST /admin/ai/test</code> para confirmar que responden.</p>"
            + "<h3>URLs que no se pueden scrapear</h3>"
            + "<p>Si la página no existe, bloquea la lectura automática (captcha, anti-bots), exige iniciar"
            + " sesión, no responde o tiene un certificado inválido, el scraping termina en <code>FAILED</code>"
            + " con el mensaje <i>No es posible scrapear esta URL. Motivo: ...</i> (el mismo mensaje llega por"
            + " correo). No se intenta saltar ninguna protección.</p>"
            + "<h3>Estados de un scraping</h3><p><code>PENDING → SCRAPING → ANALYZING →"
            + " GENERATING_PDF → SENDING_EMAIL → COMPLETED | COMPLETED_WITH_WARNINGS | FAILED</code></p>"
            + "<h3>Roles</h3><ul><li><strong>USER</strong>: usa el scraper y ve solo su historial.</li>"
            + "<li><strong>ADMIN</strong>: además gestiona usuarios y ve estadísticas, auditoría y"
            + " reportes.</li></ul>"
            + "<h3>Códigos HTTP comunes</h3><ul><li><strong>200/201/202</strong>: éxito</li>"
            + "<li><strong>400</strong>: datos o URL inválidos</li>"
            + "<li><strong>401</strong>: sin token, token inválido o vencido</li>"
            + "<li><strong>403</strong>: sin permisos (rol insuficiente)</li>"
            + "<li><strong>404</strong>: no existe (o no es tuyo)</li>"
            + "<li><strong>409</strong>: conflicto (usuario o correo ya existente)</li>"
            + "<li><strong>429/503</strong>: demasiados análisis en curso / sistema ocupado</li></ul>"
            + "<p>Los errores de <code>/scrape</code> y <code>/admin</code> vienen como JSON"
            + " <code>{status, code, message}</code>; los de <code>/auth</code> y <code>/user</code>"
            + " como texto, igual que el proyecto de referencia.</p>";

    String securityDescription =
        "Autenticación mediante JWT (JSON Web Token)."
            + "<p>Para autenticarte, sigue estos pasos:</p>"
            + "<ol>"
            + "    <li>Obtén un token JWT usando el endpoint <code>POST /auth/login</code></li>"
            + "    <li>Copia el valor de <code>token</code> de la respuesta</li>"
            + "    <li>Haz clic en el botón \"Authorize\" en la parte superior de esta página</li>"
            + "    <li>Pega el token (Swagger agrega solo la palabra Bearer)</li>"
            + "    <li>Haz clic en \"Authorize\" y luego en \"Close\"</li>"
            + "</ol>"
            + "<p>Ahora podrás acceder a los endpoints protegidos.</p>";

    Info info =
        new Info()
            .title("API del Web Scraper con análisis de 3 IAs")
            .version("1.0")
            .description(mainDescription)
            .contact(
                new Contact()
                    .name("Equipo de Desarrollo - Universidad El Bosque")
                    .email("soporte@ejemplo.com"))
            .license(new License().name("Uso académico"));

    SecurityScheme securityScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description(securityDescription);

    OpenAPI api =
        new OpenAPI()
        .info(info)
        .tags(
            List.of(
                new Tag().name("Autenticación").description("Registro, login, verificación de correo y recuperación de contraseña (públicos)"),
                new Tag().name("Scraping").description("Crear un scraping, ver el historial, el detalle con las 3 IAs y descargar el PDF"),
                new Tag().name("Gestión de Usuarios").description("CRUD de usuarios (solo ADMIN) y datos de mi propia cuenta"),
                new Tag().name("Administración").description("Estadísticas, auditoría, reporte CSV y verificación de las 3 IAs (solo ADMIN)"),
                new Tag().name("Sistema").description("Estado del servicio")))
        .components(
            new Components()
                .addSecuritySchemes("bearerAuth", securityScheme)
                .addResponses(
                    "UnauthorizedError",
                    new ApiResponse()
                        .description("No autenticado - Token JWT inválido o expirado")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .addExamples(
                                            "error",
                                            new Example()
                                                .value(
                                                    "{\"status\": 401, \"code\": \"UNAUTHORIZED\","
                                                        + " \"message\": \"Debes iniciar sesión para"
                                                        + " continuar.\"}")))))
                .addResponses(
                    "ForbiddenError",
                    new ApiResponse()
                        .description("Acceso prohibido - No tienes permisos suficientes")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .addExamples(
                                            "error",
                                            new Example()
                                                .value(
                                                    "{\"status\": 403, \"code\": \"FORBIDDEN\","
                                                        + " \"message\": \"No tienes permiso para"
                                                        + " hacer esto.\"}")))))
                .addResponses(
                    "NotFoundError",
                    new ApiResponse()
                        .description("Recurso no encontrado")
                        .content(
                            new Content()
                                .addMediaType(
                                    "application/json",
                                    new MediaType()
                                        .addExamples(
                                            "error",
                                            new Example()
                                                .value(
                                                    "{\"status\": 404, \"code\": \"NOT_FOUND\","
                                                        + " \"message\": \"No se encontró lo que"
                                                        + " buscas.\"}"))))));
    if (props.publicUrl() != null && !props.publicUrl().isBlank()) {
      api.servers(List.of(new Server().url(props.publicUrl().trim()).description("Servidor de la API")));
    }
    return api;
  }
}
