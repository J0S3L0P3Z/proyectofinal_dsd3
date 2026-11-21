package co.edu.unbosque.proyectomodulotres.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.entity.User;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.repository.UserRepository;

@Service
@Transactional
public class RecuperacionPasswordService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final NotificacionEmailService emailService;

	private final Map<String, RegistroCodigo> codigos = new ConcurrentHashMap<>();

	private final SecureRandom random = new SecureRandom();

	public RecuperacionPasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			NotificacionEmailService emailService) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	public Map<String, Object> solicitarRecuperacion(String correo) {

		if (correo == null || correo.isBlank()) {
			throw new DatoInvalidoException("El correo es obligatorio");
		}

		String correoNormalizado = correo.trim().toLowerCase();

		User user = userRepository.findByUsername(correo.trim()).orElse(null);

		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("estado", "EXITO");
		respuesta.put("mensaje", "Si el correo está registrado, recibirás un código de recuperación.");

		if (user == null) {
			return respuesta;
		}

		codigos.entrySet().removeIf(entrada -> Instant.now().isAfter(entrada.getValue().getExpiracion()));

		String codigo = String.format("%06d", random.nextInt(1_000_000));

		RegistroCodigo registro = new RegistroCodigo(calcularHash(codigo), Instant.now().plus(Duration.ofMinutes(10)));

		codigos.put(correoNormalizado, registro);

		try {
			emailService.enviarCodigoRecuperacion(user.getUsername(), user.getNombre(), codigo);

		} catch (RuntimeException e) {
			codigos.remove(correoNormalizado, registro);
			throw e;
		}

		return respuesta;
	}

	public Map<String, Object> confirmarRecuperacion(String correo, String codigo, String nuevaPassword) {

		if (correo == null || correo.isBlank()) {
			throw new DatoInvalidoException("El correo es obligatorio");
		}

		if (codigo == null || !codigo.matches("\\d{6}")) {
			throw new DatoInvalidoException("El código debe tener 6 dígitos");
		}

		if (nuevaPassword == null || nuevaPassword.length() < 8) {
			throw new DatoInvalidoException("La contraseña debe tener mínimo 8 caracteres");
		}

		String correoNormalizado = correo.trim().toLowerCase();

		RegistroCodigo registro = codigos.get(correoNormalizado);

		if (registro == null) {
			throw new DatoInvalidoException("El código no existe o ya fue utilizado");
		}

		synchronized (registro) {

			if (codigos.get(correoNormalizado) != registro) {
				throw new DatoInvalidoException("El código no es válido");
			}

			if (Instant.now().isAfter(registro.getExpiracion())) {
				codigos.remove(correoNormalizado, registro);
				throw new DatoInvalidoException("El código ha vencido. Solicita uno nuevo");
			}

			String codigoHash = calcularHash(codigo);

			boolean codigoValido = MessageDigest.isEqual(registro.getCodigoHash().getBytes(StandardCharsets.UTF_8),
					codigoHash.getBytes(StandardCharsets.UTF_8));

			if (!codigoValido) {
				throw new DatoInvalidoException("El código de recuperación es incorrecto");
			}

			User user = userRepository.findByUsername(correo.trim())
					.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

			user.setPassword(passwordEncoder.encode(nuevaPassword));

			userRepository.save(user);

			codigos.remove(correoNormalizado, registro);
		}

		Map<String, Object> respuesta = new HashMap<>();
		respuesta.put("estado", "EXITO");
		respuesta.put("mensaje", "La contraseña fue actualizada correctamente");

		return respuesta;
	}

	private String calcularHash(String codigo) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] resultado = digest.digest(codigo.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(resultado);

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No fue posible procesar el código de recuperación", e);
		}
	}

	private static class RegistroCodigo {

		private final String codigoHash;
		private final Instant expiracion;

		public RegistroCodigo(String codigoHash, Instant expiracion) {
			this.codigoHash = codigoHash;
			this.expiracion = expiracion;
		}

		public String getCodigoHash() {
			return codigoHash;
		}

		public Instant getExpiracion() {
			return expiracion;
		}
	}
}