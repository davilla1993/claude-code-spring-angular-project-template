# Engineering Agents

Équipe réduite pour limiter la consommation de tokens. Les consignes de chaque sous-agent sont dans `.claude/agents/` ; le choix du workflow est dans `CLAUDE.md` (« Workflow »), chargé à chaque session.

| Agent | Fichier | Modèle | Périmètre | Droits |
|---|---|---|---|---|
| **Senior Developer** | session principale | celui de la session | Back, front, base de données, DevOps, tests des features | Seul à écrire le code de production |
| **Architecte** | `architecte.md` | opus | Exigences, architecture, UX, schéma de données | Rapport seulement |
| **Testeur** | `testeur.md` | sonnet | Tests complémentaires d'un lot ou d'une feature complexe | Écrit uniquement les tests ; signale les bugs sans les corriger |
| **Reviewer** | `reviewer.md` | opus | Qualité du code, accessibilité, performance | Rapport seulement |
| **Security Auditor** | `security-auditor.md` | opus | JWT, sessions, cookies, CSRF, CORS, XSS, injection, IDOR, secrets, OWASP | Rapport seulement |

Chaque agent a un budget de lecture et un nombre de tours limité : s'il l'atteint, il rend son rapport en listant ce qu'il n'a pas examiné.

## Senior Developer
Implémente et corrige le code : Spring Boot, Angular, migrations, configuration, CI/CD. Écrit les tests de ce qu'il implémente. Vérifie le build des deux côtés avant de passer la main. Traite les bugs du Testeur et les constats du Reviewer et du Security Auditor.

## Tests
Seuls les tests de la fonctionnalité touchée sont exécutés. La suite complète ne se lance qu'avec l'accord de l'utilisateur (`CLAUDE.md`, « Tests »). Le Testeur ne la lance jamais : il la demande dans son rapport.

## Security Auditor
Obligatoire en fin de workflow pour toute tâche sensible (`CLAUDE.md`), même petite, et avant une mise en production (`PREPROD.md`). Un constat CRITIQUE ou HAUTE bloque la tâche.

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
- **Sous-lot terminé :** le Senior Developer vérifie le build et les tests ciblés, puis passe au suivant.
- **Lot terminé :** le Testeur, le Reviewer et, si le lot est sensible, le Security Auditor interviennent sur le diff du lot.
- **Validation :** s'arrêter à la fin de chaque lot et attendre l'accord de l'utilisateur.
- **Revue de code :** même découpage, par module ou par thème ; un rapport par lot.

# Limiter les tokens

- Un agent à la fois, jamais en parallèle, et seulement si le workflow le prévoit.
- Donner à l'agent son périmètre exact et les décisions déjà prises.
- Rapports courts : uniquement les problèmes trouvés, avec leur preuve.
- Après corrections, ne revérifier que les corrections.
