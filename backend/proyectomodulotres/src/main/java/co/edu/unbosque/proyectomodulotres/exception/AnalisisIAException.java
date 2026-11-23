package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando una API de inteligencia artificial responde con error o con una
 * respuesta no válida. Se responde con el código HTTP 500.
 */
public class AnalisisIAException extends RuntimeException {

    /**
     * Crea la excepción con su mensaje.
     *
     * @param mensaje Mensaje del error
     */
    public AnalisisIAException(String mensaje) {
        super(mensaje);
    }
}
