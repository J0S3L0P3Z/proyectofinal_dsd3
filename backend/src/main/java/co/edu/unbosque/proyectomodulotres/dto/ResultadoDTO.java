package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

public class ResultadoDTO {

	private Long idResultado;
	private String nombreIA;
	private String modelo;
	private String respuesta;
	private LocalDateTime fechaAnalisis;
	private String estado;
	private Long idScraping;

	public ResultadoDTO() {
	}

	public ResultadoDTO(Long idResultado, String nombreIA, String modelo, String respuesta, LocalDateTime fechaAnalisis,
			String estado, Long idScraping) {
		this.idResultado = idResultado;
		this.nombreIA = nombreIA;
		this.modelo = modelo;
		this.respuesta = respuesta;
		this.fechaAnalisis = fechaAnalisis;
		this.estado = estado;
		this.idScraping = idScraping;
	}

	public Long getIdResultado() {
		return idResultado;
	}

	public void setIdResultado(Long idResultado) {
		this.idResultado = idResultado;
	}

	public String getNombreIA() {
		return nombreIA;
	}

	public void setNombreIA(String nombreIA) {
		this.nombreIA = nombreIA;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getRespuesta() {
		return respuesta;
	}

	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	public LocalDateTime getFechaAnalisis() {
		return fechaAnalisis;
	}

	public void setFechaAnalisis(LocalDateTime fechaAnalisis) {
		this.fechaAnalisis = fechaAnalisis;
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
