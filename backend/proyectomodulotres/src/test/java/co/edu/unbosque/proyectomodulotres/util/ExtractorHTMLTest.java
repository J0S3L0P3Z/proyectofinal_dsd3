package co.edu.unbosque.proyectomodulotres.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de {@link ExtractorHTML}, que lee las páginas cuando no hay navegador.
 */
class ExtractorHTMLTest {

	/** Página de ejemplo con scripts, estilos, comentarios y entidades. */
	private static final String HTML = """
			<!DOCTYPE html>
			<html>
			<head>
			  <title>  Universidad &amp; Tecnolog&iacute;a  </title>
			  <style>.oculto { color: red; }</style>
			  <script>var secreto = "no debe salir";</script>
			</head>
			<body>
			  <!-- <img src="comentada.png"> -->
			  <h1>Bienvenidos</h1>
			  <p>Somos el equipo de <b>Ana G&oacute;mez</b> y Luis&nbsp;P&eacute;rez.</p>
			  <img src="a.png"><IMG src="b.png"/>
			  <a href="/uno">Uno</a> <a href="/dos">Dos</a> <abbr>no es enlace</abbr>
			  <noscript>Activa JavaScript</noscript>
			  <ul><li>Angular</li><li>Spring Boot</li></ul>
			  <p>Precio &#36;10 &#x2014; oferta</p>
			</body>
			</html>
			""";

	/** El título se limpia y decodifica las entidades. */
	@Test
	void obtieneElTitulo() {
		assertEquals("Universidad & Tecnología", ExtractorHTML.obtenerTitulo(HTML));
	}

	/** Una página sin title devuelve texto vacío. */
	@Test
	void sinTituloDevuelveVacio() {
		assertEquals("", ExtractorHTML.obtenerTitulo("<html><body>Hola</body></html>"));
		assertEquals("", ExtractorHTML.obtenerTitulo(null));
	}

	/** Cuenta imágenes y enlaces sin importar mayúsculas, sin contar comentarios ni etiquetas parecidas. */
	@Test
	void cuentaImagenesYEnlaces() {
		assertEquals(2, ExtractorHTML.contarEtiquetas(HTML, "img"));
		assertEquals(2, ExtractorHTML.contarEtiquetas(HTML, "a"));
		assertEquals(0, ExtractorHTML.contarEtiquetas(null, "a"));
	}

	/** El texto conserva lo visible y quita scripts, estilos, noscript y etiquetas. */
	@Test
	void obtieneSoloElTextoVisible() {

		String texto = ExtractorHTML.obtenerTexto(HTML);

		assertTrue(texto.contains("Bienvenidos"));
		assertTrue(texto.contains("Somos el equipo de Ana Gómez y Luis Pérez."));
		assertTrue(texto.contains("Spring Boot"));
		assertTrue(texto.contains("Precio $10 — oferta"));
		assertFalse(texto.contains("no debe salir"));
		assertFalse(texto.contains("color: red"));
		assertFalse(texto.contains("Activa JavaScript"));
		assertFalse(texto.contains("<"));
	}

	/** Cada bloque queda en su propia línea y no quedan líneas vacías. */
	@Test
	void separaElTextoEnLineas() {

		String texto = ExtractorHTML.obtenerTexto(HTML);

		assertTrue(texto.contains("Angular\nSpring Boot"));
		assertFalse(texto.contains("\n\n"));
	}

	/** Un HTML vacío devuelve texto vacío. */
	@Test
	void htmlVacioDevuelveVacio() {
		assertEquals("", ExtractorHTML.obtenerTexto("   "));
		assertEquals("", ExtractorHTML.obtenerTexto(null));
	}
}
