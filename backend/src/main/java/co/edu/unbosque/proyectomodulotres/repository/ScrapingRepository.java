package co.edu.unbosque.proyectomodulotres.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.proyectomodulotres.entity.Scraping;

@Repository
public interface ScrapingRepository extends JpaRepository<Scraping, Long> {

    List<Scraping> findByIdUsuario(Long idUsuario);

    List<Scraping> findByEstado(String estado);

}