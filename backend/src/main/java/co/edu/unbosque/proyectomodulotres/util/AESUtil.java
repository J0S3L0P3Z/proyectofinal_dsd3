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

@Component
public class AESUtil {

	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int IV_LENGTH = 12;
	private static final int TAG_LENGTH = 128;

	private final SecretKeySpec secretKey;

	public AESUtil(@Value("${aes.secret-key}") String secret) {

		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

		if (keyBytes.length != 32) {
			throw new IllegalArgumentException("La clave AES debe tener exactamente 32 bytes");
		}

		this.secretKey = new SecretKeySpec(keyBytes, "AES");
	}

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