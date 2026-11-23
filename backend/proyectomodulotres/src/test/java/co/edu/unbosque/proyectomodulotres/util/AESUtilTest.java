package co.edu.unbosque.proyectomodulotres.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del cifrado AES.
 */
class AESUtilTest {

	/** Clave de prueba de 32 caracteres (no es la clave real). */
	private static final String CLAVE_PRUEBA = "ClaveDePrueba0123456789abcdefXYZ";

	/** Utilidad que se prueba. */
	private AESUtil aesUtil;

	/** Crea la utilidad con la clave de prueba antes de cada prueba. */
	@BeforeEach
	void setUp() {
		aesUtil = new AESUtil(CLAVE_PRUEBA);
	}

	/** Al descifrar un texto cifrado se obtiene el texto original. */
	@Test
	void cifrarYDescifrarDevuelveElTextoOriginal() {

		String cifrado = aesUtil.cifrar("Información con tildes y ñ");

		assertNotEquals("Información con tildes y ñ", cifrado);
		assertEquals("Información con tildes y ñ", aesUtil.descifrar(cifrado));
	}

	/** El mismo texto cifrado dos veces da resultados distintos por el IV aleatorio. */
	@Test
	void mismoTextoProduceCifradosDistintos() {
		assertNotEquals(aesUtil.cifrar("Juan"), aesUtil.cifrar("Juan"));
	}

	/** Un texto cifrado alterado no se puede descifrar. */
	@Test
	void textoAlteradoNoSePuedeDescifrar() {

		String cifrado = aesUtil.cifrar("dato importante");

		char caracter = cifrado.charAt(20);
		String alterado = cifrado.substring(0, 20) + (caracter == 'A' ? 'B' : 'A') + cifrado.substring(21);

		assertThrows(RuntimeException.class, () -> aesUtil.descifrar(alterado));
	}

	/** Un texto cifrado con otra clave no se puede descifrar. */
	@Test
	void otraClaveNoPuedeDescifrar() {

		String cifrado = aesUtil.cifrar("secreto");

		AESUtil otraUtilidad = new AESUtil("OtraClaveDePrueba0123456789abcde");

		assertThrows(RuntimeException.class, () -> otraUtilidad.descifrar(cifrado));
	}

	/** Una clave que no tiene 32 caracteres no se acepta. */
	@Test
	void claveDeLongitudIncorrectaNoSeAcepta() {
		assertThrows(IllegalArgumentException.class, () -> new AESUtil("clave-corta"));
	}
}
