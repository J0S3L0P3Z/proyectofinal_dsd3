
package co.edu.unbosque.proyectomodulotres.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.proyectomodulotres.dto.ReporteDTO;
import co.edu.unbosque.proyectomodulotres.entity.Reporte;
import co.edu.unbosque.proyectomodulotres.repository.ReporteRepository;

@Service
@Transactional
public class ReporteService {

	private final ReporteRepository reporteRepository;

	public ReporteService(ReporteRepository reporteRepository) {
		this.reporteRepository = reporteRepository;
	}

	public ReporteDTO guardar(ReporteDTO datos) {

		Reporte reporte = new Reporte(datos.getNombreArchivo(), datos.getRutaArchivo(), datos.getIdScraping());

		if (datos.getEstado() != null) {
			reporte.setEstado(datos.getEstado());
		}

		return convertirDTO(reporteRepository.save(reporte));
	}

	public ReporteDTO buscarPorId(Long id) {

		Reporte reporte = reporteRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

		return convertirDTO(reporte);
	}

	public List<ReporteDTO> buscarTodos() {

		return reporteRepository.findAll().stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public List<ReporteDTO> buscarPorScraping(Long idScraping) {

		return reporteRepository.findByIdScraping(idScraping).stream().map(this::convertirDTO)
				.collect(Collectors.toList());
	}

	public List<ReporteDTO> buscarPorEstado(String estado) {

		return reporteRepository.findByEstado(estado).stream().map(this::convertirDTO).collect(Collectors.toList());
	}

	public ReporteDTO actualizar(Long id, ReporteDTO datos) {

		Reporte reporte = reporteRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

		if (datos.getNombreArchivo() != null) {
			reporte.setNombreArchivo(datos.getNombreArchivo());
		}

		if (datos.getRutaArchivo() != null) {
			reporte.setRutaArchivo(datos.getRutaArchivo());
		}

		if (datos.getEstado() != null) {
			reporte.setEstado(datos.getEstado());
		}

		return convertirDTO(reporteRepository.save(reporte));
	}

	public void eliminar(Long id) {

		if (!reporteRepository.existsById(id)) {
			throw new RuntimeException("Reporte no encontrado");
		}

		reporteRepository.deleteById(id);
	}

	private ReporteDTO convertirDTO(Reporte reporte) {

		return new ReporteDTO(reporte.getIdReporte(), reporte.getNombreArchivo(), reporte.getRutaArchivo(),
				reporte.getFechaGeneracion(), reporte.getEstado(), reporte.getIdScraping());
	}
}
