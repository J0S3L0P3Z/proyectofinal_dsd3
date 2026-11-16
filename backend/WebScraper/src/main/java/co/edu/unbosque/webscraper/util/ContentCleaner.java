package co.edu.unbosque.webscraper.util;

import co.edu.unbosque.webscraper.dto.CleanContentDTO;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Limpieza del contenido para la IA (F1.5). IMPORTANTE: se ejecuta DESPUÉS de contar y detectar el framework
 * sobre el DOM completo; así la limpieza no borra la evidencia. Trabaja sobre una copia del documento.
 */
@Component
public class ContentCleaner {
    static final String TRUNCATION_MARK = "\n[...contenido recortado por límite de tokens...]";
    private static final Set<String> KEEP_ATTRS = Set.of("href", "src", "alt", "title", "lang");

    /**
     * @param preferredFormat "HTML" o "MARKDOWN"
     * @param maxTokens       presupuesto (se estima como caracteres / 4)
     */
    public CleanContentDTO clean(Document source, String preferredFormat, int maxTokens) {
        int originalChars = source.html().length();
        Document doc = source.clone();
        doc.outputSettings().prettyPrint(false);

        doc.select("script, style, svg, noscript, iframe, template, canvas, object, embed, link, meta, head").remove();
        removeComments(doc);
        for (Element el : doc.select("[src^=data:]")) el.attr("src", "");
        for (Element el : doc.getAllElements()) {
            List<String> toRemove = new ArrayList<>();
            for (Attribute a : el.attributes()) {
                if (!KEEP_ATTRS.contains(a.getKey().toLowerCase(Locale.ROOT))) toRemove.add(a.getKey());
            }
            toRemove.forEach(el::removeAttr);
        }

        Element body = doc.body();
        boolean markdown = "MARKDOWN".equalsIgnoreCase(preferredFormat);
        String format = markdown ? "MARKDOWN" : "HTML";
        String text = markdown ? toMarkdown(body) : compact(body.html());

        if (estimateTokens(text) > maxTokens && !markdown) {
            text = toMarkdown(body);
            format = "MARKDOWN";
        }
        boolean truncated = false;
        long maxChars = (long) maxTokens * 4;
        if (text.length() > maxChars) {
            text = text.substring(0, (int) maxChars) + TRUNCATION_MARK;
            truncated = true;
        }
        return new CleanContentDTO(text, format, truncated, originalChars, text.length(), estimateTokens(text));
    }

    static int estimateTokens(String s) {
        return s.length() / 4;
    }

    private static void removeComments(Document doc) {
        List<Node> comments = new ArrayList<>();
        doc.traverse((node, depth) -> {
            if (node instanceof Comment) comments.add(node);
        });
        comments.forEach(Node::remove);
    }

    private static String compact(String html) {
        return html.replaceAll("[ \\t]+", " ").replaceAll("\\s*\\n\\s*", "\n").trim();
    }

    static String toMarkdown(Element root) {
        StringBuilder sb = new StringBuilder();
        walk(root, sb, 0);
        return sb.toString()
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" ?\\n ?", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private static void walk(Node node, StringBuilder sb, int depth) {
        if (depth > 200) return;
        if (node instanceof TextNode t) {
            String s = t.text();
            if (!s.isBlank()) sb.append(s.strip()).append(' ');
            return;
        }
        if (!(node instanceof Element e)) return;

        String tag = e.normalName();
        switch (tag) {
            case "h1", "h2", "h3", "h4", "h5", "h6" -> {
                String text = e.text().trim();
                if (!text.isEmpty()) {
                    sb.append("\n\n").append("#".repeat(tag.charAt(1) - '0')).append(' ').append(text).append("\n\n");
                }
                return;
            }
            case "a" -> {
                String text = e.text().trim();
                String href = e.attr("href");
                if (!text.isEmpty()) {
                    sb.append(href.isEmpty() ? text : "[" + text + "](" + href + ")").append(' ');
                }
                return;
            }
            case "img" -> {
                String alt = e.attr("alt").trim();
                if (!alt.isEmpty()) sb.append("![").append(alt).append("] ");
                return;
            }
            case "li" -> sb.append("\n- ");
            case "br" -> sb.append('\n');
            case "p", "div", "section", "article", "header", "footer", "main", "ul", "ol", "table", "tr", "blockquote", "pre" ->
                    sb.append("\n");
            default -> { }
        }
        for (Node child : e.childNodes()) walk(child, sb, depth + 1);
        if (tag.equals("p") || tag.equals("blockquote")) sb.append("\n");
    }
}
