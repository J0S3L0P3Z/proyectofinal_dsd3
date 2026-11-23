package co.edu.unbosque.proyectomodulotres.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;

/**
 * Genera el informe PDF de un scraping con OpenPDF: portada, información general, características
 * detectadas, contenido extraído, datos JSON y el análisis de cada IA.
 */
public class GeneradorPDF {

	/** Colores usados en el diseño del informe. */
	private static final Color CAFE_OSCURO = new Color(62, 39, 27);
	private static final Color CAFE_MEDIO = new Color(121, 85, 61);

	private static final Color CREMA = new Color(250, 244, 236);
	private static final Color ARENA = new Color(240, 228, 214);
	private static final Color BORDE = new Color(214, 198, 182);
	private static final Color TEXTO = new Color(70, 52, 42);

	private static final Color AGUAMARINA = new Color(64, 201, 162);
	private static final Color AGUAMARINA_OSCURO = new Color(32, 169, 135);
	private static final Color CELESTE = new Color(224, 246, 239);

	private static final Color BLANCO = Color.WHITE;
	private static final Color ROJO = new Color(190, 45, 45);

	/** Ancho de la página A4. */
	private static final float ANCHO = PageSize.A4.getWidth();

	/** Alto de la página A4. */
	private static final float ALTO = PageSize.A4.getHeight();

	/** Formato de las fechas del informe. */
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	/** Ruta de la fuente de los títulos dentro de los recursos. */
	private static final String RUTA_FUENTE = "/fonts/Fredoka-Bold.ttf";

	/** Fuente de los títulos (null si no se pudo cargar). */
	private BaseFont fuenteDivertida;

	/** Indica si ya se intentó cargar la fuente de los títulos. */
	private boolean fuenteIntentada = false;

	/**
	 * Devuelve la fuente de los títulos. Si no se encuentra el archivo usa Helvetica en negrita.
	 *
	 * @param tamano Tamaño de la fuente
	 * @param color Color del texto
	 * @return Fuente lista para usar
	 */
	private Font divertida(float tamano, Color color) {

		if (!fuenteIntentada) {

			fuenteIntentada = true;

			try (InputStream entrada = GeneradorPDF.class.getResourceAsStream(RUTA_FUENTE)) {

				if (entrada != null) {

					byte[] bytes = entrada.readAllBytes();

					fuenteDivertida = BaseFont.createFont("Fredoka-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,
							true, bytes, null);
				}

			} catch (Exception e) {
				fuenteDivertida = null;
			}
		}

		if (fuenteDivertida == null) {
			return new Font(Font.HELVETICA, tamano, Font.BOLD, color);
		}

		return new Font(fuenteDivertida, tamano, Font.NORMAL, color);
	}

	/**
	 * Genera el informe PDF completo de un scraping.
	 *
	 * @param scraping Scraping del que se genera el informe
	 * @param resultados Resultados de las IA
	 * @return Bytes del PDF
	 * @throws DocumentException Si ocurre un error al armar el documento
	 */
	public byte[] generarPDF(Scraping scraping, List<ResultadoDTO> resultados) throws DocumentException {

		ByteArrayOutputStream salida = new ByteArrayOutputStream();

		Document documento = new Document(PageSize.A4, 45, 45, 65, 55);

		PdfWriter writer = PdfWriter.getInstance(documento, salida);

		writer.setPageEvent(new EventoPagina());

		documento.open();

		agregarPortada(documento, scraping);
		agregarInformacionGeneral(documento, scraping);
		agregarCaracteristicas(documento, scraping);
		agregarContenidoExtraido(documento, scraping);
		agregarDatosJSON(documento, scraping);
		agregarResultadosIA(documento, resultados);

		documento.close();

		return salida.toByteArray();
	}

	/**
	 * Evento de página que dibuja el encabezado y el pie en todas las páginas menos en la portada.
	 */
	private static class EventoPagina extends PdfPageEventHelper {

