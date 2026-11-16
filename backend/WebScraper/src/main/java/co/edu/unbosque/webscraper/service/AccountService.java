package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.exception.PasswordNotValidException;
import co.edu.unbosque.webscraper.model.AuditAction;
import co.edu.unbosque.webscraper.model.TokenType;
import co.edu.unbosque.webscraper.model.User;
import co.edu.unbosque.webscraper.model.UserToken;
import co.edu.unbosque.webscraper.repository.UserRepository;
import co.edu.unbosque.webscraper.util.PasswordValidator;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de cuenta: verificación del correo electrónico y recuperación de contraseña. Los
 * métodos públicos nunca revelan si un correo existe o no.
 */
@Service
public class AccountService {

  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final UserTokenService tokens;
  private final NotificationService notifications;
  private final AuditService audit;
  private final AppProperties props;

  /**
   * Constructor con las dependencias del servicio.
   *
   * @param userRepo Repositorio de usuarios
   * @param passwordEncoder Codificador de contraseñas (BCrypt)
   * @param tokens Servicio de tokens de un solo uso
   * @param notifications Servicio de correos
   * @param audit Servicio de auditoría
   * @param props Propiedades de la aplicación
   */
  public AccountService(
      UserRepository userRepo,
      PasswordEncoder passwordEncoder,
      UserTokenService tokens,
      NotificationService notifications,
      AuditService audit,
      AppProperties props) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.tokens = tokens;
    this.notifications = notifications;
    this.audit = audit;
    this.props = props;
  }

  /**
   * Envía el correo de verificación a un usuario.
   *
   * @param user Usuario (debe tener correo)
   */
  public void sendVerification(User user) {
    if (user.getEmail() == null) return;
    int hours = props.auth().verificationTokenHours();
    String token = tokens.issue(user.getId(), TokenType.EMAIL_VERIFICATION, Duration.ofHours(hours));
    notifications.sendVerification(user.getEmail(), user.getUsername(), link(props.frontendVerifyEmailPath(), token), hours);
  }

  /**
   * Verifica el correo con el token recibido.
   *
   * @param token Token del correo
   * @return 0 si se verificó, 1 si el token no es válido o venció
   */
  @Transactional
  public int verifyEmail(String token) {
    Optional<UserToken> t = tokens.consume(token, TokenType.EMAIL_VERIFICATION);
    if (t.isEmpty()) return 1;
    Optional<User> user = userRepo.findById(t.get().getUserId());
    if (user.isEmpty()) return 1;
    user.get().setVerified(true);
    userRepo.save(user.get());
    audit.log(user.get().getId(), user.get().getUsername(), AuditAction.EMAIL_VERIFIED, null);
    return 0;
  }

  /**
   * Reenvía el correo de verificación si la cuenta existe y falta verificarla.
   *
   * @param email Correo de la cuenta
   */
  public void resendVerification(String email) {
    userRepo
        .findByEmail(normalize(email))
        .filter(u -> u.isEnabled() && !u.isVerified())
        .ifPresent(this::sendVerification);
  }

  /**
   * Inicia la recuperación de contraseña: si el correo existe, envía un enlace de un solo uso.
   *
   * @param email Correo de la cuenta
   */
  public void forgotPassword(String email) {
    userRepo
        .findByEmail(normalize(email))
        .filter(User::isEnabled)
        .ifPresent(
            u -> {
              int minutes = props.auth().resetTokenMinutes();
              String token = tokens.issue(u.getId(), TokenType.PASSWORD_RESET, Duration.ofMinutes(minutes));
              notifications.sendPasswordReset(u.getEmail(), u.getUsername(), link(props.frontendResetPasswordPath(), token), minutes);
              audit.log(u.getId(), u.getUsername(), AuditAction.PASSWORD_RESET_REQUESTED, null);
            });
  }

  /**
   * Cambia la contraseña con el token recibido por correo.
   *
   * @param token Token del correo
   * @param newPassword Nueva contraseña (política de contraseñas)
   * @return 0 si se cambió, 1 si el token no es válido o venció, 2 si la contraseña no es válida
   */
  @Transactional
  public int resetPassword(String token, String newPassword) {
    try {
      PasswordValidator.validate(newPassword);
    } catch (PasswordNotValidException e) {
      return 2;
    }
    Optional<UserToken> t = tokens.consume(token, TokenType.PASSWORD_RESET);
    if (t.isEmpty()) return 1;
    Optional<User> user = userRepo.findById(t.get().getUserId());
    if (user.isEmpty()) return 1;
    user.get().setPassword(passwordEncoder.encode(newPassword));
    userRepo.save(user.get());
    audit.log(user.get().getId(), user.get().getUsername(), AuditAction.PASSWORD_RESET_COMPLETED, null);
    return 0;
  }

  private String link(String path, String token) {
    String base = props.frontendUrl();
    if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
    return base + path + "?token=" + token;
  }

  private static String normalize(String email) {
    return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
  }
}
