package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.RegisterRequest;
import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailAlreadyExistsException;
import com.gfolly.backend.iam.domain.exception.ForbiddenOperationException;
import com.gfolly.backend.iam.infrastructure.mapper.request.UserRequestMapper;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inscription publique : crée un compte USER non vérifié et envoie le code de vérification.
 * Aucune session n'est ouverte : l'utilisateur se connecte après avoir vérifié son email.
 */
@Service
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final AuditLogService auditLogService;
    private final boolean registrationEnabled;

    public RegisterUseCase(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           SendVerificationEmailUseCase sendVerificationEmailUseCase,
                           AuditLogService auditLogService,
                           @Value("${app.security.registration-enabled}") boolean registrationEnabled) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
        this.auditLogService = auditLogService;
        this.registrationEnabled = registrationEnabled;
    }

    @Transactional
    public UserResponse execute(RegisterRequest request) {
        if (!registrationEnabled) {
            throw new ForbiddenOperationException(ErrorMessages.REGISTRATION_DISABLED);
        }
        if (userRepository.existsByEmail(User.normalizeEmail(request.email()))) {
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        // saveAndFlush : une violation d'unicité (inscription concurrente) échoue avant l'envoi de l'email.
        User user = userRepository.saveAndFlush(
                UserRequestMapper.toNewUser(request, passwordEncoder.encode(request.password())));

        sendVerificationEmailUseCase.execute(user.getPublicId(), user.getEmail(), user.getFirstName());
        auditLogService.logAs(user.getEmail(), "REGISTER", "USER", user.getPublicId(), null, AuditLog.ActionStatus.SUCCESS);

        return UserResponseMapper.toResponse(user);
    }
}
