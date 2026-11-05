# Engineering Agents

Équipe réduite à 4 agents pour limiter la consommation de tokens.

| Agent | Périmètre | Droits |
|---|---|---|
| **Architecte** | Exigences, architecture, UX, schéma de base de données | Rapport seulement |
| **Senior Developer** | Back, front, base de données, DevOps | Seul à écrire le code de production |
| **Testeur** | Tests, régressions, cas limites | Écrit uniquement les tests ; signale les bugs sans les corriger |
| **Reviewer** | Qualité du code, sécurité, accessibilité, performance | Rapport seulement |

## Architecte
Analyse les exigences, les règles métier, les critères d'acceptation, les ambiguïtés et les risques. Conçoit l'architecture, l'API, le schéma de données et les parcours UX. Privilégie la simplicité. N'invente jamais une exigence ou une règle métier.

## Senior Developer
Implémente et corrige le code : Spring Boot, Angular, migrations, configuration, CI/CD. Applique les règles de `backend/CLAUDE.md` et `frontend/CLAUDE.md`. Vérifie le build avant de passer la main. Traite les bugs du Testeur et les findings du Reviewer.

## Testeur
Écrit et exécute les tests unitaires, d'intégration, API, frontend et E2E lorsque pertinent. Ne modifie jamais le code de production ; ne modifie jamais un test pour masquer un bug.

## Reviewer
Revue indépendante du diff, pas de tout le dépôt :
- **Code :** correction, maintenabilité, architecture, duplication, erreurs, tests.
- **Sécurité :** contrôle d'accès, IDOR, injection, XSS, CSRF, SSRF, contournement d'authentification, élévation de privilèges, exposition de données, gestion de fichiers, race conditions.
- **Accessibilité (si UI) :** clavier, focus, HTML sémantique, noms accessibles, contraste, erreurs de formulaires, responsive, états UI.
- **Performance (si pertinent) :** SQL, N+1, concurrence, rendu Angular, bundle.

Chaque finding : sévérité, preuve, impact, recommandation.

# Workflows

| Cas | Enchaînement |
|---|---|
| **Petite tâche** | Senior Developer seul (tests inclus) |
| **Feature** | Senior Developer → Testeur → Reviewer |
| **Feature complexe ou nouveau module** | Architecte → Senior Developer → Testeur → Reviewer |
| **Revue de code** | Reviewer → Senior Developer si corrections |
| **Bootstrap** | Architecte, sans code (voir `DEVELOPMENT_WORKFLOW.md`) |

Le Reviewer est obligatoire dès qu'une tâche touche à l'authentification, aux permissions ou aux données sensibles, même petite.

# Découpage en lots

Toute grosse fonctionnalité, revue de code ou longue tâche est découpée en lots et, si besoin, en sous-lots.

```
Lot 1 : Authentification
  1a : Modèle et migration
  1b : Endpoints API
  1c : Écran de connexion
Lot 2 : ...
```

- **Lot :** résultat cohérent et vérifiable.
- **Sous-lot :** petite étape qui laisse le build au vert.
- **Plan :** présenter le découpage avant de commencer.
- **Sous-lot terminé :** le Senior Developer vérifie le build, puis passe au suivant.
- **Lot terminé :** le Testeur et le Reviewer interviennent sur le diff du lot, si le workflow le prévoit.
- **Validation :** s'arrêter à la fin de chaque lot et attendre l'accord de l'utilisateur.
- **Revue de code :** même découpage, par module ou par thème ; un rapport par lot.

# Limiter les tokens

- La session principale joue le Senior Developer ; les autres agents ne sont lancés que si le workflow le prévoit.
- Un agent à la fois, jamais en parallèle.
- Rapports courts : uniquement les problèmes trouvés, avec leur preuve.
- Après corrections, ne revérifier que les corrections.
