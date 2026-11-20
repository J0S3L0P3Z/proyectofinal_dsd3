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

@Service
@Transactional
public class ResultadoService {

	private static final String PREFIJO_AES = "AES:";

	private final ResultadoRepository resultadoRepository;
	private final AESUtil aesUtil;

	public ResultadoService(ResultadoRepository resultadoRepository, AESUtil aesUtil) {
		this.resultadoRepository = resultadoRepository;
		this.aesUtil = aesUtil;
	}

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

	public ResultadoDTO buscarPorId(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del resultado es inválido");
		}

		Resultado resultado = resultadoRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Resultado no encontrado"));

		return convertirDTO(resultado);
	}

	public List<ResultadoDTO> buscarTodos() {

		return resultadoRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<ResultadoDTO> buscarPorScraping(Long idScraping) {

		if (idScraping == null || idScraping <= 0) {
			throw new DatoInvalidoException("El identificador del scraping es inválido");
		}

		return resultadoRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ResultadoDTO> buscarPorNombreIA(String nombreIA) {

		if (nombreIA == null || nombreIA.isBlank()) {
			throw new DatoInvalidoException("El nombre de la IA es obligatorio");
		}

		return resultadoRepository.findByNombreIA(nombreIA).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ResultadoDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del resultado es obligatorio");
		}

		return resultadoRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

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

	public void eliminar(Long id) {

		if (id == null || id <= 0) {
			throw new DatoInvalidoException("El identificador del resultado es inválido");
		}

		if (!resultadoRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Resultado no encontrado");
		}

		resultadoRepository.deleteById(id);
	}

	private String cifrarRespuesta(String respuesta) {
		return PREFIJO_AES + aesUtil.cifrar(respuesta);
	}

	private String descifrarRespuesta(String respuesta) {

		if (respuesta == null) {
			return null;
		}

		if (respuesta.startsWith(PREFIJO_AES)) {
			return aesUtil.descifrar(respuesta.substring(PREFIJO_AES.length()));
		}

		return respuesta;
	}

	private ResultadoDTO convertirDTO(Resultado resultado) {

		return new ResultadoDTO(resultado.getIdResultado(), resultado.getNombreIA(), resultado.getModelo(),
				descifrarRespuesta(resultado.getRespuesta()), resultado.getFechaAnalisis(), resultado.getEstado(),
				resultado.getIdScraping());
	}
}