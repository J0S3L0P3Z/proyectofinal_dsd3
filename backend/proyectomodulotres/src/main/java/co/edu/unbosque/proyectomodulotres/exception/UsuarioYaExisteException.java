package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando se intenta registrar un correo que ya existe. Se responde con el
 * código HTTP 409.
 */
public class UsuarioYaExisteException extends RuntimeException {

	/**
	 * Crea la excepción con su mensaje.
	 *
	 * @param mensaje Mensaje del error
	 */
	public UsuarioYaExisteException(String mensaje) {
		super(mensaje);
	}
}
