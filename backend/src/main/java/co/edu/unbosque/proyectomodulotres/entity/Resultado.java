package co.edu.unbosque.proyectomodulotres.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultados")
public class Resultado {

	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idResultado;

	private String nombreIA;

	private String modelo;

	@Column(columnDefinition = "LONGTEXT")
	private String respuesta;

	private LocalDateTime fechaAnalisis;

	private String estado;

	private Long idScraping;

	public Resultado() {
		this.fechaAnalisis = LocalDateTime.now();
		this.estado = "PENDIENTE";
	}

	public Resultado(String nombreIA, String modelo, Long idScraping) {
		this();
		this.nombreIA = nombreIA;
		this.modelo = modelo;
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

	@Override
	public int hashCode() {
		return Objects.hash(idResultado, nombreIA);
	}

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

	@Override
	public String toString() {
		return "Resultado [idResultado=" + idResultado + ", nombreIA=" + nombreIA + ", modelo=" + modelo
				+ ", fechaAnalisis=" + fechaAnalisis + ", estado=" + estado + ", idScraping=" + idScraping + "]";
	}
}