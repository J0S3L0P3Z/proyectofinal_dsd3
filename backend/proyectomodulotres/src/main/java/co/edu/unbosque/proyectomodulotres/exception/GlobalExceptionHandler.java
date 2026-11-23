package co.edu.unbosque.proyectomodulotres.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones. Convierte cada excepción en una respuesta JSON con el código
 * HTTP y el mensaje: {@code {"estado": código, "mensaje": "..."}}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Arma la respuesta de error.
	 *
	 * @param estado Código HTTP
	 * @param mensaje Mensaje del error
	 * @return Respuesta con el código y el mensaje
	 */
	private ResponseEntity<Map<String, Object>> crearRespuesta(HttpStatus estado, String mensaje) {

		Map<String, Object> respuesta = new HashMap<>();

		respuesta.put("estado", estado.value());
		respuesta.put("mensaje", mensaje);

		return ResponseEntity.status(estado).body(respuesta);
	}

	/**
	 * Maneja los recursos no encontrados (404).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 404 con el mensaje
	 */
	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
		return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	/**
	 * Maneja los datos inválidos (400).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 400 con el mensaje
	 */
	@ExceptionHandler(DatoInvalidoException.class)
	public ResponseEntity<Map<String, Object>> manejarDatoInvalido(DatoInvalidoException ex) {
		return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	/**
	 * Maneja los correos ya registrados (409).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 409 con el mensaje
	 */
	@ExceptionHandler(UsuarioYaExisteException.class)
	public ResponseEntity<Map<String, Object>> manejarUsuarioYaExiste(UsuarioYaExisteException ex) {
		return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
	}

	/**
	 * Maneja las credenciales incorrectas (401).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 401 con el mensaje
	 */
	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
		return crearRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	/**
	 * Maneja los accesos no autorizados (403).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 403 con el mensaje
	 */
	@ExceptionHandler(AccesoNoAutorizadoException.class)
	public ResponseEntity<Map<String, Object>> manejarAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
		return crearRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	/**
	 * Maneja los errores del scraping (500).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 500 con el mensaje
	 */
	@ExceptionHandler(ScrapingException.class)
	public ResponseEntity<Map<String, Object>> manejarScraping(ScrapingException ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	/**
	 * Maneja los errores de las IA (500).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 500 con el mensaje
	 */
	@ExceptionHandler(AnalisisIAException.class)
	public ResponseEntity<Map<String, Object>> manejarAnalisisIA(AnalisisIAException ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	/**
	 * Maneja los errores de envío de correo (500).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 500 con el mensaje
	 */
	@ExceptionHandler(EnvioCorreoException.class)
	public ResponseEntity<Map<String, Object>> manejarEnvioCorreo(EnvioCorreoException ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	/**
	 * Maneja los errores de validación de los DTO (400). Devuelve el primer campo con error.
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 400 con el campo y su mensaje
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {

		String mensaje = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst()
				.orElse("Los datos enviados no son válidos");

		return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje);
	}

	/**
	 * Maneja los JSON mal formados (400).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 400 con el mensaje
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> manejarJSONIncorrecto(HttpMessageNotReadableException ex) {
		return crearRespuesta(HttpStatus.BAD_REQUEST, "El JSON enviado está mal formado o contiene datos incorrectos");
	}

	/**
	 * Maneja los parámetros obligatorios que faltan (400).
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 400 con el nombre del parámetro
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> manejarParametroFaltante(MissingServletRequestParameterException ex) {

		return crearRespuesta(HttpStatus.BAD_REQUEST, "Falta el parámetro obligatorio: " + ex.getParameterName());
	}

	/**
	 * Maneja cualquier otro error (500) sin mostrar detalles internos.
	 *
	 * @param ex Excepción lanzada
	 * @return Respuesta 500 con un mensaje general
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> manejarErrorGeneral(Exception ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno en el servidor.");
	}
}
