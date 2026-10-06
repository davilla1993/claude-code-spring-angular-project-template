# Claude Code - Spring Boot + Angular Project Template

Template d'initialisation pour projets professionnels en monorepo.

## Structure cible
```text
project/
├── CLAUDE.md
├── README.md
├── docs/
│   ├── cahier-des-charges.md   (à ajouter pour chaque projet)
│   ├── AGENTS.md
│   ├── PREPROD.md
│   ├── CONTRIBUTING.md
│   └── ...                     (PRD, ARCHITECTURE, API_CONTRACT, SECURITY...)
├── backend/
│   └── CLAUDE.md
└── frontend/
    └── CLAUDE.md
```

## Démarrage
1. Créer le projet à partir de ce template (socle backend et frontend inclus).
2. Ajouter le cahier des charges dans `docs/cahier-des-charges.md`.
3. Lancer Claude Code à la racine.
4. Demander le bootstrap du projet.

Prompt recommandé :
> Lis intégralement le cahier des charges et les règles du dépôt. Ne code rien. Exécute le bootstrap défini dans CLAUDE.md : analyse, exigences, architecture, documentation et plan d'implémentation. Arrête-toi avant toute implémentation et présente les décisions et questions nécessitant validation.

## Socle fourni
Authentification JWT par cookies (inscription, vérification email, mot de passe oublié, refresh, logout),
gestion des utilisateurs (rôles ADMIN/USER), journal d'audit, stockage de fichiers local, WebSocket STOMP, rate limiting.
Frontend Angular correspondant : écrans d'authentification, administration des utilisateurs, journal d'audit, compte, pages publiques et légales (placeholders).
Détails : `docs/ARCHITECTURE.md`, `docs/API_CONTRACT.md`, `docs/SECURITY.md`.

Lancement local :
Prérequis : PostgreSQL local (base `template_db`) et MailHog (SMTP `localhost:1025`, interface http://localhost:8025).
Les valeurs locales (base, JWT de dev, SMTP, CORS) sont dans `backend/src/main/resources/application-dev.yml` : à adapter à votre poste.
```bash
cd backend && ./mvnw spring-boot:run      # profil "dev" actif par défaut
```

## 🚀 Mise en production
Dérouler **`docs/PREPROD.md`** avant tout déploiement. Point bloquant : définir `SPRING_PROFILES_ACTIVE=prod` sur le serveur
(sinon le profil `dev` par défaut s'applique) et les variables listées dans `.env.example`.
Frontend (autre terminal) :
```bash
cd frontend && npm install && npm start     # http://localhost:4200 — /api proxifié vers le backend
```
Le premier compte ADMIN est à créer manuellement (inscription puis `UPDATE users SET role = 'ADMIN' WHERE email = '...'`).

## ⚠️ CI désactivée dans le template
Le workflow `.github/workflows/ci.yml` est **entièrement commenté** pour ne pas se déclencher à chaque push sur le dépôt du template.
**Sur un nouveau projet, décommentez-le** (instructions en tête du fichier) : build + tests backend (JDK 25, Testcontainers) et frontend (Node 22, Vitest, build).

## Points d'attention
- **Flexibilité du processus** : Le bootstrap et les phases de documentation sont rigoureux pour garantir la qualité. Cependant, pour des modifications mineures ou des corrections triviales, vous pouvez demander à Claude de simplifier le processus pour éviter une bureaucratie excessive.
- **Synchronisation API** : Le point le plus critique est la cohérence entre le contrat API (`docs/API_CONTRACT.md`) et le code implémenté. Veillez à toujours demander à Claude de vérifier que tout changement de code est reflété dans le contrat API avant de valider la tâche.
