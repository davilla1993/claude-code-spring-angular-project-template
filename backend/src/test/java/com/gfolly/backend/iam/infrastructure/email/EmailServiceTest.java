package com.gfolly.backend.iam.infrastructure.email;

import com.gfolly.backend.infrastructure.email.EmailDeliveryException;
import com.gfolly.backend.infrastructure.email.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Vérifie la rédaction des emails avec un faux fournisseur. */
class EmailServiceTest {

    private final List<EmailMessage> sent = new ArrayList<>();
    private ITemplateEngine templateEngine;
    private EmailService service;

    @BeforeEach
    void setUp() {
        templateEngine = mock(ITemplateEngine.class);
        service = new EmailService(sent::add, templateEngine, "Mon App");
    }

    @Test
    void sendsTheVerificationCodeThroughThePort() {
        when(templateEngine.process(eq("email/verification-code"), any(IContext.class))).thenReturn("<p>code</p>");

        service.sendVerificationCode("jane@example.com", "Jane", "123456");

        assertThat(sent).containsExactly(new EmailMessage("jane@example.com",
                "Vérification de votre adresse email — Mon App", "<p>code</p>"));
    }

    @Test
    void sendsThePasswordResetCodeThroughThePort() {
        when(templateEngine.process(eq("email/reset-password"), any(IContext.class))).thenReturn("<p>code</p>");

        service.sendPasswordResetCode("jane@example.com", "Jane", "123456");

        assertThat(sent).containsExactly(new EmailMessage("jane@example.com",
                "Réinitialisation de votre mot de passe — Mon App", "<p>code</p>"));
    }

    @Test
    void providerFailureDoesNotReachTheCaller() {
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<p>code</p>");
        EmailService failing = new EmailService(message -> {
            throw new EmailDeliveryException("down", null);
        }, templateEngine, "Mon App");

        assertThatCode(() -> failing.sendVerificationCode("jane@example.com", "Jane", "123456"))
                .doesNotThrowAnyException();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullSource
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "jane@example.com\r\nBcc: evil@example.com",
            "jane@example.com, evil@example.com",
            "jane@example.com;evil@example.com",
            "Jane <jane@example.com>",
            "not an address",
            ""
    })
    void hostileRecipientReachesNoProvider(String recipient) {
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn("<p>code</p>");

        service.sendVerificationCode(recipient, "Jane", "123456");

        assertThat(sent).isEmpty();
    }

    @Test
    void templateFailureSendsNothingAndDoesNotReachTheCaller() {
        when(templateEngine.process(any(String.class), any(IContext.class)))
                .thenThrow(new IllegalStateException("template error"));

        assertThatCode(() -> service.sendPasswordResetCode("jane@example.com", "Jane", "123456"))
                .doesNotThrowAnyException();

        assertThat(sent).isEmpty();
    }

    @Test
    void realTemplatesEscapeAHostileFirstNameAndContainTheCode() {
        EmailService real = new EmailService(sent::add, realTemplateEngine(), "Mon App");
        String hostile = "<script>alert(1)</script>";

        real.sendVerificationCode("jane@example.com", hostile, "123456");
        real.sendPasswordResetCode("jane@example.com", hostile, "654321");

        assertThat(sent).hasSize(2);
        assertThat(sent.get(0).htmlBody()).contains("123456", "Mon App", "&lt;script&gt;").doesNotContain(hostile);
        assertThat(sent.get(1).htmlBody()).contains("654321", "Mon App", "&lt;script&gt;").doesNotContain(hostile);
    }

    private static ITemplateEngine realTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
