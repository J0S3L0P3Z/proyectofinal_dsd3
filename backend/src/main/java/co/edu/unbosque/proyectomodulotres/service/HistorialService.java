package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.HistorialDTO;
import co.edu.unbosque.proyectomodulotres.entity.Historial;
import co.edu.unbosque.proyectomodulotres.repository.HistorialRepository;
import co.edu.unbosque.proyectomodulotres.exception.DatoInvalidoException;
import co.edu.unbosque.proyectomodulotres.exception.RecursoNoEncontradoException;

@Service
@Transactional
public class HistorialService {

	private final HistorialRepository historialRepository;

	public HistorialService(HistorialRepository historialRepository) {
		this.historialRepository = historialRepository;
	}

	public HistorialDTO guardar(HistorialDTO datos) {

		if (datos == null) {
			throw new DatoInvalidoException("Debe ingresar los datos del historial.");
		}

		if (datos.getDescripcion() == null || datos.getDescripcion().isBlank()) {
			throw new DatoInvalidoException("La descripcion del historial es obligatoria.");
		}

		if (datos.getIdUsuario() == null) {
			throw new DatoInvalidoException("El ID del usuario es obligatorio.");
		}

		if (datos.getIdScraping() == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		Historial historial = new Historial(datos.getDescripcion(), datos.getIdUsuario(), datos.getIdScraping());

		if (datos.getEstado() != null) {
			historial.setEstado(datos.getEstado());
		}

		return convertirDTO(historialRepository.save(historial));
	}

	public HistorialDTO buscarPorId(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del historial es obligatorio.");
		}

		Historial historial = historialRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Registro de historial no encontrado."));

		return convertirDTO(historial);
	}

	public List<HistorialDTO> buscarTodos() {

		return historialRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<HistorialDTO> buscarPorUsuario(Long idUsuario) {

		if (idUsuario == null) {
			throw new DatoInvalidoException("El ID del usuario es obligatorio.");
		}

		return historialRepository.findByIdUsuario(idUsuario).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<HistorialDTO> buscarPorScraping(Long idScraping) {

		if (idScraping == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		return historialRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<HistorialDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del historial es obligatorio.");
		}

		return historialRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public HistorialDTO actualizar(Long id, HistorialDTO datos) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del historial es obligatorio.");
		}

		if (datos == null) {
			throw new DatoInvalidoException("Debe ingresar los datos para actualizar el historial.");
		}

		Historial historial = historialRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Registro de historial no encontrado."));

		if (datos.getDescripcion() != null) {
			historial.setDescripcion(datos.getDescripcion());
		}

		if (datos.getEstado() != null) {
			historial.setEstado(datos.getEstado());
		}

		return convertirDTO(historialRepository.save(historial));
	}

	public void eliminar(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del historial es obligatorio.");
		}

		if (!historialRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Registro de historial no encontrado.");
		}

		historialRepository.deleteById(id);
	}

	private HistorialDTO convertirDTO(Historial historial) {

		return new HistorialDTO(historial.getIdHistorial(), historial.getFechaConsulta(), historial.getDescripcion(),
				historial.getEstado(), historial.getIdUsuario(), historial.getIdScraping());
	}
}
