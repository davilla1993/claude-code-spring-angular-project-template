package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.CreateUserRequest;
import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.EmailAlreadyExistsException;
import com.gfolly.backend.iam.infrastructure.mapper.UserMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.shared.util.NormalizationUtils;
import lombok.RequiredArgsConstructor;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import com.gfolly.backend.system.domain.AuditLog;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(ErrorMessages.emailAlreadyExists(request.email()));
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new EmailAlreadyExistsException(ErrorMessages.usernameAlreadyExists(request.username()));
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(NormalizationUtils.formatFirstName(request.firstName()));
        user.setLastName(NormalizationUtils.formatLastName(request.lastName()));
        user.setRole(request.role());
        user.setFirstLogin(true);
        user = userRepository.save(user);

        auditLogService.log("USER_CREATE", "USER", user.getPublicId(),
                ErrorMessages.userCreateAuditLog(user.getEmail(), user.getRole().name()),
                AuditLog.ActionStatus.SUCCESS);

        return UserMapper.toResponse(user);
    }
}

