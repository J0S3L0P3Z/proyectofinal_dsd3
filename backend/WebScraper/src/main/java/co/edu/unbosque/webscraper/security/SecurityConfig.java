package co.edu.unbosque.webscraper.security;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.exception.ApiError;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Clase de configuración de seguridad para la aplicación. Configura la autenticación y autorización
 * basada en JWT: contraseñas con BCrypt, sesiones sin estado, rutas públicas (login, registro y
 * Swagger), rutas de administrador, CORS para el frontend y respuestas 401/403 en JSON.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /** Filtro de autenticación JWT que procesa los tokens en las solicitudes. */
  private final JwtAuthenticationFilter jwtAuthFilter;

  /** Servicio que carga los detalles del usuario para la autenticación. */
  private final UserDetailsService userDetailsService;

  /**
   * Constructor que inicializa los componentes necesarios para la seguridad.
   *
   * @param jwtAuthFilter Filtro para procesar tokens JWT
   * @param userDetailsService Servicio para cargar detalles de usuarios
   */
  public SecurityConfig(
      JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Configura la cadena de filtros de seguridad HTTP. Define reglas de acceso, manejo de sesiones y
   * filtros de autenticación.
   *
   * @param http Configuración de seguridad HTTP
   * @param mapper Conversor JSON para las respuestas de error
   * @return Cadena de filtros de seguridad configurada
   * @throws Exception Si ocurre un error durante la configuración
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/**", "/health", "/error")
                    .permitAll()
                    .requestMatchers("/", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/user/me", "/user/logout")
                    .authenticated()
                    .requestMatchers("/user/**", "/admin/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(
                        (req, res, e) -> writeError(mapper, res, ErrorCode.UNAUTHORIZED))
                    .accessDeniedHandler(
                        (req, res, e) -> writeError(mapper, res, ErrorCode.FORBIDDEN)))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Configura el proveedor de autenticación. Establece el servicio de detalles de usuario y el
   * codificador de contraseñas.
   *
   * @return Proveedor de autenticación configurado
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Configura el gestor de autenticación.
   *
   * @param config Configuración de autenticación
   * @return Gestor de autenticación
   * @throws Exception Si ocurre un error durante la configuración
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configura el codificador de contraseñas. Utiliza BCrypt para el hash de contraseñas.
   *
   * @return Codificador de contraseñas BCrypt
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configura CORS para que solo el frontend permitido (CORS_ALLOWED_ORIGINS) pueda llamar a la
   * API desde el navegador.
   *
   * @param props Propiedades de la aplicación
   * @return Fuente de configuración CORS
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(AppProperties props) {
    CorsConfiguration cfg = new CorsConfiguration();
    List<String> origins =
        Arrays.stream(props.corsAllowedOrigins().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    cfg.setAllowedOrigins(origins);
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    cfg.setExposedHeaders(List.of("Content-Disposition"));
    cfg.setAllowCredentials(false);
    cfg.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

  /** Escribe un error del catálogo como JSON (401 sin token o con token inválido, 403 sin rol). */
  private static void writeError(ObjectMapper mapper, HttpServletResponse res, ErrorCode code)
      throws IOException {
    res.setStatus(code.getStatus());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.setCharacterEncoding("UTF-8");
    mapper.writeValue(res.getOutputStream(), ApiError.of(code));
  }
}
