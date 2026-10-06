# Backend - Spring Boot

Les règles de la racine s'appliquent toujours.

Respecter l'architecture approuvée :
- API/controllers
- application/services
- domain/business rules
- persistence
- infrastructure

Suivre la version Spring Boot réellement présente.

Vérifier :
- dependency injection
- validation
- transactions
- exception handling
- profiles/configuration
- observability
- secure defaults

API :
- valider toute entrée externe
- utiliser des DTO explicites lorsque pertinent
- éviter d'exposer directement les entités de persistence
- erreurs cohérentes
- pas de breaking change silencieux

DB :
- migrations
- intégrité référentielle
- transactions
- N+1
- indexes justifiés
- aucune modification destructive sans accord explicite

Tests unitaires et tests d'intégration lorsque les frontières le justifient.

## Socle existant

Le backend fournit déjà authentification, utilisateurs, audit, stockage de fichiers, WebSocket et rate limiting
(voir `docs/ARCHITECTURE.md` [socle], `docs/API_CONTRACT.md`, `docs/SECURITY.md`).
- Réutiliser ces composants ; ne pas les dupliquer ni contourner `SecurityConfig`.
- Nouvelles permissions : étendre `iam/domain/Permission` et `Role`.
- Commandes : `./mvnw test` (le test de contexte nécessite Docker, sinon il est ignoré) ;
  `./mvnw spring-boot:run` (profil `dev` par défaut ; PostgreSQL local + MailHog démarrés).
- Configuration : `application.yml` (commun, sans secret), `application-dev.yml` (valeurs locales en clair),
  `application-prod.yml` (variables d'environnement obligatoires, sans valeur par défaut).
  Profil par défaut : `dev` ; en production `SPRING_PROFILES_ACTIVE=prod` est obligatoire (voir `docs/PREPROD.md`).
  Toute nouvelle propriété sensible ou propre à l'environnement doit être ajoutée aux deux profils et à `.env.example`.

## Backend Architecture Rules

- Use DDD-light.
- Do not introduce Value Objects by default.
- Create project-owned DTOs.
- Create explicit DTO mappers.
- Do not introduce external DTO/mapping libraries without explicit approval.
- Respect Single Responsibility Principle.
- Avoid god classes and catch-all methods.
- Keep controllers thin.
- Keep business logic in appropriate services/domain components.
