package com.gfolly.backend.iam.infrastructure.mapper.request;

import com.gfolly.backend.iam.api.dto.requests.CreateUserRequest;
import com.gfolly.backend.iam.api.dto.requests.RegisterRequest;
import com.gfolly.backend.iam.domain.Role;
import com.gfolly.backend.iam.domain.User;

/**
 * Construit les entités User à partir des requêtes. Le hash du mot de passe est
 * calculé par l'appelant : le mapper ne porte aucune logique de sécurité.
 */
public final class UserRequestMapper {

    /** Inscription publique : rôle USER, email à vérifier. */
    public static User toNewUser(RegisterRequest request, String passwordHash) {
        User user = baseUser(request.firstName(), request.lastName(), request.email(), passwordHash);
        user.setRole(Role.USER);
        user.setFirstLogin(false);
        return user;
    }

    /** Création par un administrateur : mot de passe temporaire à remplacer à la première connexion. */
    public static User toNewUser(CreateUserRequest request, String passwordHash) {
        User user = baseUser(request.firstName(), request.lastName(), request.email(), passwordHash);
        user.setRole(request.role());
        user.setFirstLogin(true);
        return user;
    }

    private static User baseUser(String firstName, String lastName, String email, String passwordHash) {
        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(User.normalizeEmail(email));
        user.setPasswordHash(passwordHash);
        user.setActive(true);
        user.setEmailVerified(false);
        return user;
    }

    private UserRequestMapper() {}
}
