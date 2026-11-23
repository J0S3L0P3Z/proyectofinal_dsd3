package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando los datos enviados no son válidos o están incompletos. Se responde
 * con el código HTTP 400.
 */
public class DatoInvalidoException extends RuntimeException {

    /**
     * Crea la excepción con su mensaje.
     *
     * @param mensaje Mensaje que explica qué dato no es válido
     */
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
