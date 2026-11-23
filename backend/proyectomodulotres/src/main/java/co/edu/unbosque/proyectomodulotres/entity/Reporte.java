package co.edu.unbosque.proyectomodulotres.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa un reporte PDF generado para un scraping. El archivo se guarda en disco y
 * aquí se guarda su nombre y su ruta.
 */
@Entity
@Table(name = "reportes")
public class Reporte {

	/** Identificador del reporte. */
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idReporte;

	/** Nombre del archivo PDF. */
	private String nombreArchivo;

	/** Ruta absoluta del archivo PDF. */
	private String rutaArchivo;

	/** Fecha de generación. */
	private LocalDateTime fechaGeneracion;

	/** Estado del reporte (PENDIENTE o GENERADO). */
	private String estado;

	/** Identificador del scraping al que pertenece. */
	private Long idScraping;

	/** Constructor vacío. Asigna la fecha actual y el estado PENDIENTE. */
	public Reporte() {
		this.fechaGeneracion = LocalDateTime.now();
		this.estado = "PENDIENTE";
	}

	/**
	 * Constructor con los datos principales.
	 *
	 * @param nombreArchivo Nombre del archivo
	 * @param rutaArchivo Ruta del archivo
	 * @param idScraping Identificador del scraping
	 */
	public Reporte(String nombreArchivo, String rutaArchivo, Long idScraping) {
		this();
		this.nombreArchivo = nombreArchivo;
		this.rutaArchivo = rutaArchivo;
		this.idScraping = idScraping;
	}

	/** @return Identificador del reporte */
	public Long getIdReporte() {
		return idReporte;
	}

	/** @param idReporte Identificador del reporte */
	public void setIdReporte(Long idReporte) {
		this.idReporte = idReporte;
	}

	/** @return Nombre del archivo */
	public String getNombreArchivo() {
		return nombreArchivo;
	}

	/** @param nombreArchivo Nombre del archivo */
	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	/** @return Ruta del archivo */
	public String getRutaArchivo() {
		return rutaArchivo;
	}

	/** @param rutaArchivo Ruta del archivo */
	public void setRutaArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	/** @return Fecha de generación */
	public LocalDateTime getFechaGeneracion() {
		return fechaGeneracion;
	}

	/** @param fechaGeneracion Fecha de generación */
	public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
		this.fechaGeneracion = fechaGeneracion;
	}

	/** @return Estado del reporte */
	public String getEstado() {
		return estado;
	}

	/** @param estado Estado del reporte */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/** @return Identificador del scraping */
	public Long getIdScraping() {
		return idScraping;
	}

	/** @param idScraping Identificador del scraping */
	public void setIdScraping(Long idScraping) {
		this.idScraping = idScraping;
	}

	/** @return Código hash calculado con el identificador y el nombre del archivo */
	@Override
	public int hashCode() {
		return Objects.hash(idReporte, nombreArchivo);
	}

	/**
	 * Compara dos reportes por identificador y nombre del archivo.
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
		Reporte other = (Reporte) obj;
		return Objects.equals(idReporte, other.idReporte) && Objects.equals(nombreArchivo, other.nombreArchivo);
	}

	/** @return Texto con los datos del reporte */
	@Override
	public String toString() {
		return "Reporte [idReporte=" + idReporte + ", nombreArchivo=" + nombreArchivo + ", rutaArchivo=" + rutaArchivo
				+ ", fechaGeneracion=" + fechaGeneracion + ", estado=" + estado + ", idScraping=" + idScraping + "]";
	}
}
