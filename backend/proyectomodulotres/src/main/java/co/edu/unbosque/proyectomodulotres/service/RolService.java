package co.edu.unbosque.proyectomodulotres.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.proyectomodulotres.dto.RolDTO;
import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;

/**
 * Servicio de roles. Permite crear, actualizar, eliminar y consultar los roles.
 */
@Service
@Transactional
public class RolService {

	/** Repositorio de roles. */
	private final RolRepository rolRepository;

	/**
	 * Constructor que recibe el repositorio de roles.
	 *
	 * @param rolRepository Repositorio de roles
	 */
	public RolService(RolRepository rolRepository) {
		this.rolRepository = rolRepository;
	}

	/**
	 * Crea un rol. El nombre se guarda en mayúsculas.
	 *
	 * @param dto Datos del rol
	 * @return Rol creado
	 * @throws ResponseStatusException Si los datos no son válidos o el rol ya existe
	 */
	public RolDTO crear(RolDTO dto) {

		validarRol(dto);

		String nombre = dto.getNombre().trim().toUpperCase();

		if (rolRepository.existsByNombreIgnoreCase(nombre)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El rol ya existe.");
		}

		Rol rol = new Rol();
		rol.setNombre(nombre);

		return convertirDTO(rolRepository.save(rol));
	}

	/**
	 * Actualiza el nombre de un rol.
	 *
	 * @param id Identificador del rol
	 * @param dto Nuevos datos
	 * @return Rol actualizado
	 * @throws ResponseStatusException Si el rol no existe o ya hay otro con ese nombre
	 */
	public RolDTO actualizar(Long id, RolDTO dto) {

		validarRol(dto);

		Rol rol = rolRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol no existe."));

		String nombre = dto.getNombre().trim().toUpperCase();

		Rol rolExistente = rolRepository.findByNombreIgnoreCase(nombre).orElse(null);

		if (rolExistente != null && !rolExistente.getId().equals(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro rol con ese nombre.");
		}

		rol.setNombre(nombre);

		return convertirDTO(rolRepository.save(rol));
	}

	/**
	 * Elimina un rol.
	 *
	 * @param id Identificador del rol
	 * @throws ResponseStatusException Si el rol no existe
	 */
	public void eliminar(Long id) {

		if (!rolRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol no existe.");
		}

		rolRepository.deleteById(id);
	}

	/**
	 * Busca un rol por su identificador.
	 *
	 * @param id Identificador del rol
	 * @return Datos del rol
	 * @throws ResponseStatusException Si el rol no existe
	 */
	@Transactional(readOnly = true)
	public RolDTO buscarPorId(Long id) {

		Rol rol = rolRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol no existe."));

		return convertirDTO(rol);
	}

	/**
	 * Consulta todos los roles.
	 *
	 * @return Lista de roles
	 */
	@Transactional(readOnly = true)
	public List<RolDTO> buscarTodos() {

		List<Rol> roles = rolRepository.findAll();
		List<RolDTO> lista = new ArrayList<>();

		for (Rol rol : roles) {
			lista.add(convertirDTO(rol));
		}

		return lista;
	}

	/**
	 * Cuenta los roles registrados.
	 *
	 * @return Cantidad de roles
	 */
	@Transactional(readOnly = true)
	public long contar() {
		return rolRepository.count();
	}

	/**
	 * Valida que se hayan enviado los datos del rol con un nombre.
	 *
	 * @param dto Datos del rol
	 * @throws ResponseStatusException Si faltan datos
	 */
	private void validarRol(RolDTO dto) {

		if (dto == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar los datos del rol.");
		}

		if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del rol es obligatorio.");
		}
	}

	/**
	 * Convierte la entidad Rol en RolDTO.
	 *
	 * @param rol Rol a convertir
	 * @return DTO del rol
	 */
	private RolDTO convertirDTO(Rol rol) {

		RolDTO dto = new RolDTO();

		dto.setId(rol.getId());
		dto.setNombre(rol.getNombre());

		return dto;
	}
}
