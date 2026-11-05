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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

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

        Set<Role> previousRoles = new TreeSet<>(user.getRoles());
        Set<Role> requestedRoles = new TreeSet<>(request.roles());
        boolean rolesChanged = !previousRoles.equals(requestedRoles);
        if (rolesChanged && actorId.equals(targetId)) {
            throw new ForbiddenOperationException(ErrorMessages.CANNOT_CHANGE_OWN_ROLE);
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());

        if (rolesChanged) {
            user.getRoles().clear();
            user.getRoles().addAll(requestedRoles);
            // Une collection modifiée seule ne déclenche pas d'UPDATE sur users : sans cela, updated_at et updated_by resteraient inchangés.
            user.setUpdatedAt(LocalDateTime.now());
            // Ferme la session : les nouveaux droits s'appliquent dès la reconnexion.
            refreshTokenRepository.revokeAllByUserId(targetId);
            user.setCurrentSessionId(null);
        }

        auditLogService.log("USER_UPDATE", "USER", targetId,
                rolesChanged ? "roles " + previousRoles + " -> " + requestedRoles : null,
                AuditLog.ActionStatus.SUCCESS);

        return UserResponseMapper.toResponse(user);
    }
}
