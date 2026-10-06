# Frontend - Angular

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

Socle existant (à réutiliser, ne pas dupliquer) :
- `core/api/*` : services typés par ressource (`AuthApi`, `UsersApi`, `AuditApi`), enveloppe `ApiResponse` dépliée par `unwrap()`.
- `core/services/session-store` : utilisateur courant (signals) ; `hasPermission()` pour adapter l'UI uniquement.
- `core/interceptors` : cookies + refresh automatique sur 401 (un seul refresh à la fois, `TokenRefresher`).
- `core/guards` : `authGuard`, `guestGuard`, `firstLoginGuard`, `permissionGuard('code')`.
- `shared` : `ToastStore`, pipes `fieldError` / `confirmFieldError`, `Pagination`, modèles.
- Confirmations : `await inject(ConfirmDialogService).confirm({ title, message, confirmLabel, danger })` (modale `<dialog>` native) — ne jamais utiliser `window.confirm`.
- Styles : tokens et classes de base dans `src/styles.scss` (`.btn`, `.field`, `.card`, `.table`, `.alert`...).
- Commandes : `npm start` (proxy `/api` → `localhost:8080`), `npm test -- --watch=false`, `npm run build`.

Performance :
- éviter subscriptions inutiles
- state global inutile
- rendu excessif
- bundles surdimensionnés
- appels API dupliqués
