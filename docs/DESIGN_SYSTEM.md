# Design System

## Design principles
## Information architecture
## User flows
## Visual hierarchy
## Typography
## Colors / tokens

[socle] Tokens CSS (couleurs, typographie, espacements, rayons, ombres) définis dans `frontend/src/styles.scss` (`:root`),
avec des classes de base (`.btn`, `.field`, `.input`, `.card`, `.table`, `.alert`, `.badge`). Contrastes visés : WCAG AA.
À re-thémer ou remplacer lors du bootstrap.

## Spacing
## Components
## Forms
## Tables
## Dialogs

[socle] `ConfirmDialog` (`shared/components/confirm-dialog`) : modale de confirmation sur `<dialog>` natif, `role="alertdialog"`,
focus initial sur « Annuler », Échap / clic sur le fond = annulation, focus restitué au déclencheur.
API : `ConfirmDialogService.confirm(options): Promise<boolean>` ; `danger: true` pour les actions destructrices.

## Notifications
## Loading / empty / error / success
## Responsive behavior
## Accessibility

Réutiliser les patterns existants avant d'en créer de nouveaux.
