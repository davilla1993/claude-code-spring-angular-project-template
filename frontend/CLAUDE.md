# Frontend — Angular

Les règles de la racine s'appliquent toujours.

Respecter la version Angular réellement présente.

Privilégier :
- composants ciblés
- services typés
- frontières API claires
- états loading/error/empty
- réutilisation réelle

Workflow UI important :
UI/UX Designer → Frontend Engineer → Accessibility & Design QA

Avant toute nouvelle UI :
- inspecter les composants existants
- inspecter docs/DESIGN_SYSTEM.md
- réutiliser les patterns existants

API :
- synchroniser modèles/types/services/consommateurs/tests
- ne pas dupliquer les règles métier backend

Sécurité :
- ne jamais faire confiance à l'autorisation côté client
- aucun secret dans le frontend
- respecter le modèle de session/token
- éviter le HTML non sûr

Accessibilité :
- HTML sémantique
- labels
- clavier
- focus
- messages d'erreur accessibles
- responsive
- pas de composants fourre-tout: codes html, css, typescript bien séparés dans différents fichiers

Performance :
- éviter subscriptions inutiles
- state global inutile
- rendu excessif
- bundles surdimensionnés
- appels API dupliqués
