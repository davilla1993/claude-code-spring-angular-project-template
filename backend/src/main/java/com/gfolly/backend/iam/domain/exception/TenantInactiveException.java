package com.gfolly.quantly_backend.iam.domain.exception;

public class TenantInactiveException extends RuntimeException {
    public TenantInactiveException(String message) {
        super(message);
    }
}

