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

/**
 * Servicio del historial de consultas. Cada scraping deja un registro con su descripción y estado.
 */
@Service
@Transactional
public class HistorialService {

	/** Repositorio del historial. */
	private final HistorialRepository historialRepository;

	/**
	 * Constructor que recibe el repositorio del historial.
	 *
	 * @param historialRepository Repositorio del historial
	 */
	public HistorialService(HistorialRepository historialRepository) {
		this.historialRepository = historialRepository;
	}

	/**
	 * Guarda un registro en el historial.
	 *
	 * @param datos Datos del registro (descripción, usuario y scraping obligatorios)
	 * @return DTO del registro guardado
	 * @throws DatoInvalidoException Si falta algún dato obligatorio
	 */
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

	/**
	 * Busca un registro por su identificador.
	 *
	 * @param id Identificador del registro
	 * @return DTO del registro
	 * @throws DatoInvalidoException Si el identificador es nulo
	 * @throws RecursoNoEncontradoException Si el registro no existe
	 */
	public HistorialDTO buscarPorId(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del historial es obligatorio.");
		}

		Historial historial = historialRepository.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Registro de historial no encontrado."));

		return convertirDTO(historial);
	}

	/**
	 * Consulta todo el historial.
	 *
	 * @return Lista de registros
	 */
	public List<HistorialDTO> buscarTodos() {

		return historialRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Consulta el historial de un usuario.
	 *
	 * @param idUsuario Identificador del usuario
	 * @return Lista de registros del usuario
	 * @throws DatoInvalidoException Si el identificador es nulo
	 */
	public List<HistorialDTO> buscarPorUsuario(Long idUsuario) {

		if (idUsuario == null) {
			throw new DatoInvalidoException("El ID del usuario es obligatorio.");
		}

		return historialRepository.findByIdUsuario(idUsuario).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Consulta el historial de un scraping.
	 *
	 * @param idScraping Identificador del scraping
	 * @return Lista de registros del scraping
	 * @throws DatoInvalidoException Si el identificador es nulo
	 */
	public List<HistorialDTO> buscarPorScraping(Long idScraping) {

		if (idScraping == null) {
			throw new DatoInvalidoException("El ID del scraping es obligatorio.");
		}

		return historialRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Consulta el historial por estado.
	 *
	 * @param estado Estado a buscar
	 * @return Lista de registros con ese estado
	 * @throws DatoInvalidoException Si el estado está vacío
	 */
	public List<HistorialDTO> buscarPorEstado(String estado) {

		if (estado == null || estado.isBlank()) {
			throw new DatoInvalidoException("El estado del historial es obligatorio.");
		}

		return historialRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	/**
	 * Actualiza la descripción y el estado de un registro.
	 *
	 * @param id Identificador del registro
	 * @param datos Nuevos datos
	 * @return DTO del registro actualizado
	 * @throws DatoInvalidoException Si el identificador o los datos son nulos
	 * @throws RecursoNoEncontradoException Si el registro no existe
	 */
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

	/**
	 * Elimina un registro del historial.
	 *
	 * @param id Identificador del registro
	 * @throws DatoInvalidoException Si el identificador es nulo
	 * @throws RecursoNoEncontradoException Si el registro no existe
	 */
	public void eliminar(Long id) {

		if (id == null) {
			throw new DatoInvalidoException("El ID del historial es obligatorio.");
		}

		if (!historialRepository.existsById(id)) {
			throw new RecursoNoEncontradoException("Registro de historial no encontrado.");
		}

		historialRepository.deleteById(id);
	}

	/**
	 * Convierte la entidad Historial en HistorialDTO.
	 *
	 * @param historial Registro a convertir
	 * @return DTO del registro
	 */
	private HistorialDTO convertirDTO(Historial historial) {

		return new HistorialDTO(historial.getIdHistorial(), historial.getFechaConsulta(), historial.getDescripcion(),
				historial.getEstado(), historial.getIdUsuario(), historial.getIdScraping());
	}
}
