package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Construye los tres proveedores a partir de las variables de entorno AI_OLLAMA_*, AI_GEMINI_* y AI_OPENROUTER_*. */
@Component
public class AiProviderRegistry {
    private static final List<String> ORDER = List.of("ollama", "gemini", "openrouter");
    private static final Map<String, String> LABELS = Map.of(
            "ollama", "Ollama Cloud",
            "gemini", "Google Gemini",
            "openrouter", "OpenRouter");

    private final List<AiProvider> providers;

    public AiProviderRegistry(AppProperties props) {
        List<AiProvider> list = new ArrayList<>();
        Map<String, AppProperties.Ai.Provider> cfg = props.ai().providers();
        for (String id : ORDER) {
            if (cfg != null && cfg.containsKey(id)) {
                list.add(new OpenAiCompatibleProvider(id, LABELS.get(id), cfg.get(id), props.ai().httpTimeoutSeconds()));
            }
        }
        this.providers = List.copyOf(list);
    }

    public List<AiProvider> providers() {
        return providers;
    }
}
