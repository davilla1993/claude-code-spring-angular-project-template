# Project Rules

> Source unique du détail des règles. `CLAUDE.md` n'en donne qu'un résumé d'une ligne ; les conventions du socle sont dans `backend/CLAUDE.md` et `frontend/CLAUDE.md`.
> Lire la section concernée par la tâche, pas le fichier entier.

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

## Règles transverses (backend et frontend)

Ces règles valent pour les deux côtés. Une tâche n'est pas terminée si un seul côté a été traité : tout changement d'API, de règle ou de permission est vérifié dans le backend, le frontend et leurs tests.

### Single Responsibility côté frontend

- Un composant, un service, un guard ou un pipe a une seule responsabilité identifiable.
- HTML, CSS et TypeScript sont dans des fichiers séparés.
- Un composant affiche et réagit ; les appels HTTP vont dans `core/api`, l'état partagé dans un service dédié.
- Pas de logique métier dans les templates.

### Zéro confiance envers l'utilisateur

- Toute donnée venant du client est considérée comme hostile : corps, paramètres, en-têtes, cookies, identifiants, fichiers et champs masqués.
- Tout est refusé par défaut : chaque endpoint exige une autorisation explicite, sauf ceux déclarés publics.
- Le serveur vérifie chaque requête : authentification, permission du profil actif, droit sur la ressource visée et validation des entrées.
- Le serveur recalcule ce qu'il peut déduire lui-même : un auteur, un rôle, un statut ou un montant envoyé par le client n'est jamais cru sur parole.
- Le frontend améliore l'expérience mais ne protège rien : guards, masquage et validation Angular ne remplacent jamais le contrôle serveur.
- Les réponses et les erreurs n'exposent que le nécessaire.

### Services externes branchables (ports et adaptateurs)

Un service ou composant externe doit pouvoir être remplacé sans modifier la structure du code. Exemple : stocker les fichiers sur le disque local, puis sur MinIO, puis dans un bucket compatible S3 (Amazon, Backblaze...).

- **Périmètre :** stockage, email, SMS, paiement, API tierces et tout autre fournisseur remplaçable. Les dépendances entre modules de l'application sont normales et ne sont pas concernées, pas plus que le framework ni la base de données.
- **Port :** le code de l'application dépend d'une interface du projet, jamais d'un SDK ni d'un adaptateur concret.
- **Vocabulaire du projet :** le port n'expose aucun type, exception, identifiant ou URL propre à un fournisseur.
- **Adaptateur :** une implémentation par fournisseur, seul endroit où le SDK est importé. Des fournisseurs compatibles partagent le même adaptateur et ne diffèrent que par la configuration.
- **Choix par configuration :** le fournisseur actif se choisit par une propriété (ex. `app.storage.type`), jamais par un `if` dans le code métier. Ses identifiants suivent les règles des secrets (`application-prod.yml`, `.env.example`).
- **Contrôles hors de l'adaptateur :** l'autorisation et la validation communes (type, taille...) ne se perdent pas en changeant de fournisseur.
- **Frontend :** il passe par l'API du projet et ignore quel fournisseur est utilisé. Une bibliothèque tierce côté navigateur reste isolée derrière un service.
- **Changer de fournisseur :** ajouter un adaptateur et changer la configuration suffit, sans toucher au métier, à l'API ni au frontend. La migration des données existantes est une opération à part.
- **Tests :** le métier se teste avec une fausse implémentation du port, et tous les adaptateurs passent les mêmes tests de comportement.
- **Pas d'anticipation :** on crée le port dès la première intégration, avec le seul adaptateur nécessaire. Les autres s'ajoutent le jour où le besoin existe.

### Pagination dès le départ

La pagination se prévoit à la conception, pas quand la table a grossi : l'ajouter après coup casse le contrat API et oblige à reprendre l'écran, les tests et les consommateurs.

- **Toute liste est paginée côté serveur dès sa première version** : endpoint, use case, repository (`Pageable` → `Page`) et écran. Aucun `findAll()` sans `Pageable` derrière un endpoint, même si la table est petite aujourd'hui.
- **Backend :** `@PageableDefault(size, sort)` sur le controller, pour une taille et un tri par défaut stables ; réponse `PageResponse.from(page)`. La taille de page est bornée par le serveur (`spring.data.web.pageable.max-page-size: 50` dans `application.yml`) : une valeur demandée par le client n'est jamais crue sur parole.
- **Tri et filtres :** appliqués par la requête SQL, jamais en mémoire ni dans le navigateur ; colonnes de tri limitées à une liste connue.
- **Frontend :** composant `Pagination`, total affiché, filtres appliqués conservés d'une page à l'autre (`docs/DESIGN_SYSTEM.md`, « Tableaux »). Pas de chargement complet suivi d'un découpage côté client.
- **Contrat :** `docs/API_CONTRACT.md` décrit les paramètres `page`, `size`, `sort` et le tri par défaut de chaque liste.
- **Seule exception :** une liste de référence fermée et courte, définie par le code (rôles, statuts, types). Une liste que les utilisateurs peuvent allonger n'est jamais une exception.
- **Conception et revue :** l'Architecte prévoit la pagination de chaque liste dans son rapport ; une liste non paginée est un constat BLOQUANT du Reviewer.
- **Tests :** chaque liste a un test qui vérifie la pagination (page demandée, total) et la conservation des filtres.