		/**
		 * Dibuja el encabezado, la línea del pie y el número de página al terminar cada página.
		 *
		 * @param writer Escritor del PDF
		 * @param document Documento que se está generando
		 */
		@Override
		public void onEndPage(PdfWriter writer, Document document) {

			if (writer.getPageNumber() == 1) {
				return;
			}

			PdfContentByte lienzo = writer.getDirectContent();

			lienzo.setColorFill(CAFE_OSCURO);
			lienzo.rectangle(0, ALTO - 20, ANCHO, 20);
			lienzo.fill();

			ColumnText.showTextAligned(lienzo, Element.ALIGN_LEFT,
					new Phrase("SCRAPINGDOO | Informe de análisis web", new Font(Font.HELVETICA, 8, Font.BOLD, BLANCO)),
					45, ALTO - 14, 0);

			lienzo.setColorStroke(AGUAMARINA);
			lienzo.setLineWidth(1.5f);
			lienzo.moveTo(45, 40);
			lienzo.lineTo(ANCHO - 45, 40);
			lienzo.stroke();

			ColumnText.showTextAligned(lienzo, Element.ALIGN_LEFT,
					new Phrase("ScrapingDoo!", new Font(Font.HELVETICA, 8, Font.BOLD, CAFE_MEDIO)), 45, 26, 0);

			ColumnText.showTextAligned(lienzo, Element.ALIGN_RIGHT, new Phrase("Página " + (writer.getPageNumber() - 1),
					new Font(Font.HELVETICA, 8, Font.BOLD, CAFE_OSCURO)), ANCHO - 45, 26, 0);
		}
	}

