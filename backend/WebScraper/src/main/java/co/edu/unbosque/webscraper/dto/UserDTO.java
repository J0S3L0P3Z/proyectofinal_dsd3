package co.edu.unbosque.webscraper.dto;

import co.edu.unbosque.webscraper.model.User.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

/**
 * Clase de Objeto de Transferencia de Datos (DTO) para usuarios. Utilizada para transferir datos de
 * usuario entre capas de la aplicación, especialmente en operaciones de API.
 *
 * <p>La contraseña solo se acepta al recibir datos (WRITE_ONLY): nunca se envía en las respuestas.
 */
public class UserDTO {

  /** Identificador único del usuario. */
  private Long id;

  /** Nombre de usuario para autenticación. También se puede usar el correo al iniciar sesión. */
  private String username;

  /** Correo electrónico del usuario. */
  private String email;

  /** Contraseña del usuario (solo de entrada, nunca se serializa). */
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String password;

  /** Rol del usuario en el sistema (por ejemplo, USER, ADMIN). */
  private Role role;

  /** Indica si la cuenta está habilitada (solo de salida). */
  private Boolean enabled;

  /** Indica si el correo está verificado (solo de salida). */
  private Boolean verified;

  /** Fecha de creación de la cuenta (solo de salida). */
  private Instant createdAt;

  /** Fecha del último inicio de sesión (solo de salida). */
  private Instant lastLoginAt;

  /** Cantidad de scrapings realizados (solo de salida, en el panel de administración). */
  private Long scrapeCount;

  /** Constructor por defecto. Crea una instancia vacía de UserDTO. */
  public UserDTO() {}

  /**
   * Constructor con nombre de usuario y contraseña.
   *
   * @param username Nombre de usuario
   * @param password Contraseña del usuario
   */
  public UserDTO(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Constructor con nombre de usuario, contraseña y rol.
   *
   * @param username Nombre de usuario
   * @param password Contraseña del usuario
   * @param role Rol del usuario
   */
  public UserDTO(String username, String password, Role role) {
    this.username = username;
    this.password = password;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Boolean getVerified() {
    return verified;
  }

  public void setVerified(Boolean verified) {
    this.verified = verified;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(Instant lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public Long getScrapeCount() {
    return scrapeCount;
  }

  public void setScrapeCount(Long scrapeCount) {
    this.scrapeCount = scrapeCount;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, email, role, username);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UserDTO other = (UserDTO) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(email, other.email)
        && role == other.role
        && Objects.equals(username, other.username);
  }

  /** Representación en texto sin la contraseña. */
  @Override
  public String toString() {
    return "UserDTO [id=" + id + ", username=" + username + ", email=" + email + ", role=" + role + "]";
  }
}
