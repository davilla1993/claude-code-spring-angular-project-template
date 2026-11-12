# Frontend - Angular

Angular 22 standalone, zoneless, signals, Reactive Forms, Vitest, pages chargées à la demande (lazy). Suivre les versions réellement présentes dans `package.json`.

## Avant tout travail d'interface
Charger le skill `/ui` : il applique `docs/DESIGN_SYSTEM.md` (une seule façon de faire chaque chose, aucune valeur en dur) et impose la vérification visuelle.

## Socle existant (à réutiliser, ne pas dupliquer)
- `core/api/*` : services typés par ressource (`AuthApi`, `UsersApi`, `AuditApi`), enveloppe `ApiResponse` dépliée par `unwrap()`. Tout appel HTTP passe par là, jamais par un composant.
- `core/services/session-store` : utilisateur courant (signals) ; `hasPermission()` pour adapter l'UI uniquement.
- `core/interceptors` : cookies + refresh automatique sur 401 (un seul refresh à la fois, `TokenRefresher`).
- `core/guards` : `authGuard` (exige aussi un profil actif), `guestGuard`, `firstLoginGuard`, `profileSelectionGuard`, `permissionGuard('code')`.
- Profils : `/auth/select-profile` choisit ou change le profil ; `SessionSync` recharge les autres onglets après un changement.
- `core/validators` : mêmes règles que le serveur (nom, mot de passe) ; toute évolution se fait des deux côtés.
- `shared` : `ToastStore`, pipes `fieldError` / `confirmFieldError`, `Pagination`, modèles.
- Styles : tokens et classes de base dans `src/styles.scss` (`.btn`, `.field`, `.card`, `.table`, `.alert`...) ; règles d'usage dans `docs/DESIGN_SYSTEM.md`.

## Règles
- **Composants :** une responsabilité ; HTML, SCSS et TypeScript dans des fichiers séparés ; pas de logique métier dans les templates ; état partagé dans un service dédié.
- **États :** chaque écran traite le chargement, l'erreur et le vide.
- **API :** un changement d'API met à jour modèles, services `core/api`, consommateurs et tests. Ne pas dupliquer les règles métier du backend.
- **Sécurité :** guards et masquage n'adaptent que l'interface, le serveur décide. Aucun secret ni jeton dans le code ou le stockage du navigateur. Pas de `innerHTML` ni de `bypassSecurityTrust*`.
- **Accessibilité :** HTML sémantique, labels, navigation complète au clavier, focus visible et géré, erreurs reliées à leur champ, responsive.
- **Performance :** signals et `track` sur les listes, pas d'appel API dupliqué, pas d'état global ni de subscription inutiles.

## Confirmation des actions
- Toute opération d'écriture est confirmée avant son exécution : création, validation, modification, suppression.
- Sans confirmation : les actions qui ne changent rien (navigation, filtres, pagination, copie, affichage ou masquage) et les formulaires du parcours d'authentification avant l'ouverture de la session (connexion, inscription, vérification de l'email, mot de passe oublié).
- Un seul composant : `await inject(ConfirmDialogService).confirm({ title, message, confirmLabel, cancelLabel, danger })` (modale `<dialog>` native). L'enrichir si une option manque.
- Interdit : créer une autre modale de confirmation ou utiliser `window.confirm`.
- Le message indique précisément l'action et ses conséquences. `danger: true` pour une action destructrice ou difficile à annuler.
- La confirmation est une aide à l'utilisateur, pas un contrôle de sécurité.

## Commandes (depuis `frontend/`)
- Tests ciblés : `npm test -- --watch=false --include='src/app/chemin/**/*.spec.ts'`. La suite complète (`npm test` sans `--include`) exige l'accord de l'utilisateur.
- Build : `npm run build`.
- Lancement : `npm start` (proxy `/api` → `localhost:8080`).
