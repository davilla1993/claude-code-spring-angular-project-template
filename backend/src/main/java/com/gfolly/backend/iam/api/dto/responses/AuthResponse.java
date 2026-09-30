package com.gfolly.backend.iam.api.dto.responses;

import java.util.List;

/**
 * Les tokens JWT sont transmis via HttpOnly cookies (Set-Cookie).
 * Cette réponse contient soit les infos de l'utilisateur (si 1 boutique),
 * soit la liste des boutiques (si multi-boutique).
 */
public record AuthResponse(
        UserResponse user,
        List<TenantSelectionResponse> shops
) {
    public AuthResponse(UserResponse user) {
        this(user, null);
    }

    public AuthResponse(List<TenantSelectionResponse> shops) {
        this(null, shops);
    }
}

