package com.gfolly.backend.infrastructure.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/** Envoi par SMTP, avec les paramètres {@code spring.mail.*}. */
@Slf4j
@Component
public class SmtpEmailAdapter implements EmailPort {

    private final JavaMailSender mailSender;
    private final String from;
    private final String fromName;

    public SmtpEmailAdapter(JavaMailSender mailSender,
                            @Value("${app.email.from}") String from,
                            @Value("${app.email.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.from = from;
        this.fromName = fromName;
    }

    @Override
    public void send(EmailMessage email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(from, fromName));
            helper.setTo(email.to());
            helper.setSubject(email.subject());
            helper.setText(email.htmlBody(), true);
            mailSender.send(message);
            log.info("Email sent — subject: {}", email.subject());
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            throw new EmailDeliveryException("SMTP delivery failed", e);
        }
    }
}
