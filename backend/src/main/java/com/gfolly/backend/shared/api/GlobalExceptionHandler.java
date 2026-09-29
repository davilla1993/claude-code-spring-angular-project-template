package com.gfolly.quantly_backend.shared.api;

import com.gfolly.quantly_backend.cashdesk.domain.exception.*;
import com.gfolly.quantly_backend.purchases.domain.exception.*;
import com.gfolly.quantly_backend.sales.domain.exception.*;
import com.gfolly.quantly_backend.iam.domain.exception.*;
import com.gfolly.quantly_backend.catalog.domain.exception.*;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import com.gfolly.quantly_backend.stock.domain.exception.InsufficientStockException;
import com.gfolly.quantly_backend.stock.domain.exception.InventoryInProgressException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ErrorMessages.ACCESS_DENIED));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorMessages.missingParameter(ex.getParameterName())));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorMessages.ENDPOINT_NOT_FOUND));
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> handleEmailNotVerified(EmailNotVerifiedException ex) {
        var data = java.util.Map.of("userId", ex.getUserId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage(), data));
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailAlreadyVerified(EmailAlreadyVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidVerificationCode(InvalidVerificationCodeException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(com.gfolly.quantly_backend.iam.domain.exception.TooManyRequestsException.class)
    public ResponseEntity<ApiResponse<Void>> handleTooManyRequests(
            com.gfolly.quantly_backend.iam.domain.exception.TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({TenantNotFoundException.class, UserNotFoundException.class,
            ProductNotFoundException.class, CategoryNotFoundException.class,
            BarcodeNotFoundException.class, UnitOfMeasureNotFoundException.class,
            CashRegisterNotFoundException.class, GlobalSessionNotFoundException.class,
            CashSessionNotFoundException.class, SaleNotFoundException.class,
            SupplierNotFoundException.class, PurchaseOrderNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidSessionPinException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPin(InvalidSessionPinException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({GlobalSessionAlreadyOpenException.class, CashSessionAlreadyOpenException.class,
            CashSessionAlreadyClosedException.class, CashRegisterAlreadyInUseException.class,
            SaleAlreadyCancelledException.class, SupplierNameAlreadyExistsException.class,
            PurchaseOrderAlreadyValidatedException.class, PurchaseOrderAlreadyCancelledException.class})
    public ResponseEntity<ApiResponse<Void>> handleCashdeskConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({GlobalSessionNotOpenException.class, GlobalSessionHasOpenCashSessionsException.class})
    public ResponseEntity<ApiResponse<Void>> handleCashdeskUnprocessable(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InventoryInProgressException.class)
    public ResponseEntity<ApiResponse<Void>> handleInventoryInProgress(InventoryInProgressException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({DuplicateBarcodeException.class, ProductAlreadyExistsException.class,
            CategoryAlreadyExistsException.class, UnitOfMeasureDuplicateException.class})
    public ResponseEntity<ApiResponse<Void>> handleInventoryConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(TenantInactiveException.class)
    public ResponseEntity<ApiResponse<Void>> handleTenantInactive(TenantInactiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({EmailAlreadyExistsException.class, SlugAlreadyExistsException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error(ErrorMessages.FILE_SIZE_EXCEEDED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorMessages.INTERNAL_SERVER_ERROR));
    }
}

