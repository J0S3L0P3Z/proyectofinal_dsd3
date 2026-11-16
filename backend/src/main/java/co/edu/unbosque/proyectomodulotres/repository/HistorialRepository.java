package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Historial;

@Repository
public interface HistorialRepository extends JpaRepository<Historial, Long> {

    List<Historial> findByIdUsuario(Long idUsuario);

    List<Historial> findByIdScraping(Long idScraping);

    List<Historial> findByEstado(String estado);

}