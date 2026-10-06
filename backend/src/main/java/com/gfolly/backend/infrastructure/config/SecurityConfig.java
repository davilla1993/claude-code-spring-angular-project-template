package com.gfolly.backend.infrastructure.config;

import com.gfolly.backend.iam.infrastructure.security.JwtAuthenticationFilter;
import com.gfolly.backend.iam.infrastructure.security.JwtService;
import com.gfolly.backend.infrastructure.security.RateLimitFilter;
import com.gfolly.backend.shared.api.ApiResponse;
import com.gfolly.backend.shared.util.ErrorMessages;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Endpoints accessibles sans authentification (tous en POST). */
    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtService jwtService,
                                                   ObjectMapper objectMapper) throws Exception {
        http
                // CSRF désactivé : API stateless dont les cookies sont SameSite=Strict (jamais envoyés en cross-site).
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                writeError(response, HttpStatus.UNAUTHORIZED, ErrorMessages.AUTHENTICATION_REQUIRED, objectMapper))
                        .accessDeniedHandler((request, response, e) ->
                                writeError(response, HttpStatus.FORBIDDEN, ErrorMessages.ACCESS_DENIED, objectMapper)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                // Filtres instanciés ici (et non déclarés en beans) pour ne pas être enregistrés une seconde fois
                // comme filtres servlet globaux par Spring Boot.
                .addFilterBefore(new RateLimitFilter(objectMapper), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    private static void writeError(HttpServletResponse response, HttpStatus status, String message,
                                   ObjectMapper objectMapper) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
