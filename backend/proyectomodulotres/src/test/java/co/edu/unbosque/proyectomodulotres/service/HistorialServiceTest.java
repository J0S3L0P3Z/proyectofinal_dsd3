package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.proyectomodulotres.dto.HistorialDTO;
import co.edu.unbosque.proyectomodulotres.entity.Historial;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.repository.HistorialRepository;

/**
 * Pruebas unitarias de {@link HistorialService} con el repositorio simulado.
 */
@ExtendWith(MockitoExtension.class)
class HistorialServiceTest {

	/** Repositorio del historial simulado. */
	@Mock
	private HistorialRepository historialRepository;

	/** Servicio que se prueba. */
	@InjectMocks
	private HistorialService historialService;

	/** Guardar un registro válido devuelve sus datos. */
	@Test
	void guardarRegistroValido() {

		when(historialRepository.save(any(Historial.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		HistorialDTO guardado = historialService
				.guardar(new HistorialDTO(null, null, "Scraping realizado", "COMPLETADO", 5L, 1L));

		assertEquals("Scraping realizado", guardado.getDescripcion());
		assertEquals("COMPLETADO", guardado.getEstado());
		assertEquals(5L, guardado.getIdUsuario());
	}

	/** Sin estado el registro queda como REGISTRADO. */
	@Test
	void guardarSinEstadoQuedaRegistrado() {

		when(historialRepository.save(any(Historial.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		HistorialDTO guardado = historialService.guardar(new HistorialDTO(null, null, "Consulta", null, 5L, 1L));

		assertEquals("REGISTRADO", guardado.getEstado());
	}

	/** No se puede guardar un registro sin descripción. */
	@Test
	void guardarSinDescripcionLanzaError() {
		assertThrows(DatoInvalidoException.class,
				() -> historialService.guardar(new HistorialDTO(null, null, " ", null, 5L, 1L)));
	}

	/** No se puede guardar un registro sin usuario. */
	@Test
	void guardarSinUsuarioLanzaError() {
		assertThrows(DatoInvalidoException.class,
				() -> historialService.guardar(new HistorialDTO(null, null, "Consulta", null, null, 1L)));
	}

	/** Buscar un registro que no existe lanza recurso no encontrado. */
	@Test
	void buscarRegistroInexistenteLanzaError() {

		when(historialRepository.findById(3L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> historialService.buscarPorId(3L));
	}

	/** Buscar por usuario convierte los registros en DTO. */
	@Test
	void buscarPorUsuarioDevuelveSusRegistros() {

		when(historialRepository.findByIdUsuario(5L))
				.thenReturn(List.of(new Historial("Uno", 5L, 1L), new Historial("Dos", 5L, 2L)));

		assertEquals(2, historialService.buscarPorUsuario(5L).size());
	}
}
