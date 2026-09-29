# Project Rules

> Règles spécifiques au projet. Les règles globales sont dans CLAUDE.md.

## Architecture et conventions Backend

### DDD-light

Le backend suit une approche DDD-light.

- Ne pas implémenter un DDD complexe ou cérémoniel.
- Pas de Value Objects (VO) par défaut.
- Ne créer un VO que si une exigence métier explicite le justifie.
- Privilégier des entités, services métier, repositories et DTOs simples.
- Éviter les abstractions et couches qui n'apportent pas de valeur métier réelle.

### DTOs & Mapping

- Ne pas utiliser de librairie externe pour générer ou gérer les DTOs.
- Créer explicitement les DTOs du projet.
- Créer explicitement les mappers entre Entity/Domain et DTO.
- Les DTOs ne doivent pas contenir de logique métier.
- Les mappers doivent rester simples, explicites et testables.
- Dans chaque package mapper, créer toujours deux sous packages: request et response

### Single Responsibility Principle

- Une classe ou méthode doit avoir une responsabilité clairement identifiable.
- Ne pas créer de méthodes fourre-tout.
- Une méthode ne doit pas orchestrer plusieurs responsabilités indépendantes.
- Extraire une responsabilité lorsqu'une méthode devient trop complexe ou réalise plusieurs opérations conceptuellement distinctes.
- Préférer plusieurs méthodes simples et cohérentes à une méthode longue et polyvalente.
- Éviter les abstractions prématurées.

## Naming
## Package/module structure
## API conventions
## Validation
## Error handling
## Logging
## Database
## Testing
## Security
## Angular
## Git
## Documentation
