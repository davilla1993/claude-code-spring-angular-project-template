package com.gfolly.backend.iam.application;

import com.gfolly.backend.iam.api.dto.requests.LoginRequest;
import com.gfolly.backend.iam.application.auth.LoginFromPortalUseCase;
import com.gfolly.backend.iam.application.auth.LoginFromSubdomainUseCase;
import com.gfolly.backend.iam.application.dto.AuthSessionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final LoginFromPortalUseCase loginFromPortalUseCase;
    private final LoginFromSubdomainUseCase loginFromSubdomainUseCase;

    @Transactional
    public AuthSessionResult execute(LoginRequest request) {
        boolean isSubdomainLogin = request.subdomain() != null && !request.subdomain().isBlank();
        return isSubdomainLogin
                ? loginFromSubdomainUseCase.execute(request)
                : loginFromPortalUseCase.execute(request);
    }
}

