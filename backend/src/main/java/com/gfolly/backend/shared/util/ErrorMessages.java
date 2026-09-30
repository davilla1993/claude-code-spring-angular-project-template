package com.gfolly.backend.shared.util;

public class ErrorMessages {

    // Succès — Auth
    public static final String SESSION_EXPIRED          = "Session expirée. Veuillez vous reconnecter.";
    public static final String LOGOUT_SUCCESS           = "Déconnexion réussie.";
    public static final String EMAIL_VERIFIED_SUCCESS   = "Adresse email vérifiée avec succès.";
    public static final String VERIFICATION_CODE_SENT   = "Si l'adresse email existe et n'est pas encore vérifiée, un nouveau code a été envoyé.";
    public static final String PASSWORD_RESET_SUCCESS   = "Mot de passe réinitialisé avec succès.";
    public static final String PASSWORD_CHANGED_SUCCESS = "Mot de passe mis à jour.";

    // Succès — Utilisateurs
    public static final String USER_DEACTIVATED = "Utilisateur désactivé.";
    public static final String USER_ACTIVATED   = "Utilisateur activé.";

    // Succès — Catalogue
    public static final String UNIT_DELETED     = "Unité de mesure supprimée.";
    public static final String CATEGORY_DELETED = "Catégorie supprimée.";
    public static final String PRODUCT_DELETED  = "Produit supprimé.";

    // Succès — Caisse
    public static final String CASH_REGISTER_DELETED = "Terminal supprimé.";

    // Authentification
    public static final String TOKEN_MISSING = "Authentification requise. Veuillez vous connecter avec vos identifiants.";
    public static final String TOKEN_INVALID = "Votre session a expiré. Veuillez vous reconnecter.";
    public static final String TOKEN_EXPIRED = "Votre session a expiré. Veuillez vous reconnecter.";
    public static final String TOKEN_REVOKED = "Cette session a été révoquée. Veuillez vous reconnecter.";
    public static final String CREDENTIALS_INVALID = "Nom d'utilisateur ou mot de passe incorrect.";
    public static final String ACCOUNT_DISABLED = "Votre compte est désactivé. Contactez l'administrateur.";
    public static final String EMAIL_NOT_VERIFIED = "Veuillez vérifier votre adresse email avant de vous connecter. Consultez votre boîte de réception pour le lien de vérification.";
    public static final String LOGIN_DOMAIN_REQUIRED = "Identifiants invalides ou domaine incorrect.";
    public static final String LOGIN_NO_DOMAIN_REQUIRED = "Identifiants invalides ou domaine incorrect.";
    public static final String LINK_EXPIRED_OR_INVALID = "Lien invalide ou expiré.";
    public static final String LINK_RESEND_REQUIRED = "Le lien a expiré. Veuillez en demander un nouveau.";
    public static final String EMAIL_ALREADY_VERIFIED = "Cet email est déjà vérifié.";
    public static final String PASSWORD_RESET_GENERIC_SENT = "Si l'adresse email existe, un lien de réinitialisation sera envoyé.";
    public static final String PASSWORD_ALREADY_SET = "Ce mot de passe a déjà été défini.";

    // Rate limiting
    public static final String RATE_LIMIT_EXCEEDED = "Trop de tentatives. Veuillez réessayer dans 15 minutes.";

    public static String tooManyResends(int max) {
        return String.format("Trop de demandes. Vous pouvez demander au maximum %d codes par heure. Réessayez plus tard.", max);
    }

    // Stockage fichiers
    public static final String STORAGE_INIT_FAILED    = "Impossible d'initialiser le stockage de fichiers. Contactez l'administrateur.";
    public static final String TENANT_CONTEXT_MISSING = "Contexte tenant introuvable. Impossible d'enregistrer le fichier.";

    // Inventaire physique
    public static final String INVENTORY_ALREADY_IN_PROGRESS = "Un inventaire est déjà en cours. Clôturez-le avant d'en créer un nouveau.";
    public static final String INVENTORY_ALREADY_CLOSED      = "Cet inventaire est déjà clôturé.";
    public static final String INVENTORY_LINES_INCOMPLETE    = "Toutes les lignes doivent être comptées avant la validation de l'inventaire.";
    public static final String INVENTORY_LINE_LOCKED         = "Impossible de modifier une ligne appartenant à un inventaire clôturé.";
    public static final String INVENTORY_NOT_FOUND           = "Inventaire introuvable.";
    public static final String INVENTORY_LINE_NOT_FOUND      = "Ligne d'inventaire introuvable.";
    public static final String SALES_BLOCKED_INVENTORY_IN_PROGRESS = "Activité de caisse suspendue : un inventaire physique est en cours. Ventes, annulations et retours seront à nouveau possibles une fois l'inventaire clôturé.";

