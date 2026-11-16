package co.edu.unbosque.webscraper.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convertidor JPA que guarda un campo cifrado con AES/GCM y un IV aleatorio en cada escritura
 * ({@link AESUtil#encryptWithRandomIv(String)}). Es la opción más segura, pero no permite buscar
 * por el campo, así que se usa solo en datos que se leen y no se consultan, como la IP de la
 * auditoría.
 */
@Converter
public class EncryptedConverter implements AttributeConverter<String, String> {

  /** Logger para avisar de valores que no se pudieron descifrar. */
  private static final Logger log = LoggerFactory.getLogger(EncryptedConverter.class);

  /**
   * Cifra el valor antes de guardarlo en la base de datos.
   *
   * @param attribute Valor en texto plano (puede ser null)
   * @return IV y valor cifrado en Base64, o null
   */
  @Override
  public String convertToDatabaseColumn(String attribute) {
    return attribute == null ? null : AESUtil.encryptWithRandomIv(attribute);
  }

  /**
   * Descifra el valor leído de la base de datos. Si no se puede descifrar (dato antiguo sin
   * cifrar), lo devuelve tal cual.
   *
   * @param dbData Valor cifrado (puede ser null)
   * @return Valor en texto plano, o null
   */
  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    try {
      return AESUtil.decryptWithRandomIv(dbData);
    } catch (IllegalStateException e) {
      log.warn("Un valor de la base no se pudo descifrar; se devuelve sin descifrar (¿dato antiguo o AES_KEY distinta?)");
      return dbData;
    }
  }
}
