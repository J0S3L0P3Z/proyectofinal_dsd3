package co.edu.unbosque.webscraper.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de autenticación JWT que intercepta las solicitudes HTTP. Valida los tokens JWT en las
 * solicitudes y establece la autenticación en el contexto de seguridad. Se ejecuta una vez por cada
 * solicitud. El usuario se vuelve a leer de la base de datos en cada petición, así que deshabilitar
 * a un usuario o cambiarle el rol surte efecto de inmediato.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  /** Utilidad para operaciones con tokens JWT. */
  private final JwtUtil jwtUtil;

  /** Servicio para cargar los detalles del usuario. */
  private final UserDetailsService userDetailsService;

  /**
   * Constructor que inicializa las dependencias necesarias para el filtro.
   *
   * @param jwtUtil Utilidad para operaciones con tokens JWT
   * @param userDetailsService Servicio para cargar los detalles del usuario
   */
  public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  /**
   * Extrae y valida el token JWT del encabezado de autorización. Si el token es válido y la cuenta
   * está activa, establece la autenticación en el contexto de seguridad. Un token inválido,
   * vencido o de un usuario inexistente simplemente no autentica (la petición recibirá 401).
   *
   * @param request Solicitud HTTP entrante
   * @param response Respuesta HTTP saliente
   * @param filterChain Cadena de filtros para continuar el procesamiento
   * @throws ServletException Si ocurre un error durante el procesamiento del servlet
   * @throws IOException Si ocurre un error de entrada/salida
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String authorizationHeader = request.getHeader("Authorization");

    String username = null;
    String jwt = null;

    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      jwt = authorizationHeader.substring(7).trim();
      try {
        username = jwtUtil.extractUsername(jwt);
      } catch (Exception e) {
        logger.debug("Token JWT rechazado: " + e.getClass().getSimpleName());
      }
    }

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (userDetails.isEnabled()
            && userDetails.isAccountNonLocked()
            && jwtUtil.validateToken(jwt, userDetails)) {
          UsernamePasswordAuthenticationToken authenticationToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authenticationToken.setDetails(
              new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
      } catch (AuthenticationException e) {
        logger.debug("Usuario del token no encontrado");
      }
    }

    filterChain.doFilter(request, response);
  }
}
