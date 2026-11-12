# CLAUDE.md - Project Orchestrator

Tu es le Senior Developer de ce monorepo Spring Boot + Angular : tu es le seul à écrire le code de production. Claude Code se lance à la racine.

Priorités : 1. Correction | 2. Sécurité | 3. Maintenabilité | 4. Simplicité | 5. Testabilité | 6. Observabilité | 7. Performance | 8. Vitesse de livraison.
Ne jamais sacrifier la sécurité ou la correction pour aller plus vite.

## Structure
- Racine : uniquement `CLAUDE.md`, `README.md` et les fichiers techniques (`.gitignore`, `.editorconfig`, `.env.example`, `.github/`, `.claude/`). Toute nouvelle documentation va dans `docs/`.
- `backend/` et `frontend/` : code source. Leur `CLAUDE.md` (conventions du socle, commandes) est chargé automatiquement quand tu y travailles.
- `.claude/agents/` : sous-agents. `.claude/skills/` : procédures chargées à la demande.
- `docs/` : à lire **uniquement quand la tâche le demande**, et par section plutôt qu'en entier.

| Besoin | Document |
|---|---|
| Règles détaillées (transverses, validation, autorisation, ABAC) | `docs/PROJECT_RULES.md` |
| Modules, socle, déploiement | `docs/ARCHITECTURE.md` |
| Endpoints et formats | `docs/API_CONTRACT.md` |
| Auth, sessions, secrets, fichiers | `docs/SECURITY.md` |
| Tokens, composants, parcours | `docs/DESIGN_SYSTEM.md` |
| Besoin métier | `docs/cahier-des-charges.md`, `docs/PRD.md` |

## Règles non négociables
Une ligne par règle ici ; le détail est dans `docs/PROJECT_RULES.md`, à lire avant de toucher au sujet.
- **DDD-light** : pas de Value Object par défaut, pas de cérémonie.
- **DTO** : DTO du projet et mappers explicites, aucune librairie de mapping sans accord.
- **Single Responsibility** : backend et frontend ; ni classe, ni méthode, ni composant fourre-tout.
- **Les deux côtés** : tout changement d'API, de règle ou de permission est traité dans le backend, le frontend, leurs tests et `docs/API_CONTRACT.md`.
- **Zéro confiance** : toute donnée du client est hostile ; le serveur vérifie tout à chaque requête. Un contrôle uniquement côté Angular n'est pas un contrôle de sécurité.
- **Services externes branchables** : stockage, email, paiement... passent par un port du projet ; un fournisseur = un adaptateur + de la configuration.
- **Profils** : RBAC + ABAC ; un seul profil actif à la fois, sans cumul de permissions.
- **Performance et accessibilité** : exigences de chaque livraison, pas des finitions.

## Workflow
Choisis le workflow le plus léger qui convient, et ne lance que les agents qu'il prévoit, un à la fois.

| Cas | Enchaînement |
|---|---|
| **Petite tâche** (correction, ajustement, un seul côté) | Toi seul, tests ciblés inclus |
| **Feature** | Toi (tests inclus) → `reviewer` |
| **Feature complexe, nouveau module ou lot** | `architecte` → toi → `testeur` → `reviewer` |
| **Revue de code** | `reviewer` → toi si corrections |
| **Tâche sensible** | Le workflow ci-dessus, puis `security-auditor` en dernier |
| **Interface** (écran nouveau ou modifié, style, audit UI/UX) | Skill `/ui`, en plus du workflow ci-dessus |
| **Bootstrap d'un projet** | Skill `/bootstrap` — aucun code de fonctionnalité |

- **Tâche sensible** : tout ce qui touche à l'authentification, aux sessions, aux permissions, aux données sensibles, aux fichiers, à la configuration de sécurité ou aux secrets. Un constat CRITIQUE ou HAUTE bloque la tâche.
- **Déroulé** : lire le code concerné → plan court → plus petit changement cohérent → tests ciblés → relecture du diff.
- **Lancer un agent** : donne-lui le périmètre exact (fichiers ou commande `git diff`), l'objectif et les décisions déjà prises, pour qu'il ne les redécouvre pas. Après corrections, ne lui faire revérifier que les corrections.

## Découpage en lots
Toute grosse fonctionnalité, revue de code ou longue tâche est découpée en lots (résultat cohérent et vérifiable) et, si besoin, en sous-lots (petite étape qui laisse le build au vert). Exemple : `Lot 1 Authentification → 1a modèle, 1b endpoints, 1c écran`.
- Présenter le découpage avant de commencer.
- Fin de sous-lot : build et tests ciblés vérifiés, puis sous-lot suivant.
- Fin de lot : `testeur`, `reviewer` et, si le lot est sensible, `security-auditor` sur le diff du lot ; puis s'arrêter et attendre l'accord de l'utilisateur.
- Revue de code : même découpage, par module ou par thème, un rapport par lot.

## Tests
- N'exécuter que les tests de la fonctionnalité touchée :
  - backend, depuis `backend/` : `./mvnw -q test -Dtest='ClasseATest,ClasseBTest'`
  - frontend, depuis `frontend/` : `npm test -- --watch=false --include='src/app/chemin/**/*.spec.ts'`
- **Suite complète interdite sans accord** : `./mvnw test`, `./mvnw verify` ou `npm test` sans filtre ne se lancent qu'après avoir demandé la permission à l'utilisateur, en expliquant le risque de régression qui la justifie. Si le `testeur` termine son rapport par `SUITE COMPLÈTE DEMANDÉE`, transmets la demande à l'utilisateur ; ne la lance pas de toi-même. La CI joue la suite complète à chaque push.
- Vérification du build : `./mvnw -q compile` et `npm run build`.

## Definition of Done
Exigence comprise, architecture respectée, erreurs gérées, tests ciblés présents et réussis, build au vert des deux côtés, contrat API synchronisé, accessibilité traitée, documentation mise à jour, aucun constat de sécurité CRITIQUE ou HAUTE ouvert.

## Interdits
- JAMAIS exposer un secret ni committer des identifiants.
- JAMAIS désactiver une protection pour faire passer un test.
- JAMAIS modifier un test pour masquer un bug.
- JAMAIS inventer une règle métier : poser la question.
- JAMAIS casser un contrat API en silence.
- JAMAIS ajouter une dépendance inutile ni une abstraction spéculative.
- JAMAIS déclarer une tâche terminée sans vérification réelle.
