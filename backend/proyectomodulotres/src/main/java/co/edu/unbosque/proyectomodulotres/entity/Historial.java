package co.edu.unbosque.proyectomodulotres.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa un registro del historial de consultas de un usuario. Se crea uno por
 * cada scraping, exitoso o fallido.
 */
@Entity
@Table(name = "historial")
public class Historial {

	/** Identificador del registro. */
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idHistorial;

	/** Fecha de la consulta. */
	private LocalDateTime fechaConsulta;

	/** Descripción de la consulta. */
	private String descripcion;

	/** Estado de la consulta. */
	private String estado;

	/** Identificador del usuario que hizo la consulta. */
	private Long idUsuario;

	/** Identificador del scraping relacionado. */
	private Long idScraping;

	/** Constructor vacío. Asigna la fecha actual y el estado REGISTRADO. */
	public Historial() {
		this.fechaConsulta = LocalDateTime.now();
		this.estado = "REGISTRADO";
	}

	/**
	 * Constructor con los datos principales.
	 *
	 * @param descripcion Descripción de la consulta
	 * @param idUsuario Identificador del usuario
	 * @param idScraping Identificador del scraping
	 */
	public Historial(String descripcion, Long idUsuario, Long idScraping) {
		this();
		this.descripcion = descripcion;
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

	/** @return Código hash calculado con el identificador y la fecha */
	@Override
	public int hashCode() {
		return Objects.hash(idHistorial, fechaConsulta);
	}

	/**
	 * Compara dos registros por identificador y fecha.
	 *
	 * @param obj Objeto a comparar
	 * @return true si son iguales
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Historial other = (Historial) obj;
		return Objects.equals(idHistorial, other.idHistorial) && Objects.equals(fechaConsulta, other.fechaConsulta);
	}

	/** @return Texto con los datos del registro */
	@Override
	public String toString() {
		return "Historial [idHistorial=" + idHistorial + ", fechaConsulta=" + fechaConsulta + ", descripcion="
				+ descripcion + ", estado=" + estado + ", idUsuario=" + idUsuario + ", idScraping=" + idScraping + "]";
	}
}
