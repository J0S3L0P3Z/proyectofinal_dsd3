package co.edu.unbosque.webscraper.util;

import co.edu.unbosque.webscraper.dto.PageFactsDTO;
import co.edu.unbosque.webscraper.dto.RawPageDTO;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Extractor de hechos con Jsoup (F1.3). Con un DOM vacío no lanza excepciones. */
@Component
public class FactsExtractor {
    private static final int MAX_HEADINGS = 40;
    private static final int MAX_METAS = 40;
    private static final int MAX_IMAGES = 50;
    private static final int MAX_LINK_SAMPLES = 30;
    private static final int MAX_SCRIPTS = 40;

    private final FrameworkDetector detector;

    public FactsExtractor(FrameworkDetector detector) {
        this.detector = detector;
    }

    /** El documento debe haberse parseado con la URL final como baseUri (Jsoup.parse(html, finalUrl)). */
    public PageFactsDTO extract(String requestedUrl, RawPageDTO raw, Document doc) {
        Map<String, String> metas = new LinkedHashMap<>();
        for (Element m : doc.select("meta[name], meta[property]")) {
            if (metas.size() >= MAX_METAS) break;
            String key = (m.hasAttr("name") ? m.attr("name") : m.attr("property")).trim().toLowerCase(Locale.ROOT);
            String content = m.attr("content").trim();
            if (!key.isEmpty() && !content.isEmpty()) metas.putIfAbsent(key, clip(content, 300));
        }

        List<String> headings = new ArrayList<>();
        for (Element hd : doc.select("h1, h2, h3")) {
            if (headings.size() >= MAX_HEADINGS) break;
            String text = hd.text().trim();
            if (!text.isEmpty()) headings.add(hd.normalName() + ": " + clip(text, 160));
        }

        List<Element> imgs = doc.select("img");
        List<String> imageSources = new ArrayList<>();
        for (Element img : imgs) {
            if (imageSources.size() >= MAX_IMAGES) break;
            String src = img.absUrl("src");
            if (src.isEmpty()) src = img.absUrl("data-src");
            if (src.isEmpty()) src = img.attr("src");
            if (src.startsWith("data:")) src = "data:(imagen incrustada)";
            if (!src.isEmpty()) imageSources.add(clip(src, 300));
        }

        String pageHost = hostOf(raw.finalUrl());
        int internal = 0;
        int external = 0;
        int other = 0;
        List<String> internalSamples = new ArrayList<>();
        List<String> externalSamples = new ArrayList<>();
        for (Element a : doc.select("a[href]")) {
            String abs = a.absUrl("href");
            if (!(abs.startsWith("http://") || abs.startsWith("https://"))) {
                other++;
                continue;
            }
            String host = hostOf(abs);
            if (host != null && sameSite(host, pageHost)) {
                internal++;
                if (internalSamples.size() < MAX_LINK_SAMPLES) internalSamples.add(clip(abs, 200));
            } else {
                external++;
                if (externalSamples.size() < MAX_LINK_SAMPLES) externalSamples.add(clip(abs, 200));
            }
        }

        List<Element> scripts = doc.select("script");
        List<String> scriptSources = new ArrayList<>();
        for (Element s : scripts) {
            if (scriptSources.size() >= MAX_SCRIPTS) break;
            String src = s.absUrl("src");
            if (!src.isEmpty()) scriptSources.add(clip(src, 200));
        }

        String language = doc.select("html").attr("lang").trim();
        if (language.isEmpty()) language = doc.select("meta[http-equiv=content-language]").attr("content").trim();

        int textLength = doc.body() == null ? 0 : doc.body().text().length();

        return new PageFactsDTO(
                requestedUrl,
                raw.finalUrl(),
                raw.httpStatus(),
                raw.loadTimeMs(),
                doc.title().trim(),
                language.isEmpty() ? null : language,
                metas.get("description"),
                metas,
                headings,
                raw.html() == null ? 0 : raw.html().length(),
                textLength,
                imgs.size(),
                imageSources,
                internal,
                external,
                other,
                internalSamples,
                externalSamples,
                doc.select("form").size(),
                scripts.size(),
                scriptSources,
                detector.detect(doc, raw.responseHeaders(), raw.jsGlobals()));
    }

    static String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (RuntimeException e) {
            return null;
        }
    }

    static boolean sameSite(String a, String b) {
        if (a == null || b == null) return false;
        return stripWww(a).equalsIgnoreCase(stripWww(b));
    }

    private static String stripWww(String host) {
        return host.toLowerCase(Locale.ROOT).startsWith("www.") ? host.substring(4) : host;
    }

    private static String clip(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
