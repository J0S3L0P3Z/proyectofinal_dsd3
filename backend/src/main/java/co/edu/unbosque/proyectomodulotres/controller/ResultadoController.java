package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.ResultadoDTO;
import co.edu.unbosque.proyectomodulotres.service.ResultadoService;

@RestController
@RequestMapping("/resultado")
@CrossOrigin(origins = "http://localhost:4200")
public class ResultadoController {

    private final ResultadoService resultadoService;

    public ResultadoController(ResultadoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @PostMapping
    public ResponseEntity<ResultadoDTO> guardar(@RequestBody ResultadoDTO resultado) {
        return ResponseEntity.ok(resultadoService.guardar(resultado));
    }

    @GetMapping
    public ResponseEntity<List<ResultadoDTO>> buscarTodos() {
        return ResponseEntity.ok(resultadoService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultadoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(resultadoService.buscarPorId(id));
    }

    @GetMapping("/scraping/{idScraping}")
    public ResponseEntity<List<ResultadoDTO>> buscarPorScraping(@PathVariable Long idScraping) {
        return ResponseEntity.ok(resultadoService.buscarPorScraping(idScraping));
    }

    @GetMapping("/ia/{nombreIA}")
    public ResponseEntity<List<ResultadoDTO>> buscarPorNombreIA(@PathVariable String nombreIA) {
        return ResponseEntity.ok(resultadoService.buscarPorNombreIA(nombreIA));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ResultadoDTO>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(resultadoService.buscarPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultadoDTO> actualizar(@PathVariable Long id, @RequestBody ResultadoDTO resultado) {
        return ResponseEntity.ok(resultadoService.actualizar(id, resultado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        resultadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}