# Backend - Spring Boot

Spring Boot 4 / Java 25, monolithe modulaire DDD-light, PostgreSQL. Package racine `com.gfolly.backend`. Suivre les versions réellement présentes dans `pom.xml`.

## Structure d'un module
`api` (controllers + `dto/requests|responses`) → `application` (un use case par classe) → `domain` (entités, enums, exceptions) → `infrastructure` (repositories, `mapper/request|response`, adaptateurs).

Modules du socle : `iam` (utilisateurs, rôles, permissions, JWT, sessions), `system` (journal d'audit), `shared` (briques communes), `infrastructure` (sécurité, CORS, WebSocket, rate limiting, ports et adaptateurs de stockage et d'email).

## Conventions du socle (à réutiliser, ne pas dupliquer)
- **Entités :** étendre `BaseEntity` (`publicId` UUID, audit JPA, soft delete). Seul le `publicId` est exposé, jamais l'id technique.
- **Controllers :** minces. `@Valid` sur chaque DTO de requête, `@PreAuthorize("hasAuthority('<permission>')")` sur chaque endpoint non public, réponse `ApiResponse<T>` ; listes paginées via `PageResponse.from(page)`. Jamais d'entité JPA en entrée ou en sortie.
- **Autorisation sur la ressource (ABAC) :** dans le use case, après le chargement et avant la modification ; refus par `ForbiddenOperationException` (`docs/PROJECT_RULES.md`, « Convention ABAC »).
- **Permissions :** étendre `iam/domain/Permission` et `Role`. Seul le profil actif compte. Ne jamais contourner `SecurityConfig`.
- **Erreurs :** exception métier dédiée + handler dans `GlobalExceptionHandler`, messages dans `ErrorMessages`.
- **Validation partagée :** `PersonName`, `PasswordPolicy` ; toute règle est reprise côté frontend.
- **Audit :** `AuditLogService.log(action, entityType, entityId, details, status)` pour toute action sensible.
- **Fichiers :** injecter `FileStorageService`, qui porte les contrôles communs ; le fournisseur se branche sur `StoragePort` (`LocalFileStorageAdapter`).
- **Emails :** ajouter une méthode à `EmailService`, qui rédige le message ; le fournisseur se branche sur `EmailPort` (`SmtpEmailAdapter`).
- **Mappers :** classes explicites dans `mapper/request` et `mapper/response`, sans logique métier.

## Base de données
- Schéma géré par Hibernate (`ddl-auto=update`) dans le socle ; migrations Flyway/Liquibase si le projet l'exige.
- Intégrité référentielle, transactions au niveau du use case, pas de N+1, index justifiés, listes paginées.
- Aucune modification destructive sans accord explicite.

## Configuration
- `application.yml` (commun, sans secret), `application-dev.yml` (valeurs locales en clair), `application-prod.yml` (variables d'environnement obligatoires, sans valeur par défaut).
- Profil par défaut : `dev` ; en production `SPRING_PROFILES_ACTIVE=prod` est obligatoire (`docs/PREPROD.md`).
- Toute nouvelle propriété sensible ou propre à l'environnement est ajoutée aux deux profils et à `.env.example`.

## Commandes (depuis `backend/`)
- Tests ciblés : `./mvnw -q test -Dtest='ClasseATest,ClasseBTest'`. La suite complète (`./mvnw test`, `./mvnw verify`) exige l'accord de l'utilisateur.
- Compilation : `./mvnw -q compile`.
- Lancement : `./mvnw spring-boot:run` (profil `dev` ; PostgreSQL local et MailHog démarrés).
- **JDK 25 obligatoire.** Avec un JDK plus récent, la compilation échoue sur Lombok (`ExceptionInInitializerError ... EndPosTable`) : ce n'est pas un bug du code, relancer avec `JAVA_HOME` pointant sur un JDK 25.
- Les tests Testcontainers (`*IntegrationTest`, test de contexte) sont ignorés sans Docker : le signaler dans le compte rendu.
