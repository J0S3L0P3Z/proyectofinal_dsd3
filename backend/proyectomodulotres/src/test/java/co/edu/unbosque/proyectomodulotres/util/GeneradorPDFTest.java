package co.edu.unbosque.proyectomodulotres.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.entity.Scraping;

/**
 * Pruebas unitarias del generador de PDF. Solo genera el documento en memoria.
 */
class GeneradorPDFTest {

	/**
	 * Crea un scraping de prueba.
	 *
	 * @return Scraping con datos de ejemplo
	 */
	private Scraping crearScraping() {

		Scraping scraping = new Scraping("https://pagina.com", 1L);
		scraping.setIdScraping(1L);
		scraping.setTituloPagina("Página de prueba");
		scraping.setContenidoExtraido("# Título\n- elemento uno\n| col1 | col2 |\n|---|---|\n| a | b |\nTexto normal");
		scraping.setDatosJson("{\"titulo\":\"Página de prueba\"}");
		scraping.setEstado("COMPLETADO");

		return scraping;
	}

	/**
	 * Indica si los bytes son un PDF (empiezan por %PDF).
	 *
	 * @param bytes Bytes generados
	 * @return true si es un PDF
	 */
	private boolean esPDF(byte[] bytes) {
		return new String(bytes, 0, 4, StandardCharsets.US_ASCII).equals("%PDF");
	}

	/** Se genera un PDF válido con resultados exitosos y con error. */
	@Test
	void generaUnPDFConResultados() throws Exception {

		ResultadoDTO exitoso = new ResultadoDTO(1L, "GEMINI_GENERAL", "modelo", "## Tema\nAnálisis de la página",
				LocalDateTime.now(), "COMPLETADO", 1L);

		ResultadoDTO fallido = new ResultadoDTO(2L, "GROQ_GENERAL", "modelo", "Error de Groq. Codigo HTTP: 500",
				LocalDateTime.now(), "ERROR", 1L);

		byte[] pdf = new GeneradorPDF().generarPDF(crearScraping(), List.of(exitoso, fallido));

		assertTrue(pdf.length > 1000);
		assertTrue(esPDF(pdf));
	}

	/** Se genera un PDF aunque no haya resultados de IA ni contenido. */
	@Test
	void generaUnPDFSinResultados() throws Exception {

		Scraping scraping = new Scraping("https://pagina.com", 1L);

		byte[] pdf = new GeneradorPDF().generarPDF(scraping, List.of());

		assertEquals(true, esPDF(pdf));
	}
}
