package com.gfolly.backend.infrastructure.email;

/**
 * Port d'envoi d'emails. Implémentation fournie : {@link SmtpEmailAdapter}.
 * Pour un fournisseur par API, ajouter un adaptateur qui passe {@code EmailPortContractTest}.
 */
public interface EmailPort {

    /**
     * Envoie le message depuis l'expéditeur configuré.
     *
     * @throws EmailDeliveryException si le fournisseur n'a pas pu prendre le message en charge
     */
    void send(EmailMessage message);
}
