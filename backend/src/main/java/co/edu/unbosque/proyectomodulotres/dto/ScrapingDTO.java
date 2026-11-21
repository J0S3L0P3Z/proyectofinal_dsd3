package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

public class ScrapingDTO {

	private Long idScraping;
	private String url;
	private String tituloPagina;
	private String contenidoExtraido;
	private String datosJson;
	private int cantidadImagenes;
	private int cantidadEnlaces;
	private int cantidadPalabras;
	private String nombresPersonas;
	private String tecnologiasIdentificadas;
	private LocalDateTime fechaScraping;
	private String estado;
	private Long idUsuario;

	public ScrapingDTO() {
	}

	public ScrapingDTO(Long idScraping, String url, String tituloPagina, String contenidoExtraido, String datosJson,
			int cantidadImagenes, int cantidadEnlaces, int cantidadPalabras, String nombresPersonas,
			String tecnologiasIdentificadas, LocalDateTime fechaScraping, String estado, Long idUsuario) {
		this.idScraping = idScraping;
		this.url = url;
		this.tituloPagina = tituloPagina;
		this.contenidoExtraido = contenidoExtraido;
		this.datosJson = datosJson;
		this.cantidadImagenes = cantidadImagenes;
		this.cantidadEnlaces = cantidadEnlaces;
		this.cantidadPalabras = cantidadPalabras;
		this.nombresPersonas = nombresPersonas;
		this.tecnologiasIdentificadas = tecnologiasIdentificadas;
		this.fechaScraping = fechaScraping;
		this.estado = estado;
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
}
