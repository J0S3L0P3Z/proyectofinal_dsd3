package co.edu.unbosque.webscraper.util;

import co.edu.unbosque.webscraper.dto.RawPageDTO;
import co.edu.unbosque.webscraper.exception.ErrorCode;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Clasificación del estado de la página (F1.7). No mira solo el código HTTP: también la URL final y la
 * cantidad de contenido útil, porque sitios como Netflix pueden dar 403, redirigir al login o mostrar un muro con 200.
 * Nunca intenta evadir una protección: si la detecta, informa el motivo.
 */
@Component
public class PageStateClassifier {

    /** error != null → el job termina en FAILED con ese código. warnings → COMPLETED_WITH_WARNINGS. */
    public record Result(ErrorCode error, List<String> warnings) {}

    static final int MIN_TEXT_CHARS = 20;
    static final int SHORT_TEXT_CHARS = 100;
    private static final int CHALLENGE_MAX_TEXT = 1500;

    private static final List<String> CHALLENGE_MARKERS = List.of(
            "just a moment", "attention required", "verify you are human", "verifying you are human",
            "are you a robot", "captcha", "access denied", "request blocked", "unusual traffic",
            "checking your browser", "enable javascript and cookies to continue", "pardon our interruption",
            "px-captcha", "cf-challenge", "has been blocked", "bot detection");

    private static final Pattern LOGIN_PATH = Pattern.compile(
            "(?i)/(log-?in|sign-?in|signin|sso|auth(entication)?|accounts?/login|session/new)(/|$|\\?)");

    public Result classify(String requestedUrl, RawPageDTO raw, Document doc) {
        int status = raw.httpStatus();
        String text = doc.body() == null ? "" : doc.body().text();
        String probe = (doc.title() + " " + (text.length() > 2000 ? text.substring(0, 2000) : text)).toLowerCase(Locale.ROOT);
        boolean challenge = text.length() < CHALLENGE_MAX_TEXT && CHALLENGE_MARKERS.stream().anyMatch(probe::contains);

        if (status == 404 || status == 410) return fail(ErrorCode.PAGE_NOT_FOUND);
        if (status == 401) return fail(ErrorCode.LOGIN_REQUIRED);
        if (status == 403) return fail(challenge ? ErrorCode.BLOCKED_BY_SITE : ErrorCode.ACCESS_FORBIDDEN);
        if (status == 429) return fail(ErrorCode.BLOCKED_BY_SITE);
        if (status >= 500) return fail(ErrorCode.SERVER_ERROR);
        if (status >= 400) return fail(ErrorCode.HTTP_ERROR);

        if (challenge) return fail(ErrorCode.BLOCKED_BY_SITE);

        boolean redirected = !sameUrl(requestedUrl, raw.finalUrl());
        if (redirected && looksLikeLogin(requestedUrl, raw.finalUrl())) return fail(ErrorCode.LOGIN_REQUIRED);
        if (text.length() < 500 && !doc.select("input[type=password]").isEmpty()) return fail(ErrorCode.LOGIN_REQUIRED);

        if (text.length() < MIN_TEXT_CHARS) return fail(ErrorCode.EMPTY_CONTENT);

        List<String> warnings = new ArrayList<>();
        if (redirected) {
            warnings.add("La URL fue redirigida: se analizó " + raw.finalUrl() + " y no la dirección ingresada.");
        }
        if (text.length() < SHORT_TEXT_CHARS) {
            warnings.add("La página tiene muy poco texto; el análisis puede ser limitado.");
        }
        return new Result(null, warnings);
    }

    private static Result fail(ErrorCode code) {
        return new Result(code, List.of());
    }

    static boolean looksLikeLogin(String requested, String finalUrl) {
        try {
            String reqPath = URI.create(requested).getPath();
            String finalPath = URI.create(finalUrl).getPath();
            String finalFull = finalPath + (URI.create(finalUrl).getQuery() != null ? "?" + URI.create(finalUrl).getQuery() : "");
            boolean finalIsLogin = LOGIN_PATH.matcher(finalPath == null ? "" : finalPath).find() || LOGIN_PATH.matcher(finalFull).find();
            boolean requestedIsLogin = LOGIN_PATH.matcher(reqPath == null ? "" : reqPath).find();
            return finalIsLogin && !requestedIsLogin;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Compara ignorando mayúsculas, "/" final y fragmento. Cualquier otro cambio (http a https, www., ruta) cuenta como redirección. */
    static boolean sameUrl(String a, String b) {
        return normalize(a).equals(normalize(b));
    }

    private static String normalize(String url) {
        try {
            URI u = URI.create(url);
            String host = u.getHost() == null ? "" : u.getHost().toLowerCase(Locale.ROOT);
            String path = u.getPath() == null ? "" : u.getPath();
            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
            return (u.getScheme() == null ? "" : u.getScheme().toLowerCase(Locale.ROOT)) + "://" + host
                    + (u.getPort() == -1 ? "" : ":" + u.getPort()) + path
                    + (u.getQuery() == null ? "" : "?" + u.getQuery());
        } catch (RuntimeException e) {
            return url == null ? "" : url;
        }
    }
}
