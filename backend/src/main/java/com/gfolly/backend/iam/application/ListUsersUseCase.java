package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.responses.UserResponse;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.infrastructure.mapper.response.UserResponseMapper;
import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import com.gfolly.backend.shared.api.PageResponse;
import com.gfolly.backend.shared.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {

    /** Liste blanche : trier sur une collection (roles) dupliquerait des lignes, trier sur passwordHash révélerait de l'information. */
    private static final Set<String> SORTABLE_PROPERTIES = Set.of("createdAt", "email", "firstName", "lastName", "active");

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> execute(Role role, Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!SORTABLE_PROPERTIES.contains(order.getProperty())) {
                throw new IllegalArgumentException(ErrorMessages.INVALID_PARAMETER);
            }
        });

        var page = role != null
                ? userRepository.findAllNotDeletedHavingRole(role, pageable)
                : userRepository.findAllByDeletedFalse(pageable);
        return PageResponse.from(page.map(UserResponseMapper::toResponse));
    }
}
