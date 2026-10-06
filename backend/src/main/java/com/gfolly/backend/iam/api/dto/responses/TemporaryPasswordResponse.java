package com.gfolly.backend.iam.api.dto.responses;

/** Mot de passe temporaire généré par un administrateur, à transmettre à l'utilisateur. */
public record TemporaryPasswordResponse(String temporaryPassword) {}
