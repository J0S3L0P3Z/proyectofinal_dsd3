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

/**
 * Servicio de recuperación de contraseña. Envía al correo un código de 6 dígitos que vence en 10
 * minutos y permite cambiar la contraseña con ese código. Los códigos se guardan en memoria como
 * hash SHA-256.
 */
@Service
@Transactional
public class RecuperacionPasswordService {

	/** Repositorio de usuarios. */
	private final UserRepository userRepository;

	/** Codificador de contraseñas (BCrypt). */
	private final PasswordEncoder passwordEncoder;

	/** Servicio de correo. */
	private final NotificacionEmailService emailService;

	/** Códigos vigentes por correo (en minúsculas). */
	private final Map<String, RegistroCodigo> codigos = new ConcurrentHashMap<>();

	/** Generador aleatorio seguro para los códigos. */
	private final SecureRandom random = new SecureRandom();

	/**
	 * Constructor que recibe las dependencias del servicio.
	 *
	 * @param userRepository Repositorio de usuarios
	 * @param passwordEncoder Codificador de contraseñas
	 * @param emailService Servicio de correo
	 */
	public RecuperacionPasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			NotificacionEmailService emailService) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
	}

	/**
	 * Genera un código de recuperación y lo envía al correo. Si el correo no está registrado responde
	 * el mismo mensaje, para no revelar qué correos existen.
	 *
	 * @param correo Correo del usuario
	 * @return Mapa con el estado y el mensaje
	 * @throws DatoInvalidoException Si el correo está vacío
	 */
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

	/**
	 * Valida el código de recuperación y cambia la contraseña. El código solo se puede usar una vez.
	 *
	 * @param correo Correo del usuario
	 * @param codigo Código de 6 dígitos recibido por correo
	 * @param nuevaPassword Nueva contraseña (mínimo 8 caracteres)
	 * @return Mapa con el estado y el mensaje
	 * @throws DatoInvalidoException Si los datos no son válidos o el código no existe, venció o es
	 *     incorrecto
	 * @throws RecursoNoEncontradoException Si el usuario no existe
	 */
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

	/**
	 * Calcula el hash SHA-256 de un código.
	 *
	 * @param codigo Código en texto plano
	 * @return Hash en hexadecimal
	 */
	private String calcularHash(String codigo) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] resultado = digest.digest(codigo.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(resultado);

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No fue posible procesar el código de recuperación", e);
		}
	}

	/**
	 * Código de recuperación guardado en memoria: su hash y la fecha en que vence.
	 */
	private static class RegistroCodigo {

		/** Hash SHA-256 del código. */
		private final String codigoHash;

		/** Momento en que vence el código. */
		private final Instant expiracion;

		/**
		 * Constructor con el hash y el vencimiento.
		 *
		 * @param codigoHash Hash del código
		 * @param expiracion Momento en que vence
		 */
		public RegistroCodigo(String codigoHash, Instant expiracion) {
			this.codigoHash = codigoHash;
			this.expiracion = expiracion;
		}

		/** @return Hash del código */
		public String getCodigoHash() {
			return codigoHash;
		}

		/** @return Momento en que vence el código */
		public Instant getExpiracion() {
			return expiracion;
		}
	}
}