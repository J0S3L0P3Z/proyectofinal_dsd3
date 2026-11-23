package co.edu.unbosque.proyectomodulotres.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que se ejecuta una vez por petición. Lee el token JWT del encabezado Authorization y, si es
 * válido, deja autenticado al usuario en el contexto de seguridad.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	/** Utilidad de tokens JWT. */
	private final JwtUtil jwtUtil;

	/** Servicio que carga los usuarios desde la base de datos. */
	private final UserDetailsService userDetailsService;

	/**
	 * Constructor que recibe la utilidad JWT y el servicio de usuarios.
	 *
	 * @param jwtUtil Utilidad de tokens JWT
	 * @param userDetailsService Servicio de usuarios
	 */
	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Indica que las rutas de /auth no pasan por el filtro.
	 *
	 * @param request Petición HTTP
	 * @return true si la ruta empieza por /auth/
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().startsWith("/auth/");
	}

	/**
	 * Valida el token Bearer y autentica al usuario si el token es válido.
	 *
	 * @param request Petición HTTP
	 * @param response Respuesta HTTP
	 * @param filterChain Cadena de filtros
	 * @throws ServletException Si ocurre un error del servlet
	 * @throws IOException Si ocurre un error de entrada o salida
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

			String jwt = authorizationHeader.substring(7);

			try {
				String username = jwtUtil.extractUsername(jwt);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

					UserDetails userDetails = userDetailsService.loadUserByUsername(username);

					if (jwtUtil.validateToken(jwt, userDetails)) {

						UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());

						authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						SecurityContextHolder.getContext().setAuthentication(authenticationToken);
					}
				}

			} catch (Exception e) {
				logger.error("No se pudo autenticar el usuario mediante JWT", e);
			}
		}

		filterChain.doFilter(request, response);
	}
}