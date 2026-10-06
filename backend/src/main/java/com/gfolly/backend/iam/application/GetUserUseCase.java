package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.exception.UserNotFoundException;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.util.ErrorMessages;
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
}
