package co.edu.unbosque.proyectomodulotres.exception;

/**
 * Excepción que se lanza cuando falla el scraping o la generación del PDF. Se responde con el
 * código HTTP 500.
 */
public class ScrapingException extends RuntimeException {

    /**
     * Crea la excepción con su mensaje.
     *
     * @param mensaje Mensaje del error
     */
    public ScrapingException(String mensaje) {
        super(mensaje);
    }
}
