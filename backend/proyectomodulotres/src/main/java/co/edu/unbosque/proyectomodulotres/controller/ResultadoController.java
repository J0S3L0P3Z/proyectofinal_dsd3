package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.service.ResultadoService;

/**
 * Controlador REST de los resultados de las IA.
 */
@RestController
@RequestMapping("/resultado")
@CrossOrigin(origins = "${app.cors.origenes}")
public class ResultadoController {

    /** Servicio de resultados. */
    private final ResultadoService resultadoService;

    /**
     * Constructor que recibe el servicio de resultados.
     *
     * @param resultadoService Servicio de resultados
     */
    public ResultadoController(ResultadoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    /**
     * Guarda un resultado.
     *
     * @param resultado Datos del resultado
     * @return Resultado guardado
     */
    @PostMapping
    public ResponseEntity<ResultadoDTO> guardar(@RequestBody ResultadoDTO resultado) {
        return ResponseEntity.ok(resultadoService.guardar(resultado));
    }

    /**
     * Consulta todos los resultados.
     *
     * @return Lista de resultados
     */
    @GetMapping
    public ResponseEntity<List<ResultadoDTO>> buscarTodos() {
        return ResponseEntity.ok(resultadoService.buscarTodos());
    }

    /**
     * Busca un resultado por su identificador.
     *
     * @param id Identificador del resultado
     * @return Datos del resultado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResultadoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(resultadoService.buscarPorId(id));
    }

    /**
     * Consulta los resultados de un scraping.
     *
     * @param idScraping Identificador del scraping
     * @return Lista de resultados del scraping
     */
    @GetMapping("/scraping/{idScraping}")
    public ResponseEntity<List<ResultadoDTO>> buscarPorScraping(@PathVariable Long idScraping) {
        return ResponseEntity.ok(resultadoService.buscarPorScraping(idScraping));
    }

    /**
     * Consulta los resultados de una IA.
     *
     * @param nombreIA Nombre de la IA (por ejemplo GEMINI_GENERAL)
     * @return Lista de resultados de esa IA
     */
    @GetMapping("/ia/{nombreIA}")
    public ResponseEntity<List<ResultadoDTO>> buscarPorNombreIA(@PathVariable String nombreIA) {
        return ResponseEntity.ok(resultadoService.buscarPorNombreIA(nombreIA));
    }

    /**
     * Consulta los resultados por estado.
     *
     * @param estado Estado a buscar (COMPLETADO o ERROR)
     * @return Lista de resultados con ese estado
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ResultadoDTO>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(resultadoService.buscarPorEstado(estado));
    }

    /**
     * Actualiza un resultado.
     *
     * @param id Identificador del resultado
     * @param resultado Nuevos datos
     * @return Resultado actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResultadoDTO> actualizar(@PathVariable Long id, @RequestBody ResultadoDTO resultado) {
        return ResponseEntity.ok(resultadoService.actualizar(id, resultado));
    }

    /**
     * Elimina un resultado.
     *
     * @param id Identificador del resultado
     * @return Respuesta sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        resultadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
