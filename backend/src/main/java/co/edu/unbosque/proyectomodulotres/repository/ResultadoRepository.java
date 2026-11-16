package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Resultado;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Long> {

    List<Resultado> findByIdScraping(Long idScraping);

    List<Resultado> findByNombreIA(String nombreIA);

    List<Resultado> findByEstado(String estado);

}