package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.proyectomodulotres.dto.ReporteDTO;
import co.edu.unbosque.proyectomodulotres.entity.Reporte;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.repository.ReporteRepository;
import co.edu.unbosque.proyectomodulotres.repository.ScrapingRepository;

/**
 * Pruebas unitarias de {@link ReporteService} con los repositorios simulados.
 */
@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

	/** Repositorio de reportes simulado. */
	@Mock
	private ReporteRepository reporteRepository;

	/** Repositorio de scrapings simulado. */
	@Mock
	private ScrapingRepository scrapingRepository;

	/** Servicio de resultados simulado. */
	@Mock
	private ResultadoService resultadoService;

	/** Servicio que se prueba. */
	@InjectMocks
	private ReporteService reporteService;

	/** Generar el PDF con un id inválido no se acepta. */
	@Test
	void generarPDFConIdInvalidoLanzaError() {
		assertThrows(DatoInvalidoException.class, () -> reporteService.generarPDF(0L));
	}

	/** Generar el PDF de un scraping que no existe lanza recurso no encontrado. */
	@Test
	void generarPDFDeScrapingInexistenteLanzaError() {

		when(scrapingRepository.findById(4L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> reporteService.generarPDF(4L));
	}

	/** Descargar un reporte que no existe lanza recurso no encontrado. */
	@Test
	void descargarReporteInexistenteLanzaError() {

		when(reporteRepository.findById(6L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> reporteService.descargarPDF(6L));
	}

	/** Descargar un reporte cuyo archivo ya no existe lanza recurso no encontrado. */
	@Test
	void descargarReporteSinArchivoLanzaError() {

		Reporte reporte = new Reporte("reporte.pdf", "ruta/que/no/existe/reporte.pdf", 1L);

		when(reporteRepository.findById(6L)).thenReturn(Optional.of(reporte));

		assertThrows(RecursoNoEncontradoException.class, () -> reporteService.descargarPDF(6L));
	}

	/** Guardar un reporte sin nombre de archivo no se acepta. */
	@Test
	void guardarReporteSinNombreLanzaError() {
		assertThrows(DatoInvalidoException.class,
				() -> reporteService.guardar(new ReporteDTO(null, " ", "ruta.pdf", null, null, 1L)));
	}

	/** Guardar un reporte válido conserva sus datos. */
	@Test
	void guardarReporteValido() {

		when(reporteRepository.save(any(Reporte.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		ReporteDTO guardado = reporteService
				.guardar(new ReporteDTO(null, "reporte.pdf", "archivos/reporte.pdf", null, "GENERADO", 1L));

		assertEquals("reporte.pdf", guardado.getNombreArchivo());
		assertEquals("GENERADO", guardado.getEstado());
	}
}
