package co.edu.unbosque.webscraper.exception;

/** El job ya terminó por otra vía (por ejemplo, lo cerró el vigilante por tiempo): el hilo debe parar sin más. */
public class JobAbortedException extends RuntimeException {
    public JobAbortedException(String message) {
        super(message);
    }
}
