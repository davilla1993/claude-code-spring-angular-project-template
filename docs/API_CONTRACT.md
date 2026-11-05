# API Contract

OpenAPI est la source machine-readable préférée lorsqu'une API REST est utilisée.

> Les sections marquées **[socle]** décrivent l'API fournie par le template (authentification, utilisateurs, audit).
> Elles doivent être conservées et complétées, pas réécrites, lors du bootstrap.

## Backend API

Préfixe : `/api`. JSON uniquement. Identifiants exposés : `publicId` (UUID), jamais l'id technique.

### Auth — `/api/auth` [socle] (public, `POST` uniquement)

| Endpoint | Corps | Succès | Erreurs notables |
|---|---|---|---|
| `/register` | `{firstName, lastName, email, password}` | 201 `UserResponse` (compte USER non vérifié, code envoyé par email) | 409 email déjà utilisé, 403 inscription désactivée |
| `/login` | `{email, password}` | 200 `UserResponse` + cookies | 401 identifiants invalides / compte désactivé, 403 email non vérifié (`data.userId` fourni, nouveau code envoyé) |
| `/refresh` | — (cookie `refresh_token`) | 200 `UserResponse` + nouveaux cookies (rotation) | 401 token invalide/expiré/réutilisé |
| `/logout` | — (cookie `refresh_token`) | 200, cookies effacés, session courante révoquée | — |
| `/verify-email` | `{userId, code}` | 200 | 400 code invalide/expiré, 409 déjà vérifié |
| `/resend-verification` | `{email}` | 200 (réponse générique) | 409 déjà vérifié |
| `/forgot-password` | `{email}` | 200 (réponse générique) | — |
| `/reset-password` | `{email, code, newPassword}` | 200, toutes les sessions révoquées | 400 code invalide/expiré |

Rate limiting sur `register`, `login`, `verify-email`, `resend-verification`, `forgot-password`, `reset-password` : 429 au-delà de 5 tentatives / 15 min par compte et par endpoint.

### Utilisateurs — `/api/users` [socle] (authentifié)

| Méthode | Endpoint | Autorisation | Description |
|---|---|---|---|
| GET | `/me` | authentifié | Utilisateur courant, avec son profil actif |
| PUT | `/me/change-password` | authentifié | `{currentPassword, newPassword}` — 400 si mot de passe actuel faux |
| PUT | `/me/setup-password` | authentifié | `{newPassword}` — remplace le mot de passe temporaire (`firstLogin=true`), 409 sinon |
| GET | `?role=&page=&size=&sort=` | `user:view` | Liste paginée. `sort` : `createdAt`, `email`, `firstName`, `lastName`, `active` ; 400 sinon |
| GET | `/{id}` | `user:view` | Détail d'un utilisateur — 404 si introuvable |
| POST | `/` | `user:create` | `{firstName, lastName, email, temporaryPassword, roles[]}` (au moins un rôle) → 201 |
| PUT | `/{id}` | `user:update` | `{firstName, lastName, roles[]}` (au moins un rôle) — 403 si on modifie ses propres rôles |
| PATCH | `/{id}/activate` · `/{id}/deactivate` | `user:update` | 403 si on se désactive soi-même ; désactivation = sessions révoquées |
| POST | `/{id}/reset-password` | `user:update` | → `{temporaryPassword}` ; `firstLogin=true`, sessions révoquées |

`UserResponse` : `{publicId, email, firstName, lastName, roles[], activeRole, permissions[], active, emailVerified, firstLogin, createdAt}`.
`activeRole` et `permissions` ne sont renseignés que pour la session courante (`login`, `refresh`, `/me`, `PUT /api/auth/active-profile`) : `activeRole` vaut `null` et `permissions` est vide hors session ou tant qu'aucun profil n'est choisi.
`permissions` (codes du profil actif, ex : `user:view`) sert uniquement à adapter l'interface ; l'autorisation reste vérifiée côté serveur. Les permissions de plusieurs rôles ne se cumulent jamais.