	/**
	 * Agrega la portada con el encabezado, el estado, la fecha y el identificador.
	 *
	 * @param documento Documento PDF
	 * @param scraping Scraping del informe
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarPortada(Document documento, Scraping scraping) throws DocumentException {

		documento.add(new Paragraph("\n\n\n\n"));

		PdfPTable encabezado = crearTabla(1);

		PdfPCell celdaEncabezado = new PdfPCell();

		celdaEncabezado.setBackgroundColor(CAFE_OSCURO);
		celdaEncabezado.setBorder(Rectangle.NO_BORDER);
		celdaEncabezado.setPadding(30);
		celdaEncabezado.setPaddingTop(35);
		celdaEncabezado.setPaddingBottom(30);

		Paragraph marca = new Paragraph("SCRAPINGDOO!", divertida(17, AGUAMARINA));

		marca.setSpacingAfter(18);
		celdaEncabezado.addElement(marca);

		Paragraph titulo = new Paragraph("Informe de análisis web", divertida(27, BLANCO));

		titulo.setLeading(34);
		titulo.setSpacingAfter(12);
		celdaEncabezado.addElement(titulo);

		Paragraph subtitulo = new Paragraph("Reporte de scraping e inteligencia artificial",
				new Font(Font.HELVETICA, 11, Font.NORMAL, CREMA));

		celdaEncabezado.addElement(subtitulo);

		encabezado.addCell(celdaEncabezado);

		documento.add(encabezado);

		PdfPTable collar = crearTabla(1);

		PdfPCell franja = new PdfPCell(new Phrase(" "));
		franja.setFixedHeight(20);
		franja.setPadding(0);
		franja.setBorder(Rectangle.NO_BORDER);
		franja.setBackgroundColor(AGUAMARINA);

		collar.addCell(franja);

		PdfPCell linea = new PdfPCell(new Phrase(" "));
		linea.setFixedHeight(6);
		linea.setPadding(0);
		linea.setBorder(Rectangle.NO_BORDER);
		linea.setBackgroundColor(AGUAMARINA_OSCURO);

		collar.addCell(linea);

		collar.setSpacingAfter(35);

		documento.add(collar);

		agregarDatoPortada(documento, "ESTADO DEL PROCESO", texto(scraping.getEstado()), AGUAMARINA_OSCURO, 18);

		agregarDatoPortada(documento, "FECHA DEL ANÁLISIS", obtenerFecha(scraping.getFechaScraping()), CAFE_OSCURO, 11);

		agregarDatoPortada(documento, "IDENTIFICADOR", String.valueOf(scraping.getIdScraping()), CAFE_OSCURO, 11);

		documento.newPage();
	}

	/**
	 * Agrega en la portada una etiqueta pequeña con su valor debajo.
	 *
	 * @param documento Documento PDF
	 * @param etiqueta Texto de la etiqueta
	 * @param valor Valor a mostrar
	 * @param colorValor Color del valor
	 * @param tamano Tamaño de la fuente del valor
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarDatoPortada(Document documento, String etiqueta, String valor, Color colorValor, int tamano)
			throws DocumentException {

		Paragraph textoEtiqueta = new Paragraph(etiqueta, new Font(Font.HELVETICA, 8, Font.BOLD, CAFE_MEDIO));

		textoEtiqueta.setSpacingAfter(3);
		documento.add(textoEtiqueta);

		Paragraph textoValor = new Paragraph(valor, divertida(tamano, colorValor));

		textoValor.setSpacingAfter(18);
		documento.add(textoValor);
	}

	/**
	 * Agrega la sección de información general (URL, título, fecha, estado e identificador).
	 *
	 * @param documento Documento PDF
	 * @param scraping Scraping del informe
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarInformacionGeneral(Document documento, Scraping scraping) throws DocumentException {

		agregarTitulo(documento, "01. INFORMACIÓN GENERAL");

		PdfPTable tabla = crearTabla(2);

		agregarFila(tabla, "URL ANALIZADA", texto(scraping.getUrl()));
		agregarFila(tabla, "TÍTULO DE LA PÁGINA", texto(scraping.getTituloPagina()));
		agregarFila(tabla, "FECHA DEL ANÁLISIS", obtenerFecha(scraping.getFechaScraping()));
		agregarFila(tabla, "ESTADO", texto(scraping.getEstado()));
		agregarFila(tabla, "IDENTIFICADOR", String.valueOf(scraping.getIdScraping()));

		documento.add(tabla);
		documento.add(new Paragraph("\n"));
	}

	/**
	 * Agrega la sección de características detectadas (imágenes, enlaces, palabras, personas y
	 * tecnologías).
	 *
	 * @param documento Documento PDF
	 * @param scraping Scraping del informe
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarCaracteristicas(Document documento, Scraping scraping) throws DocumentException {

		agregarTitulo(documento, "02. CARACTERÍSTICAS DETECTADAS");

		PdfPTable tabla = crearTabla(2);

		agregarFila(tabla, "IMÁGENES DETECTADAS", String.valueOf(scraping.getCantidadImagenes()));
		agregarFila(tabla, "ENLACES DETECTADOS", String.valueOf(scraping.getCantidadEnlaces()));
		agregarFila(tabla, "PALABRAS EXTRAÍDAS", String.valueOf(scraping.getCantidadPalabras()));
		agregarFila(tabla, "PERSONAS IDENTIFICADAS", texto(scraping.getNombresPersonas()));
		agregarFila(tabla, "TECNOLOGÍAS IDENTIFICADAS", texto(scraping.getTecnologiasIdentificadas()));

		documento.add(tabla);
		documento.add(new Paragraph("\n"));
	}

	/**
	 * Agrega la sección con el texto extraído de la página.
	 *
	 * @param documento Documento PDF
	 * @param scraping Scraping del informe
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarContenidoExtraido(Document documento, Scraping scraping) throws DocumentException {

		agregarTitulo(documento, "03. CONTENIDO EXTRAÍDO");

		String contenido = texto(scraping.getContenidoExtraido());

		agregarTextoEnCaja(documento, contenido, 9);

		documento.add(new Paragraph("\n"));
	}

	/**
	 * Agrega la sección con los datos en JSON, formateados si es posible.
	 *
	 * @param documento Documento PDF
	 * @param scraping Scraping del informe
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarDatosJSON(Document documento, Scraping scraping) throws DocumentException {

		agregarTitulo(documento, "04. DATOS ESTRUCTURADOS JSON");

		String json = texto(scraping.getDatosJson());

		try {

			ObjectMapper mapper = new ObjectMapper();

			Object objeto = mapper.readValue(json, Object.class);

			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objeto);

		} catch (Exception e) {
			// Si no es un JSON válido se muestra el texto tal como está.
		}

		PdfPTable tabla = crearTabla(1);

		PdfPCell celda = new PdfPCell(new Phrase(json, new Font(Font.COURIER, 8, Font.NORMAL, CAFE_OSCURO)));

		celda.setBackgroundColor(CELESTE);
		celda.setPadding(12);
		celda.setBorderColor(AGUAMARINA);
		celda.setBorderWidth(0.8f);
		celda.setBorderWidthLeft(4f);
		celda.setLeading(12f, 1.2f);

		tabla.addCell(celda);

		documento.add(tabla);
		documento.add(new Paragraph("\n"));
	}

	/**
	 * Agrega la sección con el análisis de cada IA. Las respuestas con error se muestran en rojo.
	 *
	 * @param documento Documento PDF
	 * @param resultados Resultados de las IA
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarResultadosIA(Document documento, List<ResultadoDTO> resultados) throws DocumentException {

		agregarTitulo(documento, "05. ANÁLISIS DE INTELIGENCIA ARTIFICIAL");

		if (resultados == null || resultados.isEmpty()) {

			documento.add(new Paragraph("No se encontraron resultados de inteligencia artificial.",
					new Font(Font.HELVETICA, 10, Font.ITALIC, TEXTO)));

			return;
		}

		int numero = 1;

		for (ResultadoDTO resultado : resultados) {

			agregarSubtitulo(documento, "ANÁLISIS " + numero + " - " + texto(resultado.getNombreIA()));

			PdfPTable tabla = crearTabla(2);

			agregarFila(tabla, "INTELIGENCIA ARTIFICIAL", texto(resultado.getNombreIA()));
			agregarFila(tabla, "MODELO UTILIZADO", texto(resultado.getModelo()));
			agregarFila(tabla, "FECHA DEL ANÁLISIS", obtenerFecha(resultado.getFechaAnalisis()));
			agregarFila(tabla, "ESTADO", texto(resultado.getEstado()));

			documento.add(tabla);

			Paragraph tituloRespuesta = new Paragraph("RESPUESTA", divertida(10, AGUAMARINA_OSCURO));

			tituloRespuesta.setSpacingBefore(5);
			tituloRespuesta.setSpacingAfter(6);

			documento.add(tituloRespuesta);

			String respuesta = texto(resultado.getRespuesta());

			if (respuesta.startsWith("Error")) {

				PdfPTable tablaError = crearTabla(1);

				PdfPCell celdaError = new PdfPCell(
						new Phrase(respuesta, new Font(Font.HELVETICA, 9, Font.NORMAL, ROJO)));

				celdaError.setBackgroundColor(ARENA);
				celdaError.setPadding(10);
				celdaError.setBorderColor(BORDE);

				tablaError.addCell(celdaError);

				documento.add(tablaError);

			} else {

				agregarTextoEnCaja(documento, respuesta, 9);
			}

			documento.add(new Paragraph("\n"));

			numero++;
		}
	}

	/**
	 * Agrega un texto dentro de una caja con borde, organizado en títulos, viñetas y párrafos.
	 *
	 * @param documento Documento PDF
	 * @param contenido Texto a mostrar
	 * @param tamano Tamaño de la fuente
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarTextoEnCaja(Document documento, String contenido, int tamano) throws DocumentException {

		PdfPTable tabla = crearTabla(1);

		tabla.setSplitLate(false);

		PdfPCell celda = new PdfPCell();

		celda.setBackgroundColor(CREMA);
		celda.setPadding(12);
		celda.setBorderColor(BORDE);
		celda.setBorderWidth(0.8f);
		celda.setBorderColorLeft(AGUAMARINA);
		celda.setBorderWidthLeft(4f);

		for (Element elemento : construirTextoOrganizado(contenido, tamano)) {
			celda.addElement(elemento);
		}

		tabla.addCell(celda);

		documento.add(tabla);
	}

	/**
	 * Convierte un texto con Markdown sencillo (títulos, viñetas y tablas) en párrafos del PDF. Las
	 * filas de tablas se muestran como texto separado por guiones.
	 *
	 * @param contenido Texto a convertir
	 * @param tamano Tamaño de la fuente
	 * @return Lista de elementos para agregar al PDF
	 */
	private List<Element> construirTextoOrganizado(String contenido, int tamano) {

		List<Element> elementos = new ArrayList<>();

		if (contenido == null || contenido.trim().isEmpty()) {

			elementos.add(new Paragraph("No hay contenido disponible.",
					new Font(Font.HELVETICA, tamano, Font.ITALIC, TEXTO)));

			return elementos;
		}

		String[] lineas = contenido.split("\\R");

		for (String linea : lineas) {

			String textoLinea = linea.trim();

			if (textoLinea.isEmpty()) {

				elementos.add(new Paragraph(" ", new Font(Font.HELVETICA, 6)));

				continue;
			}

			if (textoLinea.matches("\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?")) {

				continue;
			}

			if (textoLinea.contains("|")) {

				String[] columnas = textoLinea.split("\\|");

				StringBuilder fila = new StringBuilder();

				for (String columna : columnas) {

					String valor = columna.trim();

					if (!valor.isEmpty()) {

						if (fila.length() > 0) {
							fila.append(" - ");
						}

						fila.append(limpiarMarkdown(valor));
					}
				}

				if (fila.length() > 0) {

					Paragraph parrafo = new Paragraph(fila.toString(),
							new Font(Font.HELVETICA, tamano, Font.NORMAL, TEXTO));

					parrafo.setLeading(13);
					parrafo.setSpacingAfter(5);

					elementos.add(parrafo);
				}

			} else if (textoLinea.startsWith("### ")) {

				elementos
						.add(crearLineaEspecial(limpiarMarkdown(textoLinea.substring(4)), 10, true, AGUAMARINA_OSCURO));

			} else if (textoLinea.startsWith("## ")) {

				elementos.add(crearLineaEspecial(limpiarMarkdown(textoLinea.substring(3)), 11, true, CAFE_MEDIO));

			} else if (textoLinea.startsWith("# ")) {

				elementos.add(crearLineaEspecial(limpiarMarkdown(textoLinea.substring(2)), 13, true, CAFE_OSCURO));

			} else if (textoLinea.startsWith("- ") || textoLinea.startsWith("* ")) {

				Paragraph elemento = new Paragraph("• " + limpiarMarkdown(textoLinea.substring(2)),
						new Font(Font.HELVETICA, tamano, Font.NORMAL, TEXTO));

				elemento.setIndentationLeft(12);
				elemento.setLeading(13);
				elemento.setSpacingAfter(4);

				elementos.add(elemento);

			} else {

				Paragraph parrafo = new Paragraph(limpiarMarkdown(textoLinea),
						new Font(Font.HELVETICA, tamano, Font.NORMAL, TEXTO));

				parrafo.setAlignment(Element.ALIGN_LEFT);
				parrafo.setLeading(13);
				parrafo.setSpacingAfter(5);

				elementos.add(parrafo);
			}
		}

		return elementos;
	}

