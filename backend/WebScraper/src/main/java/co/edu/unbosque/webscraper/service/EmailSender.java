package co.edu.unbosque.webscraper.service;

/** Envío de correo (F4.1). Otra implementación (p. ej. la API HTTP de Brevo) puede reemplazar a SMTP sin tocar el resto. */
public interface EmailSender {

    record Attachment(String filename, byte[] content, String contentType) {}

    /** Lanza AppException(EMAIL_FAILED) si no se pudo enviar. */
    void send(String to, String subject, String htmlBody, Attachment attachment);
}
