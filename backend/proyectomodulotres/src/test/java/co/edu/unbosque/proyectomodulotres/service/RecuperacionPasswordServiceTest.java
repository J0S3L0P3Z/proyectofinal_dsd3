package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

/**
 * Pruebas unitarias de {@link RecuperacionPasswordService}. El correo se simula y se captura el
 * código enviado para usarlo en la confirmación.
 */
@ExtendWith(MockitoExtension.class)
class RecuperacionPasswordServiceTest {

	/** Repositorio de usuarios simulado. */
	@Mock
	private UserRepository userRepository;

	/** Codificador de contraseñas simulado. */
	@Mock
	private PasswordEncoder passwordEncoder;

	/** Servicio de correo simulado. */
	@Mock
	private NotificacionEmailService emailService;

	/** Servicio que se prueba. */
	@InjectMocks
	private RecuperacionPasswordService recuperacionService;

	/**
	 * Solicita la recuperación para un usuario existente y devuelve el código que se envió por correo.
	 *
	 * @param usuario Usuario que pide la recuperación
	 * @return Código de 6 dígitos enviado
	 */
	private String solicitarYCapturarCodigo(User usuario) {

		when(userRepository.findByUsername("ana@correo.com")).thenReturn(Optional.of(usuario));

		recuperacionService.solicitarRecuperacion("ana@correo.com");

		ArgumentCaptor<String> codigo = ArgumentCaptor.forClass(String.class);

		verify(emailService).enviarCodigoRecuperacion(org.mockito.ArgumentMatchers.eq("ana@correo.com"),
				org.mockito.ArgumentMatchers.eq("Ana"), codigo.capture());

		return codigo.getValue();
	}

	/** Si el correo no existe responde el mensaje genérico y no envía correo. */
	@Test
	void correoNoRegistradoNoEnviaCodigo() {

		when(userRepository.findByUsername("nadie@correo.com")).thenReturn(Optional.empty());

		Map<String, Object> respuesta = recuperacionService.solicitarRecuperacion("nadie@correo.com");

		assertEquals("EXITO", respuesta.get("estado"));
		verify(emailService, never()).enviarCodigoRecuperacion(anyString(), anyString(), anyString());
	}

	/** El código enviado tiene 6 dígitos. */
	@Test
	void elCodigoEnviadoTieneSeisDigitos() {

		User usuario = new User("ana@correo.com", "hashViejo", null, "Ana", "Gomez");

		String codigo = solicitarYCapturarCodigo(usuario);

		assertEquals(6, codigo.length());
		assertEquals(true, codigo.matches("\\d{6}"));
	}

	/** Con el código correcto se cambia la contraseña. */
	@Test
	void codigoCorrectoCambiaLaContrasena() {

		User usuario = new User("ana@correo.com", "hashViejo", null, "Ana", "Gomez");

		String codigo = solicitarYCapturarCodigo(usuario);

		when(passwordEncoder.encode("NuevaClave2026")).thenReturn("hashNuevo");

		recuperacionService.confirmarRecuperacion("ana@correo.com", codigo, "NuevaClave2026");

		assertEquals("hashNuevo", usuario.getPassword());
		verify(userRepository).save(usuario);
	}

	/** El código solo se puede usar una vez. */
	@Test
	void elCodigoNoSePuedeReutilizar() {

		User usuario = new User("ana@correo.com", "hashViejo", null, "Ana", "Gomez");

		String codigo = solicitarYCapturarCodigo(usuario);

		when(passwordEncoder.encode("NuevaClave2026")).thenReturn("hashNuevo");

		recuperacionService.confirmarRecuperacion("ana@correo.com", codigo, "NuevaClave2026");

		assertThrows(DatoInvalidoException.class,
				() -> recuperacionService.confirmarRecuperacion("ana@correo.com", codigo, "OtraClave2026"));
	}

	/** Un código incorrecto no cambia la contraseña. */
	@Test
	void codigoIncorrectoLanzaError() {

		User usuario = new User("ana@correo.com", "hashViejo", null, "Ana", "Gomez");

		String codigo = solicitarYCapturarCodigo(usuario);

		String codigoIncorrecto = codigo.equals("000000") ? "111111" : "000000";

		assertThrows(DatoInvalidoException.class,
				() -> recuperacionService.confirmarRecuperacion("ana@correo.com", codigoIncorrecto, "NuevaClave2026"));

		assertEquals("hashViejo", usuario.getPassword());
	}

	/** Un código que no tiene 6 dígitos no se acepta. */
	@Test
	void codigoConFormatoInvalidoLanzaError() {
		assertThrows(DatoInvalidoException.class,
				() -> recuperacionService.confirmarRecuperacion("ana@correo.com", "12ab", "NuevaClave2026"));
	}

	/** Sin haber pedido un código no se puede confirmar. */
	@Test
	void confirmarSinSolicitudLanzaError() {
		assertThrows(DatoInvalidoException.class,
				() -> recuperacionService.confirmarRecuperacion("ana@correo.com", "123456", "NuevaClave2026"));
	}
}
