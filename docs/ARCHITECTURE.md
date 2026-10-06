# Architecture

> À générer/compléter durant le bootstrap. Les sections marquées **[socle]** décrivent le code fourni par le template.

## Architecture overview
## System context

## Backend architecture [socle]

Spring Boot 4 / Java 25, monolithe modulaire DDD-light, **mono-tenant**. Package racine `com.gfolly.backend` :

| Module | Rôle |
|---|---|
| `iam` | Identité : utilisateurs, rôles/permissions, authentification JWT, emails transactionnels |
| `system` | Journal d'audit |
| `shared` | `ApiResponse`, `PageResponse`, `GlobalExceptionHandler`, `BaseEntity`, utilitaires sécurité |
| `infrastructure` | Configuration transverse : sécurité, CORS, WebSocket, async, stockage de fichiers, rate limiting |

Chaque module métier suit : `api` (controllers + `dto/requests|responses`) → `application` (un use case par classe) → `domain` (entités, enums, exceptions) → `infrastructure` (repositories, `mapper/request|response`, adaptateurs).

Conventions du socle à réutiliser :
- Entités : étendre `BaseEntity` (publicId UUID, audit JPA createdAt/By, soft delete).
- Réponses : toujours `ApiResponse<T>` ; listes paginées via `PageResponse.from(page)`.
- Erreurs : exception métier dédiée + handler dans `GlobalExceptionHandler`, messages dans `ErrorMessages`.
- Audit : `AuditLogService.log(action, entityType, entityId, details, status)`.
- Fichiers : injecter `StoragePort`.

## Frontend architecture [socle]

Angular 22 standalone, zoneless, signals, Reactive Forms, Vitest. Toutes les pages sont chargées à la demande (lazy).

| Dossier | Rôle |
|---|---|
| `core/api` | Services HTTP typés par ressource |
| `core/services` | `SessionStore` (utilisateur courant), `TokenRefresher` |
| `core/interceptors` | `withCredentials` + refresh automatique sur 401 |
| `core/guards` | Accès aux routes (session, première connexion, permissions) |
| `core/layout` | En-tête et pied de page |
| `features/auth` | Connexion, inscription, vérification email, mot de passe oublié/réinitialisation, première connexion |
| `features/admin` | Gestion des utilisateurs |
| `features/auditor` | Journal d'audit |
| `features/account`, `features/dashboard` | Compte courant, accueil de l'espace connecté (placeholder) |
| `features/public` | Accueil, pages légales (placeholders), 404 |
| `shared` | Modèles, pipes, composants et toasts réutilisables |

Routes : `/` et `/legal/*` publiques, `/auth/*` parcours d'authentification, `/app/*` espace connecté.
En dev, `proxy.conf.json` redirige `/api` (et le WebSocket) vers le backend : frontend et API restent same-origin.


## Database architecture

PostgreSQL. Schéma géré par Hibernate (`ddl-auto=update`) dans le socle — à remplacer par des migrations (Flyway/Liquibase) si le projet l'exige.

## API / integrations

Voir `docs/API_CONTRACT.md`.

## Authentication and authorization

Voir `docs/SECURITY.md`.

## Deployment

Variables d'environnement : voir `.env.example` et `backend/src/main/resources/application.yml`.
Profils Spring : `dev` (poste local, valeurs en clair dans `application-dev.yml`) et `prod` (`application-prod.yml`,
variables d'environnement obligatoires listées dans `.env.example`). Profil par défaut : `dev` (si aucun profil n'est demandé) ; en production, `SPRING_PROFILES_ACTIVE=prod` est obligatoire.
Checklist de mise en production : `PREPROD.md`.
Prérequis locaux : PostgreSQL (base `template_db`) et MailHog (SMTP 1025, interface 8025).

## Observability

Logs SLF4J ; journal d'audit applicatif en base.

## Architectural decisions

Voir docs/adr/.
