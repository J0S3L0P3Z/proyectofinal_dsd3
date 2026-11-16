package co.edu.unbosque.webscraper.exception;

/**
 * Catálogo de errores. Cada fallo tiene un nombre, un mensaje claro para el usuario y una pista
 * de qué puede intentar. Nada debe terminar en un error genérico ni en silencio.
 */
public enum ErrorCode {
    INVALID_URL(400, "La dirección no es válida. Escribe una URL completa que empiece por http:// o https://.",
            "Revisa que la URL esté bien escrita."),
    URL_NOT_ALLOWED(400, "Esa dirección no está permitida: apunta a una red interna o privada y, por seguridad, no se puede analizar.",
            "Usa la dirección pública de una página web."),
    PAGE_NOT_FOUND(422, "La página no existe (error 404 o 410).",
            "Verifica que la URL sea correcta e inténtalo de nuevo."),
    ACCESS_FORBIDDEN(422, "El sitio no permite el acceso a esta página (error 403: acceso prohibido).",
            "Prueba con una página pública del mismo sitio."),
    LOGIN_REQUIRED(422, "La página exige iniciar sesión para mostrar su contenido, por eso no se puede analizar.",
            "Prueba con una página pública, que no pida cuenta."),
    BLOCKED_BY_SITE(422, "El sitio bloquea la lectura automática (captcha, verificación o protección anti-bots) "
            + "y no se intenta saltar esa protección.",
            "Prueba con otra página u otra sección pública del sitio."),
    HTTP_ERROR(422, "El sitio respondió con un error HTTP y no entregó la página.",
            "Inténtalo de nuevo más tarde o prueba con otra URL."),
    SERVER_ERROR(422, "El servidor del sitio respondió con un error interno (5xx).",
            "Inténtalo de nuevo más tarde."),
    TIMEOUT(422, "La página tardó demasiado en cargar y se agotó el tiempo de espera.",
            "Inténtalo de nuevo o prueba con una página más liviana."),
    NETWORK_ERROR(422, "No se pudo conectar con el sitio: el dominio no existe, no responde o rechazó la conexión.",
            "Revisa que la dirección exista y que el sitio esté en línea."),
    SSL_ERROR(422, "El certificado de seguridad (SSL) del sitio no es válido.",
            "Prueba con otra URL o con la versión segura (https) correcta del sitio."),
    EMPTY_CONTENT(422, "La página cargó, pero no tiene texto útil para analizar.",
            "Prueba con otra página del sitio que tenga contenido."),

    AI_NOT_CONFIGURED(502, "Esta IA no está configurada (falta su clave de acceso).", "Avisa al administrador del sistema."),
    AI_QUOTA_EXCEEDED(502, "Esta IA alcanzó su límite de uso gratuito por ahora.", "Inténtalo de nuevo más tarde."),
    AI_AUTH_FAILED(502, "La clave de acceso de esta IA fue rechazada.", "Avisa al administrador del sistema."),
    AI_UNAVAILABLE(502, "Esta IA no está disponible en este momento.", "Inténtalo de nuevo más tarde."),
    AI_TIMEOUT(502, "Esta IA tardó demasiado en responder.", "Inténtalo de nuevo más tarde."),
    AI_INVALID_RESPONSE(502, "Esta IA respondió con un formato que no se pudo usar.", "Inténtalo de nuevo."),
    AI_ALL_FAILED(502, "Ninguna de las tres IAs pudo analizar la página.", "Inténtalo de nuevo en unos minutos."),

    PDF_FAILED(500, "No se pudo generar el PDF del reporte.", "El resultado quedó guardado en tu historial; intenta descargarlo de nuevo."),
    EMAIL_FAILED(500, "No se pudo enviar el correo.", "El resultado quedó guardado en tu historial."),
    QUEUE_FULL(503, "El sistema está muy ocupado en este momento.", "Inténtalo de nuevo en unos minutos."),
    JOB_TIMEOUT(500, "El análisis tardó más de lo permitido y se canceló.", "Inténtalo de nuevo."),
    TOO_MANY_ACTIVE_JOBS(429, "Ya tienes varios análisis en curso. Espera a que termine alguno.", "Espera unos instantes."),
    INTERNAL_ERROR(500, "Ocurrió un error inesperado. Ya quedó registrado para revisarlo.", "Inténtalo de nuevo."),

    VALIDATION_ERROR(400, "Hay datos inválidos en la petición.", ""),
    EMAIL_TAKEN(409, "Ya existe una cuenta con ese correo.", ""),
    INVALID_CREDENTIALS(401, "Correo o contraseña incorrectos.", ""),
    EMAIL_NOT_VERIFIED(403, "Debes verificar tu correo antes de iniciar sesión.", ""),
    ACCOUNT_DISABLED(403, "Tu cuenta está deshabilitada. Contacta al administrador.", ""),
    TOKEN_INVALID(400, "El enlace no es válido o ya venció.", ""),
    UNAUTHORIZED(401, "Debes iniciar sesión para continuar.", ""),
    FORBIDDEN(403, "No tienes permiso para hacer esto.", ""),
    NOT_FOUND(404, "No se encontró lo que buscas.", ""),
    PDF_NOT_AVAILABLE(409, "El PDF todavía no está disponible: el análisis sigue en curso.", ""),
    CONFLICT(409, "La operación entra en conflicto con datos existentes.", ""),
    CANNOT_MODIFY_SELF(400, "No puedes deshabilitar ni quitarte el rol de administrador a ti mismo.", "");

    private final int status;
    private final String message;
    private final String hint;

    ErrorCode(int status, String message, String hint) {
        this.status = status;
        this.message = message;
        this.hint = hint;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getHint() { return hint; }
}
