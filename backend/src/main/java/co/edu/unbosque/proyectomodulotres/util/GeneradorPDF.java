package co.edu.unbosque.proyectomodulotres.util;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;

import co.edu.unbosque.proyectomodulotres.entity.Resultado;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;

public class GeneradorPDF {

	// Colores principales del documento.
	private static final Color AZUL_OSCURO = new Color(7, 26, 53);
	private static final Color AZUL_MEDIO = new Color(16, 53, 91);
	private static final Color AZUL_CLARO = new Color(38, 132, 214);
	private static final Color AZUL_FONDO = new Color(235, 243, 251);
	private static final Color GRIS_TEXTO = new Color(75, 91, 110);
	private static final Color BLANCO = Color.WHITE;

	// Formato de fecha.
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	// Genera el PDF y lo devuelve como un arreglo de bytes.
	public byte[] generarPDF(Scraping scraping, List<Resultado> resultados) throws DocumentException {

		ByteArrayOutputStream salida = new ByteArrayOutputStream();

		Document documento = new Document(PageSize.A4, 45, 45, 55, 55);

		PdfWriter.getInstance(documento, salida);

		documento.open();

		// Generar las diferentes secciones del documento.
		agregarPortada(documento, scraping);
		agregarInformacionGeneral(documento, scraping);
		agregarCaracteristicas(documento, scraping);
		agregarContenidoExtraido(documento, scraping);
		agregarDatosJSON(documento, scraping);
		agregarResultadosIA(documento, resultados);

		documento.close();

		return salida.toByteArray();
	}

	// Construye la portada del PDF.
	private void agregarPortada(Document documento, Scraping scraping) throws DocumentException {

		Paragraph espacio = new Paragraph("\n\n\n\n\n\n");
		documento.add(espacio);

		PdfPTable portada = new PdfPTable(1);
		portada.setWidthPercentage(100);

		PdfPCell celda = new PdfPCell();
		celda.setBackgroundColor(AZUL_OSCURO);
		celda.setPadding(35);
		celda.setBorder(Rectangle.NO_BORDER);
		celda.setMinimumHeight(300);

		Paragraph nombreProyecto = new Paragraph("PROYECTOMODULOTRES",
				new Font(Font.HELVETICA, 13, Font.BOLD, new Color(130, 190, 255)));

		nombreProyecto.setAlignment(Element.ALIGN_CENTER);
		celda.addElement(nombreProyecto);

		Paragraph titulo = new Paragraph("\nINFORME DE\nANÁLISIS WEB", new Font(Font.HELVETICA, 30, Font.BOLD, BLANCO));

		titulo.setAlignment(Element.ALIGN_CENTER);
		celda.addElement(titulo);

		Paragraph subtitulo = new Paragraph("\nREPORTE DE SCRAPING",
				new Font(Font.HELVETICA, 15, Font.BOLD, new Color(105, 180, 255)));

		subtitulo.setAlignment(Element.ALIGN_CENTER);
		celda.addElement(subtitulo);

		Paragraph descripcion = new Paragraph(
				"\nExtracción de información y análisis mediante inteligencia artificial.",
				new Font(Font.HELVETICA, 11, Font.NORMAL, BLANCO));

		descripcion.setAlignment(Element.ALIGN_CENTER);
		celda.addElement(descripcion);

		Paragraph fecha = new Paragraph("\n\nFecha del análisis: " + obtenerFecha(scraping.getFechaScraping()),
				new Font(Font.HELVETICA, 10, Font.NORMAL, BLANCO));

		fecha.setAlignment(Element.ALIGN_CENTER);
		celda.addElement(fecha);

		portada.addCell(celda);
		documento.add(portada);

		documento.add(new Paragraph("\n"));

		Paragraph estado = new Paragraph("ESTADO DEL SCRAPING: " + texto(scraping.getEstado()),
				new Font(Font.HELVETICA, 12, Font.BOLD, AZUL_MEDIO));

		estado.setAlignment(Element.ALIGN_CENTER);
		documento.add(estado);

		documento.newPage();
	}

	// Agrega la información general de la página analizada.
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

	// Presenta las características detectadas durante el scraping.
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

	// Agrega el contenido de texto extraído de la página.
	private void agregarContenidoExtraido(Document documento, Scraping scraping) throws DocumentException {

		agregarTitulo(documento, "03. CONTENIDO EXTRAÍDO");

		String contenido = texto(scraping.getContenidoExtraido());

		Paragraph parrafo = new Paragraph(contenido, new Font(Font.HELVETICA, 9, Font.NORMAL, GRIS_TEXTO));

		parrafo.setAlignment(Element.ALIGN_JUSTIFIED);
		parrafo.setLeading(14);

		documento.add(parrafo);
		documento.add(new Paragraph("\n"));
	}

