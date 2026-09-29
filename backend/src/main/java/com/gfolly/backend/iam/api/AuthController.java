package com.gfolly.quantly_backend.iam.api;

import com.gfolly.quantly_backend.iam.application.*;
import com.gfolly.quantly_backend.iam.application.auth.*;
import com.gfolly.quantly_backend.iam.application.dto.AuthSessionResult;
import com.gfolly.quantly_backend.iam.api.dto.requests.ForgotPasswordRequest;
import com.gfolly.quantly_backend.iam.api.dto.requests.CreateShopRequest;
import com.gfolly.quantly_backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.quantly_backend.iam.api.dto.requests.RegisterTenantRequest;
import com.gfolly.quantly_backend.iam.api.dto.requests.ResendVerificationRequest;
import com.gfolly.quantly_backend.iam.api.dto.requests.ResetPasswordRequest;
import com.gfolly.quantly_backend.iam.api.dto.requests.VerifyEmailRequest;
import com.gfolly.quantly_backend.iam.api.dto.responses.AuthResponse;
import com.gfolly.quantly_backend.iam.infrastructure.security.CookieService;
import com.gfolly.quantly_backend.shared.api.ApiResponse;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import com.gfolly.quantly_backend.shared.util.UserPrincipal;
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
    private final RegisterAdditionalShopUseCase registerAdditionalShopUseCase;
    private final GetMyShopsUseCase getMyShopsUseCase;
    private final GetOwnerSummaryUseCase getOwnerSummaryUseCase;
    private final GetOwnerGlobalDashboardStatsUseCase getOwnerGlobalDashboardStatsUseCase;
    private final SwitchShopUseCase switchShopUseCase;
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

    @PostMapping("/register-shop")
    public ResponseEntity<ApiResponse<AuthResponse>> registerShop(
            @Valid @RequestBody CreateShopRequest request,
            HttpServletResponse response) {
        AuthSessionResult result = registerAdditionalShopUseCase.execute(request);
        cookieService.setAccessTokenCookie(response, result.tokens().accessToken());
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


    @GetMapping("/my-shops")
    public ResponseEntity<ApiResponse<AuthResponse>> getMyShops(
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var result = getMyShopsUseCase.execute(principal.email());
        return ResponseEntity.ok(ApiResponse.success(result));
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

    @GetMapping("/global-dashboard")
    public ResponseEntity<ApiResponse<com.gfolly.quantly_backend.iam.api.dto.responses.OwnerGlobalDashboardResponse>> getGlobalDashboard(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) java.time.LocalDate from,
            @RequestParam(required = false) java.time.LocalDate to) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        java.time.LocalDate f = (from != null) ? from : java.time.LocalDate.now();
        java.time.LocalDate t = (to != null) ? to : java.time.LocalDate.now();

        var stats = getOwnerGlobalDashboardStatsUseCase.execute(principal.email(), f, t);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/switch-shop/{shopPublicId}")
    public ResponseEntity<ApiResponse<AuthResponse>> switchShop(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String shopPublicId,
            HttpServletResponse response) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        // Clear SecurityContextHolder before attempting to switch tenant and generate new tokens
        SecurityContextHolder.getContext().setAuthentication(null);
        
        var result = switchShopUseCase.execute(principal.email(), shopPublicId);
        cookieService.setAccessTokenCookie(response, result.tokens().accessToken());
        cookieService.setRefreshTokenCookie(response, result.tokens().refreshToken());
        
        return ResponseEntity.ok(ApiResponse.success(result.response()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractCookie(request, CookieService.REFRESH_TOKEN_COOKIE);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorMessages.SESSION_EXPIRED));
        }
        AuthSessionResult result = refreshTokenUseCase.execute(refreshToken);
        cookieService.setAccessTokenCookie(response, result.tokens().accessToken());
        cookieService.setRefreshTokenCookie(response, result.tokens().refreshToken());
        return ResponseEntity.ok(ApiResponse.success(result.response()));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletResponse response) {
        logoutUseCase.execute(principal != null ? principal.userId() : null);
        cookieService.clearTokenCookies(response);
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.LOGOUT_SUCCESS));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.execute(request.userId(), request.code());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.EMAIL_VERIFIED_SUCCESS));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        resendVerificationEmailUseCase.execute(request.email());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.VERIFICATION_CODE_SENT));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request.email());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_RESET_GENERIC_SENT));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(request.email(), request.code(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_RESET_SUCCESS));
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

