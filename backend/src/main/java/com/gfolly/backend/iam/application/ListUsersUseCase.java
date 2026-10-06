package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.api.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> execute(Role role, Pageable pageable) {
        var page = role != null
                ? userRepository.findAllByDeletedFalseAndRole(role, pageable)
                : userRepository.findAllByDeletedFalse(pageable);
        return PageResponse.from(page.map(UserResponseMapper::toResponse));
    }
}
