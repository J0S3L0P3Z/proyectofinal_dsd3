package co.edu.unbosque.proyectomodulotres.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Utilidad para generar y validar los tokens JWT. El token lleva el correo del usuario como sujeto,
 * su rol en el claim {@code role} y dura 12 horas.
 */
@Component
public class JwtUtil {

	/** Tiempo de validez del token: 12 horas en milisegundos. */
	private static final long JWT_TOKEN_VALIDITY = 12 * 60 * 60 * 1000;

	/** Clave secreta para firmar los tokens (mínimo 32 caracteres). */
	@Value("${jwt.secret}")
	private String secret;

	/**
	 * Crea la clave HMAC a partir de la clave secreta.
	 *
	 * @return Clave de firma
	 */
	private SecretKey getSigningKey() {

		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Obtiene el correo del usuario guardado en el token.
	 *
	 * @param token Token JWT
	 * @return Correo del usuario
	 */
	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	/**
	 * Obtiene la fecha de vencimiento del token.
	 *
	 * @param token Token JWT
	 * @return Fecha de vencimiento
	 */
	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	/**
	 * Obtiene el rol guardado en el token.
	 *
	 * @param token Token JWT
	 * @return Rol del usuario (por ejemplo ROLE_ADMIN)
	 */
	public String extractRole(String token) {
		return extractAllClaims(token).get("role", String.class);
	}

	/**
	 * Verifica la firma del token y devuelve todos sus datos.
	 *
	 * @param token Token JWT
	 * @return Datos del token
	 */
	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	/**
	 * Indica si el token ya venció.
	 *
	 * @param token Token JWT
	 * @return true si venció
	 */
	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	/**
	 * Genera un token para el usuario con su rol.
	 *
	 * @param userDetails Usuario autenticado
	 * @return Token JWT firmado
	 */
	public String generateToken(UserDetails userDetails) {

		Map<String, Object> claims = new HashMap<>();

		String rol = "";

		for (GrantedAuthority authority : userDetails.getAuthorities()) {
			rol = authority.getAuthority();
			break;
		}

		claims.put("role", rol);

		return createToken(claims, userDetails.getUsername());
	}

	/**
	 * Construye y firma el token.
	 *
	 * @param claims Datos adicionales del token
	 * @param subject Correo del usuario
	 * @return Token JWT firmado
	 */
	private String createToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().claims(claims).subject(subject).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY)).signWith(getSigningKey())
				.compact();
	}

	/**
	 * Valida que el token pertenezca al usuario y no haya vencido.
	 *
	 * @param token Token JWT
	 * @param userDetails Usuario cargado de la base de datos
	 * @return true si el token es válido
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {

		try {

			String username = extractUsername(token);

			return username.equals(userDetails.getUsername()) && !isTokenExpired(token);

		} catch (Exception e) {

			return false;
		}
	}
}
