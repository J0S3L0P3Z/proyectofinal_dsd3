package co.edu.unbosque.proyectomodulotres.dto;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia con los datos de un scraping. Para ejecutar un scraping basta con enviar
 * la URL y el identificador del usuario.
 */
public class ScrapingDTO {

	/** Identificador del scraping. */
	private Long idScraping;

	/** URL analizada. */
	private String url;

	/** Título de la página. */
	private String tituloPagina;

	/** Texto extraído de la página. */
	private String contenidoExtraido;

	/** Datos estructurados en JSON. */
	private String datosJson;

	/** Cantidad de imágenes. */
	private int cantidadImagenes;

	/** Cantidad de enlaces. */
	private int cantidadEnlaces;

	/** Cantidad de palabras. */
	private int cantidadPalabras;

	/** Nombres de personas identificados. */
	private String nombresPersonas;

	/** Tecnologías identificadas. */
	private String tecnologiasIdentificadas;

	/** Fecha del scraping. */
	private LocalDateTime fechaScraping;

	/** Estado del scraping. */
	private String estado;

	/** Identificador del usuario. */
	private Long idUsuario;

	/** Constructor vacío. */
	public ScrapingDTO() {
	}

	/**
	 * Constructor con todos los datos.
	 *
	 * @param idScraping Identificador del scraping
	 * @param url URL analizada
	 * @param tituloPagina Título de la página
	 * @param contenidoExtraido Texto extraído
	 * @param datosJson Datos en JSON
	 * @param cantidadImagenes Cantidad de imágenes
	 * @param cantidadEnlaces Cantidad de enlaces
	 * @param cantidadPalabras Cantidad de palabras
	 * @param nombresPersonas Nombres de personas identificados
	 * @param tecnologiasIdentificadas Tecnologías identificadas
	 * @param fechaScraping Fecha del scraping
	 * @param estado Estado del scraping
	 * @param idUsuario Identificador del usuario
	 */
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
}
