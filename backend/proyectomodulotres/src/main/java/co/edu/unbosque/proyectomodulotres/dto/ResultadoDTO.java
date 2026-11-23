package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia con el resultado de una IA. La respuesta viaja descifrada.
 */
public class ResultadoDTO {

	/** Identificador del resultado. */
	private Long idResultado;

	/** Nombre de la IA. */
	private String nombreIA;

	/** Modelo usado. */
	private String modelo;

	/** Respuesta del modelo en texto plano. */
	private String respuesta;

	/** Fecha del análisis. */
	private LocalDateTime fechaAnalisis;

	/** Estado del resultado (COMPLETADO o ERROR). */
	private String estado;

	/** Identificador del scraping. */
	private Long idScraping;

	/** Constructor vacío. */
	public ResultadoDTO() {
	}

	/**
	 * Constructor con todos los datos.
	 *
	 * @param idResultado Identificador del resultado
	 * @param nombreIA Nombre de la IA
	 * @param modelo Modelo usado
	 * @param respuesta Respuesta del modelo
	 * @param fechaAnalisis Fecha del análisis
	 * @param estado Estado del resultado
	 * @param idScraping Identificador del scraping
	 */
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

	/** @return Identificador del resultado */
	public Long getIdResultado() {
		return idResultado;
	}

	/** @param idResultado Identificador del resultado */
	public void setIdResultado(Long idResultado) {
		this.idResultado = idResultado;
	}

	/** @return Nombre de la IA */
	public String getNombreIA() {
		return nombreIA;
	}

	/** @param nombreIA Nombre de la IA */
	public void setNombreIA(String nombreIA) {
		this.nombreIA = nombreIA;
	}

	/** @return Modelo usado */
	public String getModelo() {
		return modelo;
	}

	/** @param modelo Modelo usado */
	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	/** @return Respuesta del modelo */
	public String getRespuesta() {
		return respuesta;
	}

	/** @param respuesta Respuesta del modelo */
	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}

	/** @return Fecha del análisis */
	public LocalDateTime getFechaAnalisis() {
		return fechaAnalisis;
	}

	/** @param fechaAnalisis Fecha del análisis */
	public void setFechaAnalisis(LocalDateTime fechaAnalisis) {
		this.fechaAnalisis = fechaAnalisis;
	}

	/** @return Estado del resultado */
	public String getEstado() {
		return estado;
	}

	/** @param estado Estado del resultado */
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
