package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.entity.Resultado;
import co.edu.unbosque.proyectomodulotres.repository.ResultadoRepository;

@Service
@Transactional
public class ResultadoService {

	private final ResultadoRepository resultadoRepository;

	public ResultadoService(ResultadoRepository resultadoRepository) {
		this.resultadoRepository = resultadoRepository;
	}

	public ResultadoDTO guardar(ResultadoDTO datos) {

		Resultado resultado = new Resultado(datos.getNombreIA(), datos.getModelo(), datos.getIdScraping());

		resultado.setRespuesta(datos.getRespuesta());

		if (datos.getEstado() != null) {
			resultado.setEstado(datos.getEstado());
		}

		return convertirDTO(resultadoRepository.save(resultado));
	}

	public ResultadoDTO buscarPorId(Long id) {

		Resultado resultado = resultadoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Resultado no encontrado"));

		return convertirDTO(resultado);
	}

	public List<ResultadoDTO> buscarTodos() {

		return resultadoRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<ResultadoDTO> buscarPorScraping(Long idScraping) {

		return resultadoRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ResultadoDTO> buscarPorNombreIA(String nombreIA) {

		return resultadoRepository.findByNombreIA(nombreIA).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ResultadoDTO> buscarPorEstado(String estado) {

		return resultadoRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public ResultadoDTO actualizar(Long id, ResultadoDTO datos) {

		Resultado resultado = resultadoRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Resultado no encontrado"));

		if (datos.getNombreIA() != null) {
			resultado.setNombreIA(datos.getNombreIA());
		}

		if (datos.getModelo() != null) {
			resultado.setModelo(datos.getModelo());
		}

		if (datos.getRespuesta() != null) {
			resultado.setRespuesta(datos.getRespuesta());
		}

		if (datos.getEstado() != null) {
			resultado.setEstado(datos.getEstado());
		}

		return convertirDTO(resultadoRepository.save(resultado));
	}

	public void eliminar(Long id) {

		if (!resultadoRepository.existsById(id)) {
			throw new RuntimeException("Resultado no encontrado");
		}

		resultadoRepository.deleteById(id);
	}

	private ResultadoDTO convertirDTO(Resultado resultado) {

		return new ResultadoDTO(resultado.getIdResultado(), resultado.getNombreIA(), resultado.getModelo(),
				resultado.getRespuesta(), resultado.getFechaAnalisis(), resultado.getEstado(),
				resultado.getIdScraping());
	}
}
