# Claude Code - Spring Boot + Angular Project Template

Template d'initialisation pour projets professionnels en monorepo : un socle Spring Boot + Angular prêt à l'emploi, et une configuration Claude Code calibrée pour travailler vite avec peu de tokens.

## Structure
```text
project/
├── CLAUDE.md                   (priorités, règles non négociables, workflows, tests)
├── README.md
├── .claude/
│   ├── agents/                 (architecte, testeur, reviewer, security-auditor)
│   ├── skills/                 (bootstrap, ui)
│   └── settings.json           (permissions partagées)
├── docs/
│   ├── cahier-des-charges.md   (à ajouter pour chaque projet)
│   ├── PROJECT_RULES.md        (détail des règles)
│   ├── DESIGN_SYSTEM.md        (règles d'interface)
│   ├── API_CONTRACT.md         (source de vérité de l'API)
│   ├── ARCHITECTURE.md, SECURITY.md, PRD.md, IMPLEMENTATION_PLAN.md
│   ├── DEVELOPMENT_WORKFLOW.md (procédure de bootstrap)
│   ├── PREPROD.md, CONTRIBUTING.md, adr/
│   └── scripts/                (scripts SQL : premier admin, utilisateur de test)
├── backend/
│   └── CLAUDE.md               (conventions du socle Spring Boot, commandes)
└── frontend/
    └── CLAUDE.md               (conventions du socle Angular, commandes)
```

## Démarrer un projet
1. Créer le projet à partir de ce template (socle backend et frontend inclus).
2. Ajouter le cahier des charges dans `docs/cahier-des-charges.md`.
3. Décommenter la CI (voir plus bas).
4. Lancer Claude Code à la racine, puis la commande `/bootstrap`.

Le bootstrap (`docs/DEVELOPMENT_WORKFLOW.md`) analyse le cahier des charges, écrit la documentation et le plan d'implémentation en lots, puis s'arrête avant tout code pour faire valider ses décisions et ses questions.

## Travailler avec Claude Code

### Workflows
`CLAUDE.md` choisit le workflow le plus léger qui convient. La session principale joue le Senior Developer : elle est seule à écrire le code de production.

| Cas | Enchaînement |
|---|---|
| Petite tâche | Session principale seule, tests ciblés inclus |
| Feature | Session principale (tests inclus) → `reviewer` |
| Feature complexe, nouveau module ou lot | `architecte` → session principale → `testeur` → `reviewer` |
| Tâche sensible (authentification, permissions, fichiers, secrets...) | Le workflow ci-dessus, puis `security-auditor` |
| Interface (écran, style, audit UI/UX) | Skill `/ui` |
| Nouveau projet | Skill `/bootstrap` |

Les grosses tâches sont découpées en lots, avec un arrêt pour validation à la fin de chaque lot.

### Sous-agents (`.claude/agents/`)
| Agent | Modèle | Rôle |
|---|---|---|
| `architecte` | opus | Exigences, architecture, API, schéma, UX — rapport seulement |
| `testeur` | sonnet | Tests complémentaires d'un lot ou d'une feature complexe — n'écrit que des tests |
| `reviewer` | opus | Qualité, cohérence visuelle, accessibilité, performance, pagination — rapport seulement |
| `security-auditor` | opus | Audit de sécurité d'un diff ou d'un module — rapport seulement |

Chaque agent a un périmètre, un budget de lecture et un nombre de tours limités, et rend un rapport court. Un seul agent à la fois.

### Skills (`.claude/skills/`)
- **`/bootstrap`** : initialisation d'un projet, sans code.
- **`/ui`** : création, modification ou audit d'une interface selon `docs/DESIGN_SYSTEM.md`, avec vérification visuelle obligatoire à 1280, 768 et 375 px. Sans outil de navigateur, le script `.claude/skills/ui/capture.mjs` pilote Chrome en mode headless (Node 22+ et Google Chrome requis).

### Tests
- Seuls les tests de la fonctionnalité touchée sont exécutés :
  ```bash
  cd backend  && ./mvnw -q test -Dtest='ClasseATest,ClasseBTest'
  cd frontend && npm test -- --watch=false --include='src/app/chemin/**/*.spec.ts'
  ```
