package co.edu.unbosque.proyectomodulotres.entity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad que representa un usuario del sistema. Implementa UserDetails para integrarse con Spring
 * Security; el nombre de usuario es el correo electrónico. Se guarda en la tabla "usuarios".
 */
@Entity
@Table(name = "usuarios")
public class User implements UserDetails {

	/** Número de versión para la serialización. */
	private static final long serialVersionUID = 1L;

	/** Identificador del usuario. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Correo del usuario, usado para iniciar sesión. */
	@Column(name = "correo", unique = true, nullable = false)
	private String username;

	/** Nombre del usuario. */
	private String nombre;

	/** Apellido del usuario. */
	private String apellido;

	/** Contraseña guardada con BCrypt. */
	@Column(name = "contrasena", nullable = false)
	private String password;

	/** Rol del usuario (ADMIN o USER). */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "rol_id", nullable = false)
	private Rol role;

	/** Fecha de registro. */
	private LocalDate fechaRegistro;

	/** Indica si la cuenta no ha expirado. */
	private boolean accountNonExpired;

	/** Indica si la cuenta no está bloqueada. */
	private boolean accountNonLocked;

	/** Indica si las credenciales no han expirado. */
	private boolean credentialsNonExpired;

	/** Indica si la cuenta está habilitada. */
	private boolean enabled;

	/** Indica si la cuenta está activa. */
	@Column(nullable = false)
	private boolean activo;

	/**
	 * Constructor vacío. Deja la cuenta habilitada, activa, sin expirar ni bloquear y con la fecha de
	 * registro actual.
	 */
	public User() {
		this.accountNonExpired = true;
		this.accountNonLocked = true;
		this.credentialsNonExpired = true;
		this.enabled = true;
		this.activo = true;
		this.fechaRegistro = LocalDate.now();
	}

	/**
	 * Constructor con correo y contraseña.
	 *
	 * @param username Correo del usuario
	 * @param password Contraseña (ya codificada)
	 */
	public User(String username, String password) {
		this();
		this.username = username;
		this.password = password;
	}

	/**
	 * Constructor con los datos básicos.
	 *
	 * @param username Correo del usuario
	 * @param password Contraseña (ya codificada)
	 * @param rol Rol del usuario
	 * @param nombre Nombre del usuario
	 * @param apellido Apellido del usuario
	 */
	public User(String username, String password, Rol rol, String nombre, String apellido) {
		this();
		this.username = username;
		this.password = password;
		this.role = rol;
		this.nombre = nombre;
		this.apellido = apellido;
	}

	/**
	 * Devuelve la autoridad del rol con el prefijo "ROLE_".
	 *
	 * @return Lista con la autoridad del usuario, o vacía si no tiene rol
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		if (role == null || role.getNombre() == null) {
			return List.of();
		}

		String nombreRol = role.getNombre().toUpperCase();

		if (!nombreRol.startsWith("ROLE_")) {
			nombreRol = "ROLE_" + nombreRol;
		}

		return List.of(new SimpleGrantedAuthority(nombreRol));
	}

	/** @return true si la cuenta no ha expirado */
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	/** @return true si la cuenta no está bloqueada */
	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	/** @return true si las credenciales no han expirado */
	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	/** @return true si la cuenta está habilitada y activa */
	@Override
	public boolean isEnabled() {
		return enabled && activo;
	}

	/** @return Rol del usuario */
	public Rol getRole() {
		return role;
	}

	/** @param role Rol del usuario */
	public void setRole(Rol role) {
		this.role = role;
	}

	/** @param accountNonExpired Si la cuenta no ha expirado */
	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	/** @param accountNonLocked Si la cuenta no está bloqueada */
	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	/** @param credentialsNonExpired Si las credenciales no han expirado */
	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	/** @param enabled Si la cuenta está habilitada */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/** @return true si la cuenta está activa */
	public boolean isActivo() {
		return activo;
	}

	/** @param activo Si la cuenta está activa */
	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	/** @return Identificador del usuario */
	public Long getId() {
		return id;
	}

	/** @param id Identificador del usuario */
	public void setId(Long id) {
		this.id = id;
	}

	/** @return Correo del usuario */
	@Override
	public String getUsername() {
		return username;
	}

	/** @param username Correo del usuario */
	public void setUsername(String username) {
		this.username = username;
	}

	/** @return Nombre del usuario */
	public String getNombre() {
		return nombre;
	}

	/** @param nombre Nombre del usuario */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/** @return Apellido del usuario */
	public String getApellido() {
		return apellido;
	}

	/** @param apellido Apellido del usuario */
	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	/** @return Contraseña codificada */
	@Override
	public String getPassword() {
		return password;
	}

	/** @param password Contraseña */
	public void setPassword(String password) {
		this.password = password;
	}

	/** @return Fecha de registro */
	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	/** @param fechaRegistro Fecha de registro */
	public void setFechaRegistro(LocalDate fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	/** @return Código hash calculado con el id, la contraseña y el correo */
	@Override
	public int hashCode() {
		return Objects.hash(id, password, username);
	}

	/**
	 * Compara dos usuarios por id, contraseña y correo.
	 *
	 * @param obj Objeto a comparar
	 * @return true si son iguales
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		User other = (User) obj;

		return Objects.equals(id, other.id) && Objects.equals(password, other.password)
				&& Objects.equals(username, other.username);
	}

	/** @return Texto con los datos del usuario (sin la contraseña) */
	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", nombre=" + nombre + ", apellido=" + apellido + ", role="
				+ role + ", fechaRegistro=" + fechaRegistro + ", activo=" + activo + "]";
	}
}
