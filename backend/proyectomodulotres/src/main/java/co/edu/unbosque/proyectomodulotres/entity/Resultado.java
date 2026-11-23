package co.edu.unbosque.proyectomodulotres.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad que representa la respuesta de una IA para un scraping. La respuesta se guarda cifrada
 * con AES (prefijo {@code AES:}).
 */
@Entity
@Table(name = "resultados")
public class Resultado {

	/** Identificador del resultado. */
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idResultado;

	/** Nombre de la IA (NVIDIA, DOTS, GEMINI, PARTICLE o GPT). */
	private String nombreIA;

	/** Identificador del modelo usado en OpenRouter. */
	private String modelo;

	/** Respuesta del modelo, cifrada. */
	@Column(columnDefinition = "LONGTEXT")
	private String respuesta;

	/** Fecha en la que se hizo el análisis. */
	private LocalDateTime fechaAnalisis;

	/** Estado del resultado (PENDIENTE, COMPLETADO o ERROR). */
	private String estado;

	/** Identificador del scraping al que pertenece. */
	private Long idScraping;

	/** Constructor vacío. Asigna la fecha actual y el estado PENDIENTE. */
	public Resultado() {
		this.fechaAnalisis = LocalDateTime.now();
		this.estado = "PENDIENTE";
	}

	/**
	 * Constructor con los datos principales.
	 *
	 * @param nombreIA Nombre de la IA
	 * @param modelo Modelo usado
	 * @param idScraping Identificador del scraping
	 */
	public Resultado(String nombreIA, String modelo, Long idScraping) {
		this();
		this.nombreIA = nombreIA;
		this.modelo = modelo;
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

	/** @return Respuesta cifrada */
	public String getRespuesta() {
		return respuesta;
	}

	/** @param respuesta Respuesta cifrada */
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

	/** @return Código hash calculado con el identificador y el nombre de la IA */
	@Override
	public int hashCode() {
		return Objects.hash(idResultado, nombreIA);
	}

	/**
	 * Compara dos resultados por identificador y nombre de la IA.
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
		Resultado other = (Resultado) obj;
		return Objects.equals(idResultado, other.idResultado) && Objects.equals(nombreIA, other.nombreIA);
	}

	/** @return Texto con los datos del resultado (sin la respuesta) */
	@Override
	public String toString() {
		return "Resultado [idResultado=" + idResultado + ", nombreIA=" + nombreIA + ", modelo=" + modelo
				+ ", fechaAnalisis=" + fechaAnalisis + ", estado=" + estado + ", idScraping=" + idScraping + "]";
	}
}
