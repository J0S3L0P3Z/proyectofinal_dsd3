package co.edu.unbosque.proyectomodulotres.dto;

/**
 * Objeto de transferencia con los datos de un rol.
 */
public class RolDTO {

	/** Identificador del rol. */
	private Long id;

	/** Nombre del rol. */
	private String nombre;

	/** Constructor vacío. */
	public RolDTO() {
	}

	/**
	 * Constructor con todos los datos.
	 *
	 * @param id Identificador del rol
	 * @param nombre Nombre del rol
	 */
	public RolDTO(Long id, String nombre) {
		this.id = id;
		this.nombre = nombre;
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
}
