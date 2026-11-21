package co.edu.unbosque.proyectomodulotres.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import co.edu.unbosque.proyectomodulotres.exception.EnvioCorreoException;
import co.edu.unbosque.proyectomodulotres.util.DisenoEmail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class GestionEmailService implements NotificacionEmailService {

	private final JavaMailSender mailSender;
	private final String remitente;

	public GestionEmailService(JavaMailSender mailSender, @Value("${correo.remitente}") String remitente) {
		this.mailSender = mailSender;
		this.remitente = remitente;
	}

	@Override
	public void enviarNotificacion(String correoDestino, String nombreUsuario, String url, String estado,
			String detalle, File archivoPDF) {

		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

			helper.setFrom(remitente);
			helper.setTo(correoDestino);
			helper.setSubject("Resultado del scraping: " + estado);

			String html = DisenoEmail.crearHTML(nombreUsuario, url, estado, detalle);
			String texto = DisenoEmail.crearTextoPlano(nombreUsuario, url, estado, detalle);

			helper.setText(texto, html);

			if (archivoPDF != null && archivoPDF.exists()) {
				FileSystemResource archivo = new FileSystemResource(archivoPDF);
				helper.addAttachment(archivoPDF.getName(), archivo);
			}

			mailSender.send(mensaje);

		} catch (MessagingException e) {
			throw new EnvioCorreoException("Error al preparar el correo: " + e.getMessage());

		} catch (Exception e) {
			throw new EnvioCorreoException("Error al enviar el correo: " + e.getMessage());
		}
	}

	@Override
	public void enviarCodigoRecuperacion(String correoDestino, String nombreUsuario, String codigo) {

		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

			helper.setFrom(remitente);
			helper.setTo(correoDestino);
			helper.setSubject("Código para recuperar tu contraseña - ScrapingDoo!");

			String html = DisenoEmail.crearHTMLRecuperacion(nombreUsuario, codigo);
			String texto = DisenoEmail.crearTextoPlanoRecuperacion(nombreUsuario, codigo);

			helper.setText(texto, html);

			mailSender.send(mensaje);

		} catch (MessagingException e) {
			throw new EnvioCorreoException("Error al preparar el correo de recuperación: " + e.getMessage());

		} catch (Exception e) {
			throw new EnvioCorreoException("Error al enviar el correo de recuperación: " + e.getMessage());
		}
	}
}
