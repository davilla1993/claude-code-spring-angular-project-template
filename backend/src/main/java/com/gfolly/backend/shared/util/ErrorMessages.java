package com.gfolly.backend.shared.util;

/**
 * Messages exposés au client. Centralisés pour garantir la cohérence des réponses API.
 */
public final class ErrorMessages {

    // Succès — Auth
    public static final String LOGOUT_SUCCESS           = "Déconnexion réussie.";
    public static final String REGISTER_SUCCESS         = "Compte créé. Un code de vérification a été envoyé par email.";
    public static final String EMAIL_VERIFIED_SUCCESS   = "Adresse email vérifiée avec succès.";
    public static final String VERIFICATION_CODE_SENT   = "Si l'adresse email existe et n'est pas encore vérifiée, un nouveau code a été envoyé.";
    public static final String PASSWORD_RESET_GENERIC_SENT = "Si l'adresse email existe, un code de réinitialisation a été envoyé.";
    public static final String PASSWORD_RESET_SUCCESS   = "Mot de passe réinitialisé avec succès.";
    public static final String PASSWORD_CHANGED_SUCCESS = "Mot de passe mis à jour.";

    // Succès — Utilisateurs
    public static final String USER_DEACTIVATED = "Utilisateur désactivé.";
    public static final String USER_ACTIVATED   = "Utilisateur activé.";

    // Authentification
    public static final String AUTHENTICATION_REQUIRED = "Authentification requise. Veuillez vous connecter.";
    public static final String TOKEN_INVALID       = "Votre session a expiré. Veuillez vous reconnecter.";
    public static final String CREDENTIALS_INVALID = "Email ou mot de passe incorrect.";
    public static final String ACCOUNT_DISABLED    = "Votre compte est désactivé. Contactez l'administrateur.";
    public static final String EMAIL_NOT_VERIFIED  = "Veuillez vérifier votre adresse email avant de vous connecter. Un code de vérification vient de vous être envoyé.";
    public static final String CODE_INVALID        = "Code invalide ou expiré.";
    public static final String CODE_EXPIRED        = "Le code a expiré. Veuillez en demander un nouveau.";
    public static final String EMAIL_ALREADY_VERIFIED = "Cet email est déjà vérifié.";
    public static final String PASSWORD_ALREADY_SET   = "Ce mot de passe a déjà été défini.";
    public static final String CURRENT_PASSWORD_INCORRECT = "Le mot de passe actuel est incorrect.";
    public static final String REGISTRATION_DISABLED  = "L'inscription est désactivée sur cette instance.";

    // Rate limiting
    public static final String RATE_LIMIT_EXCEEDED = "Trop de tentatives. Veuillez réessayer dans 15 minutes.";

    // Autorisation
    public static final String ACCESS_DENIED          = "Vous n'avez pas la permission d'accéder à cette ressource.";
    public static final String CANNOT_CHANGE_OWN_ROLE = "Vous ne pouvez pas modifier votre propre rôle.";
    public static final String CANNOT_DEACTIVATE_SELF = "Vous ne pouvez pas désactiver votre propre compte.";

    // Ressources
    public static final String USER_NOT_FOUND     = "Utilisateur introuvable.";
    public static final String ENDPOINT_NOT_FOUND = "L'endpoint demandé est introuvable.";

    // Conflits
    public static final String EMAIL_ALREADY_EXISTS = "Cette adresse email est déjà utilisée.";
    public static final String DUPLICATE_ENTRY      = "Cette entrée existe déjà.";

    // Validation
    public static final String VALIDATION_ERROR       = "Erreur de validation des données.";
    public static final String MALFORMED_REQUEST_BODY = "Corps de la requête invalide ou mal formé.";
    public static final String INVALID_PARAMETER      = "Paramètre invalide.";

    // Fichiers
    public static final String FILE_REQUIRED          = "Un fichier est requis pour cette opération.";
    public static final String FILE_UPLOAD_FAILED     = "Le chargement du fichier a échoué. Veuillez réessayer.";
    public static final String FILE_SIZE_EXCEEDED     = "La taille du fichier dépasse la limite autorisée.";
    public static final String UNSUPPORTED_FILE_TYPE  = "Ce type de fichier n'est pas autorisé.";
    public static final String STORAGE_INIT_FAILED    = "Impossible d'initialiser le stockage de fichiers.";

    // Système
    public static final String INTERNAL_SERVER_ERROR = "Une erreur système est survenue. Veuillez réessayer plus tard.";

    public static String missingParameter(String paramName) {
        return String.format("Paramètre requis manquant : '%s'.", paramName);
    }

    private ErrorMessages() {}
}
