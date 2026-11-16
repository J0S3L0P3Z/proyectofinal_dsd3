package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.model.TokenType;
import co.edu.unbosque.webscraper.model.UserToken;
import co.edu.unbosque.webscraper.repository.UserTokenRepository;
import co.edu.unbosque.webscraper.util.AESUtil;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de tokens de un solo uso (verificar correo y recuperar contraseña). El valor real del
 * token solo viaja por correo; en la base de datos se guarda únicamente su hash SHA-256.
 */
@Service
public class UserTokenService {

  /** Repositorio de tokens. */
  private final UserTokenRepository repo;

  /** Generador de números aleatorios criptográficamente seguro. */
  private final SecureRandom random = new SecureRandom();

  /**
   * Constructor con el repositorio de tokens.
   *
   * @param repo Repositorio de tokens
   */
  public UserTokenService(UserTokenRepository repo) {
    this.repo = repo;
  }

  /**
   * Genera un token nuevo para el usuario e invalida los anteriores del mismo tipo.
   *
   * @param userId Id del usuario
   * @param type Tipo de token
   * @param ttl Tiempo de vida
   * @return El token en texto (para enviarlo por correo)
   */
  @Transactional
  public String issue(Long userId, TokenType type, Duration ttl) {
    Instant now = Instant.now();
    repo.invalidateOpen(userId, type, now);
    byte[] raw = new byte[32];
    random.nextBytes(raw);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    repo.save(new UserToken(userId, type, AESUtil.hashingToSHA256(token), now.plus(ttl)));
    return token;
  }

  /**
   * Marca el token como usado y devuelve el registro.
   *
   * @param token Token recibido por correo
   * @param type Tipo esperado
   * @return El registro, o vacío si no existe, venció, ya se usó o es de otro tipo
   */
  @Transactional
  public Optional<UserToken> consume(String token, TokenType type) {
    if (token == null || token.isBlank()) return Optional.empty();
    Optional<UserToken> found = repo.findByTokenHash(AESUtil.hashingToSHA256(token.trim()));
    if (found.isEmpty()) return Optional.empty();
    UserToken t = found.get();
    if (t.getType() != type || t.getUsedAt() != null || t.getExpiresAt().isBefore(Instant.now())) {
      return Optional.empty();
    }
    t.setUsedAt(Instant.now());
    repo.save(t);
    return Optional.of(t);
  }
}
