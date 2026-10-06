package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.CreateUserRequest;
import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailAlreadyExistsException;
import com.gfolly.backend.iam.infrastructure.mapper.request.UserRequestMapper;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Création d'un compte par un administrateur, avec mot de passe temporaire.
 * À la première connexion, l'utilisateur vérifie son email puis définit son mot de passe.
 */
@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        if (userRepository.existsByEmail(User.normalizeEmail(request.email()))) {
            throw new EmailAlreadyExistsException(ErrorMessages.EMAIL_ALREADY_EXISTS);
        }

        User user = userRepository.saveAndFlush(
                UserRequestMapper.toNewUser(request, passwordEncoder.encode(request.temporaryPassword())));

        auditLogService.log("USER_CREATE", "USER", user.getPublicId(),
                "email=" + user.getEmail() + ", role=" + user.getRole(), AuditLog.ActionStatus.SUCCESS);

        return UserResponseMapper.toResponse(user);
    }
}
