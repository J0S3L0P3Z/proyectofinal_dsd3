package co.edu.unbosque.proyectomodulotres.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.proyectomodulotres.util.DiseñoEmail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class GestionEmailService implements NotificacionEmailService {

	private final JavaMailSender mailSender;

	private final String remitente;

	// Constructor para recibir el servicio de correo y el remitente.
	public GestionEmailService(JavaMailSender mailSender, @Value("${correo.remitente}") String remitente) {
		this.mailSender = mailSender;
		this.remitente = remitente;
	}

	// Envía la notificación con el resultado del scraping.
	@Override
	public void enviarNotificacion(String correoDestino, String nombreUsuario, String url, String estado,
			String detalle, File archivoPDF) {

		try {

			MimeMessage mensaje = mailSender.createMimeMessage();

			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

			helper.setFrom(remitente);
			helper.setTo(correoDestino);

			helper.setSubject("Resultado del scraping: " + estado);

			String html = DiseñoEmail.crearHTML(nombreUsuario, url, estado, detalle);

			String texto = DiseñoEmail.crearTextoPlano(nombreUsuario, url, estado, detalle);

			helper.setText(texto, html);

			// Adjunta el PDF si está disponible.
			if (archivoPDF != null && archivoPDF.exists()) {

				FileSystemResource archivo = new FileSystemResource(archivoPDF);

				helper.addAttachment(archivoPDF.getName(), archivo);
			}

			mailSender.send(mensaje);

		} catch (MessagingException e) {

			throw new RuntimeException("Error al preparar el correo: " + e.getMessage(), e);

		} catch (Exception e) {

			throw new RuntimeException("Error al enviar el correo: " + e.getMessage(), e);
		}
	}
}