    // Autorisation
    public static final String ACCESS_DENIED = "Vous n'avez pas la permission d'accéder à cette ressource.";
    public static final String ADMIN_REQUIRED = "Seuls les administrateurs peuvent accéder à cette ressource.";
    public static final String ROLE_REQUIRED = "Vous n'avez pas le rôle requis pour effectuer cette action.";

    // Ressources
    public static final String RESOURCE_NOT_FOUND = "La ressource demandée n'existe pas.";
    public static final String USER_NOT_FOUND = "Utilisateur introuvable.";
    public static final String ROLE_NOT_FOUND = "Rôle introuvable.";
    public static final String CATEGORY_NOT_FOUND = "Catégorie introuvable.";
    public static final String PRODUCT_NOT_FOUND = "Produit introuvable.";

    // Conflits
    public static final String USERNAME_ALREADY_EXISTS = "Ce nom d'utilisateur est déjà utilisé. Veuillez en choisir un autre.";
    public static final String EMAIL_ALREADY_EXISTS = "Cet email est déjà utilisé. Veuillez en choisir un autre.";
    public static final String SUBDOMAIN_ALREADY_EXISTS = "Ce sous-domaine est déjà utilisé. Veuillez en choisir un autre.";
    public static final String CATEGORY_ALREADY_EXISTS = "Cette catégorie existe déjà.";
    public static final String PRODUCT_ALREADY_EXISTS = "Ce produit existe déjà.";
    public static final String RESOURCE_ALREADY_EXISTS = "Cette ressource existe déjà.";
    public static final String DUPLICATE_ENTRY = "Cette entrée existe déjà.";

    // Validation
    public static final String VALIDATION_ERROR = "Erreur de validation des données.";
    public static final String INVALID_INPUT = "Les données fournies sont invalides.";
    public static final String MALFORMED_REQUEST_BODY = "Corps de la requête invalide ou mal formé.";
    public static final String ENDPOINT_NOT_FOUND = "L'endpoint demandé est introuvable.";

    public static final String PASSWORD_INVALID = "Le mot de passe doit contenir au moins 6 caractères, une majuscule, un chiffre et un caractère spécial.";
    public static final String PASSWORD_MISMATCH = "Les mots de passe ne correspondent pas.";
    public static final String CURRENT_PASSWORD_INCORRECT = "Le mot de passe actuel est incorrect.";
    public static final String EMPTY_FIELD = "Ce champ est obligatoire.";
    public static final String SUBDOMAIN_INVALID = "Le format du sous-domaine est invalide. Utilisez uniquement des lettres minuscules, des chiffres et des tirets.";
    public static final String TERMS_NOT_ACCEPTED = "Vous devez accepter les conditions d'utilisation pour continuer.";

    // Opérations
    public static final String OPERATION_FAILED = "L'opération n'a pas pu être complétée. Veuillez réessayer.";
    public static final String DELETE_FAILED = "La suppression a échoué. Vérifiez que la ressource existe.";
    public static final String UPDATE_FAILED = "La mise à jour a échoué. Vérifiez les données fournies.";
    public static final String CREATE_FAILED = "La création a échoué. Veuillez vérifier les données fournies.";
    public static final String FILE_UPLOAD_FAILED = "Le chargement du fichier a échoué. Veuillez réessayer.";
    public static final String FILE_REQUIRED = "Un fichier est requis pour cette opération.";
    public static final String IMAGE_REQUIRED = "Une image est requise.";
    public static final String LOGO_REQUIRED = "Le logo du restaurant est requis.";
    public static final String FILE_SIZE_EXCEEDED = "La taille du fichier ne doit pas dépasser 5 Mo.";
    public static final String UNSUPPORTED_IMAGE_TYPE = "Seules les images JPEG, PNG, GIF et WebP sont autorisées.";

    // Métier — Inventory
    public static final String BARCODE_NOT_FOUND = "Code-barres introuvable.";
    public static final String DUPLICATE_BARCODE = "Ce code-barres est déjà associé à un autre produit.";
    public static final String UNIT_OF_MEASURE_NOT_FOUND = "Unité de mesure introuvable.";
    public static final String UNIT_OF_MEASURE_DUPLICATE = "Une unité avec ce symbole existe déjà.";

