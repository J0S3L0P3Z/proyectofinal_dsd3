package co.edu.unbosque.webscraper.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controlador del estado del servicio (público). Útil para comprobar que el back está arriba. */
@RestController
@Tag(name = "Sistema", description = "Estado del servicio")
public class HealthController {

  /**
   * Estado del servicio.
   *
   * @return {"status": "UP"}
   */
  @Operation(summary = "Estado del servicio", description = "Público. Responde `UP` si el back está funcionando.")
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "UP");
  }
}
