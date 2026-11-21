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
@Table(name = "scraping")
public class Scraping {

	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idScraping;

	private String url;

	private String tituloPagina;

	@Column(columnDefinition = "LONGTEXT")
	private String contenidoExtraido;

	@Column(columnDefinition = "LONGTEXT")
	private String datosJson;

	private int cantidadImagenes;

	private int cantidadEnlaces;

	private int cantidadPalabras;

	@Column(columnDefinition = "TEXT")
	private String nombresPersonas;

	@Column(columnDefinition = "TEXT")
	private String tecnologiasIdentificadas;

	private LocalDateTime fechaScraping;

	private String estado;

	private Long idUsuario;

	public Scraping() {
		this.fechaScraping = LocalDateTime.now();
		this.estado = "PENDIENTE";
	}

	public Scraping(String url, Long idUsuario) {
		this();
		this.url = url;
		this.idUsuario = idUsuario;
	}

	public Long getIdScraping() {
		return idScraping;
	}

	public void setIdScraping(Long idScraping) {
		this.idScraping = idScraping;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTituloPagina() {
		return tituloPagina;
	}

	public void setTituloPagina(String tituloPagina) {
		this.tituloPagina = tituloPagina;
	}

	public String getContenidoExtraido() {
		return contenidoExtraido;
	}

	public void setContenidoExtraido(String contenidoExtraido) {
		this.contenidoExtraido = contenidoExtraido;
	}

	public String getDatosJson() {
		return datosJson;
	}

	public void setDatosJson(String datosJson) {
		this.datosJson = datosJson;
	}

	public int getCantidadImagenes() {
		return cantidadImagenes;
	}

	public void setCantidadImagenes(int cantidadImagenes) {
		this.cantidadImagenes = cantidadImagenes;
	}

	public int getCantidadEnlaces() {
		return cantidadEnlaces;
	}

	public void setCantidadEnlaces(int cantidadEnlaces) {
		this.cantidadEnlaces = cantidadEnlaces;
	}

	public int getCantidadPalabras() {
		return cantidadPalabras;
	}

	public void setCantidadPalabras(int cantidadPalabras) {
		this.cantidadPalabras = cantidadPalabras;
	}

	public String getNombresPersonas() {
		return nombresPersonas;
	}

	public void setNombresPersonas(String nombresPersonas) {
		this.nombresPersonas = nombresPersonas;
	}

	public String getTecnologiasIdentificadas() {
		return tecnologiasIdentificadas;
	}

	public void setTecnologiasIdentificadas(String tecnologiasIdentificadas) {
		this.tecnologiasIdentificadas = tecnologiasIdentificadas;
	}

	public LocalDateTime getFechaScraping() {
		return fechaScraping;
	}

	public void setFechaScraping(LocalDateTime fechaScraping) {
		this.fechaScraping = fechaScraping;
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

	@Override
	public int hashCode() {
		return Objects.hash(idScraping, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Scraping other = (Scraping) obj;
		return Objects.equals(idScraping, other.idScraping) && Objects.equals(url, other.url);
	}

	@Override
	public String toString() {
		return "Scraping [idScraping=" + idScraping + ", url=" + url + ", tituloPagina=" + tituloPagina
				+ ", cantidadImagenes=" + cantidadImagenes + ", cantidadEnlaces=" + cantidadEnlaces
				+ ", cantidadPalabras=" + cantidadPalabras + ", nombresPersonas=" + nombresPersonas
				+ ", tecnologiasIdentificadas=" + tecnologiasIdentificadas + ", fechaScraping=" + fechaScraping
				+ ", estado=" + estado + ", idUsuario=" + idUsuario + "]";
	}
}