package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.HistorialDTO;
import co.edu.unbosque.proyectomodulotres.service.HistorialService;

@RestController
@RequestMapping("/historial")
@CrossOrigin(origins = "http://localhost:4200")
public class HistorialController {

    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @PostMapping
    public ResponseEntity<HistorialDTO> guardar(@RequestBody HistorialDTO historial) {
        return ResponseEntity.ok(historialService.guardar(historial));
    }

    @GetMapping
    public ResponseEntity<List<HistorialDTO>> buscarTodos() {
        return ResponseEntity.ok(historialService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistorialDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(historialService.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<HistorialDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(historialService.buscarPorUsuario(idUsuario));
    }

    @GetMapping("/scraping/{idScraping}")
    public ResponseEntity<List<HistorialDTO>> buscarPorScraping(@PathVariable Long idScraping) {
        return ResponseEntity.ok(historialService.buscarPorScraping(idScraping));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<HistorialDTO>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(historialService.buscarPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HistorialDTO> actualizar(@PathVariable Long id, @RequestBody HistorialDTO historial) {
        return ResponseEntity.ok(historialService.actualizar(id, historial));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        historialService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}