package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Reporte;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    List<Reporte> findByIdScraping(Long idScraping);

    List<Reporte> findByEstado(String estado);

}