package co.edu.unbosque.webscraper.util;

import co.edu.unbosque.webscraper.dto.PageFactsDTO.Technology;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Detección por "huellas" pequeñas (F1.4): meta generator, URLs de scripts/estilos, atributos del DOM,
 * variables globales del navegador y cabeceras HTTP. Si no hay señales claras devuelve lista vacía
 * (la IA responde "no determinado") en lugar de adivinar.
 * Debe ejecutarse sobre el DOM completo, ANTES de limpiar el contenido.
 */
@Component
public class FrameworkDetector {

    private record UrlRule(String needle, String name, String category) {}

    private static final List<UrlRule> URL_RULES = List.of(
            new UrlRule("/wp-content/", "WordPress", "CMS"),
            new UrlRule("/wp-includes/", "WordPress", "CMS"),
            new UrlRule("/_next/", "Next.js", "Framework"),
            new UrlRule("/_nuxt/", "Nuxt", "Framework"),
            new UrlRule("/_astro/", "Astro", "Framework"),
            new UrlRule("cdn.shopify.com", "Shopify", "Plataforma"),
            new UrlRule("wixstatic.com", "Wix", "Plataforma"),
            new UrlRule("squarespace", "Squarespace", "Plataforma"),
            new UrlRule("bootstrap", "Bootstrap", "Biblioteca"),
            new UrlRule("jquery", "jQuery", "Biblioteca"));

    public List<Technology> detect(Document doc, Map<String, String> headers, Map<String, Object> globals) {
        Map<String, Technology> found = new LinkedHashMap<>();
        Map<String, String> h = headers == null ? Map.of() : headers;
        Map<String, Object> g = globals == null ? Map.of() : globals;

        String generator = doc.select("meta[name=generator]").attr("content").trim();
        if (!generator.isEmpty()) {
            add(found, canonical(generator), "CMS/Generador", "meta generator: " + clip(generator, 80));
        }

        List<String> urls = new ArrayList<>();
        for (Element e : doc.select("script[src]")) urls.add(e.attr("src").toLowerCase(Locale.ROOT));
        for (Element e : doc.select("link[href]")) urls.add(e.attr("href").toLowerCase(Locale.ROOT));
        for (UrlRule rule : URL_RULES) {
            for (String u : urls) {
                if (u.contains(rule.needle())) {
                    add(found, rule.name(), rule.category(), "recurso que incluye \"" + rule.needle() + "\"");
                    break;
                }
            }
        }

        String ngVersion = doc.select("[ng-version]").attr("ng-version").trim();
        if (!ngVersion.isEmpty()) add(found, "Angular", "Framework", "atributo ng-version=" + ngVersion);
        if (!doc.select("script#__NEXT_DATA__").isEmpty() || !doc.select("#__next").isEmpty()) {
            add(found, "Next.js", "Framework", "elemento __NEXT_DATA__ o #__next en el DOM");
        }
        if (!doc.select("#__nuxt").isEmpty()) add(found, "Nuxt", "Framework", "elemento #__nuxt en el DOM");
        if (!doc.select("#___gatsby").isEmpty()) add(found, "Gatsby", "Framework", "elemento #___gatsby en el DOM");
        if (!doc.select("[data-reactroot]").isEmpty()) add(found, "React", "Biblioteca", "atributo data-reactroot");
        if (!doc.select("[data-v-app]").isEmpty()) add(found, "Vue.js", "Framework", "atributo data-v-app");

        String angularVersion = str(g.get("angularVersion"));
        if (angularVersion != null) add(found, "Angular", "Framework", "atributo ng-version=" + angularVersion);
        if (bool(g.get("angularJs"))) add(found, "AngularJS", "Framework", "objeto global window.angular");
        if (bool(g.get("nextData"))) add(found, "Next.js", "Framework", "variable global __NEXT_DATA__");
        if (bool(g.get("nuxt"))) add(found, "Nuxt", "Framework", "variable global __NUXT__");
        if (bool(g.get("gatsby"))) add(found, "Gatsby", "Framework", "variable global ___gatsby");
        if (bool(g.get("remix"))) add(found, "Remix", "Framework", "variable global __remixContext");
        if (bool(g.get("sveltekit"))) add(found, "SvelteKit", "Framework", "marcas de SvelteKit en el DOM");
        if (bool(g.get("react"))) add(found, "React", "Biblioteca", "marcas de React en el DOM o window.React");
        if (bool(g.get("vue"))) add(found, "Vue.js", "Framework", "marcas de Vue en el DOM o window.Vue");
        if (bool(g.get("shopify"))) add(found, "Shopify", "Plataforma", "objeto global window.Shopify");
        String jq = str(g.get("jquery"));
        if (jq != null) add(found, "jQuery", "Biblioteca", "window.jQuery versión " + jq);

        String powered = h.get("x-powered-by");
        if (notBlank(powered)) add(found, canonical(powered), "Backend", "cabecera X-Powered-By: " + clip(powered, 80));
        String xGenerator = h.get("x-generator");
        if (notBlank(xGenerator)) add(found, canonical(xGenerator), "CMS/Generador", "cabecera X-Generator: " + clip(xGenerator, 80));
        String server = h.get("server");
        if (notBlank(server)) add(found, clip(server.trim(), 40), "Servidor", "cabecera Server: " + clip(server, 80));
        if (h.containsKey("cf-ray")) add(found, "Cloudflare", "CDN", "cabecera cf-ray");
        if (h.containsKey("x-vercel-id")) add(found, "Vercel", "Plataforma", "cabecera x-vercel-id");
        if (h.containsKey("x-nf-request-id")) add(found, "Netlify", "Plataforma", "cabecera x-nf-request-id");
        if (h.containsKey("x-shopify-stage")) add(found, "Shopify", "Plataforma", "cabecera x-shopify-stage");
        if (h.containsKey("x-drupal-cache")) add(found, "Drupal", "CMS", "cabecera x-drupal-cache");

        return new ArrayList<>(found.values());
    }

    private static void add(Map<String, Technology> found, String name, String category, String evidence) {
        found.putIfAbsent(name.toLowerCase(Locale.ROOT), new Technology(name, category, evidence));
    }

    private static String canonical(String value) {
        String v = value.toLowerCase(Locale.ROOT);
        if (v.contains("wordpress")) return "WordPress";
        if (v.contains("drupal")) return "Drupal";
        if (v.contains("joomla")) return "Joomla";
        if (v.contains("wix")) return "Wix";
        if (v.contains("shopify")) return "Shopify";
        if (v.contains("gatsby")) return "Gatsby";
        if (v.contains("docusaurus")) return "Docusaurus";
        if (v.contains("hugo")) return "Hugo";
        if (v.contains("jekyll")) return "Jekyll";
        if (v.contains("ghost")) return "Ghost";
        if (v.contains("astro")) return "Astro";
        if (v.contains("next.js")) return "Next.js";
        if (v.contains("express")) return "Express (Node.js)";
        if (v.contains("asp.net")) return "ASP.NET";
        if (v.startsWith("php")) return "PHP";
        return clip(value.trim(), 60);
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private static boolean bool(Object o) { return o instanceof Boolean b && b; }

    private static String str(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        return s.isEmpty() || s.equals("false") ? null : s;
    }

    private static String clip(String s, int max) { return s.length() <= max ? s : s.substring(0, max); }
}
