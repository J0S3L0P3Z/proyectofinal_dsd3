package co.edu.unbosque.webscraper.service;

import co.edu.unbosque.webscraper.configuration.AppProperties;
import co.edu.unbosque.webscraper.exception.AppException;
import co.edu.unbosque.webscraper.exception.ErrorCode;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * Implementación de EmailSender que envía correos HTML con adjuntos por SMTP. Si MAIL_ENABLED es
 * false solo registra en el log lo que habría enviado.
 */
@Component
public class SmtpEmailSender implements EmailSender {
    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final AppProperties props;

    public SmtpEmailSender(JavaMailSender mailSender, AppProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    @Override
    public void send(String to, String subject, String htmlBody, Attachment attachment) {
        if (!props.mail().enabled()) {
            log.info("Correo desactivado (MAIL_ENABLED=false). Habría ido a {} con asunto: {}", to, subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, attachment != null, "UTF-8");
            helper.setFrom(props.mail().from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (attachment != null) {
                helper.addAttachment(attachment.filename(), new ByteArrayResource(attachment.content()), attachment.contentType());
            }
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new AppException(ErrorCode.EMAIL_FAILED, e.getMessage(), e);
        }
    }
}
