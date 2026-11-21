package co.edu.unbosque.proyectomodulotres.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reportes")
public class Reporte {

	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idReporte;

	private String nombreArchivo;

	private String rutaArchivo;

	private LocalDateTime fechaGeneracion;

	private String estado;

	private Long idScraping;

	public Reporte() {
		this.fechaGeneracion = LocalDateTime.now();
		this.estado = "PENDIENTE";
	}

	public Reporte(String nombreArchivo, String rutaArchivo, Long idScraping) {
		this();
		this.nombreArchivo = nombreArchivo;
		this.rutaArchivo = rutaArchivo;
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

	@Override
	public int hashCode() {
		return Objects.hash(idReporte, nombreArchivo);
	}

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

	@Override
	public String toString() {
		return "Reporte [idReporte=" + idReporte + ", nombreArchivo=" + nombreArchivo + ", rutaArchivo=" + rutaArchivo
				+ ", fechaGeneracion=" + fechaGeneracion + ", estado=" + estado + ", idScraping=" + idScraping + "]";
	}
}