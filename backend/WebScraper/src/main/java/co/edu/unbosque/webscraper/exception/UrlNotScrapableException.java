package co.edu.unbosque.webscraper.exception;

import java.util.Locale;

/**
 * Excepción que indica que una URL no se puede scrapear y explica el motivo (la página no existe,
 * bloquea la lectura automática, exige iniciar sesión, no responde, tiene un certificado inválido,
 * etc.). No se intenta evadir ninguna protección: el caso solo se informa al usuario con la razón.
 */
public class UrlNotScrapableException extends AppException {

  /** Número de serie para la serialización. */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor con el motivo y un detalle técnico.
   *
   * @param reason Código del catálogo que explica por qué no es posible
   * @param detail Detalle técnico (va al log, no al usuario)
   */
  public UrlNotScrapableException(ErrorCode reason, String detail) {
    super(reason, detail);
  }

  /**
   * Constructor con el motivo, un detalle técnico y la causa original.
   *
   * @param reason Código del catálogo que explica por qué no es posible
   * @param detail Detalle técnico (va al log, no al usuario)
   * @param cause Excepción original
   */
  public UrlNotScrapableException(ErrorCode reason, String detail, Throwable cause) {
    super(reason, detail, cause);
  }

  /**
   * Mensaje claro para el usuario: indica que no es posible scrapear la URL y el porqué.
   *
   * @return Mensaje con el formato "No es posible scrapear esta URL. Motivo: ..."
   */
  @Override
  public String getUserMessage() {
    String reason = getCode().getMessage();
    String lowered =
        reason.isEmpty() ? reason : reason.substring(0, 1).toLowerCase(Locale.ROOT) + reason.substring(1);
    return "No es posible scrapear esta URL. Motivo: " + lowered;
  }

  /**
   * Devuelve el mismo mensaje que {@link #getUserMessage()}.
   *
   * @return Mensaje para el usuario
   */
  @Override
  public String getMessage() {
    return getUserMessage();
  }
}
