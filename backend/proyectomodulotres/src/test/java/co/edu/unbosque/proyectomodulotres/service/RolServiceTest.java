package co.edu.unbosque.proyectomodulotres.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.proyectomodulotres.dto.RolDTO;
import co.edu.unbosque.proyectomodulotres.entity.Rol;
import co.edu.unbosque.proyectomodulotres.repository.RolRepository;

/**
 * Pruebas unitarias de {@link RolService} con el repositorio simulado.
 */
@ExtendWith(MockitoExtension.class)
class RolServiceTest {

	/** Repositorio de roles simulado. */
	@Mock
	private RolRepository rolRepository;

	/** Servicio que se prueba. */
	@InjectMocks
	private RolService rolService;

	/** Al crear un rol el nombre se guarda en mayúsculas y sin espacios. */
	@Test
	void crearGuardaElNombreEnMayusculas() {

		when(rolRepository.existsByNombreIgnoreCase("EDITOR")).thenReturn(false);
		when(rolRepository.save(any(Rol.class))).thenAnswer(invocacion -> {
			Rol rol = invocacion.getArgument(0);
			rol.setId(3L);
			return rol;
		});

		RolDTO creado = rolService.crear(new RolDTO(null, "  editor "));

		assertEquals(3L, creado.getId());
		assertEquals("EDITOR", creado.getNombre());
	}

	/** No se puede crear un rol que ya existe. */
	@Test
	void crearRolRepetidoLanzaError() {

		when(rolRepository.existsByNombreIgnoreCase("ADMIN")).thenReturn(true);

		assertThrows(ResponseStatusException.class, () -> rolService.crear(new RolDTO(null, "admin")));

		verify(rolRepository, never()).save(any(Rol.class));
	}

	/** No se puede crear un rol sin nombre. */
	@Test
	void crearRolSinNombreLanzaError() {
		assertThrows(ResponseStatusException.class, () -> rolService.crear(new RolDTO(null, " ")));
	}

	/** Buscar todos convierte cada rol en DTO. */
	@Test
	void buscarTodosDevuelveLosRoles() {

		Rol admin = new Rol("ADMIN", "Administrador");
		admin.setId(1L);

		Rol usuario = new Rol("USER", "Usuario");
		usuario.setId(2L);

		when(rolRepository.findAll()).thenReturn(List.of(admin, usuario));

		List<RolDTO> roles = rolService.buscarTodos();

		assertEquals(2, roles.size());
		assertEquals("USER", roles.get(1).getNombre());
	}
}
