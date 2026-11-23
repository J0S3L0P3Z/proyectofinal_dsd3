package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.HistorialDTO;
import co.edu.unbosque.proyectomodulotres.service.HistorialService;

/**
 * Controlador REST del historial de consultas.
 */
@RestController
@RequestMapping("/historial")
@CrossOrigin(origins = "${app.cors.origenes}")
public class HistorialController {

    /** Servicio del historial. */
    private final HistorialService historialService;

    /**
     * Constructor que recibe el servicio del historial.
     *
     * @param historialService Servicio del historial
     */
    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    /**
     * Guarda un registro del historial.
     *
     * @param historial Datos del registro
     * @return Registro guardado
     */
    @PostMapping
    public ResponseEntity<HistorialDTO> guardar(@RequestBody HistorialDTO historial) {
        return ResponseEntity.ok(historialService.guardar(historial));
    }

    /**
     * Consulta todo el historial.
     *
     * @return Lista de registros
     */
    @GetMapping
    public ResponseEntity<List<HistorialDTO>> buscarTodos() {
        return ResponseEntity.ok(historialService.buscarTodos());
    }

    /**
     * Busca un registro por su identificador.
     *
     * @param id Identificador del registro
     * @return Datos del registro
     */
    @GetMapping("/{id}")
    public ResponseEntity<HistorialDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(historialService.buscarPorId(id));
    }

    /**
     * Consulta el historial de un usuario.
     *
     * @param idUsuario Identificador del usuario
     * @return Lista de registros del usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<HistorialDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(historialService.buscarPorUsuario(idUsuario));
    }

    /**
     * Consulta el historial de un scraping.
     *
     * @param idScraping Identificador del scraping
     * @return Lista de registros del scraping
     */
    @GetMapping("/scraping/{idScraping}")
    public ResponseEntity<List<HistorialDTO>> buscarPorScraping(@PathVariable Long idScraping) {
        return ResponseEntity.ok(historialService.buscarPorScraping(idScraping));
    }

    /**
     * Consulta el historial por estado.
     *
     * @param estado Estado a buscar
     * @return Lista de registros con ese estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<HistorialDTO>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(historialService.buscarPorEstado(estado));
    }

    /**
     * Actualiza un registro del historial.
     *
     * @param id Identificador del registro
     * @param historial Nuevos datos
     * @return Registro actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<HistorialDTO> actualizar(@PathVariable Long id, @RequestBody HistorialDTO historial) {
        return ResponseEntity.ok(historialService.actualizar(id, historial));
    }

    /**
     * Elimina un registro del historial.
     *
     * @param id Identificador del registro
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        historialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
