package com.gfolly.backend.iam.api;

import com.gfolly.backend.iam.application.*;
import com.gfolly.backend.iam.application.auth.*;
import com.gfolly.backend.iam.application.dto.AuthSessionResult;
import com.gfolly.backend.iam.api.dto.requests.ForgotPasswordRequest;
import com.gfolly.backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.backend.iam.api.dto.requests.RegisterTenantRequest;
import com.gfolly.backend.iam.api.dto.requests.ResendVerificationRequest;
import com.gfolly.backend.iam.api.dto.requests.ResetPasswordRequest;
import com.gfolly.backend.iam.api.dto.requests.VerifyEmailRequest;
import com.gfolly.backend.iam.api.dto.responses.AuthResponse;
import com.gfolly.backend.iam.infrastructure.security.CookieService;
import com.gfolly.backend.shared.api.ApiResponse;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.shared.util.UserPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterTenantUseCase registerTenantUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final GetOwnerSummaryUseCase getOwnerSummaryUseCase;
    private final CookieService cookieService;

    @Value("${app.mode:standalone}")
    private String appMode;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterTenantRequest request,
            HttpServletResponse response) {

        if ("standalone".equalsIgnoreCase(appMode)) {
            throw new AccessDeniedException(ErrorMessages.REGISTRATION_DISABLED);
        }

        AuthSessionResult result = registerTenantUseCase.execute(request);
        cookieService.setAccessTokenCookie(response, result.tokens().accessToken());
        cookieService.setRefreshTokenCookie(response, result.tokens().refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result.response()));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthSessionResult result = loginUseCase.execute(request);
        if (result.tokens() != null) {
            cookieService.setAccessTokenCookie(response, result.tokens().accessToken());
            cookieService.setRefreshTokenCookie(response, result.tokens().refreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(result.response()));
    }

    @GetMapping("/owner-summary")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getOwnerSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) java.time.LocalDate from,
            @RequestParam(required = false) java.time.LocalDate to) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        java.time.LocalDate f = (from != null) ? from : java.time.LocalDate.now();
        java.time.LocalDate t = (to != null) ? to : java.time.LocalDate.now();

        var totalCA = getOwnerSummaryUseCase.execute(principal.email(), f, t);
        return ResponseEntity.ok(ApiResponse.success(totalCA));
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}


