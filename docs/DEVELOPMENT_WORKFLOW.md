# Bootstrap d'un nouveau projet

Procédure d'initialisation d'un projet créé à partir du template. Elle se lance par le skill `/bootstrap`.
Le workflow des tâches courantes (features, revues, audits) est dans `CLAUDE.md`.

**Aucun code pendant le bootstrap :** ni logique métier, ni endpoint, ni entité, ni composant, ni refactoring. Seuls des fichiers de `docs/` sont écrits.

## Phase 1 — Découverte (session principale)
- Lire `docs/cahier-des-charges.md` en entier. S'il est absent ou vide, s'arrêter et le demander.
- Lire `docs/ARCHITECTURE.md`, `docs/API_CONTRACT.md`, `docs/SECURITY.md` et `docs/DESIGN_SYSTEM.md` : les sections **[socle]** décrivent ce que le template fournit déjà. Ne pas relire le code du socle pour le redécouvrir ; ne vérifier dans le code que ce que le cahier des charges remet en cause.
- Relever les versions dans `backend/pom.xml` et `frontend/package.json`.

## Phase 2 — Conception (un seul lancement de l'`architecte`)
Lui demander, en une fois, un rapport couvrant :
- **Exigences :** acteurs, cas d'utilisation, exigences fonctionnelles et non fonctionnelles, règles métier, critères d'acceptation, cas limites, hypothèses, risques, questions ouvertes.
- **Architecture :** modules à créer, ce qui est repris du socle, schéma de données, intégrations (un port par service externe), rôles et permissions, règles ABAC, déploiement.
- **API :** endpoints à ajouter, avec leur permission ; toute liste paginée, avec son tri par défaut et ses filtres.
- **UX :** parcours, écrans, états, composants à réutiliser ou à créer.
- **Plan :** découpage en lots et sous-lots, dans l'ordre des dépendances.

Aucune exigence ni règle métier n'est inventée : tout manque devient une question ouverte.

## Phase 3 — Documentation (session principale)
Écrire à partir du rapport, en conservant et en complétant les sections **[socle]** sans les réécrire :

| Document | Contenu |
|---|---|
| `docs/PRD.md` | Exigences, règles métier, critères d'acceptation, hypothèses, questions ouvertes |
| `docs/ARCHITECTURE.md` | Vue d'ensemble, modules du projet, base de données, intégrations, déploiement |
| `docs/adr/ADR-NNN-titre.md` | Une décision structurante par fichier |
| `docs/API_CONTRACT.md` | Endpoints prévus ; ce document est la source de vérité de l'API |
| `docs/SECURITY.md` | Modèle de menace, rôles et permissions, données sensibles, hypothèses |
| `docs/DESIGN_SYSTEM.md` | Sections **[projet]** : charte (tokens de `styles.scss`), parcours, architecture de l'information |
| `docs/PROJECT_RULES.md` | « Conventions à compléter au bootstrap », uniquement ce dont le projet a besoin |
| `docs/IMPLEMENTATION_PLAN.md` | Lots et sous-lots : objectif, backend, frontend, base, API, sécurité, tests, critères d'acceptation |

N'écrire que ce que le projet apporte : pas de section générique, pas de répétition d'un autre document.

## Phase 4 — Revue et arrêt
- Vérifier la cohérence : exigences ↔ architecture, API ↔ écrans, permissions ↔ parcours, hypothèses de sécurité.
- Rappeler l'action manuelle du nouveau projet : activer la CI (`.github/workflows/ci.yml`, voir `README.md`).
- Présenter : décisions prises, documents écrits, hypothèses, questions ouvertes, plan d'implémentation.

**S'arrêter. Aucun code avant la validation explicite de l'utilisateur.**
