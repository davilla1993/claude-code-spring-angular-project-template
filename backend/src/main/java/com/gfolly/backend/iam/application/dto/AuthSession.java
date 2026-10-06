package com.gfolly.backend.iam.application.dto;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;

/**
 * Résultat d'une authentification : les tokens (destinés aux cookies) et l'utilisateur (corps de réponse).
 */
public record AuthSession(TokenPair tokens, UserResponse user) {}