- La suite complète (`./mvnw test`, `./mvnw verify`, `npm test` sans filtre) n'est lancée qu'avec votre accord. Une fois activée, la CI la joue à chaque push.
- `.claude/settings.json` applique cette règle : les tests ciblés, la compilation, le build et git en lecture passent sans demande ; la suite complète déclenche une confirmation ; la lecture des fichiers `.env` est refusée.

### Règles du template
Résumées dans `CLAUDE.md`, détaillées dans `docs/PROJECT_RULES.md`. Chaque projet y ajoute les siennes.
- DDD-light, DTO et mappers explicites, Single Responsibility côté backend et frontend.
- Zéro confiance : le serveur vérifie tout à chaque requête.
- Tout changement d'API est traité des deux côtés et reporté dans `docs/API_CONTRACT.md`.
- Services externes (stockage, email...) derrière un port du projet.
- Pagination dès le départ : toute liste est paginée côté serveur dès sa première version ; taille de page plafonnée à 50.
- RBAC + ABAC, un seul profil actif à la fois.
- Interface : une seule façon de faire chaque chose, aucune valeur en dur hors des tokens de `frontend/src/styles.scss`, toute opération d'écriture confirmée.

## Socle fourni
- **Backend** (Spring Boot 4, Java 25, PostgreSQL) : authentification JWT par cookies (inscription, vérification email, mot de passe oublié, refresh, logout), utilisateurs à plusieurs rôles avec profil actif et session unique, journal d'audit, stockage de fichiers et email derrière des ports, WebSocket STOMP, rate limiting.
- **Frontend** (Angular 22, SCSS maison) : écrans d'authentification et de choix du profil, administration des utilisateurs, journal d'audit, compte, pages publiques et légales (à compléter), design system sobre à re-thémer par les tokens.

Détails : `docs/ARCHITECTURE.md`, `docs/API_CONTRACT.md`, `docs/SECURITY.md`, `docs/DESIGN_SYSTEM.md`.

## Lancement local
Prérequis :
- **JDK 25** : avec un JDK plus récent, la compilation échoue sur Lombok.
- PostgreSQL local avec une base `template_db` vide : le schéma est créé au démarrage. Après une évolution du modèle, Hibernate ne supprime pas les anciennes colonnes : en développement, recréer la base si un script ou une insertion échoue.
- MailHog pour les emails (SMTP `localhost:1025`, interface http://localhost:8025).
- Docker pour les tests d'intégration (Testcontainers) ; sans lui, ils sont ignorés.

Les valeurs locales (base, JWT de dev, SMTP, CORS) sont dans `backend/src/main/resources/application-dev.yml` : à adapter à votre poste.

```bash
cd backend && ./mvnw spring-boot:run        # profil "dev" actif par défaut
cd frontend && npm install && npm start     # autre terminal — http://localhost:4200, /api proxifié vers le backend
```

Premier compte : s'inscrire puis exécuter `docs/scripts/create-admin.sql` pour lui donner le rôle ADMIN. Pour tester le choix du profil, `docs/scripts/create-user-two-roles.sql` crée directement un utilisateur avec deux rôles.

## 🚀 Mise en production
Dérouler **`docs/PREPROD.md`** avant tout déploiement. Point bloquant : définir `SPRING_PROFILES_ACTIVE=prod` sur le serveur (sinon le profil `dev` par défaut s'applique) et les variables listées dans `.env.example`.

## ⚠️ CI désactivée dans le template
Le workflow `.github/workflows/ci.yml` est **entièrement commenté** pour ne pas se déclencher à chaque push sur le dépôt du template.
**Sur un nouveau projet, décommentez-le** (instructions en tête du fichier) : build + tests backend (JDK 25, Testcontainers) et frontend (Node 22, Vitest, build). C'est elle qui joue la suite complète des tests.

## Points d'attention
- **Processus proportionné** : pour une modification mineure, Claude travaille seul ; les agents n'interviennent que si le workflow de la tâche le prévoit.
- **Synchronisation API** : le point le plus critique reste la cohérence entre `docs/API_CONTRACT.md` et le code. Tout changement d'endpoint, de DTO ou de code d'audit y est reporté dans la même tâche.
