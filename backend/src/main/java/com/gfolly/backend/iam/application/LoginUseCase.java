package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.backend.iam.application.dto.AuthSession;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailNotVerifiedException;
import com.gfolly.backend.iam.domain.exception.InvalidCredentialsException;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final AuditLogService auditLogService;

    /** Hash factice : un email inconnu coûte autant qu'un mauvais mot de passe (pas d'énumération par timing). */
    private final String dummyPasswordHash;

    public LoginUseCase(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AuthTokenService authTokenService,
                        SendVerificationEmailUseCase sendVerificationEmailUseCase,
                        AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
        this.auditLogService = auditLogService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional
    public AuthSession execute(LoginRequest request) {
        String email = User.normalizeEmail(request.email());
        User user = authenticate(email, request.password());

        // Vérifiés uniquement après le mot de passe : un tiers ne peut pas sonder l'état d'un compte.
        if (!user.isActive()) {
            throw new InvalidCredentialsException(ErrorMessages.ACCOUNT_DISABLED);
        }
        if (!user.isEmailVerified()) {
            sendVerificationEmailUseCase.execute(user.getPublicId(), user.getEmail(), user.getFirstName());
            throw new EmailNotVerifiedException(ErrorMessages.EMAIL_NOT_VERIFIED, user.getPublicId());
        }

        AuthSession session = new AuthSession(authTokenService.issueTokens(user), UserResponseMapper.toResponse(user));
        auditLogService.logAs(email, "LOGIN", "USER", user.getPublicId(), null, AuditLog.ActionStatus.SUCCESS);
        return session;
    }

    private User authenticate(String email, String rawPassword) {
        Optional<User> user = userRepository.findByEmailAndDeletedFalse(email);
        String hash = user.map(User::getPasswordHash).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(rawPassword, hash);

        if (user.isEmpty() || !passwordMatches) {
            auditLogService.logAs(email, "LOGIN", "USER", user.map(User::getPublicId).orElse(null),
                    "Invalid credentials", AuditLog.ActionStatus.FAILED);
            throw new InvalidCredentialsException(ErrorMessages.CREDENTIALS_INVALID);
        }
        return user.get();
    }
}
