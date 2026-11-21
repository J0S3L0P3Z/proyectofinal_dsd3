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

@Component
public class JwtUtil {

	private static final long JWT_TOKEN_VALIDITY = 12 * 60 * 60 * 1000;

	@Value("${jwt.secret}")
	private String secret;

	private SecretKey getSigningKey() {

		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

		return Keys.hmacShaKeyFor(keyBytes);
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public Date extractExpiration(String token) {
		return extractAllClaims(token).getExpiration();
	}

	public String extractRole(String token) {
		return extractAllClaims(token).get("role", String.class);
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

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

	private String createToken(Map<String, Object> claims, String subject) {

		return Jwts.builder().claims(claims).subject(subject).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY)).signWith(getSigningKey())
				.compact();
	}

	public Boolean validateToken(String token, UserDetails userDetails) {

		try {

			String username = extractUsername(token);

			return username.equals(userDetails.getUsername()) && !isTokenExpired(token);

		} catch (Exception e) {

			return false;
		}
	}
}