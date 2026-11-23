package co.edu.unbosque.proyectomodulotres.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utilidad de cifrado AES-256 en modo GCM. Cada cifrado usa un vector de inicialización (IV)
 * aleatorio que se guarda al inicio del texto cifrado, por lo que el mismo texto produce un
 * resultado distinto cada vez.
 */
@Component
public class AESUtil {

	/** Algoritmo de cifrado (AES en modo GCM sin padding). */
	private static final String ALGORITHM = "AES/GCM/NoPadding";

	/** Longitud del vector de inicialización en bytes. */
	private static final int IV_LENGTH = 12;

	/** Longitud de la etiqueta de autenticación en bits. */
	private static final int TAG_LENGTH = 128;

	/** Clave secreta usada para cifrar y descifrar. */
	private final SecretKeySpec secretKey;

	/**
	 * Crea la utilidad a partir de la clave configurada.
	 *
	 * @param secret Clave secreta de exactamente 32 caracteres (propiedad aes.secret-key)
	 * @throws IllegalArgumentException Si la clave no tiene 32 bytes
	 */
	public AESUtil(@Value("${aes.secret-key}") String secret) {

		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

		if (keyBytes.length != 32) {
			throw new IllegalArgumentException("La clave AES debe tener exactamente 32 bytes");
		}

		this.secretKey = new SecretKeySpec(keyBytes, "AES");
	}

	/**
	 * Cifra un texto con AES-256-GCM y un IV aleatorio.
	 *
	 * @param texto Texto a cifrar
	 * @return Texto cifrado en Base64 (IV + contenido cifrado)
	 * @throws RuntimeException Si ocurre un error al cifrar
	 */
	public String cifrar(String texto) {
		try {
			byte[] iv = new byte[IV_LENGTH];
			new SecureRandom().nextBytes(iv);

			Cipher cipher = Cipher.getInstance(ALGORITHM);

			GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);

			cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

			byte[] textoCifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));

			ByteBuffer buffer = ByteBuffer.allocate(iv.length + textoCifrado.length);

			buffer.put(iv);
			buffer.put(textoCifrado);

			return Base64.getEncoder().encodeToString(buffer.array());

		} catch (Exception e) {
			throw new RuntimeException("Error al cifrar la información", e);
		}
	}

	/**
	 * Descifra un texto cifrado con {@link #cifrar(String)}.
	 *
	 * @param textoCifrado Texto cifrado en Base64
	 * @return Texto original
	 * @throws RuntimeException Si los datos están alterados o la clave no corresponde
	 */
	public String descifrar(String textoCifrado) {
		try {
			byte[] datos = Base64.getDecoder().decode(textoCifrado);

			if (datos.length < IV_LENGTH + 16) {
				throw new IllegalArgumentException("Datos cifrados inválidos");
			}

			ByteBuffer buffer = ByteBuffer.wrap(datos);

			byte[] iv = new byte[IV_LENGTH];
			buffer.get(iv);

			byte[] contenidoCifrado = new byte[buffer.remaining()];
			buffer.get(contenidoCifrado);

			Cipher cipher = Cipher.getInstance(ALGORITHM);

			GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);

			cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

			byte[] textoOriginal = cipher.doFinal(contenidoCifrado);

			return new String(textoOriginal, StandardCharsets.UTF_8);

		} catch (Exception e) {
			throw new RuntimeException("Error al descifrar la información", e);
		}
	}
}