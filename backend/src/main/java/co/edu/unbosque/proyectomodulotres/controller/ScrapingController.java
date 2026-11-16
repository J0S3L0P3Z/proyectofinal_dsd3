package co.edu.unbosque.proyectomodulotres.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import co.edu.unbosque.proyectomodulotres.dto.ScrapingDTO;
import co.edu.unbosque.proyectomodulotres.service.ScrapingService;

@RestController
@RequestMapping("/scraping")
@CrossOrigin(origins = "http://localhost:4200")
public class ScrapingController {

    private final ScrapingService scrapingService;

    public ScrapingController(ScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    @PostMapping
    public ResponseEntity<ScrapingDTO> guardar(@RequestBody ScrapingDTO scraping) {
        return ResponseEntity.ok(scrapingService.guardar(scraping));
    }

    @GetMapping
    public ResponseEntity<List<ScrapingDTO>> buscarTodos() {
        return ResponseEntity.ok(scrapingService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScrapingDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(scrapingService.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ScrapingDTO>> buscarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(scrapingService.buscarPorUsuario(idUsuario));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ScrapingDTO>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(scrapingService.buscarPorEstado(estado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScrapingDTO> actualizar(@PathVariable Long id, @RequestBody ScrapingDTO scraping) {
        return ResponseEntity.ok(scrapingService.actualizar(id, scraping));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        scrapingService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}