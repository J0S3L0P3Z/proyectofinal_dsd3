package co.edu.unbosque.proyectomodulotres.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lee el código HTML de una página sin usar navegador. Se usa cuando el servidor no tiene Microsoft
 * Edge instalado y la página se descarga directamente con HttpClient.
 */
public class ExtractorHTML {

	/** Etiqueta title de la página. */
	private static final Pattern TITULO = Pattern.compile("<title[^>]*>(.*?)</title>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/** Bloques que no tienen texto visible: head, scripts, estilos, svg, plantillas y comentarios. */
	private static final Pattern BLOQUES_OCULTOS = Pattern.compile(
			"<!--.*?-->|<(head|script|style|noscript|svg|template|iframe)\\b[^>]*>.*?</\\1\\s*>",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

	/** Etiquetas que separan el texto en líneas (párrafos, títulos, listas, filas, etc.). */
	private static final Pattern SALTOS_DE_LINEA = Pattern.compile(
			"<\\s*(br|/p|/div|/li|/h[1-6]|/tr|/section|/article|/header|/footer|/nav|/ul|/ol|/table|/blockquote)\\b[^>]*>",
			Pattern.CASE_INSENSITIVE);

	/** Cualquier otra etiqueta HTML. */
	private static final Pattern ETIQUETAS = Pattern.compile("<[^>]+>");

	/** Entidades numéricas como &#225; o &#xE1;. */
	private static final Pattern ENTIDAD_NUMERICA = Pattern.compile("&#(x?)([0-9a-fA-F]+);");

	/** Constructor privado: la clase solo tiene métodos estáticos. */
	private ExtractorHTML() {
	}

	/**
	 * Obtiene el título de la página (la etiqueta title).
	 *
	 * @param html Código HTML de la página
	 * @return Título de la página, o vacío si no tiene
	 */
	public static String obtenerTitulo(String html) {

		if (html == null) {
			return "";
		}

		Matcher titulo = TITULO.matcher(html);

		if (titulo.find()) {
			return limpiarEspacios(decodificarEntidades(titulo.group(1).replaceAll("<[^>]+>", "")));
		}

		return "";
	}

	/**
	 * Cuenta cuántas veces aparece una etiqueta en el HTML, por ejemplo "img" o "a".
	 *
	 * @param html Código HTML de la página
	 * @param etiqueta Nombre de la etiqueta sin los símbolos &lt; &gt;
	 * @return Cantidad de etiquetas encontradas
	 */
	public static int contarEtiquetas(String html, String etiqueta) {

		if (html == null || etiqueta == null || etiqueta.isBlank()) {
			return 0;
		}

		String sinComentarios = html.replaceAll("(?s)<!--.*?-->", "");

		Matcher coincidencias = Pattern
				.compile("<" + Pattern.quote(etiqueta) + "(\\s|>|/)", Pattern.CASE_INSENSITIVE)
				.matcher(sinComentarios);

		int cantidad = 0;

		while (coincidencias.find()) {
			cantidad++;
		}

		return cantidad;
	}

	/**
	 * Obtiene el texto visible de la página: quita scripts, estilos y etiquetas, decodifica las
	 * entidades HTML y deja una línea por bloque de texto.
	 *
	 * @param html Código HTML de la página
	 * @return Texto visible de la página, o vacío si no tiene
	 */
	public static String obtenerTexto(String html) {

		if (html == null || html.isBlank()) {
			return "";
		}

		String texto = BLOQUES_OCULTOS.matcher(html).replaceAll(" ");
		texto = SALTOS_DE_LINEA.matcher(texto).replaceAll("\n");
		texto = ETIQUETAS.matcher(texto).replaceAll(" ");
		texto = decodificarEntidades(texto);

		StringBuilder resultado = new StringBuilder();

		for (String linea : texto.split("\\r?\\n")) {

			String limpia = limpiarEspacios(linea);

			if (!limpia.isEmpty()) {

				if (resultado.length() > 0) {
					resultado.append('\n');
				}

				resultado.append(limpia);
			}
		}

		return resultado.toString();
	}

	/**
	 * Cambia las entidades HTML más comunes por su carácter (&amp;amp;, &amp;nbsp;, &amp;#225;, etc.).
	 *
	 * @param texto Texto con entidades
	 * @return Texto con los caracteres reales
	 */
	private static String decodificarEntidades(String texto) {

		Matcher numericas = ENTIDAD_NUMERICA.matcher(texto);
		StringBuilder resultado = new StringBuilder();

		while (numericas.find()) {

			String reemplazo = numericas.group();

			try {
				int codigo = Integer.parseInt(numericas.group(2), numericas.group(1).isEmpty() ? 10 : 16);
				reemplazo = new String(Character.toChars(codigo));
			} catch (IllegalArgumentException e) {
				// Si el número no es un carácter válido se deja como estaba.
			}

			numericas.appendReplacement(resultado, Matcher.quoteReplacement(reemplazo));
		}

		numericas.appendTail(resultado);

		return resultado.toString().replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">")
				.replace("&quot;", "\"").replace("&apos;", "'").replace("&aacute;", "á").replace("&eacute;", "é")
				.replace("&iacute;", "í").replace("&oacute;", "ó").replace("&uacute;", "ú").replace("&ntilde;", "ñ")
				.replace("&Ntilde;", "Ñ").replace("&copy;", "©").replace("&amp;", "&");
	}

	/**
	 * Deja un solo espacio entre palabras y quita los espacios de los extremos.
	 *
	 * @param texto Texto a limpiar
	 * @return Texto limpio
	 */
	private static String limpiarEspacios(String texto) {
		return texto.replace(' ', ' ').replaceAll("[ \\t\\x0B\\f]+", " ").trim();
	}
}
