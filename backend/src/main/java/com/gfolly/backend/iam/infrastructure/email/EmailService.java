package com.gfolly.backend.iam.infrastructure.email;

import com.gfolly.backend.infrastructure.email.EmailMessage;
import com.gfolly.backend.infrastructure.email.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.regex.Pattern;

/**
 * Rédaction et envoi asynchrone des emails transactionnels, quel que soit le fournisseur branché sur {@link EmailPort}.
 * Templates Thymeleaf : src/main/resources/templates/email/*.html
 */
@Service
@Slf4j
public class EmailService {

    /** Une seule adresse : ni retour à la ligne, ni séparateur. Contrôle indépendant du fournisseur. */
    private static final Pattern SINGLE_ADDRESS = Pattern.compile("[^\\s,;<>\"]+@[^\\s,;<>\"]+");

    private final EmailPort emailPort;
    private final ITemplateEngine templateEngine;
    private final String appName;

    public EmailService(EmailPort emailPort,
                        ITemplateEngine templateEngine,
                        @Value("${app.email.from-name}") String appName) {
        this.emailPort = emailPort;
        this.templateEngine = templateEngine;
        this.appName = appName;
    }

    @Async
    public void sendVerificationCode(String toEmail, String firstName, String code) {
        sendCode(toEmail, firstName, code, "email/verification-code",
                "Vérification de votre adresse email — " + appName);
    }

    @Async
    public void sendPasswordResetCode(String toEmail, String firstName, String code) {
        sendCode(toEmail, firstName, code, "email/reset-password",
                "Réinitialisation de votre mot de passe — " + appName);
    }

    private void sendCode(String toEmail, String firstName, String code, String template, String subject) {
        try {
            if (toEmail == null || !SINGLE_ADDRESS.matcher(toEmail).matches()) {
                throw new IllegalArgumentException("Invalid email recipient");
            }
            Context ctx = new Context();
            ctx.setVariable("firstName", firstName);
            ctx.setVariable("code", code);
            ctx.setVariable("appName", appName);
            emailPort.send(new EmailMessage(toEmail, subject, templateEngine.process(template, ctx)));
        } catch (Exception e) {
            // Exécution asynchrone : l'erreur ne peut pas remonter à l'appelant, on la journalise.
            log.error("Failed to send email '{}': {}", template, e.getMessage(), e);
        }
    }
}
