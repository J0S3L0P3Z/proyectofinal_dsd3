package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia con los datos de un reporte PDF.
 */
public class ReporteDTO {

	/** Identificador del reporte. */
	private Long idReporte;

	/** Nombre del archivo PDF. */
	private String nombreArchivo;

	/** Ruta del archivo PDF. */
	private String rutaArchivo;

	/** Fecha de generación. */
	private LocalDateTime fechaGeneracion;

	/** Estado del reporte. */
	private String estado;

	/** Identificador del scraping. */
	private Long idScraping;

	/** Constructor vacío. */
	public ReporteDTO() {
	}

	/**
	 * Constructor con todos los datos.
	 *
	 * @param idReporte Identificador del reporte
	 * @param nombreArchivo Nombre del archivo
	 * @param rutaArchivo Ruta del archivo
	 * @param fechaGeneracion Fecha de generación
	 * @param estado Estado del reporte
	 * @param idScraping Identificador del scraping
	 */
	public ReporteDTO(Long idReporte, String nombreArchivo, String rutaArchivo, LocalDateTime fechaGeneracion,
			String estado, Long idScraping) {
		this.idReporte = idReporte;
		this.nombreArchivo = nombreArchivo;
		this.rutaArchivo = rutaArchivo;
		this.fechaGeneracion = fechaGeneracion;
		this.estado = estado;
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
}
