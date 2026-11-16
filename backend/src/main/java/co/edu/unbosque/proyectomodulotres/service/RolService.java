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

@Service
@Transactional
public class RolService {

	private final RolRepository rolRepository;

	public RolService(RolRepository rolRepository) {
		this.rolRepository = rolRepository;
	}

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

	public void eliminar(Long id) {

		if (!rolRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol no existe.");
		}

		rolRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public RolDTO buscarPorId(Long id) {

		Rol rol = rolRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol no existe."));

		return convertirDTO(rol);
	}

	@Transactional(readOnly = true)
	public List<RolDTO> buscarTodos() {

		List<Rol> roles = rolRepository.findAll();
		List<RolDTO> lista = new ArrayList<>();

		for (Rol rol : roles) {
			lista.add(convertirDTO(rol));
		}

		return lista;
	}

	@Transactional(readOnly = true)
	public long contar() {
		return rolRepository.count();
	}

	private void validarRol(RolDTO dto) {

		if (dto == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar los datos del rol.");
		}

		if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del rol es obligatorio.");
		}
	}

	private RolDTO convertirDTO(Rol rol) {

		RolDTO dto = new RolDTO();

		dto.setId(rol.getId());
		dto.setNombre(rol.getNombre());

		return dto;
	}
}
