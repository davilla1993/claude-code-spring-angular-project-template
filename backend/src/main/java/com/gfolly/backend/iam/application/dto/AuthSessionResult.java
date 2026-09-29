package com.gfolly.quantly_backend.iam.application.dto;

import com.gfolly.quantly_backend.iam.api.dto.responses.AuthResponse;

/**
 * Résultat d'une opération d'authentification.
 * Le controller utilise les tokens pour setter les HttpOnly cookies
 * et retourne uniquement la réponse (sans tokens) au client.
 */
public record AuthSessionResult(TokenPair tokens, AuthResponse response) {}
