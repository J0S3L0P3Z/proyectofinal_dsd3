package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando no se pudo preparar o enviar un correo. Se responde con el código
 * HTTP 500.
 */
public class EnvioCorreoException extends RuntimeException {

    /**
     * Crea la excepción con su mensaje.
     *
     * @param mensaje Mensaje del error
     */
    public EnvioCorreoException(String mensaje) {
        super(mensaje);
    }
}
