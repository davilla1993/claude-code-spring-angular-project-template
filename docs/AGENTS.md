# Engineering Agents

Équipe réduite pour limiter la consommation de tokens. Les consignes de chaque sous-agent sont dans `.claude/agents/`.

| Agent | Fichier | Périmètre | Droits |
|---|---|---|---|
| **Senior Developer** | session principale | Back, front, base de données, DevOps | Seul à écrire le code de production |
| **Architecte** | `architecte.md` | Exigences, architecture, UX, schéma de base de données | Rapport seulement |
| **Testeur** | `testeur.md` | Tests, régressions, cas limites | Écrit uniquement les tests ; signale les bugs sans les corriger |
| **Reviewer** | `reviewer.md` | Qualité du code, accessibilité, performance | Rapport seulement |
| **Security Auditor** | `security-auditor.md` | JWT, sessions, cookies, CSRF, CORS, XSS, injection, IDOR, secrets, OWASP | Rapport seulement |

## Exigences communes
Tous les agents travaillent sur le backend **et** le frontend, et appliquent les « Règles transverses » de `PROJECT_RULES.md` : Single Responsibility, zéro confiance, services externes branchables, performance et accessibilité.

## Senior Developer
Implémente et corrige le code : Spring Boot, Angular, migrations, configuration, CI/CD. Applique les règles de `backend/CLAUDE.md` et `frontend/CLAUDE.md`. Vérifie le build des deux côtés avant de passer la main. Traite les bugs du Testeur et les findings du Reviewer et du Security Auditor.

# Workflows

| Cas | Enchaînement |
|---|---|
| **Petite tâche** | Senior Developer seul (tests inclus) |
| **Feature** | Senior Developer → Testeur → Reviewer |
| **Feature complexe ou nouveau module** | Architecte → Senior Developer → Testeur → Reviewer |
| **Revue de code** | Reviewer → Senior Developer si corrections |
| **Audit de sécurité** | Security Auditor → Senior Developer si corrections → Security Auditor sur les corrections |
| **Bootstrap** | Architecte, sans code (voir `DEVELOPMENT_WORKFLOW.md`) |

## Tâches sensibles
Le Security Auditor est obligatoire, même pour une petite tâche, dès qu'elle touche à l'authentification, aux sessions, aux permissions, aux données sensibles, aux fichiers, à la configuration de sécurité ou aux secrets. Il intervient en fin de workflow, et un constat CRITIQUE ou HAUTE bloque la tâche.

Il est aussi lancé avant une mise en production (`PREPROD.md`).

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
- **Lot terminé :** le Testeur, le Reviewer et le Security Auditor interviennent sur le diff du lot, si le workflow le prévoit.
- **Validation :** s'arrêter à la fin de chaque lot et attendre l'accord de l'utilisateur.
- **Revue de code :** même découpage, par module ou par thème ; un rapport par lot.

# Limiter les tokens

- La session principale joue le Senior Developer ; les autres agents ne sont lancés que si le workflow le prévoit.
- Un agent à la fois, jamais en parallèle.
- Rapports courts : uniquement les problèmes trouvés, avec leur preuve.
- Après corrections, ne revérifier que les corrections.
