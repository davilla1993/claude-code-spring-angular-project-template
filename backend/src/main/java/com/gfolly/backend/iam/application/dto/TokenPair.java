package com.gfolly.backend.iam.application.dto;

/**
 * Paire de tokens générée lors du login/register/refresh.
 * Transmise au controller pour être placée dans les HttpOnly cookies.
 */
public record TokenPair(String accessToken, String refreshToken) {}

