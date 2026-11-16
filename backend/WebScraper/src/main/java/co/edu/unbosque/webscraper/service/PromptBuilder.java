package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.dto.ChatMessageDTO;
import co.edu.unbosque.webscraper.dto.CleanContentDTO;
import co.edu.unbosque.webscraper.dto.PageFactsDTO;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Arma los mensajes para las IAs. El prompt vive en un archivo versionado (resources/prompts/analysis-v1.md),
 * no dentro del código. Es el MISMO para las tres IAs: las diferencias de perspectiva vienen de los modelos.
 */
@Component
public class PromptBuilder {
    private static final String SEPARATOR = "=== USER ===";

    private final ObjectMapper mapper;
    private final String systemPrompt;
    private final String userTemplate;

    public PromptBuilder(ObjectMapper mapper, @Value("classpath:prompts/analysis-v1.md") Resource prompt) throws IOException {
        this.mapper = mapper;
        String text = prompt.getContentAsString(StandardCharsets.UTF_8);
        int idx = text.indexOf(SEPARATOR);
        if (idx < 0) throw new IllegalStateException("El prompt debe contener la línea '" + SEPARATOR + "'");
        this.systemPrompt = text.substring(0, idx).strip();
        this.userTemplate = text.substring(idx + SEPARATOR.length()).strip();
    }

    public List<ChatMessageDTO> build(PageFactsDTO facts, CleanContentDTO content) {
        String factsJson;
        try {
            factsJson = mapper.writeValueAsString(facts);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudieron serializar los hechos", e);
        }
        String safeContent = content.text().replace("</contenido_pagina>", "");
        String note = content.truncated() ? " — OJO: el contenido fue recortado por tamaño" : "";

        String user = userTemplate
                .replace("{{FACTS}}", factsJson)
                .replace("{{CONTENT_FORMAT}}", content.format())
                .replace("{{TRUNCATED_NOTE}}", note)
                .replace("{{CONTENT}}", safeContent);
        return List.of(ChatMessageDTO.system(systemPrompt), ChatMessageDTO.user(user));
    }
}
