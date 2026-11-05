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

### Autorisation et séparation des profils

Règle applicable à toutes les applications.

- L'autorisation combine RBAC (rôles et permissions) et ABAC (attributs de l'utilisateur, de la ressource ou du contexte).
- Un rôle regroupe plusieurs permissions. Un utilisateur peut détenir plusieurs rôles.
- Chaque rôle détenu correspond à un profil. Un utilisateur travaille avec **un seul profil actif à la fois**.
- Les permissions de plusieurs rôles ne se cumulent jamais : seules celles du profil actif s'appliquent, y compris pour l'interface affichée.
- Après la connexion, un utilisateur ayant plusieurs rôles voit ses profils et choisit celui avec lequel il travaille. Avec un seul rôle, ce profil est activé directement.
- Pour agir avec un autre profil, l'utilisateur en change explicitement.
- Le serveur vérifie que le profil demandé appartient à l'utilisateur et applique l'autorisation à partir du profil actif. Le frontend ne fait qu'adapter l'interface.
- Le journal d'audit enregistre le profil actif de chaque action.
- Un utilisateur ne peut jamais valider ce qu'il a lui-même créé, quel que soit le profil utilisé. Le serveur applique ce contrôle sur chaque action de validation.

Exemple : un utilisateur a les rôles d'Ordonnateur délégué et d'Ordonnateur principal. Après sa connexion, il choisit l'un des deux profils et n'accède qu'aux actions de celui-ci, jusqu'à ce qu'il en change.

## Angular
## Git
## Documentation
