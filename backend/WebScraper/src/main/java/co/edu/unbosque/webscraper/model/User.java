package co.edu.unbosque.webscraper.model;

import co.edu.unbosque.webscraper.util.SearchableEncryptedConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Clase que representa un usuario en el sistema. Implementa UserDetails para integrarse con Spring
 * Security. Se puede iniciar sesión con el nombre de usuario o con el correo electrónico.
 *
 * <p>Esta entidad se almacena en la tabla "useraccount" en la base de datos.
 */
@Entity
@Table(name = "useraccount")
public class User implements UserDetails {

  /** Número de versión para la serialización. */
  private static final long serialVersionUID = 1L;

  /** Identificador único del usuario. Se genera automáticamente con estrategia de identidad. */
  private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

  /** Nombre de usuario único en el sistema (sin el símbolo @, para no confundirlo con un correo). */
  @Column(unique = true, nullable = false, length = 50)
  private String username;

  /**
   * Correo electrónico único del usuario (se normaliza a minúsculas). Dato sensible: se guarda
   * cifrado con AES/GCM en la base de datos, pero se puede seguir buscando por él.
   */
  @Convert(converter = SearchableEncryptedConverter.class)
  @Column(unique = true, length = 512)
  private String email;

  /** Contraseña del usuario (almacenada como hash BCrypt, nunca en texto plano). */
  @Column(nullable = false, length = 100)
  private String password;

  /** Rol del usuario en el sistema (USER o ADMIN). */
  @Enumerated(EnumType.STRING)
  private Role role;

  /** Indica si la cuenta del usuario no ha expirado. */
  private boolean accountNonExpired;

  /** Indica si la cuenta del usuario no está bloqueada. */
  private boolean accountNonLocked;

  /** Indica si las credenciales del usuario no han expirado. */
  private boolean credentialsNonExpired;

  /** Indica si la cuenta del usuario está habilitada (un administrador puede deshabilitarla). */
  private boolean enabled;

  /** Indica si el correo electrónico del usuario ya fue verificado. */
  private boolean verified;

  /** Fecha de creación de la cuenta. */
  @Column(updatable = false)
  private Instant createdAt;

  /** Fecha del último inicio de sesión exitoso. */
  private Instant lastLoginAt;

  /**
   * Constructor por defecto. Inicializa un usuario con valores predeterminados: cuenta no
   * expirada, no bloqueada, credenciales no expiradas, cuenta habilitada y rol USER.
   */
  public User() {
    this.accountNonExpired = true;
    this.accountNonLocked = true;
    this.credentialsNonExpired = true;
    this.enabled = true;
    this.role = Role.USER;
  }

  /**
   * Constructor con nombre de usuario y contraseña.
   *
   * @param username Nombre de usuario
   * @param password Contraseña del usuario (ya hasheada)
   */
  public User(String username, String password) {
    this();
    this.username = username;
    this.password = password;
  }

  /**
   * Constructor con nombre de usuario, contraseña y rol específico.
   *
   * @param username Nombre de usuario
   * @param password Contraseña del usuario (ya hasheada)
   * @param rol Rol asignado al usuario
   */
  public User(String username, String password, Role rol) {
    this();
    this.username = username;
    this.password = password;
    this.role = rol;
  }

  /**
   * Enumeración que define los roles disponibles en el sistema.
   *
   * <p>USER: usuario regular que usa el scraper y consulta su historial. ADMIN: administrador con
   * permisos completos (usuarios, estadísticas, reportes y auditoría).
   */
  public enum Role {
    /** Usuario regular con permisos básicos */
    USER,
    /** Administrador con permisos completos */
    ADMIN
  }

  /** Registra la fecha de creación antes de guardar por primera vez. */
  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  /**
   * Obtiene las autoridades (roles) asignadas al usuario. Implementación de UserDetails.
   *
   * @return Colección de autoridades del usuario con el prefijo "ROLE_"
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public boolean isAccountNonExpired() {
    return accountNonExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return credentialsNonExpired;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Obtiene el rol del usuario.
   *
   * @return El rol asignado al usuario
   */
  public Role getRole() {
    return role;
  }

  /**
   * Establece el rol del usuario.
   *
   * @param role El nuevo rol a asignar
   */
  public void setRole(Role role) {
    this.role = role;
  }

  public void setAccountNonExpired(boolean accountNonExpired) {
    this.accountNonExpired = accountNonExpired;
  }

  public void setAccountNonLocked(boolean accountNonLocked) {
    this.accountNonLocked = accountNonLocked;
  }

  public void setCredentialsNonExpired(boolean credentialsNonExpired) {
    this.credentialsNonExpired = credentialsNonExpired;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
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

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Indica si el correo electrónico del usuario fue verificado.
   *
   * @return true si el correo está verificado
   */
  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(Instant lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  /**
   * Calcula el código hash de este usuario según su id y su nombre de usuario.
   *
   * @return El código hash calculado
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, username);
  }

  /**
   * Dos usuarios son iguales si tienen el mismo id y el mismo nombre de usuario.
   *
   * @param obj El objeto a comparar con este usuario
   * @return true si los objetos son iguales, false en caso contrario
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    User other = (User) obj;
    return Objects.equals(id, other.id) && Objects.equals(username, other.username);
  }

  /**
   * Representación en texto del usuario. Nunca incluye la contraseña ni su hash.
   *
   * @return Una cadena que representa este usuario
   */
  @Override
  public String toString() {
    return "User [id=" + id + ", username=" + username + ", role=" + role + "]";
  }
}
