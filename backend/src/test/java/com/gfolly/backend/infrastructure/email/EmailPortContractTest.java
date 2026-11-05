package com.gfolly.backend.infrastructure.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Comportement attendu de tout fournisseur d'email : chaque adaptateur a sa classe qui hérite de ce test. */
abstract class EmailPortContractTest {

    private static final EmailMessage MESSAGE = new EmailMessage("jane@example.com", "Sujet", "<p>Bonjour</p>");

    protected abstract EmailPort createPort();

    /** Le même fournisseur, en panne. */
    protected abstract EmailPort createUnavailablePort();

    @Test
    void acceptsAValidMessage() {
        assertThatCode(() -> createPort().send(MESSAGE)).doesNotThrowAnyException();
    }

    @Test
    void outageBecomesADeliveryException() {
        assertThatThrownBy(() -> createUnavailablePort().send(MESSAGE)).isInstanceOf(EmailDeliveryException.class);
    }
}