	/**
	 * Crea un párrafo de título con el tamaño y el color indicados.
	 *
	 * @param contenido Texto del título
	 * @param tamano Tamaño de la fuente
	 * @param negrita Si el texto va en negrita
	 * @param color Color del texto
	 * @return Párrafo listo para agregar
	 */
	private Paragraph crearLineaEspecial(String contenido, int tamano, boolean negrita, Color color) {

		Paragraph parrafo = new Paragraph(contenido,
				new Font(Font.HELVETICA, tamano, negrita ? Font.BOLD : Font.NORMAL, color));

		parrafo.setSpacingBefore(7);
		parrafo.setSpacingAfter(6);
		parrafo.setLeading(tamano + 4);

		return parrafo;
	}

	/**
	 * Quita las marcas de Markdown (negrita, código y tachado) de un texto.
	 *
	 * @param contenido Texto con Markdown
	 * @return Texto limpio
	 */
	private String limpiarMarkdown(String contenido) {

		String resultado = contenido;

		resultado = resultado.replace("**", "");
		resultado = resultado.replace("__", "");
		resultado = resultado.replace("`", "");
		resultado = resultado.replace("~~", "");

		return resultado;
	}

	/**
	 * Agrega el título de una sección.
	 *
	 * @param documento Documento PDF
	 * @param titulo Texto del título
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarTitulo(Document documento, String titulo) throws DocumentException {

		PdfPTable tabla = crearTabla(1);

		PdfPCell celda = new PdfPCell(new Phrase(titulo, divertida(12, BLANCO)));

		celda.setBackgroundColor(CAFE_OSCURO);
		celda.setPadding(10);
		celda.setPaddingLeft(14);
		celda.setBorder(Rectangle.BOTTOM);
		celda.setBorderColorBottom(AGUAMARINA);
		celda.setBorderWidthBottom(3.5f);

		tabla.addCell(celda);

		documento.add(tabla);
		documento.add(new Paragraph("\n"));
	}

	/**
	 * Agrega un subtítulo dentro de una sección.
	 *
	 * @param documento Documento PDF
	 * @param titulo Texto del subtítulo
	 * @throws DocumentException Si ocurre un error al agregar los elementos
	 */
	private void agregarSubtitulo(Document documento, String titulo) throws DocumentException {

		PdfPTable tabla = crearTabla(1);

		PdfPCell celda = new PdfPCell(new Phrase(titulo, divertida(10, AGUAMARINA_OSCURO)));

		celda.setBackgroundColor(CELESTE);
		celda.setPadding(8);
		celda.setPaddingLeft(12);
		celda.setBorderColor(AGUAMARINA);
		celda.setBorderWidth(0.7f);
		celda.setBorderWidthLeft(4f);

		tabla.addCell(celda);

		documento.add(tabla);
		documento.add(new Paragraph("\n"));
	}

