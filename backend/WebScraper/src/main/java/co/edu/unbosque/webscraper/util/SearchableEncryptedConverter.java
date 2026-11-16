package co.edu.unbosque.webscraper.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convertidor JPA que guarda un campo cifrado con AES/GCM y IV fijo ({@link AESUtil#encrypt(String)}).
 * Como el mismo texto siempre da el mismo valor cifrado, se puede seguir buscando por ese campo
 * (por ejemplo {@code findByEmail("ana@correo.com")}): Hibernate cifra también el parámetro de la
 * consulta. No sirve para búsquedas parciales (LIKE).
 *
 * <p>Se usa en {@code User.email}. En el código el campo se sigue usando como texto normal; solo
 * queda cifrado en la base de datos.
 */
@Converter
public class SearchableEncryptedConverter implements AttributeConverter<String, String> {

  /** Logger para avisar de valores que no se pudieron descifrar. */
  private static final Logger log = LoggerFactory.getLogger(SearchableEncryptedConverter.class);

  /**
   * Cifra el valor antes de guardarlo en la base de datos.
   *
   * @param attribute Valor en texto plano (puede ser null)
   * @return Valor cifrado en Base64, o null
   */
  @Override
  public String convertToDatabaseColumn(String attribute) {
    return attribute == null ? null : AESUtil.encrypt(attribute);
  }

  /**
   * Descifra el valor leído de la base de datos. Si no se puede descifrar (por ejemplo, un dato
   * guardado antes de activar el cifrado), devuelve el valor tal cual y lo registra en el log.
   *
   * @param dbData Valor cifrado (puede ser null)
   * @return Valor en texto plano, o null
   */
  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    try {
      return AESUtil.decrypt(dbData);
    } catch (IllegalStateException e) {
      log.warn("Un valor de la base no se pudo descifrar; se devuelve sin descifrar (¿dato antiguo o AES_KEY distinta?)");
      return dbData;
    }
  }
}
