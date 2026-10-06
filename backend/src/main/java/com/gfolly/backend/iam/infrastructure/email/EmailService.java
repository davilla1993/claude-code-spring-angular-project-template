package com.gfolly.backend.iam.infrastructure.email;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

/**
 * Envoi asynchrone des emails transactionnels.
 * Templates Thymeleaf : src/main/resources/templates/email/*.html
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final String from;
    private final String fromName;

    public EmailService(JavaMailSender mailSender,
                        ITemplateEngine templateEngine,
                        @Value("${app.email.from}") String from,
                        @Value("${app.email.from-name}") String fromName) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
        this.fromName = fromName;
    }

    @Async
    public void sendVerificationCode(String toEmail, String firstName, String code) {
        sendCode(toEmail, firstName, code, "email/verification-code",
                "Vérification de votre adresse email — " + fromName);
    }

    @Async
    public void sendPasswordResetCode(String toEmail, String firstName, String code) {
        sendCode(toEmail, firstName, code, "email/reset-password",
                "Réinitialisation de votre mot de passe — " + fromName);
    }

    private void sendCode(String toEmail, String firstName, String code, String template, String subject) {
        try {
            Context ctx = new Context();
            ctx.setVariable("firstName", firstName);
            ctx.setVariable("code", code);
            ctx.setVariable("appName", fromName);
            send(toEmail, subject, templateEngine.process(template, ctx));
        } catch (Exception e) {
            // Exécution asynchrone : l'erreur ne peut pas remonter à l'appelant, on la journalise.
            log.error("Failed to send email '{}': {}", template, e.getMessage(), e);
        }
    }

    private void send(String toEmail, String subject, String html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
        helper.setFrom(new InternetAddress(from, fromName));
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
        log.info("Email sent — subject: {}", subject);
    }
}
