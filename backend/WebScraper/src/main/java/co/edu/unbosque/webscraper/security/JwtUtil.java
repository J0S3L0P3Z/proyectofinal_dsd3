package co.edu.unbosque.webscraper.security;

import co.edu.unbosque.webscraper.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Clase utilitaria para operaciones con JSON Web Tokens (JWT). Proporciona métodos para generar,
 * validar y extraer información de tokens JWT firmados con HS256.
 */
@Component
public class JwtUtil {

  /** Tiempo de validez del token JWT en minutos (por defecto 1 hora, como pide el plan). */
  @Value("${jwt.expiration-minutes:60}")
  private long expirationMinutes;

  /**
   * Clave secreta utilizada para firmar los tokens JWT. Es obligatoria (variable de entorno
   * JWT_SECRET) y debe tener al menos 32 caracteres.
   */
  @Value("${jwt.secret}")
  private String secret;

  /** Constructor por defecto. */
  public JwtUtil() {}

  /**
   * Verifica al arrancar que el secreto tenga la longitud mínima que exige HS256.
   *
   * @throws IllegalStateException Si el secreto tiene menos de 32 bytes
   */
  @PostConstruct
  void validateSecret() {
    if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
    }
  }

  /**
   * Obtiene la clave de firma para los tokens JWT.
   *
   * @return Clave de firma generada a partir del secreto configurado
   */
  private Key getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Extrae el nombre de usuario del token JWT.
   *
   * @param token Token JWT
   * @return Nombre de usuario contenido en el token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extrae la fecha de expiración del token JWT.
   *
   * @param token Token JWT
   * @return Fecha de expiración del token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extrae el rol del usuario del token JWT.
   *
   * @param token Token JWT
   * @return Rol del usuario contenido en el token
   */
  public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
  }

  /**
   * Método genérico para extraer cualquier reclamación (claim) del token JWT.
   *
   * @param token Token JWT del cual extraer la reclamación
   * @param claimsResolver Función para resolver la reclamación específica
   * @return Valor de la reclamación extraída
   * @param <T> Tipo de dato de la reclamación a extraer
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extrae todas las reclamaciones del token. Lanza JwtException si la firma no coincide o el token
   * está vencido o manipulado.
   *
   * @param token Token JWT
   * @return Objeto Claims con todas las reclamaciones del token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Verifica si un token JWT ha expirado.
   *
   * @param token Token JWT a verificar
   * @return true si el token ha expirado, false en caso contrario
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Genera un token JWT para un usuario. Incluye el rol como reclamación.
   *
   * @param userDetails Detalles del usuario para el cual generar el token
   * @return Token JWT generado
   */
  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("authorities", userDetails.getAuthorities());

    if (userDetails instanceof User) {
      User user = (User) userDetails;
      claims.put("role", user.getRole().name());
    }

    return createToken(claims, userDetails.getUsername());
  }

  /**
   * Crea un token JWT con las reclamaciones especificadas.
   *
   * @param claims Reclamaciones a incluir en el token
   * @param subject Asunto del token (el nombre de usuario)
   * @return Token JWT creado
   */
  private String createToken(Map<String, Object> claims, String subject) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(now + expirationMinutes * 60 * 1000))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * Valida un token JWT para un usuario específico.
   *
   * @param token Token JWT a validar
   * @param userDetails Detalles del usuario contra los cuales validar el token
   * @return true si el token es válido para el usuario, false en caso contrario
   */
  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
