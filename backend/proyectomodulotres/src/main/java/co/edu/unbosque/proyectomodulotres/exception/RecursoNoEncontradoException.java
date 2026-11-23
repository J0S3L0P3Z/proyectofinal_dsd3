package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando el recurso buscado no existe. Se responde con el código HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

	/**
	 * Crea la excepción con su mensaje.
	 *
	 * @param mensaje Mensaje que indica qué recurso no se encontró
	 */
	public RecursoNoEncontradoException(String mensaje) {
		super(mensaje);
	}
}
