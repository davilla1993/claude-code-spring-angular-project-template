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
Architecte (UX) → Senior Developer → Reviewer (accessibilité)

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

Confirmation des actions :
- Toute action de l'utilisateur est confirmée avant son exécution : création, modification, suppression, changement de mot de passe, déconnexion, etc.
- Sans confirmation : les actions qui ne changent rien (navigation, filtres, pagination, copie, affichage ou masquage) et les formulaires du parcours d'authentification avant l'ouverture de la session (connexion, inscription, vérification de l'email, mot de passe oublié).
- Un seul composant dans toute l'application : `ConfirmDialogService`. Le personnaliser par ses options (`title`, `message`, `confirmLabel`, `cancelLabel`, `danger`), et l'enrichir si une option manque.
- Interdit : créer une autre modale de confirmation ou utiliser `window.confirm`.
- Le message indique précisément l'action et ses conséquences. `danger: true` pour une action destructrice ou difficile à annuler.
- La confirmation est une aide à l'utilisateur, pas un contrôle de sécurité : le serveur vérifie toujours la requête.

Socle existant (à réutiliser, ne pas dupliquer) :
- `core/api/*` : services typés par ressource (`AuthApi`, `UsersApi`, `AuditApi`), enveloppe `ApiResponse` dépliée par `unwrap()`.
- `core/services/session-store` : utilisateur courant (signals) ; `hasPermission()` pour adapter l'UI uniquement.
- `core/interceptors` : cookies + refresh automatique sur 401 (un seul refresh à la fois, `TokenRefresher`).
- `core/guards` : `authGuard` (exige aussi un profil actif), `guestGuard`, `firstLoginGuard`, `profileSelectionGuard`, `permissionGuard('code')`.
- Profils : `/auth/select-profile` choisit ou change le profil ; `SessionSync` recharge les autres onglets après un changement.
- `shared` : `ToastStore`, pipes `fieldError` / `confirmFieldError`, `Pagination`, modèles.
- Confirmations : `await inject(ConfirmDialogService).confirm({ title, message, confirmLabel, cancelLabel, danger })` (modale `<dialog>` native), voir « Confirmation des actions ».
- Styles : tokens et classes de base dans `src/styles.scss` (`.btn`, `.field`, `.card`, `.table`, `.alert`...).
- Commandes : `npm start` (proxy `/api` → `localhost:8080`), `npm test -- --watch=false`, `npm run build`.

Performance :
- éviter subscriptions inutiles
- state global inutile
- rendu excessif
- bundles surdimensionnés
- appels API dupliqués
