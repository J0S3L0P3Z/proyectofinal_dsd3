package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDate;

/**
 * Objeto de transferencia con los datos públicos de un usuario (sin la contraseña).
 */
public class UserDTO {

	/** Identificador del usuario. */
	private Long id;

	/** Correo del usuario. */
	private String username;

	/** Nombre del usuario. */
	private String nombre;

	/** Apellido del usuario. */
	private String apellido;

	/** Nombre del rol. */
	private String role;

	/** Fecha de registro. */
	private LocalDate fechaRegistro;

	/** Indica si la cuenta está habilitada. */
	private Boolean enabled;

	/** Indica si la cuenta está activa. */
	private Boolean activo;

	/**
	 * Constructor con todos los datos.
	 *
	 * @param id Identificador del usuario
	 * @param username Correo del usuario
	 * @param nombre Nombre del usuario
	 * @param apellido Apellido del usuario
	 * @param role Nombre del rol
	 * @param fechaRegistro Fecha de registro
	 * @param enabled Si la cuenta está habilitada
	 * @param activo Si la cuenta está activa
	 */
	public UserDTO(Long id, String username, String nombre, String apellido, String role, LocalDate fechaRegistro,
			Boolean enabled, Boolean activo) {

		this.id = id;
		this.username = username;
		this.nombre = nombre;
		this.apellido = apellido;
		this.role = role;
		this.fechaRegistro = fechaRegistro;
		this.enabled = enabled;
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

	/** @return Nombre del rol */
	public String getRole() {
		return role;
	}

	/** @param role Nombre del rol */
	public void setRole(String role) {
		this.role = role;
	}

	/** @return Fecha de registro */
	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	/** @param fechaRegistro Fecha de registro */
	public void setFechaRegistro(LocalDate fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	/** @return Si la cuenta está habilitada */
	public Boolean getEnabled() {
		return enabled;
	}

	/** @param enabled Si la cuenta está habilitada */
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	/** @return Si la cuenta está activa */
	public Boolean getActivo() {
		return activo;
	}

	/** @param activo Si la cuenta está activa */
	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
}
