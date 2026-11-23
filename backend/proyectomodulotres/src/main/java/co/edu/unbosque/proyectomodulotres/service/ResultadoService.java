package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.entity.Resultado;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;
import co.edu.unbosque.proyectomodulotres.repository.ResultadoRepository;
import co.edu.unbosque.proyectomodulotres.util.AESUtil;

/**
 * Servicio de resultados de las IA. Guarda la respuesta de cada modelo cifrada con AES (prefijo
 * {@code AES:}) y la descifra al consultarla.
 */
@Service
@Transactional
public class ResultadoService {

	/** Prefijo con el que se marcan las respuestas cifradas. */
	private static final String PREFIJO_AES = "AES:";

	/** Repositorio de resultados. */
	private final ResultadoRepository resultadoRepository;

	/** Utilidad de cifrado AES. */
	private final AESUtil aesUtil;

	/**
	 * Constructor que recibe las dependencias del servicio.
	 *
	 * @param resultadoRepository Repositorio de resultados
	 * @param aesUtil Utilidad de cifrado
	 */
	public ResultadoService(ResultadoRepository resultadoRepository, AESUtil aesUtil) {
		this.resultadoRepository = resultadoRepository;
		this.aesUtil = aesUtil;
	}

	/**
	 * Guarda el resultado de una IA cifrando su respuesta.
	 *
	 * @param datos Datos del resultado (nombre de la IA, modelo y scraping obligatorios)
	 * @return DTO del resultado guardado con la respuesta descifrada
	 * @throws DatoInvalidoException Si falta algún dato obligatorio
	 */
	public ResultadoDTO guardar(ResultadoDTO datos) {

		if (datos == null) {
			throw new DatoInvalidoException("Los datos del resultado son obligatorios");
		}

		if (datos.getNombreIA() == null || datos.getNombreIA().isBlank()) {
			throw new DatoInvalidoException("El nombre de la IA es obligatorio");
		}

		if (datos.getModelo() == null || datos.getModelo().isBlank()) {
			throw new DatoInvalidoException("El modelo de IA es obligatorio");
		}

		if (datos.getIdScraping() == null) {
			throw new DatoInvalidoException("El identificador del scraping es obligatorio");
		}

		Resultado resultado = new Resultado(datos.getNombreIA(), datos.getModelo(), datos.getIdScraping());

		if (datos.getRespuesta() != null) {
			resultado.setRespuesta(cifrarRespuesta(datos.getRespuesta()));
		}

		if (datos.getEstado() != null) {
			resultado.setEstado(datos.getEstado());
		}

		return convertirDTO(resultadoRepository.save(resultado));
	}

	/**
	 * Busca un resultado por su identificador.
	 *
	 * @param id Identificador del resultado
	 * @return DTO del resultado
	 * @throws DatoInvalidoException Si el identificador no es válido
	 * @throws RecursoNoEncontradoException Si el resultado no existe
	 */
	public ResultadoDTO buscarPorId(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del resultado es inválido");
		}

		Resultado resultado = resultadoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Resultado no encontrado"));

		return convertirDTO(resultado);
	}

	/**
	 * Consulta todos los resultados.
	 *
	 * @return Lista de resultados
	 */
	public List<ResultadoDTO> buscarTodos() {

		return resultadoRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Consulta los resultados de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Lista de resultados del scraping
	 * @throws DatoInvalidoException Si el identificador no es válido
	 */
	public List<ResultadoDTO> buscarPorScraping(Long idScraping) {

		if (idScraping == null || idScraping <= 0) {
			throw new DatoInvalidoException("El identificador del scraping es inválido");
		}

		return resultadoRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Consulta los resultados de una IA.
	 *
	 * @param nombreIA Nombre de la IA
	 * @return Lista de resultados de esa IA
	 * @throws DatoInvalidoException Si el nombre está vacío
	 */
	public List<ResultadoDTO> buscarPorNombreIA(String nombreIA) {

		if (nombreIA == null || nombreIA.isBlank()) {
			throw new DatoInvalidoException("El nombre de la IA es obligatorio");
		}

		return resultadoRepository.findByNombreIA(nombreIA).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Consulta los resultados por estado.
	 *
	 * @param estado Estado a buscar
	 * @return Lista de resultados con ese estado
	 * @throws DatoInvalidoException Si el estado está vacío
	 */
	public List<ResultadoDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del resultado es obligatorio");
		}

		return resultadoRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Actualiza un resultado. Solo cambia los campos que llegan con valor.
	 *
	 * @param id Identificador del resultado
	 * @param datos Nuevos datos
	 * @return DTO del resultado actualizado
	 * @throws DatoInvalidoException Si el identificador o los datos no son válidos
	 * @throws RecursoNoEncontradoException Si el resultado no existe
	 */
	public ResultadoDTO actualizar(Long id, ResultadoDTO datos) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del resultado es inválido");
		}

		if (datos == null) {
			throw new DatoInvalidoException("Los datos para actualizar el resultado son obligatorios");
		}

		Resultado resultado = resultadoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Resultado no encontrado"));

		if (datos.getNombreIA() != null) {
			resultado.setNombreIA(datos.getNombreIA());
		}

		if (datos.getModelo() != null) {
			resultado.setModelo(datos.getModelo());
		}

		if (datos.getRespuesta() != null) {
			resultado.setRespuesta(cifrarRespuesta(datos.getRespuesta()));
		}

		if (datos.getEstado() != null) {
			resultado.setEstado(datos.getEstado());
		}

		return convertirDTO(resultadoRepository.save(resultado));
	}

	/**
	 * Elimina un resultado.
	 *
	 * @param id Identificador del resultado
	 * @throws DatoInvalidoException Si el identificador no es válido
	 * @throws RecursoNoEncontradoException Si el resultado no existe
	 */
	public void eliminar(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del resultado es inválido");
		}

		if (!resultadoRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Resultado no encontrado");
		}

		resultadoRepository.deleteById(id);
	}

	/**
	 * Cifra una respuesta y le agrega el prefijo {@code AES:}.
	 *
	 * @param respuesta Respuesta en texto plano
	 * @return Respuesta cifrada con prefijo
	 */
	private String cifrarRespuesta(String respuesta) {
		return PREFIJO_AES + aesUtil.cifrar(respuesta);
	}

	/**
	 * Descifra una respuesta guardada. Si no tiene el prefijo {@code AES:} (datos antiguos) la
	 * devuelve tal cual.
	 *
	 * @param respuesta Respuesta guardada
	 * @return Respuesta en texto plano, o null si era null
	 */
	private String descifrarRespuesta(String respuesta) {

		if (respuesta == null) {
			return null;
		}

		if (respuesta.startsWith(PREFIJO_AES)) {
			return aesUtil.descifrar(respuesta.substring(PREFIJO_AES.length()));
		}

		return respuesta;
	}

	/**
	 * Convierte la entidad Resultado en ResultadoDTO descifrando la respuesta.
	 *
	 * @param resultado Resultado a convertir
	 * @return DTO del resultado
	 */
	private ResultadoDTO convertirDTO(Resultado resultado) {

		return new ResultadoDTO(resultado.getIdResultado(), resultado.getNombreIA(), resultado.getModelo(),
				descifrarRespuesta(resultado.getRespuesta()), resultado.getFechaAnalisis(), resultado.getEstado(),
				resultado.getIdScraping());
	}
}