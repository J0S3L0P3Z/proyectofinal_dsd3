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

@RestControllerAdvice
public class GlobalExceptionHandler {

	private ResponseEntity<Map<String, Object>> crearRespuesta(HttpStatus estado, String mensaje) {

		Map<String, Object> respuesta = new HashMap<>();

		respuesta.put("estado", estado.value());
		respuesta.put("mensaje", mensaje);

		return ResponseEntity.status(estado).body(respuesta);
	}

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
		return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DatoInvalidoException.class)
	public ResponseEntity<Map<String, Object>> manejarDatoInvalido(DatoInvalidoException ex) {
		return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(UsuarioYaExisteException.class)
	public ResponseEntity<Map<String, Object>> manejarUsuarioYaExiste(UsuarioYaExisteException ex) {
		return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
		return crearRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(AccesoNoAutorizadoException.class)
	public ResponseEntity<Map<String, Object>> manejarAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
		return crearRespuesta(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(ScrapingException.class)
	public ResponseEntity<Map<String, Object>> manejarScraping(ScrapingException ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(AnalisisIAException.class)
	public ResponseEntity<Map<String, Object>> manejarAnalisisIA(AnalisisIAException ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(EnvioCorreoException.class)
	public ResponseEntity<Map<String, Object>> manejarEnvioCorreo(EnvioCorreoException ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> manejarValidacion(MethodArgumentNotValidException ex) {

		String mensaje = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst()
				.orElse("Los datos enviados no son válidos");

		return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> manejarJSONIncorrecto(HttpMessageNotReadableException ex) {
		return crearRespuesta(HttpStatus.BAD_REQUEST, "El JSON enviado está mal formado o contiene datos incorrectos");
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<Map<String, Object>> manejarParametroFaltante(MissingServletRequestParameterException ex) {

		return crearRespuesta(HttpStatus.BAD_REQUEST, "Falta el parámetro obligatorio: " + ex.getParameterName());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> manejarErrorGeneral(Exception ex) {
		return crearRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno en el servidor.");
	}
}