	/**
	 * Crea una tabla al 100 % del ancho. Las de dos columnas usan la proporción etiqueta/valor.
	 *
	 * @param columnas Número de columnas
	 * @return Tabla vacía
	 * @throws DocumentException Si los anchos no son válidos
	 */
	private PdfPTable crearTabla(int columnas) throws DocumentException {

		PdfPTable tabla = new PdfPTable(columnas);

		tabla.setWidthPercentage(100);
		tabla.setSpacingAfter(10);

		if (columnas == 2) {
			tabla.setWidths(new float[] { 1.3f, 2.7f });
		}

		return tabla;
	}

	/**
	 * Agrega a una tabla de dos columnas una fila con etiqueta y valor.
	 *
	 * @param tabla Tabla de dos columnas
	 * @param etiqueta Texto de la etiqueta
	 * @param valor Valor a mostrar
	 */
	private void agregarFila(PdfPTable tabla, String etiqueta, String valor) {

		PdfPCell celdaEtiqueta = new PdfPCell(
				new Phrase(etiqueta, new Font(Font.HELVETICA, 8, Font.BOLD, CAFE_OSCURO)));

		celdaEtiqueta.setBackgroundColor(ARENA);
		celdaEtiqueta.setPadding(9);
		celdaEtiqueta.setBorderColor(BORDE);
		celdaEtiqueta.setVerticalAlignment(Element.ALIGN_MIDDLE);

		PdfPCell celdaValor = new PdfPCell(new Phrase(valor, new Font(Font.HELVETICA, 9, Font.NORMAL, TEXTO)));

		celdaValor.setBackgroundColor(BLANCO);
		celdaValor.setPadding(9);
		celdaValor.setBorderColor(BORDE);
		celdaValor.setVerticalAlignment(Element.ALIGN_MIDDLE);

		tabla.addCell(celdaEtiqueta);
		tabla.addCell(celdaValor);
	}

	/**
	 * Devuelve el valor o "No disponible" si está vacío.
	 *
	 * @param valor Texto a revisar
	 * @return El texto o "No disponible"
	 */
	private String texto(String valor) {

		if (valor == null || valor.trim().isEmpty()) {
			return "No disponible";
		}

		return valor;
	}

	/**
	 * Formatea una fecha para el informe.
	 *
	 * @param fecha Fecha a formatear
	 * @return Fecha formateada o "No disponible"
	 */
	private String obtenerFecha(LocalDateTime fecha) {

		if (fecha == null) {
			return "No disponible";
		}

		return fecha.format(FORMATO_FECHA);
	}
}