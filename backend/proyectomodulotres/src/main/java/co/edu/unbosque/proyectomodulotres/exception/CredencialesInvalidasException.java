package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando el correo o la contraseña son incorrectos. Se responde con el
 * código HTTP 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

	/**
	 * Crea la excepción con su mensaje.
	 *
	 * @param mensaje Mensaje del error
	 */
	public CredencialesInvalidasException(String mensaje) {
		super(mensaje);
	}
}
