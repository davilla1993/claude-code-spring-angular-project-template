package com.gfolly.backend.iam.infrastructure.security;

import com.gfolly.backend.iam.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Vérifie à chaque requête qu'un access token appartient à la session ouverte de l'utilisateur.
 * Une lecture d'une ligne via l'index unique de public_id ; le compte doit aussi être actif et non supprimé.
 */
@Service
@RequiredArgsConstructor
public class CurrentSessionChecker {

    private final UserRepository userRepository;

    public boolean isCurrent(String userId, String sessionId) {
        return sessionId != null && userRepository.findCurrentSessionId(userId).filter(sessionId::equals).isPresent();
    }
}
