package com.gfolly.backend.iam.infrastructure.email;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String from;

    @Value("${app.email.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender,
                        @Qualifier("emailTemplateEngine") SpringTemplateEngine templateEngine) {
        this.mailSender     = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendVerificationCode(String toEmail, String firstName, String code) {
        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("code", code);
        String html = templateEngine.process("verification-code", ctx);
        send(toEmail, "Vérification de votre adresse email — Qentox", html);
    }

    @Async
    public void sendPasswordResetCode(String toEmail, String firstName, String code) {
        Context ctx = new Context();
        ctx.setVariable("firstName", firstName);
        ctx.setVariable("code", code);
        String html = templateEngine.process("reset-password", ctx);
        send(toEmail, "Réinitialisation de votre mot de passe — Qentox", html);
    }

    private void send(String toEmail, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(from, fromName));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", toEmail, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}

