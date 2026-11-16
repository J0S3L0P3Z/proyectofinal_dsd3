package co.edu.unbosque.webscraper.util;

import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Extrae y valida el JSON de la IA (F2.6). Aunque el modelo lo envuelva en texto o en bloques de código,
 * se recupera. La salida se NORMALIZA: solo los campos del esquema, con límites de tamaño, y el número de
 * imágenes se fija con el dato calculado por código (no el que diga la IA).
 */
public class AiResponseParser {
    static final int MAX_TEXT = 2000;
    static final int MAX_ITEMS = 30;

    private final ObjectMapper mapper;

    public AiResponseParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonNode parseAndValidate(String raw, int imageCount) {
        String json = extractJson(raw);
        JsonNode node;
        try {
            node = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw invalid("el JSON está mal formado: " + shorten(e.getOriginalMessage()));
        }
        if (node == null || !node.isObject()) throw invalid("la respuesta debe ser un objeto JSON");

        ObjectNode out = mapper.createObjectNode();
        out.put("deQueTrata", requireText(node, "deQueTrata"));
        out.set("informacionPrincipal", requireStringArray(node, "informacionPrincipal"));
        out.set("personasMencionadas", personas(node));

        ObjectNode img = out.putObject("imagenes");
        img.put("cantidad", imageCount);
        img.put("comentario", optionalText(node.path("imagenes").path("comentario")));

        JsonNode tec = node.get("tecnologia");
        if (tec == null || !tec.isObject()) throw invalid("falta el objeto \"tecnologia\"");
        ObjectNode t = out.putObject("tecnologia");
        String nombre = optionalText(tec.get("nombre"));
        t.put("nombre", nombre.isBlank() ? "no determinado" : nombre);
        t.put("evidencia", optionalText(tec.get("evidencia")));

        ArrayNode obs = mapper.createArrayNode();
        JsonNode obsNode = node.get("observaciones");
        if (obsNode != null && obsNode.isArray()) {
            for (JsonNode n : obsNode) {
                if (obs.size() >= MAX_ITEMS) break;
                if (n.isTextual() && !n.asText().isBlank()) obs.add(clip(n.asText()));
            }
        }
        out.set("observaciones", obs);
        return out;
    }

    /** Quita bloques de razonamiento (&lt;think&gt;) y toma desde la primera "{" hasta la última "}". */
    static String extractJson(String raw) {
        if (raw == null) throw invalid("respuesta vacía");
        String s = raw.replaceAll("(?s)<think>.*?</think>", "").strip();
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start < 0 || end <= start) throw invalid("no se encontró un objeto JSON en la respuesta");
        return s.substring(start, end + 1);
    }

    private ArrayNode personas(JsonNode node) {
        JsonNode arr = node.get("personasMencionadas");
        if (arr == null || !arr.isArray()) throw invalid("\"personasMencionadas\" debe ser una lista (vacía si no hay nombres)");
        ArrayNode out = mapper.createArrayNode();
        for (JsonNode p : arr) {
            if (out.size() >= MAX_ITEMS) break;
            String nombre;
            String contexto = "";
            if (p.isTextual()) {
                nombre = p.asText();
            } else if (p.isObject()) {
                nombre = optionalText(p.get("nombre"));
                contexto = optionalText(p.get("contexto"));
            } else {
                continue;
            }
            if (nombre.isBlank()) continue;
            ObjectNode o = out.addObject();
            o.put("nombre", clip(nombre));
            o.put("contexto", contexto);
        }
        return out;
    }

    private ArrayNode requireStringArray(JsonNode node, String field) {
        JsonNode arr = node.get(field);
        if (arr == null || !arr.isArray()) throw invalid("\"" + field + "\" debe ser una lista de textos");
        ArrayNode out = mapper.createArrayNode();
        for (JsonNode n : arr) {
            if (out.size() >= MAX_ITEMS) break;
            if (n.isTextual() && !n.asText().isBlank()) out.add(clip(n.asText()));
        }
        if (out.isEmpty()) throw invalid("\"" + field + "\" no puede estar vacío");
        return out;
    }

    private String requireText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || !n.isTextual() || n.asText().isBlank()) throw invalid("falta el texto \"" + field + "\"");
        return clip(n.asText());
    }

    private static String optionalText(JsonNode n) {
        return n != null && n.isTextual() ? clip(n.asText()) : "";
    }

    private static String clip(String s) {
        String t = s.strip();
        return t.length() <= MAX_TEXT ? t : t.substring(0, MAX_TEXT);
    }

    private static String shorten(String s) {
        return s == null ? "" : (s.length() > 120 ? s.substring(0, 120) : s);
    }

    private static AppException invalid(String reason) {
        return new AppException(ErrorCode.AI_INVALID_RESPONSE, reason);
    }
}
