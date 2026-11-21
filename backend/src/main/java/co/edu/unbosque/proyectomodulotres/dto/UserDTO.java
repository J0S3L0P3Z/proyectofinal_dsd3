package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDate;

public class UserDTO {

	private Long id;

	private String username;

	private String nombre;

	private String apellido;

	private String role;

	private LocalDate fechaRegistro;

	private Boolean enabled;

	private Boolean activo;

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

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDate getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDate fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
}