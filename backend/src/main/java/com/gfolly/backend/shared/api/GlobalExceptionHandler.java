package com.gfolly.backend.shared.api;

import com.gfolly.backend.iam.domain.exception.*;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // --- Erreurs de requête ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " : " + e.getDefaultMessage())
                .toList();
        log.warn("Validation error: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.validationError(ErrorMessages.VALIDATION_ERROR, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorMessages.MALFORMED_REQUEST_BODY));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorMessages.missingParameter(ex.getParameterName())));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPart(MissingServletRequestPartException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorMessages.missingParameter(ex.getRequestPartName())));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, PropertyReferenceException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidParameter(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorMessages.INVALID_PARAMETER));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ApiResponse.error(ErrorMessages.FILE_SIZE_EXCEEDED));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorMessages.ENDPOINT_NOT_FOUND));
    }

    // --- Authentification / autorisation ---

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), Map.of("userId", ex.getUserId())));
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ErrorMessages.ACCESS_DENIED));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbiddenOperation(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({InvalidVerificationCodeException.class, InvalidPasswordException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidUserInput(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    // --- Ressources / conflits ---

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, EmailAlreadyVerifiedException.class, IllegalStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ErrorMessages.DUPLICATE_ENTRY));
    }

    // --- Fallback ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorMessages.INTERNAL_SERVER_ERROR));
    }
}
