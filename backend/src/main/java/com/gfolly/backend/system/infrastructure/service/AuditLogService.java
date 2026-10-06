package com.gfolly.backend.system.infrastructure.service;

import com.gfolly.backend.shared.util.SecurityUtils;
import com.gfolly.backend.system.domain.AuditLog;
import com.gfolly.backend.system.infrastructure.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Journal d'audit.
 * <p>
 * Synchrone (le contexte HTTP et de sécurité est lu dans le thread de la requête) et
 * exécuté dans sa propre transaction : l'entrée est conservée même si l'opération
 * métier appelante échoue ensuite (ex : tentative de connexion refusée).
 * Une erreur d'audit est journalisée mais n'interrompt jamais l'opération métier.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private static final int MAX_USER_AGENT_LENGTH = 500;

    private final AuditLogRepository auditLogRepository;

    /** Audit attribué à l'utilisateur authentifié courant. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, String entityId, String details, AuditLog.ActionStatus status) {
        logAs(SecurityUtils.getCurrentUserEmail(), action, entityType, entityId, details, status);
    }

    /** Audit attribué explicitement (ex : connexion, où l'utilisateur n'est pas encore authentifié). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAs(String userEmail, String action, String entityType, String entityId, String details,
                      AuditLog.ActionStatus status) {
        try {
            HttpServletRequest request = currentRequest();
            AuditLog entry = AuditLog.builder()
                    .userEmail(userEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(request != null ? request.getRemoteAddr() : null)
                    .userAgent(request != null ? truncate(request.getHeader(HttpHeaders.USER_AGENT)) : null)
                    .status(status)
                    .actionDate(LocalDateTime.now())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write audit log '{}' for entity {}/{}", action, entityType, entityId, e);
        }
    }

    /**
     * L'IP est lue via getRemoteAddr() : derrière un reverse proxy, activer
     * server.forward-headers-strategy pour qu'elle reflète le client réel sans
     * faire confiance à un en-tête X-Forwarded-For forgé.
     */
    private static HttpServletRequest currentRequest() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs
                ? attrs.getRequest()
                : null;
    }

    private static String truncate(String value) {
        return value != null && value.length() > MAX_USER_AGENT_LENGTH ? value.substring(0, MAX_USER_AGENT_LENGTH) : value;
    }
}
