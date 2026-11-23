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
 * Entidad que representa el scraping de una página web: su contenido, sus características y las
 * personas y tecnologías identificadas por las IA.
 */
@Entity
@Table(name = "scraping")
public class Scraping {

	/** Identificador del scraping. */
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long idScraping;

	/** URL analizada. */
	private String url;

	/** Título de la página. */
	private String tituloPagina;

	/** Texto visible extraído de la página. */
	@Column(columnDefinition = "LONGTEXT")
	private String contenidoExtraido;

	/** Datos estructurados de la página en formato JSON. */
	@Column(columnDefinition = "LONGTEXT")
	private String datosJson;

	/** Cantidad de imágenes encontradas. */
	private int cantidadImagenes;

	/** Cantidad de enlaces encontrados. */
	private int cantidadEnlaces;

	/** Cantidad de palabras del contenido. */
	private int cantidadPalabras;

	/** Nombres de personas identificados por la IA. */
	@Column(columnDefinition = "TEXT")
	private String nombresPersonas;

	/** Tecnologías identificadas por la IA. */
	@Column(columnDefinition = "TEXT")
	private String tecnologiasIdentificadas;

	/** Fecha del scraping. */
	private LocalDateTime fechaScraping;

	/** Estado del scraping (PENDIENTE, COMPLETADO o ERROR). */
	private String estado;

	/** Identificador del usuario que pidió el scraping. */
	private Long idUsuario;

	/** Constructor vacío. Asigna la fecha actual y el estado PENDIENTE. */
	public Scraping() {
		this.fechaScraping = LocalDateTime.now();
		this.estado = "PENDIENTE";
	}

	/**
	 * Constructor con la URL y el usuario.
	 *
	 * @param url URL a analizar
	 * @param idUsuario Identificador del usuario
	 */
	public Scraping(String url, Long idUsuario) {
		this();
		this.url = url;
		this.idUsuario = idUsuario;
	}

	/** @return Identificador del scraping */
	public Long getIdScraping() {
		return idScraping;
	}

	/** @param idScraping Identificador del scraping */
	public void setIdScraping(Long idScraping) {
		this.idScraping = idScraping;
	}

	/** @return URL analizada */
	public String getUrl() {
		return url;
	}

	/** @param url URL analizada */
	public void setUrl(String url) {
		this.url = url;
	}

	/** @return Título de la página */
	public String getTituloPagina() {
		return tituloPagina;
	}

	/** @param tituloPagina Título de la página */
	public void setTituloPagina(String tituloPagina) {
		this.tituloPagina = tituloPagina;
	}

	/** @return Texto extraído */
	public String getContenidoExtraido() {
		return contenidoExtraido;
	}

	/** @param contenidoExtraido Texto extraído */
	public void setContenidoExtraido(String contenidoExtraido) {
		this.contenidoExtraido = contenidoExtraido;
	}

	/** @return Datos en JSON */
	public String getDatosJson() {
		return datosJson;
	}

	/** @param datosJson Datos en JSON */
	public void setDatosJson(String datosJson) {
		this.datosJson = datosJson;
	}

	/** @return Cantidad de imágenes */
	public int getCantidadImagenes() {
		return cantidadImagenes;
	}

	/** @param cantidadImagenes Cantidad de imágenes */
	public void setCantidadImagenes(int cantidadImagenes) {
		this.cantidadImagenes = cantidadImagenes;
	}

	/** @return Cantidad de enlaces */
	public int getCantidadEnlaces() {
		return cantidadEnlaces;
	}

	/** @param cantidadEnlaces Cantidad de enlaces */
	public void setCantidadEnlaces(int cantidadEnlaces) {
		this.cantidadEnlaces = cantidadEnlaces;
	}

	/** @return Cantidad de palabras */
	public int getCantidadPalabras() {
		return cantidadPalabras;
	}

	/** @param cantidadPalabras Cantidad de palabras */
	public void setCantidadPalabras(int cantidadPalabras) {
		this.cantidadPalabras = cantidadPalabras;
	}

	/** @return Nombres de personas identificados */
	public String getNombresPersonas() {
		return nombresPersonas;
	}

	/** @param nombresPersonas Nombres de personas identificados */
	public void setNombresPersonas(String nombresPersonas) {
		this.nombresPersonas = nombresPersonas;
	}

	/** @return Tecnologías identificadas */
	public String getTecnologiasIdentificadas() {
		return tecnologiasIdentificadas;
	}

	/** @param tecnologiasIdentificadas Tecnologías identificadas */
	public void setTecnologiasIdentificadas(String tecnologiasIdentificadas) {
		this.tecnologiasIdentificadas = tecnologiasIdentificadas;
	}

	/** @return Fecha del scraping */
	public LocalDateTime getFechaScraping() {
		return fechaScraping;
	}

	/** @param fechaScraping Fecha del scraping */
	public void setFechaScraping(LocalDateTime fechaScraping) {
		this.fechaScraping = fechaScraping;
	}

	/** @return Estado del scraping */
	public String getEstado() {
		return estado;
	}

	/** @param estado Estado del scraping */
	public void setEstado(String estado) {
		this.estado = estado;
	}

	/** @return Identificador del usuario */
	public Long getIdUsuario() {
		return idUsuario;
	}

	/** @param idUsuario Identificador del usuario */
	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	/** @return Código hash calculado con el identificador y la URL */
	@Override
	public int hashCode() {
		return Objects.hash(idScraping, url);
	}

	/**
	 * Compara dos scrapings por identificador y URL.
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
		Scraping other = (Scraping) obj;
		return Objects.equals(idScraping, other.idScraping) && Objects.equals(url, other.url);
	}

	/** @return Texto con los datos principales del scraping */
	@Override
	public String toString() {
		return "Scraping [idScraping=" + idScraping + ", url=" + url + ", tituloPagina=" + tituloPagina
				+ ", cantidadImagenes=" + cantidadImagenes + ", cantidadEnlaces=" + cantidadEnlaces
				+ ", cantidadPalabras=" + cantidadPalabras + ", nombresPersonas=" + nombresPersonas
				+ ", tecnologiasIdentificadas=" + tecnologiasIdentificadas + ", fechaScraping=" + fechaScraping
				+ ", estado=" + estado + ", idUsuario=" + idUsuario + "]";
	}
}