	// Agrega los datos JSON obtenidos durante la extracción.
	private void agregarDatosJSON(Document documento, Scraping scraping) throws DocumentException {

		agregarTitulo(documento, "04. DATOS ESTRUCTURADOS JSON");

		String json = texto(scraping.getDatosJson());

		PdfPTable tabla = crearTabla(1);

		PdfPCell celda = new PdfPCell(new Phrase(json, new Font(Font.COURIER, 8, Font.NORMAL, AZUL_MEDIO)));

		celda.setBackgroundColor(AZUL_FONDO);
		celda.setPadding(12);
		celda.setBorderColor(new Color(210, 224, 240));

		tabla.addCell(celda);

		documento.add(tabla);
		documento.add(new Paragraph("\n"));
	}

	// Presenta los resultados de los tres modelos de inteligencia artificial.
	private void agregarResultadosIA(Document documento, List<Resultado> resultados) throws DocumentException {

		agregarTitulo(documento, "05. ANÁLISIS DE INTELIGENCIA ARTIFICIAL");

		if (resultados == null || resultados.isEmpty()) {

			documento.add(new Paragraph("No se encontraron resultados de inteligencia artificial.",
					new Font(Font.HELVETICA, 10, Font.ITALIC, GRIS_TEXTO)));

			return;
		}

		int numero = 1;

		for (Resultado resultado : resultados) {

			PdfPTable tabla = crearTabla(2);

			agregarFila(tabla, "MODELO DE INTELIGENCIA ARTIFICIAL", texto(resultado.getNombreIA()));

			agregarFila(tabla, "MODELO UTILIZADO", texto(resultado.getModelo()));

			agregarFila(tabla, "FECHA DEL ANÁLISIS", obtenerFecha(resultado.getFechaAnalisis()));

			agregarFila(tabla, "ESTADO", texto(resultado.getEstado()));

			documento.add(tabla);

			Paragraph tituloRespuesta = new Paragraph("\nRESPUESTA DEL MODELO " + numero,
					new Font(Font.HELVETICA, 11, Font.BOLD, AZUL_MEDIO));

			documento.add(tituloRespuesta);

			Paragraph respuesta = new Paragraph(texto(resultado.getRespuesta()),
					new Font(Font.HELVETICA, 9, Font.NORMAL, GRIS_TEXTO));

			respuesta.setAlignment(Element.ALIGN_JUSTIFIED);
			respuesta.setLeading(14);

			documento.add(respuesta);
			documento.add(new Paragraph("\n"));

			numero++;
		}
	}

	// Crea un título de sección con diseño azul oscuro.
	private void agregarTitulo(Document documento, String titulo) throws DocumentException {

		PdfPTable tabla = crearTabla(1);

		PdfPCell celda = new PdfPCell(new Phrase(titulo, new Font(Font.HELVETICA, 12, Font.BOLD, BLANCO)));

		celda.setBackgroundColor(AZUL_OSCURO);
		celda.setPadding(10);
		celda.setBorder(Rectangle.NO_BORDER);

		tabla.addCell(celda);

		documento.add(tabla);
		documento.add(new Paragraph("\n"));
	}

	// Crea una tabla con los anchos de columna correspondientes.
	private PdfPTable crearTabla(int columnas) throws DocumentException {

		PdfPTable tabla = new PdfPTable(columnas);

		tabla.setWidthPercentage(100);
		tabla.setSpacingAfter(10);

		if (columnas == 2) {
			tabla.setWidths(new float[] { 1.2f, 2.8f });
		}

		return tabla;
	}

	// Agrega una fila con su etiqueta y su valor.
	private void agregarFila(PdfPTable tabla, String etiqueta, String valor) {

		PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, new Font(Font.HELVETICA, 9, Font.BOLD, BLANCO)));

		celdaEtiqueta.setBackgroundColor(AZUL_MEDIO);
		celdaEtiqueta.setPadding(9);
		celdaEtiqueta.setBorderColor(new Color(210, 224, 240));

		PdfPCell celdaValor = new PdfPCell(new Phrase(valor, new Font(Font.HELVETICA, 9, Font.NORMAL, GRIS_TEXTO)));

		celdaValor.setBackgroundColor(AZUL_FONDO);
		celdaValor.setPadding(9);
		celdaValor.setBorderColor(new Color(210, 224, 240));

		tabla.addCell(celdaEtiqueta);
		tabla.addCell(celdaValor);
	}

	// Devuelve un texto que evita valores nulos.
	private String texto(String valor) {

		if (valor == null || valor.trim().isEmpty()) {
			return "No disponible";
		}

		return valor;
	}

	// Convierte la fecha a un formato legible.
	private String obtenerFecha(java.time.LocalDateTime fecha) {

		if (fecha == null) {
			return "No disponible";
		}

		return fecha.format(FORMATO_FECHA);
	}
}
