package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.infrastructure.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void execute(String userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}


