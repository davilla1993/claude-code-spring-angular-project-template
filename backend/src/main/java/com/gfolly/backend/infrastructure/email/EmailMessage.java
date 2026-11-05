package com.gfolly.backend.infrastructure.email;

/** Email à envoyer, indépendant du fournisseur. */
public record EmailMessage(String to, String subject, String htmlBody) {}
