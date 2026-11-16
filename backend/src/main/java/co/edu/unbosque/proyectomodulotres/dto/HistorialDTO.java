package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

public class HistorialDTO {

	private Long idHistorial;
	private LocalDateTime fechaConsulta;
	private String descripcion;
	private String estado;
	private Long idUsuario;
	private Long idScraping;

	public HistorialDTO() {
	}

	public HistorialDTO(Long idHistorial, LocalDateTime fechaConsulta, String descripcion, String estado,
			Long idUsuario, Long idScraping) {
		this.idHistorial = idHistorial;
		this.fechaConsulta = fechaConsulta;
		this.descripcion = descripcion;
		this.estado = estado;
		this.idUsuario = idUsuario;
		this.idScraping = idScraping;
	}

	public Long getIdHistorial() {
		return idHistorial;
	}

	public void setIdHistorial(Long idHistorial) {
		this.idHistorial = idHistorial;
	}

	public LocalDateTime getFechaConsulta() {
		return fechaConsulta;
	}

	public void setFechaConsulta(LocalDateTime fechaConsulta) {
		this.fechaConsulta = fechaConsulta;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Long getIdScraping() {
		return idScraping;
	}

	public void setIdScraping(Long idScraping) {
		this.idScraping = idScraping;
	}
}