    public static String insufficientStock(String productName) {
        return String.format("Stock insuffisant pour le produit '%s'.", productName);
    }

    // Métier — Cashdesk
    public static final String CASH_REGISTER_NOT_FOUND = "Terminal de caisse introuvable.";
    public static final String GLOBAL_SESSION_NOT_FOUND = "Session journalière introuvable.";
    public static final String CASH_SESSION_NOT_FOUND = "Session caissier introuvable.";
    public static final String GLOBAL_SESSION_ALREADY_OPEN = "Une session journalière est déjà ouverte.";
    public static final String GLOBAL_SESSION_NOT_OPEN = "Aucune session journalière n'est ouverte. Veuillez ouvrir la journée avant de démarrer une session.";
    public static final String GLOBAL_SESSION_HAS_OPEN_CASH_SESSIONS = "Impossible de fermer la journée : des sessions caissier sont encore ouvertes.";
    public static final String CASH_SESSION_ALREADY_OPEN = "Vous avez déjà une session de caisse ouverte.";
    public static final String CASH_SESSION_ALREADY_CLOSED = "Cette session de caisse est déjà fermée.";
    public static final String INVALID_SESSION_PIN = "Code PIN incorrect.";
    public static final String CASH_REGISTER_ALREADY_IN_USE = "Ce terminal est déjà utilisé par une session ouverte.";

    // Métier — Sales
    public static final String SALE_NOT_FOUND = "Vente introuvable.";
    public static final String SALE_ALREADY_CANCELLED = "Cette vente a déjà été annulée.";

    // Métier — Purchases
    public static final String SUPPLIER_NOT_FOUND = "Fournisseur introuvable.";
    public static final String SUPPLIER_NAME_ALREADY_EXISTS = "Un fournisseur avec ce nom existe déjà.";
    public static final String PURCHASE_ORDER_NOT_FOUND = "Bon de réception introuvable.";
    public static final String PURCHASE_ORDER_ALREADY_VALIDATED = "Ce bon de réception est déjà validé.";
    public static final String PURCHASE_ORDER_ALREADY_CANCELLED = "Ce bon de réception est déjà annulé.";

    // Impersonation (mode support Commander)
    public static final String IMPERSONATION_READ_ONLY = "Mode support en lecture seule : les modifications doivent être effectuées par le propriétaire de la boutique ou l'un de ses collaborateurs.";

    // Métier — IAM
    public static final String USER_INACTIVE = "Ce compte est inactif.";
    public static final String TENANT_INACTIVE = "Ce commerce est temporairement inactif.";
    public static final String TENANT_NOT_FOUND = "Commerce introuvable.";
    public static final String INSUFFICIENT_PERMISSIONS = "Vous n'avez pas les permissions nécessaires pour cette action.";
    public static final String ACTION_NOT_ALLOWED = "Cette action n'est pas autorisée.";
    public static final String REGISTRATION_DISABLED = "L'inscription est désactivée sur cette instance.";

    // Paramétrage
    public static final String ADMIN_ROLE_NOT_FOUND = "Le rôle ADMIN n'a pas pu être trouvé. L'initialisation du restaurant a peut-être échoué.";
    public static final String DATA_INIT_FAILED = "L'initialisation des données par défaut a échoué.";

    // Erreurs système
    public static final String INTERNAL_SERVER_ERROR = "Une erreur système est survenue. Veuillez réessayer plus tard.";
    public static final String SERVICE_UNAVAILABLE = "Le service est temporairement indisponible. Veuillez réessayer plus tard.";


    public static final String USER_NOT_FOUND_IN_CONTEXT = "Utilisateur actuel introuvable.";

    public static String emailAlreadyExists(String email) {
        return String.format(
                "L'adresse email '%s' est déjà utilisée par un autre propriétaire. Veuillez en utiliser une autre ou vous connecter.",
                email);
    }

    public static String usernameAlreadyExists(String username) {
        return String.format("Le nom d'utilisateur '%s' est déjà utilisé. Veuillez en choisir un autre.", username);
    }

    public static String userCreateAuditLog(String email, String role) {
        return String.format("Création de l'utilisateur %s avec le rôle %s", email, role);
    }

    public static String subdomainAlreadyExists(String subdomain) {
        return String.format("Le sous-domaine '%s' est déjà utilisé. Veuillez en choisir un autre.", subdomain);
    }

    public static String missingParameter(String paramName) {
        return String.format("Paramètre requis manquant : '%s'.", paramName);
    }
}