### Performance et rapidité

- **Backend :** pas de N+1, index justifiés, requêtes limitées aux données utiles.
- **Frontend :** lazy loading, `OnPush` et signals lorsque c'est possible, `track` sur les listes, pas d'appel API dupliqué, bundle surveillé.
- Mesurer avant d'optimiser : pas de cache ni d'abstraction sans besoin constaté.

### Accessibilité

- HTML sémantique, navigation complète au clavier, focus visible et géré, noms accessibles, contraste suffisant.
- Erreurs de formulaire reliées à leur champ, responsive, états de chargement, d'erreur et vides présents.

## Validation

Chaque règle est appliquée côté serveur et reprise côté frontend pour un retour immédiat. Toute évolution se fait des deux côtés.

- **Prénom et nom :** 2 à 25 caractères, espaces de début et de fin non comptés (`PersonName`).
- **Mot de passe :** 8 à 25 caractères, avec au moins une majuscule, une minuscule, un chiffre et un caractère spécial (`PasswordPolicy`).

## Testing

- Exécuter uniquement les tests de la fonctionnalité touchée (commandes dans `CLAUDE.md`, « Tests »).
- La suite complète ne se lance qu'avec l'accord explicite de l'utilisateur ; la CI la joue à chaque push.
- Chaque règle d'autorisation a un test du cas autorisé et un test du cas refusé, côté serveur.

## Security

### Autorisation et séparation des profils

Règle applicable à toutes les applications.

- L'autorisation combine RBAC (rôles et permissions) et ABAC (attributs de l'utilisateur, de la ressource ou du contexte).
- Un rôle regroupe plusieurs permissions. Un utilisateur peut détenir plusieurs rôles.
- Chaque rôle détenu correspond à un profil. Un utilisateur travaille avec **un seul profil actif à la fois**.
- Les permissions de plusieurs rôles ne se cumulent jamais : seules celles du profil actif s'appliquent, y compris pour l'interface affichée.
- Après la connexion, un utilisateur ayant plusieurs rôles voit ses profils et choisit celui avec lequel il travaille. Avec un seul rôle, ce profil est activé directement.
- Pour agir avec un autre profil, l'utilisateur en change explicitement.
- Un utilisateur n'a qu'**une seule session ouverte**. Une nouvelle connexion ferme la précédente, avec effet immédiat.
- Le serveur vérifie que le profil demandé appartient à l'utilisateur et applique l'autorisation à partir du profil actif. Le frontend ne fait qu'adapter l'interface.
- Le journal d'audit enregistre le profil actif de chaque action.
- Un utilisateur ne peut jamais valider ce qu'il a lui-même créé, quel que soit le profil utilisé. Le serveur applique ce contrôle sur chaque action de validation.

Exemple : un utilisateur a les rôles d'Ordonnateur délégué et d'Ordonnateur principal. Après sa connexion, il choisit l'un des deux profils et n'accède qu'aux actions de celui-ci, jusqu'à ce qu'il en change.

### Convention ABAC

Le RBAC répond à « ce profil peut-il faire cette action ? », l'ABAC à « sur cette ressource, dans ce contexte ? ». Pas de moteur de règles : ce contrôle reste du code simple.

- **RBAC sur l'endpoint :** `@PreAuthorize("hasAuthority('<permission>')")`.
- **ABAC dans le use case :** après le chargement de la ressource et avant toute modification, comparer les attributs de l'utilisateur, de la ressource et du contexte.
- **Refus :** lever `ForbiddenOperationException`, qui renvoie 403. Si l'existence même de la ressource est sensible, renvoyer 404.
- **Frontend :** il peut masquer une action, mais le contrôle reste côté serveur.
- **Tests :** chaque règle a un test du cas autorisé et un test du cas refusé.

Exemple réel du socle : `UpdateUserUseCase` refuse de modifier ses propres rôles.

Exemple de la règle anti-auto-validation, à placer dans chaque action de validation d'une application :

```java
if (actorId.equals(document.getCreatedByUserId())) {
    throw new ForbiddenOperationException(ErrorMessages.CANNOT_VALIDATE_OWN_WORK);
}
```

Stocker sur la ressource le `publicId` de son auteur : `BaseEntity.createdBy` contient un email ou `SYSTEM`, et ne convient pas à cette comparaison. Le message d'erreur est à ajouter à `ErrorMessages`.

## Conventions à compléter au bootstrap

À ajouter ici, uniquement si le projet en a besoin et sans répéter ce qui précède : nommage, structure des modules, conventions d'API, gestion des erreurs, logs, base de données, tests, Angular, Git.