Choix ou changement de profil : `PUT /api/auth/active-profile` (authentifié), `{role}` — réémet les cookies → `UserResponse` ; 403 si le rôle n'appartient pas à l'utilisateur, 401 sans refresh token valide. Le cookie `refresh_token` est requis : un client qui n'utilise que l'en-tête Bearer ne peut pas choisir de profil.

Sans profil actif (utilisateur à plusieurs rôles, juste après la connexion), seuls `/api/users/me`, `/api/users/me/**` et `/api/auth/active-profile` sont accessibles ; le reste renvoie 403, hors endpoints publics.

### Audit — `/api/audit-logs` [socle]

`GET ?userEmail=&entityType=&status=SUCCESS|FAILED&from=&to=&page=&size=&sort=` — permission `audit:log:view`. Tri par défaut : `actionDate,desc`. Chaque entrée indique `activeProfile`, le profil actif de l'auteur au moment de l'action.

### Fichiers — `/api/uploads/{clé}` [socle]

`GET` : lecture authentifiée des fichiers déposés via `FileStorageService` (pas d'endpoint d'upload générique : à ajouter par fonctionnalité). 200 avec le contenu du fichier, 401 sans session, 403 sans profil actif, 404 si la clé est invalide ou le fichier introuvable, 500 si le stockage est en panne. Le fichier est toujours renvoyé en entier : l'en-tête `Range` est ignoré.

### WebSocket — `/api/ws` [socle]

STOMP. Handshake authentifié par le cookie `access_token`, origines limitées à `CORS_ALLOWED_ORIGINS`.
Préfixes : `/app` (entrant), `/topic` (broadcast), `/user/queue` (ciblé, principal = `publicId`).

## Authentication [socle]

- Tokens transmis **uniquement** par cookies `HttpOnly; Secure; SameSite=Strict` :
  `access_token` (JWT, 15 min, `Path=/api`) et `refresh_token` (opaque, 7 jours, `Path=/api/auth`).
- Le frontend n'a jamais accès aux tokens : il appelle l'API avec `withCredentials: true`.
- Sur 401 : appeler `POST /api/auth/refresh` une seule fois (requêtes concurrentes à sérialiser : un refresh token réutilisé ferme la session), puis rejouer la requête ; si le refresh échoue → écran de connexion.
- `Authorization: Bearer <jwt>` est accepté pour les clients non-navigateur.
- Session unique : une nouvelle connexion ferme la session précédente de l'utilisateur ; ses tokens reçoivent aussitôt 401.

## Error model [socle]

Toutes les réponses utilisent l'enveloppe `ApiResponse` :

```json
{ "success": false, "message": "Message lisible", "data": null, "errors": ["champ : message"] }
```

Champs `null` omis. `errors` présent uniquement pour les erreurs de validation (400).
Codes : 400 validation/paramètre, 401 non authentifié, 403 interdit, 404 introuvable, 405 méthode non autorisée (avec l'en-tête `Allow`), 409 conflit, 413 fichier trop volumineux, 429 rate limit, 500 erreur interne (message générique).

## Pagination [socle]

Paramètres Spring : `page` (0-based), `size`, `sort=champ,asc|desc`. Réponse `data` :
`{content, page, size, totalElements, totalPages, last}`.

## Filtering and sorting
## Versioning
## Frontend integration rules [socle]

- Un service par ressource dans `frontend/src/app/core/api`, modèles TypeScript dans `shared/models` alignés sur les DTO backend.
- Ne jamais lire ni stocker de token côté frontend ; la session se lit via `GET /api/users/me` (`SessionStore`).
- Le refresh sur 401 est géré par l'intercepteur ; ne pas le réimplémenter dans les pages.
- Afficher `message` (et `errors`) de l'enveloppe d'erreur via `apiErrorMessage()`.
- Les permissions servent à adapter l'UI ; ne jamais s'y fier pour la sécurité.

Toute modification doit être vérifiée côté Spring Boot et Angular.
