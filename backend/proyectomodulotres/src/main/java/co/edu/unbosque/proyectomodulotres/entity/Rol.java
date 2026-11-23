package co.edu.unbosque.proyectomodulotres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa un rol del sistema (ADMIN o USER).
 */
@Entity
@Table(name = "Roles")
public class Rol {

	/** Identificador del rol. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Nombre único del rol. */
	@Column(nullable = false, unique = true, length = 50)
	private String nombre;

	/** Descripción del rol. */
	@Column(length = 150)
	private String descripcion;

	/** Constructor vacío requerido por JPA. */
	public Rol() {
	}

	/**
	 * Constructor con nombre y descripción.
	 *
	 * @param nombre Nombre del rol
	 * @param descripcion Descripción del rol
	 */
	public Rol(String nombre, String descripcion) {
		this.nombre = nombre;
		this.descripcion = descripcion;
	}

	/** @return Identificador del rol */
	public Long getId() {
		return id;
	}

	/** @param id Identificador del rol */
	public void setId(Long id) {
		this.id = id;
	}

	/** @return Nombre del rol */
	public String getNombre() {
		return nombre;
	}

	/** @param nombre Nombre del rol */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/** @return Descripción del rol */
	public String getDescripcion() {
		return descripcion;
	}

	/** @param descripcion Descripción del rol */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/** @return Texto con los datos del rol */
	@Override
	public String toString() {
		return "Rol{" + "id=" + id + ", nombre='" + nombre + '\'' + ", descripcion='" + descripcion + '\'' + '}';
	}
}
