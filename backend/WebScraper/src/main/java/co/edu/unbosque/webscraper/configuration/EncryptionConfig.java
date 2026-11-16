package co.edu.unbosque.webscraper.configuration;

import co.edu.unbosque.webscraper.util.AESUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del cifrado AES de datos sensibles. Al arrancar lee la llave y el vector de
 * inicialización desde las variables de entorno AES_KEY y AES_IV y se los entrega a {@link
 * AESUtil}. Si faltan o no tienen el tamaño correcto, la aplicación no arranca y el mensaje dice
 * qué variable revisar.
 *
 * <p>Importante: si se cambia AES_KEY o AES_IV, los datos ya guardados no se podrán descifrar.
 */
@Configuration
public class EncryptionConfig {

  /** Logger para registrar la activación del cifrado. */
  private static final Logger log = LoggerFactory.getLogger(EncryptionConfig.class);

  /** Llave AES (16, 24 o 32 caracteres). */
  @Value("${aes.key}")
  private String key;

  /** Vector de inicialización fijo (12 o 16 caracteres). */
  @Value("${aes.iv}")
  private String iv;

  /** Constructor por defecto. */
  public EncryptionConfig() {}

  /**
   * Valida y activa la configuración de cifrado.
   *
   * @throws IllegalArgumentException Si la llave o el IV no son válidos
   */
  @PostConstruct
  void init() {
    AESUtil.configure(key, iv);
    log.info("Cifrado AES/GCM activo para los datos sensibles (AES-{})", key.length() * 8);
  }
}
