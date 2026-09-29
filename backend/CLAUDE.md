# Backend — Spring Boot

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
