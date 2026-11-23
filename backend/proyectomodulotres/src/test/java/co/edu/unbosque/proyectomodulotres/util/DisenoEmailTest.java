package co.edu.unbosque.proyectomodulotres.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las plantillas de correo.
 */
class DisenoEmailTest {

	/** El HTML escapa los datos del usuario para evitar inyección de código. */
	@Test
	void elHTMLEscapaLosDatos() {

		String html = DisenoEmail.crearHTML("<script>alert(1)</script>", "https://pagina.com", "COMPLETADO",
				"detalle");

		assertFalse(html.contains("<script>"));
		assertTrue(html.contains("&lt;script&gt;"));
	}

	/** El estado COMPLETADO se muestra en verde. */
	@Test
	void estadoCompletadoSaleEnVerde() {
		assertTrue(DisenoEmail.crearHTML("Ana", "https://pagina.com", "COMPLETADO", "ok").contains("#198754"));
	}

	/** Un estado de error se muestra en rojo. */
	@Test
	void estadoErrorSaleEnRojo() {
		assertTrue(DisenoEmail.crearHTML("Ana", "https://pagina.com", "ERROR", "falló").contains("#dc3545"));
	}

	/** El correo de recuperación incluye el código. */
	@Test
	void correoDeRecuperacionIncluyeElCodigo() {

		assertTrue(DisenoEmail.crearHTMLRecuperacion("Ana", "123456").contains("123456"));
		assertTrue(DisenoEmail.crearTextoPlanoRecuperacion("Ana", "123456").contains("123456"));
	}

	/** Los valores nulos no rompen la plantilla. */
	@Test
	void valoresNulosNoRompenLaPlantilla() {
		assertTrue(DisenoEmail.crearHTML(null, null, null, null).contains("ScrapingDoo!"));
	}
}
