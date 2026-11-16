package co.edu.unbosque.webscraper.util;

import co.edu.unbosque.webscraper.exception.PasswordNotValidException;

/**
 * Utilitario que valida la política de contraseñas de la aplicación: mínimo 8 caracteres, al menos
 * una minúscula, una mayúscula, un número y un símbolo. El máximo es 72 caracteres (límite de
 * BCrypt, que ignora lo que exceda).
 */
public class PasswordValidator {

  /** Longitud mínima de una contraseña. */
  public static final int MIN_LENGTH = 8;

  /** Longitud máxima de una contraseña (límite práctico de BCrypt). */
  public static final int MAX_LENGTH = 72;

  private PasswordValidator() {}

  /**
   * Valida una contraseña en texto plano.
   *
   * @param password Contraseña a validar
   * @throws PasswordNotValidException Si no cumple la política de seguridad
   */
  public static void validate(String password) throws PasswordNotValidException {
    if (password == null || password.length() < MIN_LENGTH) {
      throw new PasswordNotValidException();
    }
    if (password.length() > MAX_LENGTH) {
      throw new PasswordNotValidException(
          "La contraseña no puede superar los " + MAX_LENGTH + " caracteres.");
    }
    boolean lower = false;
    boolean upper = false;
    boolean digit = false;
    boolean symbol = false;
    for (char c : password.toCharArray()) {
      if (Character.isLowerCase(c)) lower = true;
      else if (Character.isUpperCase(c)) upper = true;
      else if (Character.isDigit(c)) digit = true;
      else if (!Character.isWhitespace(c)) symbol = true;
    }
    if (!(lower && upper && digit && symbol)) {
      throw new PasswordNotValidException();
    }
  }

  /**
   * Indica si una contraseña es válida sin lanzar excepción.
   *
   * @param password Contraseña a validar
   * @return true si cumple la política, false en caso contrario
   */
  public static boolean isValid(String password) {
    try {
      validate(password);
      return true;
    } catch (PasswordNotValidException e) {
      return false;
    }
  }
}
