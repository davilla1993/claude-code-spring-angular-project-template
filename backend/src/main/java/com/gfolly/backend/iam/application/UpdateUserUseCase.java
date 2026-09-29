package com.gfolly.quantly_backend.iam.application;

import com.gfolly.quantly_backend.iam.api.dto.requests.UpdateUserRequest;
import com.gfolly.quantly_backend.iam.api.dto.responses.UserResponse;
import com.gfolly.quantly_backend.iam.domain.User;
import com.gfolly.quantly_backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.quantly_backend.iam.infrastructure.mapper.UserMapper;
import com.gfolly.quantly_backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.quantly_backend.shared.util.ErrorMessages;
import com.gfolly.quantly_backend.shared.util.NormalizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse execute(String userPublicId, UpdateUserRequest request) {
        User user = userRepository.findByPublicIdGlobal(userPublicId)
                .orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND));

        String firstName = NormalizationUtils.formatFirstName(request.firstName());
        String lastName = NormalizationUtils.formatLastName(request.lastName());

        if (user.getRole() == com.gfolly.quantly_backend.iam.domain.Role.OWNER) {
            userRepository.updateProfileGlobal(user.getEmail(), firstName, lastName);
            user.setFirstName(firstName);
            user.setLastName(lastName);
        } else {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(request.role());
            userRepository.save(user);
        }

        return UserMapper.toResponse(user);
    }
}
