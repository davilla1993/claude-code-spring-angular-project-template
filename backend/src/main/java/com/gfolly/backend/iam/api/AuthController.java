package com.gfolly.backend.iam.api;

import com.gfolly.backend.iam.api.dto.requests.*;
import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.application.*;
import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.infrastructure.security.CookieService;
import com.gfolly.backend.shared.api.ApiResponse;
import com.gfolly.backend.shared.util.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints publics d'authentification. Les tokens sont transmis uniquement via cookies HttpOnly.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationEmailUseCase resendVerificationEmailUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ErrorMessages.REGISTER_SUCCESS, registerUseCase.execute(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(startSession(loginUseCase.execute(request), response)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserResponse>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieService.readCookie(request, CookieService.REFRESH_TOKEN_COOKIE).orElse(null);
        return ResponseEntity.ok(ApiResponse.success(startSession(refreshTokenUseCase.execute(refreshToken), response)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutUseCase.execute(CookieService.readCookie(request, CookieService.REFRESH_TOKEN_COOKIE).orElse(null));
        cookieService.clearAuthCookies(response);
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.LOGOUT_SUCCESS));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailUseCase.execute(request.userId(), request.code());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.EMAIL_VERIFIED_SUCCESS));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        resendVerificationEmailUseCase.execute(request.email());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.VERIFICATION_CODE_SENT));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request.email());
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_RESET_GENERIC_SENT));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.execute(request);
        return ResponseEntity.ok(ApiResponse.message(ErrorMessages.PASSWORD_RESET_SUCCESS));
    }

    private UserResponse startSession(AuthSession session, HttpServletResponse response) {
        cookieService.setAuthCookies(response, session.tokens().accessToken(), session.tokens().refreshToken());
        return session.user();
    }
}
