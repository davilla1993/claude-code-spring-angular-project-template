package com.gfolly.backend.iam.application.dto;

/**
 * Paire de tokens émise au login et au refresh.
 * Transmise au controller pour être placée dans les cookies HttpOnly, jamais dans le corps de réponse.
 */
public record TokenPair(String accessToken, String refreshToken) {}
