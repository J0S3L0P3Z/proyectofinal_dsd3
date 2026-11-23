package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando el usuario no tiene acceso (cuenta deshabilitada, bloqueada o
 * inactiva). Se responde con el código HTTP 403.
 */
public class AccesoNoAutorizadoException extends RuntimeException {

    /**
     * Crea la excepción con su mensaje.
     *
     * @param mensaje Mensaje del error
     */
    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
