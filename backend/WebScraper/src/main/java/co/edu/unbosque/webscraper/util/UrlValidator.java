package co.edu.unbosque.webscraper.util;

import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;
import co.edu.unbosque.webscraper.exception.UrlNotScrapableException;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Validación de URL con protección anti-SSRF (F1.6). Clase sin dependencias de Spring para poder probarla sola.
 *
 * <ul>
 *   <li>Solo http y https, sin credenciales dentro de la URL.</li>
 *   <li>Resuelve el DNS y rechaza loopback, IPs privadas, link-local (incluye 169.254.169.254 de metadatos de nube),
 *       CGNAT, multicast y rangos reservados.</li>
 * </ul>
 * Límite conocido: Chromium vuelve a resolver el DNS por su cuenta, así que un ataque de "DNS rebinding"
 * muy específico no queda cubierto al 100 %. Por eso WebScraperService revalida cada petición de red.
 */
public class UrlValidator {
    private static final int MAX_URL_LENGTH = 2048;

    /** Validación completa para la API: formato + host público. Un dominio que no resuelve NO se rechaza aquí (falla luego como error de red). */
    public URI validate(String raw) {
        URI uri = parse(raw);
        assertPublicHost(uri, false);
        return uri;
    }

    /** Solo formato (sin DNS). Si falta el esquema se asume https://. */
    public URI parse(String raw) {
        if (raw == null || raw.isBlank()) throw new AppException(ErrorCode.INVALID_URL, "URL vacía");
        String s = raw.trim();
        if (s.length() > MAX_URL_LENGTH) throw new AppException(ErrorCode.INVALID_URL, "URL demasiado larga");
        if (s.chars().anyMatch(c -> Character.isWhitespace(c) || Character.isISOControl(c))) {
            throw new AppException(ErrorCode.INVALID_URL, "la URL contiene espacios o caracteres de control");
        }
        if (!s.contains("://")) s = "https://" + s;

        URI uri;
        try {
            uri = new URI(s);
        } catch (URISyntaxException e) {
            throw new AppException(ErrorCode.INVALID_URL, "sintaxis: " + e.getReason(), e);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new AppException(ErrorCode.INVALID_URL, "esquema no permitido: " + scheme);
        }
        if (uri.getUserInfo() != null) throw new AppException(ErrorCode.INVALID_URL, "no se permiten credenciales en la URL");
        String host = uri.getHost();
        if (host == null || host.isBlank()) throw new AppException(ErrorCode.INVALID_URL, "sin host");
        return uri;
    }

    /**
     * Comprueba que el host resuelva solo a direcciones públicas.
     *
     * @param failIfUnresolvable true = un dominio inexistente lanza NETWORK_ERROR (pipeline);
     *                           false = se deja pasar (la API no lo bloquea: el job fallará con el error correcto).
     */
    public void assertPublicHost(URI uri, boolean failIfUnresolvable) {
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.equals("localhost") || host.endsWith(".localhost")) {
            throw new AppException(ErrorCode.URL_NOT_ALLOWED, "host local: " + host);
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            if (failIfUnresolvable) throw new UrlNotScrapableException(ErrorCode.NETWORK_ERROR, "DNS no resuelve: " + host, e);
            return;
        }
        for (InetAddress a : addresses) {
            if (isForbidden(a)) {
                throw new AppException(ErrorCode.URL_NOT_ALLOWED, host + " -> " + a.getHostAddress());
            }
        }
    }

    static boolean isForbidden(InetAddress a) {
        if (a.isAnyLocalAddress() || a.isLoopbackAddress() || a.isLinkLocalAddress()
                || a.isSiteLocalAddress() || a.isMulticastAddress()) {
            return true;
        }
        byte[] b = a.getAddress();
        if (b.length == 4) {
            int b0 = b[0] & 0xff;
            int b1 = b[1] & 0xff;
            int b2 = b[2] & 0xff;
            if (b0 == 0) return true;
            if (b0 == 100 && b1 >= 64 && b1 <= 127) return true;
            if (b0 == 192 && b1 == 0 && b2 == 0) return true;
            if (b0 == 198 && (b1 == 18 || b1 == 19)) return true;
            if (b0 >= 240) return true;
        } else if (b.length == 16) {
            if ((b[0] & 0xfe) == 0xfc) return true;
        }
        return false;
    }
}
