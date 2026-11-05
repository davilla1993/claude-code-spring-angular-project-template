package com.gfolly.backend.infrastructure.email;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpEmailAdapterTest extends EmailPortContractTest {

    private static final EmailMessage EMAIL = new EmailMessage("jane@example.com", "Sujet", "<p>Bonjour</p>");

    private JavaMailSender mailSender;
    private SmtpEmailAdapter adapter;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage((Session) null));
        adapter = new SmtpEmailAdapter(mailSender, "noreply@example.com", "Mon App");
    }

    @Override
    protected EmailPort createPort() {
        return adapter;
    }

    @Override
    protected EmailPort createUnavailablePort() {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
        return adapter;
    }

    @Test
    void sendsTheMessageFromTheConfiguredSender() throws Exception {
        adapter.send(EMAIL);

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(sent.capture());
        MimeMessage message = sent.getValue();

        InternetAddress from = (InternetAddress) message.getFrom()[0];
        assertThat(from.getAddress()).isEqualTo("noreply@example.com");
        assertThat(from.getPersonal()).isEqualTo("Mon App");
        assertThat(message.getRecipients(Message.RecipientType.TO)[0]).hasToString("jane@example.com");
        assertThat(message.getSubject()).isEqualTo("Sujet");

        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        message.writeTo(raw);
        assertThat(raw.toString(StandardCharsets.UTF_8)).contains("<p>Bonjour</p>", "text/html");
    }

    @Test
    void providerFailureBecomesADeliveryException() {
        doThrow(new MailSendException("smtp down")).when(mailSender).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.send(EMAIL)).isInstanceOf(EmailDeliveryException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "jane@example.com\r\nBcc: evil@example.com",
            "jane@example.com, evil@example.com",
            "not an address",
            ""
    })
    void hostileRecipientIsRefusedAndNothingIsSent(String recipient) {
        EmailMessage hostile = new EmailMessage(recipient, "Sujet", "<p>Bonjour</p>");

        assertThatThrownBy(() -> adapter.send(hostile)).isInstanceOf(EmailDeliveryException.class);

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(MimeMessage.class));
    }

    @Test
    void accentsInSubjectAndSenderNameSurvive() throws Exception {
        SmtpEmailAdapter accented = new SmtpEmailAdapter(mailSender, "noreply@example.com", "Équipe Été");

        accented.send(new EmailMessage("jane@example.com", "Vérification — Été", "<p>é</p>"));

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(sent.capture());
        assertThat(sent.getValue().getSubject()).isEqualTo("Vérification — Été");
        assertThat(((InternetAddress) sent.getValue().getFrom()[0]).getPersonal()).isEqualTo("Équipe Été");
    }
}
