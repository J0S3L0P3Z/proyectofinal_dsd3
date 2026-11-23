package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.entity.Resultado;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.repository.ResultadoRepository;
import co.edu.unbosque.proyectomodulotres.util.AESUtil;

/**
 * Pruebas unitarias de {@link ResultadoService}. Usa el cifrado AES real con una clave de prueba
 * para verificar que la respuesta se guarda cifrada y se devuelve descifrada.
 */
@ExtendWith(MockitoExtension.class)
class ResultadoServiceTest {

	/** Clave de prueba de 32 caracteres (no es la clave real). */
	private static final String CLAVE_PRUEBA = "ClaveDePrueba0123456789abcdefXYZ";

	/** Repositorio de resultados simulado. */
	@Mock
	private ResultadoRepository resultadoRepository;

	/** Servicio que se prueba. */
	private ResultadoService resultadoService;

	/** Crea el servicio con el repositorio simulado y el cifrado real. */
	@BeforeEach
	void setUp() {
		resultadoService = new ResultadoService(resultadoRepository, new AESUtil(CLAVE_PRUEBA));
	}

	/**
	 * Crea un resultado de prueba.
	 *
	 * @return DTO con nombre de IA, modelo, respuesta y scraping
	 */
	private ResultadoDTO crearResultado() {

		ResultadoDTO datos = new ResultadoDTO();
		datos.setNombreIA("GEMINI_GENERAL");
		datos.setModelo("gemini-modelo");
		datos.setRespuesta("Respuesta confidencial");
		datos.setEstado("COMPLETADO");
		datos.setIdScraping(1L);

		return datos;
	}

	/** La respuesta se guarda cifrada con el prefijo AES: y se devuelve descifrada. */
	@Test
	void guardarCifraLaRespuesta() {

		when(resultadoRepository.save(any(Resultado.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

		ResultadoDTO guardado = resultadoService.guardar(crearResultado());

		ArgumentCaptor<Resultado> captor = ArgumentCaptor.forClass(Resultado.class);
		verify(resultadoRepository).save(captor.capture());

		assertTrue(captor.getValue().getRespuesta().startsWith("AES:"));
		assertEquals("Respuesta confidencial", guardado.getRespuesta());
		assertEquals("COMPLETADO", guardado.getEstado());
	}

	/** Las respuestas antiguas sin cifrar se devuelven tal cual. */
	@Test
	void respuestaSinCifrarSeDevuelveIgual() {

		Resultado antiguo = new Resultado("GROQ_GENERAL", "modelo", 1L);
		antiguo.setRespuesta("texto plano");

		when(resultadoRepository.findByIdScraping(1L)).thenReturn(List.of(antiguo));

		assertEquals("texto plano", resultadoService.buscarPorScraping(1L).get(0).getRespuesta());
	}

	/** No se puede guardar un resultado sin nombre de IA. */
	@Test
	void guardarSinNombreIALanzaError() {

		ResultadoDTO datos = crearResultado();
		datos.setNombreIA(null);

		assertThrows(DatoInvalidoException.class, () -> resultadoService.guardar(datos));
	}

	/** No se puede guardar un resultado sin scraping. */
	@Test
	void guardarSinScrapingLanzaError() {

		ResultadoDTO datos = crearResultado();
		datos.setIdScraping(null);

		assertThrows(DatoInvalidoException.class, () -> resultadoService.guardar(datos));
	}

	/** Buscar un resultado que no existe lanza recurso no encontrado. */
	@Test
	void buscarResultadoInexistenteLanzaError() {

		when(resultadoRepository.findById(7L)).thenReturn(Optional.empty());

		assertThrows(RecursoNoEncontradoException.class, () -> resultadoService.buscarPorId(7L));
	}
}
