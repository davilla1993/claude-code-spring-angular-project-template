package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.UpdateUserRequest;
import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;
import com.gfolly.backend.iam.domain.exception.ForbiddenOperationException;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogService auditLogService;

    /**
     * @param actorId publicId de l'utilisateur qui effectue la modification
     */
    @Transactional
    public UserResponse execute(String actorId, String targetId, UpdateUserRequest request) {
        User user = userRepository.findByPublicIdAndDeletedFalse(targetId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        Role previousRole = user.getRole();
        boolean roleChanged = previousRole != request.role();
        if (roleChanged && actorId.equals(targetId)) {
            throw new ForbiddenOperationException(ErrorMessages.CANNOT_CHANGE_OWN_ROLE);
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(request.role());

        if (roleChanged) {
            // Force une reconnexion pour que les nouveaux droits soient appliqués à l'expiration de l'access token.
            refreshTokenRepository.revokeAllByUserId(targetId);
        }

        auditLogService.log("USER_UPDATE", "USER", targetId,
                roleChanged ? "role " + previousRole + " -> " + request.role() : null,
                AuditLog.ActionStatus.SUCCESS);

        return UserResponseMapper.toResponse(user);
    }
}
