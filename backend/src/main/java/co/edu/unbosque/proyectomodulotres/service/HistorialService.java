package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.HistorialDTO;
import co.edu.unbosque.proyectomodulotres.entity.Historial;
import co.edu.unbosque.proyectomodulotres.repository.HistorialRepository;

@Service
@Transactional
public class HistorialService {

	private final HistorialRepository historialRepository;

	public HistorialService(HistorialRepository historialRepository) {
		this.historialRepository = historialRepository;
	}

	public HistorialDTO guardar(HistorialDTO datos) {

		Historial historial = new Historial(datos.getDescripcion(), datos.getIdUsuario(), datos.getIdScraping());

		if (datos.getEstado() != null) {
			historial.setEstado(datos.getEstado());
		}

		return convertirDTO(historialRepository.save(historial));
	}

	public HistorialDTO buscarPorId(Long id) {

		Historial historial = historialRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Registro de historial no encontrado"));

		return convertirDTO(historial);
	}

	public List<HistorialDTO> buscarTodos() {

		return historialRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<HistorialDTO> buscarPorUsuario(Long idUsuario) {

		return historialRepository.findByIdUsuario(idUsuario).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<HistorialDTO> buscarPorScraping(Long idScraping) {

		return historialRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<HistorialDTO> buscarPorEstado(String estado) {

		return historialRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public HistorialDTO actualizar(Long id, HistorialDTO datos) {

		Historial historial = historialRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Registro de historial no encontrado"));

		if (datos.getDescripcion() != null) {
			historial.setDescripcion(datos.getDescripcion());
		}

		if (datos.getEstado() != null) {
			historial.setEstado(datos.getEstado());
		}

		return convertirDTO(historialRepository.save(historial));
	}

	public void eliminar(Long id) {

		if (!historialRepository.existsById(id)) {
			throw new RuntimeException("Registro de historial no encontrado");
		}

		historialRepository.deleteById(id);
	}

	private HistorialDTO convertirDTO(Historial historial) {

		return new HistorialDTO(historial.getIdHistorial(), historial.getFechaConsulta(), historial.getDescripcion(),
				historial.getEstado(), historial.getIdUsuario(), historial.getIdScraping());
	}
}
