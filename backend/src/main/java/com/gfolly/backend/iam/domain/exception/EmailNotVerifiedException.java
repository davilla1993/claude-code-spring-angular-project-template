package com.gfolly.backend.iam.domain.exception;

public class EmailNotVerifiedException extends RuntimeException {

    private final String userId;

    public EmailNotVerifiedException(String message, String userId) {
        super(message);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}

