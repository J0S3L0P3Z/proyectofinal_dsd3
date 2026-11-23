package co.edu.unbosque.proyectomodulotres.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

/**
 * Configuración de Spring Security: autenticación sin estado con JWT, rutas públicas y permisos por
 * rol.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	/** Filtro que valida el token JWT de cada petición. */
	private final JwtAuthenticationFilter jwtAuthFilter;

	/** Servicio que carga los usuarios desde la base de datos. */
	private final UserDetailsService userDetailsService;

	/**
	 * Constructor que recibe el filtro JWT y el servicio de usuarios.
	 *
	 * @param jwtAuthFilter Filtro JWT
	 * @param userDetailsService Servicio de usuarios
	 */
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Define las reglas de acceso: /auth y Swagger son públicos, /usuarios y /rol son solo para
	 * ADMIN, y el resto de módulos para USER y ADMIN.
	 *
	 * @param http Configuración de seguridad HTTP
	 * @return Cadena de filtros de seguridad
	 * @throws Exception Si ocurre un error al construir la configuración
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults()).authorizeHttpRequests(auth -> auth

				.requestMatchers("/auth/**").permitAll()

				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

				.requestMatchers("/usuarios/**").hasRole("ADMIN")

				.requestMatchers("/rol/**").hasRole("ADMIN")

				.requestMatchers("/scraping/**").hasAnyRole("USER", "ADMIN")

				.requestMatchers("/resultado/**").hasAnyRole("USER", "ADMIN")

				.requestMatchers("/reporte/**").hasAnyRole("USER", "ADMIN")

				.requestMatchers("/historial/**").hasAnyRole("USER", "ADMIN")

				.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Crea el proveedor de autenticación que valida usuario y contraseña contra la base de datos.
	 *
	 * @return Proveedor de autenticación
	 */
	@Bean
	public AuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);

		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	/**
	 * Expone el administrador de autenticación para usarlo en el login.
	 *
	 * @param config Configuración de autenticación
	 * @return Administrador de autenticación
	 * @throws Exception Si no se puede obtener
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	/**
	 * Crea el codificador de contraseñas BCrypt.
	 *
	 * @return Codificador de contraseñas
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}