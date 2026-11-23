package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia con los datos de un registro del historial.
 */
public class HistorialDTO {

	/** Identificador del registro. */
	private Long idHistorial;

	/** Fecha de la consulta. */
	private LocalDateTime fechaConsulta;

	/** Descripción de la consulta. */
	private String descripcion;

	/** Estado de la consulta. */
	private String estado;

	/** Identificador del usuario. */
	private Long idUsuario;

	/** Identificador del scraping. */
	private Long idScraping;

	/** Constructor vacío. */
	public HistorialDTO() {
	}

	/**
	 * Constructor con todos los datos.
	 *
	 * @param idHistorial Identificador del registro
	 * @param fechaConsulta Fecha de la consulta
	 * @param descripcion Descripción de la consulta
	 * @param estado Estado de la consulta
	 * @param idUsuario Identificador del usuario
	 * @param idScraping Identificador del scraping
	 */
	public HistorialDTO(Long idHistorial, LocalDateTime fechaConsulta, String descripcion, String estado,
			Long idUsuario, Long idScraping) {
		this.idHistorial = idHistorial;
		this.fechaConsulta = fechaConsulta;
		this.descripcion = descripcion;
		this.estado = estado;
		this.idUsuario = idUsuario;
		this.idScraping = idScraping;
	}

	/** @return Identificador del registro */
	public Long getIdHistorial() {
		return idHistorial;
	}

	/** @param idHistorial Identificador del registro */
	public void setIdHistorial(Long idHistorial) {
		this.idHistorial = idHistorial;
	}

	/** @return Fecha de la consulta */
	public LocalDateTime getFechaConsulta() {
		return fechaConsulta;
	}

	/** @param fechaConsulta Fecha de la consulta */
	public void setFechaConsulta(LocalDateTime fechaConsulta) {
		this.fechaConsulta = fechaConsulta;
	}

	/** @return Descripción de la consulta */
	public String getDescripcion() {
		return descripcion;
	}

	/** @param descripcion Descripción de la consulta */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/** @return Estado de la consulta */
	public String getEstado() {
		return estado;
	}

	/** @param estado Estado de la consulta */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/** @return Identificador del usuario */
	public Long getIdUsuario() {
		return idUsuario;
	}

	/** @param idUsuario Identificador del usuario */
	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	/** @return Identificador del scraping */
	public Long getIdScraping() {
		return idScraping;
	}

	/** @param idScraping Identificador del scraping */
	public void setIdScraping(Long idScraping) {
		this.idScraping = idScraping;
	}
}
