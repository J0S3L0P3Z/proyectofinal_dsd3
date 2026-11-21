package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

public class ReporteDTO {

	private Long idReporte;
	private String nombreArchivo;
	private String rutaArchivo;
	private LocalDateTime fechaGeneracion;
	private String estado;
	private Long idScraping;

	public ReporteDTO() {
	}

	public ReporteDTO(Long idReporte, String nombreArchivo, String rutaArchivo, LocalDateTime fechaGeneracion,
			String estado, Long idScraping) {
		this.idReporte = idReporte;
		this.nombreArchivo = nombreArchivo;
		this.rutaArchivo = rutaArchivo;
		this.fechaGeneracion = fechaGeneracion;
		this.estado = estado;
		this.idScraping = idScraping;
	}

	public Long getIdReporte() {
		return idReporte;
	}

	public void setIdReporte(Long idReporte) {
		this.idReporte = idReporte;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getRutaArchivo() {
		return rutaArchivo;
	}

	public void setRutaArchivo(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	public LocalDateTime getFechaGeneracion() {
		return fechaGeneracion;
	}

	public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
		this.fechaGeneracion = fechaGeneracion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Long getIdScraping() {
		return idScraping;
	}

	public void setIdScraping(Long idScraping) {
		this.idScraping = idScraping;
	}
}