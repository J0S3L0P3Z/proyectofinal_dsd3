package co.edu.unbosque.webscraper.util;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Clase de utilidad para operaciones de cifrado AES y funciones de hash. Usa el mismo algoritmo
 * del proyecto de referencia SpringFirstAppJWT: AES en modo GCM sin padding, etiqueta de 128 bits
 * y resultado en Base64.
 *
 * <p>Diferencias con la referencia: la llave y el vector de inicialización (IV) NO están escritos
 * en el código; se cargan al arrancar desde las variables de entorno AES_KEY y AES_IV (ver {@code
 * EncryptionConfig}). Además el texto se convierte siempre con UTF-8, para que las tildes y eñes
 * se cifren igual en Windows y en Linux.
 *
 * <p>Hay dos formas de cifrar:
 *
 * <ul>
 *   <li>{@link #encrypt(String)}: IV fijo (como la referencia). El mismo texto siempre da el mismo
 *       resultado, así que se puede buscar en la base por el valor cifrado. Se usa para el correo.
 *   <li>{@link #encryptWithRandomIv(String)}: IV aleatorio en cada cifrado (guardado junto al
 *       texto cifrado). Es más seguro, pero no permite buscar. Se usa para datos que solo se leen,
 *       como la IP de la auditoría.
 * </ul>
 *
 * <p>Las contraseñas NO usan esta clase: se guardan con hash BCrypt (PasswordEncoder).
 */
public class AESUtil {

  /** Algoritmo de cifrado utilizado (AES). */
  private static final String ALGORITMO = "AES";

  /** Modo de cifrado y padding utilizados (AES en modo GCM sin padding). */
  private static final String TIPOCIFRADO = "AES/GCM/NoPadding";

  /** Tamaño de la etiqueta de autenticación de GCM, en bits. */
  private static final int TAG_BITS = 128;

  /** Tamaño del IV aleatorio, en bytes (12 es el recomendado para GCM). */
  private static final int RANDOM_IV_BYTES = 12;

  /** Generador seguro para los IV aleatorios. */
  private static final SecureRandom RANDOM = new SecureRandom();

  /** Llave configurada desde AES_KEY. */
  private static volatile String configuredKey;

  /** IV fijo configurado desde AES_IV. */
  private static volatile String configuredIv;

  /** Clase utilitaria: no se instancia. */
  private AESUtil() {}

  /**
   * Configura la llave y el IV que usan los métodos sin parámetros. Lo llama {@code
   * EncryptionConfig} al arrancar la aplicación.
   *
   * @param llave Llave de 16, 24 o 32 caracteres (AES-128, AES-192 o AES-256)
   * @param iv Vector de inicialización de 12 o 16 caracteres
   * @throws IllegalArgumentException Si la llave o el IV no tienen un tamaño válido
   */
  public static void configure(String llave, String iv) {
    int keyLen = llave == null ? 0 : llave.getBytes(StandardCharsets.UTF_8).length;
    if (keyLen != 16 && keyLen != 24 && keyLen != 32) {
      throw new IllegalArgumentException(
          "AES_KEY debe tener exactamente 16, 24 o 32 caracteres (tiene " + keyLen + ")");
    }
    int ivLen = iv == null ? 0 : iv.getBytes(StandardCharsets.UTF_8).length;
    if (ivLen != 12 && ivLen != 16) {
      throw new IllegalArgumentException(
          "AES_IV debe tener exactamente 12 o 16 caracteres (tiene " + ivLen + ")");
    }
    configuredKey = llave;
    configuredIv = iv;
  }

  /**
   * Indica si ya se configuró la llave.
   *
   * @return true si se puede cifrar con los métodos sin parámetros
   */
  public static boolean isConfigured() {
    return configuredKey != null && configuredIv != null;
  }

  /**
   * Cifra un texto utilizando AES en modo GCM.
   *
   * @param llave Clave de cifrado (16, 24 o 32 caracteres)
   * @param iv Vector de inicialización (IV) para el cifrado
   * @param texto Texto a cifrar
   * @return Texto cifrado en formato Base64
   * @throws IllegalStateException Si el cifrado falla (llave o IV inválidos)
   */
  public static String encrypt(String llave, String iv, String texto) {
    try {
      Cipher cipher = Cipher.getInstance(TIPOCIFRADO);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec(llave), new GCMParameterSpec(TAG_BITS, bytes(iv)));
      return new String(encodeBase64(cipher.doFinal(bytes(texto))), StandardCharsets.US_ASCII);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("No se pudo cifrar el texto", e);
    }
  }

  /**
   * Descifra un texto cifrado con AES en modo GCM.
   *
   * @param llave Clave de cifrado (debe ser la misma utilizada para cifrar)
   * @param iv Vector de inicialización (debe ser el mismo utilizado para cifrar)
   * @param encrypted Texto cifrado en formato Base64
   * @return Texto descifrado
   * @throws IllegalStateException Si el descifrado falla (datos alterados, llave o IV incorrectos)
   */
  public static String decrypt(String llave, String iv, String encrypted) {
    try {
      Cipher cipher = Cipher.getInstance(TIPOCIFRADO);
      cipher.init(Cipher.DECRYPT_MODE, keySpec(llave), new GCMParameterSpec(TAG_BITS, bytes(iv)));
      return new String(cipher.doFinal(decodeBase64(encrypted)), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new IllegalStateException("No se pudo descifrar el texto", e);
    }
  }

  /**
   * Cifra un texto con la llave y el IV configurados (IV fijo: el mismo texto da siempre el mismo
   * resultado, por eso se puede buscar por el valor cifrado).
   *
   * @param plainText Texto a cifrar
   * @return Texto cifrado en formato Base64
   */
  public static String encrypt(String plainText) {
    requireConfigured();
    return encrypt(configuredKey, configuredIv, plainText);
  }

  /**
   * Descifra un texto cifrado con {@link #encrypt(String)}.
   *
   * @param encrypted Texto cifrado en formato Base64
   * @return Texto descifrado
   */
  public static String decrypt(String encrypted) {
    requireConfigured();
    return decrypt(configuredKey, configuredIv, encrypted);
  }

  /**
   * Cifra un texto con la llave configurada y un IV aleatorio nuevo. El IV se guarda al inicio del
   * resultado (IV + texto cifrado, todo en Base64), así que el mismo texto da un resultado distinto
   * cada vez.
   *
   * @param plainText Texto a cifrar
   * @return IV y texto cifrado en Base64
   */
  public static String encryptWithRandomIv(String plainText) {
    requireConfigured();
    try {
      byte[] iv = new byte[RANDOM_IV_BYTES];
      RANDOM.nextBytes(iv);
      Cipher cipher = Cipher.getInstance(TIPOCIFRADO);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec(configuredKey), new GCMParameterSpec(TAG_BITS, iv));
      byte[] encrypted = cipher.doFinal(bytes(plainText));
      byte[] out = new byte[iv.length + encrypted.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(encrypted, 0, out, iv.length, encrypted.length);
      return new String(encodeBase64(out), StandardCharsets.US_ASCII);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("No se pudo cifrar el texto", e);
    }
  }

  /**
   * Descifra un texto cifrado con {@link #encryptWithRandomIv(String)}.
   *
   * @param encrypted IV y texto cifrado en Base64
   * @return Texto descifrado
   * @throws IllegalStateException Si el descifrado falla
   */
  public static String decryptWithRandomIv(String encrypted) {
    requireConfigured();
    try {
      byte[] all = decodeBase64(encrypted);
      if (all.length <= RANDOM_IV_BYTES) {
        throw new IllegalStateException("Texto cifrado demasiado corto");
      }
      byte[] iv = Arrays.copyOfRange(all, 0, RANDOM_IV_BYTES);
      byte[] data = Arrays.copyOfRange(all, RANDOM_IV_BYTES, all.length);
      Cipher cipher = Cipher.getInstance(TIPOCIFRADO);
      cipher.init(Cipher.DECRYPT_MODE, keySpec(configuredKey), new GCMParameterSpec(TAG_BITS, iv));
      return new String(cipher.doFinal(data), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException | IllegalArgumentException e) {
      throw new IllegalStateException("No se pudo descifrar el texto", e);
    }
  }

  /**
   * Genera un hash MD5 del contenido proporcionado.
   *
   * @param content Texto a convertir en hash
   * @return Representación hexadecimal del hash MD5
   */
  public static String hashingToMD5(String content) {
    return DigestUtils.md5Hex(content);
  }

  /**
   * Genera un hash SHA-1 del contenido proporcionado.
   *
   * @param content Texto a convertir en hash
   * @return Representación hexadecimal del hash SHA-1
   */
  public static String hashingToSHA1(String content) {
    return DigestUtils.sha1Hex(content);
  }

  /**
   * Genera un hash SHA-256 del contenido proporcionado. Se usa para guardar los tokens de
   * verificación de correo y recuperación de contraseña.
   *
   * @param content Texto a convertir en hash
   * @return Representación hexadecimal del hash SHA-256 (64 caracteres)
   */
  public static String hashingToSHA256(String content) {
    return DigestUtils.sha256Hex(content);
  }

  /**
   * Genera un hash SHA-384 del contenido proporcionado.
   *
   * @param content Texto a convertir en hash
   * @return Representación hexadecimal del hash SHA-384
   */
  public static String hashingToSHA384(String content) {
    return DigestUtils.sha384Hex(content);
  }

  /**
   * Genera un hash SHA-512 del contenido proporcionado.
   *
   * @param content Texto a convertir en hash
   * @return Representación hexadecimal del hash SHA-512
   */
  public static String hashingToSHA512(String content) {
    return DigestUtils.sha512Hex(content);
  }

  /** Lanza un error claro si se intenta cifrar antes de configurar la llave. */
  private static void requireConfigured() {
    if (!isConfigured()) {
      throw new IllegalStateException("AESUtil no está configurado: faltan AES_KEY y AES_IV");
    }
  }

  /** Crea la especificación de la llave AES a partir del texto. */
  private static SecretKeySpec keySpec(String llave) {
    return new SecretKeySpec(bytes(llave), ALGORITMO);
  }

  /** Convierte un texto a bytes en UTF-8. */
  private static byte[] bytes(String s) {
    return s.getBytes(StandardCharsets.UTF_8);
  }
}
