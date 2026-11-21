package co.edu.unbosque.proyectomodulotres.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "historial")
public class Historial {

	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idHistorial;

	private LocalDateTime fechaConsulta;

	private String descripcion;

	private String estado;

	private Long idUsuario;

	private Long idScraping;

	public Historial() {
		this.fechaConsulta = LocalDateTime.now();
		this.estado = "REGISTRADO";
	}

	public Historial(String descripcion, Long idUsuario, Long idScraping) {
		this();
		this.descripcion = descripcion;
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

	@Override
	public int hashCode() {
		return Objects.hash(idHistorial, fechaConsulta);
	}

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

	@Override
	public String toString() {
		return "Historial [idHistorial=" + idHistorial + ", fechaConsulta=" + fechaConsulta + ", descripcion="
				+ descripcion + ", estado=" + estado + ", idUsuario=" + idUsuario + ", idScraping=" + idScraping + "]";
	}
}