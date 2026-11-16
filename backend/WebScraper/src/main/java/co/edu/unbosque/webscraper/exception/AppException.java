package co.edu.unbosque.webscraper.exception;

/** Excepción de negocio con código del catálogo. {@code detail} es técnico: va al log, no al usuario. */
public class AppException extends RuntimeException {
    private final ErrorCode code;
    private final String detail;

    public AppException(ErrorCode code) {
        this(code, null, null);
    }

    public AppException(ErrorCode code, String detail) {
        this(code, detail, null);
    }

    public AppException(ErrorCode code, String detail, Throwable cause) {
        super(code.getMessage(), cause);
        this.code = code;
        this.detail = detail;
    }

    /**
     * Mensaje claro para el usuario. Por defecto es el mensaje del código del catálogo.
     *
     * @return Mensaje para el usuario
     */
    public String getUserMessage() { return code.getMessage(); }

    public ErrorCode getCode() { return code; }
    public String getDetail() { return detail; }
}
