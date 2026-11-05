package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
import com.gfolly.backend.shared.util.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse execute(String userId) {
        return userRepository.findByPublicIdAndDeletedFalse(userId)
                .map(UserResponseMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));
    }

    /** Utilisateur courant, avec le profil actif de sa session et les permissions de ce seul profil. */
    @Transactional(readOnly = true)
    public UserResponse executeForSession(UserPrincipal principal) {
        Role activeRole = principal.activeRole() != null ? Role.valueOf(principal.activeRole()) : null;
        return userRepository.findByPublicIdAndDeletedFalse(principal.userId())
                // Rôle retiré pendant la durée de vie de l'access token : le profil n'est plus présenté comme actif.
                .map(user -> UserResponseMapper.toResponse(user, user.getRoles().contains(activeRole) ? activeRole : null))
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));
    }
}
