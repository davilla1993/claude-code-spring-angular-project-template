# Security

> Les sections marquées **[socle]** décrivent les mécanismes fournis par le template. À compléter, pas à réécrire, lors du bootstrap.

## Threat model

## Authentication [socle]

- JWT HS256 (access token 15 min) + refresh token opaque 256 bits (7 jours), stocké **haché (SHA-256)** en base.
- Cookies `HttpOnly; Secure; SameSite=Strict`. CSRF désactivé en conséquence (API stateless, cookies jamais envoyés en cross-site).
- Rotation du refresh token à chaque usage ; la réutilisation d'un token révoqué révoque toutes les sessions de l'utilisateur.
- Multi-sessions : le logout ne révoque que la session courante ; désactivation, changement de rôle et réinitialisation du mot de passe révoquent toutes les sessions.
- Login : message identique pour email inconnu / mauvais mot de passe, temps de réponse constant (hash factice), état du compte (désactivé, non vérifié) révélé seulement après un mot de passe correct.
- Email vérifié obligatoire pour se connecter (code à 6 chiffres, 15 min, comparaison en temps constant).
- Mots de passe : BCrypt (coût 12), 8–72 caractères avec majuscule, minuscule, chiffre et caractère spécial.

## Authorization [socle]

- URL : tout est authentifié sauf les `POST /api/auth/*` publics.
- Méthode : `@PreAuthorize("hasAuthority('<permission>')")` sur chaque endpoint protégé.
- Les permissions sont dérivées du rôle à chaque requête (non embarquées dans le JWT).

## Roles and permissions [socle]

| Rôle | Permissions |
|---|---|
| ADMIN | `user:create`, `user:update`, `user:view`, `audit:log:view` |
| USER | aucune (accès authentifié simple) |

Règles : un utilisateur ne peut ni modifier son propre rôle, ni se désactiver. Le premier ADMIN est créé manuellement (SQL) pour chaque projet.
Étendre `Permission` et `Role` (`iam/domain`) pour les besoins du projet.

## Sensitive data

## Secrets [socle]

- `application-dev.yml` contient des valeurs **locales** en clair (base de dev, secret JWT de dev) : sans valeur hors du poste.
- `application-prod.yml` ne lit que des variables d'environnement, sans valeur par défaut (échec au démarrage si l'une manque).
  `JWT_SECRET` : au moins 32 caractères aléatoires, propre à chaque environnement.
- Profil par défaut `dev` (confort local) : en production, `SPRING_PROFILES_ACTIVE=prod` est **obligatoire**,
  sinon l'application tourne avec les valeurs de développement. Voir `PREPROD.md`.

## Input validation [socle]

Bean Validation sur tous les DTO de requête ; erreurs normalisées par `GlobalExceptionHandler`.

## API security [socle]

CORS limité aux origines exactes de `CORS_ALLOWED_ORIGINS` (aussi utilisées pour les WebSockets).

## File handling [socle]

- `LocalFileStorageAdapter` : noms générés (UUID), liste blanche d'extensions (`UPLOAD_ALLOWED_EXTENSIONS`, sans HTML/SVG), suppression confinée au répertoire de stockage, taille max 5 Mo.
- Fichiers servis sous `/api/uploads/**`, authentifiés.
- Le contenu réel des fichiers (magic bytes) n'est pas vérifié : à ajouter si le projet le requiert.

## Rate limiting [socle]

5 tentatives / 15 min par compte (email ou userId) et par endpoint sensible d'authentification. Compteurs en mémoire : à remplacer par un store partagé en cas de déploiement multi-instances.

## Audit logging [socle]

`AuditLogService` (transaction indépendante : conservé même si l'opération échoue). Événements : inscription, connexions réussies/échouées, réinitialisations et changements de mot de passe, création/modification/(dés)activation d'utilisateurs. IP issue de `getRemoteAddr()` : configurer `FORWARD_HEADERS_STRATEGY` derrière un proxy.

## Dependencies
## Security testing
## Security assumptions
