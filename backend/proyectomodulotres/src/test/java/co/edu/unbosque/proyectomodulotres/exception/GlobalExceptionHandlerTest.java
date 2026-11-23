package co.edu.unbosque.proyectomodulotres.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Pruebas unitarias del manejador global de excepciones: cada excepción debe responder con su
 * código HTTP y su mensaje.
 */
class GlobalExceptionHandlerTest {

	/** Manejador que se prueba. */
	private final GlobalExceptionHandler manejador = new GlobalExceptionHandler();

	/** Recurso no encontrado responde 404 con el mensaje. */
	@Test
	void recursoNoEncontradoResponde404() {

		ResponseEntity<Map<String, Object>> respuesta = manejador
				.manejarRecursoNoEncontrado(new RecursoNoEncontradoException("No existe"));

		assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
		assertEquals("No existe", respuesta.getBody().get("mensaje"));
		assertEquals(404, respuesta.getBody().get("estado"));
	}

	/** Dato inválido responde 400. */
	@Test
	void datoInvalidoResponde400() {
		assertEquals(HttpStatus.BAD_REQUEST,
				manejador.manejarDatoInvalido(new DatoInvalidoException("Dato malo")).getStatusCode());
	}

	/** Usuario repetido responde 409. */
	@Test
	void usuarioYaExisteResponde409() {
		assertEquals(HttpStatus.CONFLICT,
				manejador.manejarUsuarioYaExiste(new UsuarioYaExisteException("Ya existe")).getStatusCode());
	}

	/** Credenciales inválidas responde 401. */
	@Test
	void credencialesInvalidasResponde401() {
		assertEquals(HttpStatus.UNAUTHORIZED, manejador
				.manejarCredencialesInvalidas(new CredencialesInvalidasException("Incorrectas")).getStatusCode());
	}

	/** Acceso no autorizado responde 403. */
	@Test
	void accesoNoAutorizadoResponde403() {
		assertEquals(HttpStatus.FORBIDDEN,
				manejador.manejarAccesoNoAutorizado(new AccesoNoAutorizadoException("Prohibido")).getStatusCode());
	}

	/** Un error inesperado responde 500 sin mostrar el detalle interno. */
	@Test
	void errorGeneralNoMuestraElDetalle() {

		ResponseEntity<Map<String, Object>> respuesta = manejador
				.manejarErrorGeneral(new IllegalStateException("detalle interno"));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, respuesta.getStatusCode());
		assertEquals("Ocurrió un error interno en el servidor.", respuesta.getBody().get("mensaje"));
	}
}
