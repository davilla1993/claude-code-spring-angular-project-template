package com.gfolly.backend.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP sur WebSocket.
 * <ul>
 *   <li>Endpoint : /api/ws — le handshake HTTP passe par la chaîne Spring Security (cookie access_token requis).</li>
 *   <li>Origines autorisées : celles de la configuration CORS (protection contre le cross-site WebSocket hijacking).</li>
 *   <li>Destinations : /topic (broadcast), /queue + /user (messages ciblés, principal = publicId utilisateur).</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;

    public WebSocketConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/ws").setAllowedOrigins(allowedOrigins);
    }
}
