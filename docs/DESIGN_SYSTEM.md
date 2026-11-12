# Design System

Source unique des règles d'interface. Les valeurs sont dans `frontend/src/styles.scss` (`:root`) ; ce document dit comment les employer.
Lire la section concernée par la tâche, pas le fichier entier. Les sections **[projet]** sont à compléter au bootstrap.

## Principes

Application professionnelle : sobre, lisible, prévisible. Pas de fantaisie.

1. **Une seule façon de faire chaque chose.** Un seul style de bouton, de champ, de tableau, de modale, de message. Deux écrans qui font la même chose se ressemblent trait pour trait.
2. **L'utilisateur ne cherche pas.** Chaque écran a un titre qui dit où l'on est, une action principale évidente, et les mêmes éléments aux mêmes endroits que sur les autres écrans.
3. **Aucune ambiguïté.** Libellés explicites (un verbe et son objet : « Créer l'utilisateur », jamais « OK » ou « Valider » seul), un seul mot pour une même notion dans toute l'application, conséquences annoncées avant une action.
4. **Le contenu d'abord.** Pas de décoration : ni dégradé, ni animation d'agrément, ni illustration, ni icône sans utilité, ni couleur qui ne porte pas de sens.
5. **Sobriété des moyens.** Peu de tailles, peu de graisses, peu de couleurs. Ajouter un token ou une variante est une décision, pas un réflexe.
6. **Accessible par construction.** WCAG 2.2 AA : ce n'est pas une étape de fin.

## Charte du projet [projet]

Chaque projet définit sa charte en modifiant **uniquement les tokens** de `styles.scss` : couleur primaire (et sa variante au survol), police, éventuellement rayon. Vérifier ensuite les contrastes (texte 4,5:1, éléments d'interface et focus 3:1).
Logo, nom de l'application et ton rédactionnel : à préciser ici.

## Tokens [socle]

Aucune valeur en dur dans un composant : ni couleur, ni taille de police, ni espacement, ni rayon, ni ombre, ni `z-index`. Si une valeur manque, on l'ajoute aux tokens et à ce tableau.

| Famille | Tokens | Usage |
|---|---|---|
| Couleur d'action | `--color-primary`, `--color-primary-hover`, `--color-on-primary` | Bouton principal, liens, élément actif. Rien d'autre. |
| Texte | `--color-text`, `--color-text-muted` | Texte courant ; texte secondaire, aides, en-têtes de tableau |
| Fonds et traits | `--color-bg`, `--color-surface`, `--color-border` | Fond de page ; cartes, champs, tableaux ; toutes les bordures |
| États | `--color-success`, `--color-danger` (+ `-hover`), `--color-warning`, `--color-info` | Uniquement pour porter ce sens. Jamais la couleur seule : toujours un texte. |
| Focus, voile | `--color-focus`, `--color-overlay` | Contour de focus ; fond derrière une modale |
| Police | `--font-family` | Une seule famille dans toute l'application |
| Tailles | `--font-size-sm` (0,875) · `base` (1) · `md` (1,125) · `lg` (1,25) · `xl` (1,75 rem) | Voir « Typographie » |
| Espacements | `--space-1` · `2` · `3` · `4` · `6` · `8` (0,25 à 2 rem) | Voir « Espacements » |
| Contrôles | `--control-height` (2,5 rem), `--control-height-sm` (2 rem) | Hauteur commune des boutons, champs et listes |
| Formes | `--radius`, `--shadow`, `--shadow-overlay` | Un seul rayon ; ombre des cartes et toasts ; ombre des modales |
| Largeurs | `--content-width` (72 rem), `--width-form` (40 rem), `--width-narrow` (28 rem) | Page ; formulaire ou texte ; carte d'authentification et modale |
| Plans | `--z-toast`, `--z-skip-link` | Seuls `z-index` autorisés, avec la modale native |

## Typographie

| Élément | Taille | Graisse |
|---|---|---|
| Titre de page `h1` (un seul par écran) | `xl` | 700 |
| Titre de section `h2` | `lg` | 700 |
| Sous-titre `h3` | `md` | 700 |
| Texte, champs, boutons | `base` | 400 ; 600 pour boutons et libellés de champ |
| Aides, erreurs de champ, en-têtes de tableau, badges | `sm` | 400 ; 600 pour les badges |

- Deux graisses de texte : 400 et 600 (700 réservé aux titres). Pas d'italique, pas de majuscules forcées, pas de texte souligné hors liens.
- Interligne 1,5 pour le texte, 1,25 pour les titres. Lignes de texte limitées à `--width-form`.
- Les niveaux de titre suivent la structure du document, sans saut ; on ne choisit pas un niveau pour sa taille.
- Chiffres à comparer (montants, quantités) : alignés à droite dans les tableaux, avec `font-variant-numeric: tabular-nums`.

## Espacements

- Uniquement l'échelle `--space-*`. Entre un libellé et son champ : `1`. Entre éléments d'un même groupe : `2` ou `3`. Entre champs d'un formulaire : `4`. Intérieur d'une carte et entre blocs d'une page : `6`. Grandes respirations : `8`.
- Ce qui va ensemble est proche, ce qui est distinct est séparé : l'espacement exprime la structure avant tout trait ou tout fond.
- Alignement à gauche par défaut, sur une même verticale. Pas de contenu centré, sauf états vides et cartes d'authentification.

## Structure d'un écran

De haut en bas, toujours dans cet ordre :

1. **En-tête de page** (`.page-header`) : `h1` à gauche, action principale à droite.
2. **Message contextuel** éventuel (`.alert`).
3. **Filtres** (`.card.filters` au-dessus de la liste), s'il y en a.
4. **Contenu** : tableau, formulaire ou cartes.
5. **Pagination** (`Pagination`), sous la liste.

- Une seule action principale (`.btn`) par écran ou par formulaire ; les autres sont secondaires (`.btn--secondary`) ou des liens.
- Titre de page : il nomme l'écran. Groupe nominal pour une page de consultation (« Utilisateurs », « Journal d'audit »), verbe à l'infinitif et objet pour une page d'action (« Modifier l'utilisateur »).
- Liste → détail ou formulaire → retour à la liste : le même parcours pour toutes les ressources.
- Navigation : en-tête unique (`core/layout/header`), entrées affichées selon le profil actif, entrée courante marquée. Pas de second système de navigation.

## Composants [socle]

Avant de créer un composant ou une classe, chercher ici et dans `styles.scss`. On enrichit l'existant, on ne le double pas.

| Besoin | À utiliser |
|---|---|
| Action principale / secondaire / destructrice / discrète | `.btn` / `.btn--secondary` / `.btn--danger` / `.btn--link` ; `.btn--sm` dans les tableaux ; `.btn--block` sur les cartes étroites |
| Formulaire | `.form`, `.form-row`, `.field`, `.input`, `.select`, `.checkbox-group`, `.checkbox`, `.field-hint`, `.field-error`, `.form-actions` |
| Bloc de contenu | `.card` ; `.form-card` pour un formulaire ; `.auth-card` pour l'authentification |
| Liste de données | `.table-wrapper` > `.table`, `.table-actions`, composant `Pagination` |
| Filtres d'une liste | `<form class="card filters">` avec `.form-row` et `.form-actions` |
| Statut d'un élément | `.badge`, `.badge--success`, `.badge--danger` |
| Message dans la page | `.alert--error` / `--success` / `--info` / `--warning` |
| Retour après une action | `ToastStore` |
| Confirmation | `ConfirmDialogService` (seule modale de confirmation) |
| Attente, absence de données | `.loading-state`, `.empty-state` |
| Mise en page | `.container`, `.page-header`, `.muted`, `.visually-hidden` |

### Boutons
- Libellé : verbe à l'infinitif et objet. Le même libellé pour la même action partout.
- Ordre dans un groupe : l'action principale est au bord d'alignement. Formulaire (aligné à gauche) : principale, puis « Annuler ». Modale (alignée à droite) : « Annuler », puis principale.
- `.btn--danger` uniquement pour une action destructrice ou difficile à annuler.
- Un bouton désactivé reste lisible ; pendant un envoi, il est désactivé et son libellé indique l'action en cours.
- Pas de bouton réduit à une icône. Si une icône est ajoutée, elle accompagne le texte. Seule exception : la fermeture « × » d'un toast, avec son `aria-label`.

### Formulaires
- Une colonne par défaut ; `.form-row` pour des champs courts et liés (prénom et nom).
- Libellé toujours visible au-dessus du champ : un placeholder ne remplace jamais un libellé.
- Champs obligatoires par défaut ; on signale les champs facultatifs par « (facultatif) » dans le libellé.
- Aide sous le champ (`.field-hint`), avant la saisie, pour toute contrainte de format.
- Erreur sous le champ (`.field-error`), reliée par `aria-describedby`, avec `aria-invalid` ; message qui dit quoi corriger. Affichée après la sortie du champ ou l'envoi, pas pendant la frappe. Pipes `fieldError` / `confirmFieldError`.
- Erreur du serveur : `.alert--error` en tête du formulaire, saisie conservée.
- Bouton d'envoi : verbe et objet (« Créer l'utilisateur », « Changer le mot de passe »), jamais « Enregistrer » ou « Valider » seul.
- Actions en bas à gauche (`.form-actions`) : principale, puis « Annuler ».

### Tableaux
- En-têtes courts, `scope="col"`, `<caption>` (éventuellement `.visually-hidden`).
- Texte aligné à gauche, nombres à droite, dates dans un format unique.
- Actions de ligne dans la dernière colonne (`.table-actions`, `.btn--sm`), dans le même ordre sur chaque ligne et dans chaque tableau.
- Toujours paginé côté serveur ; total affiché.
- Filtres : un seul modèle, quel que soit leur nombre. Formulaire `.card.filters` au-dessus du tableau, appliqué par le bouton « Filtrer » et vidé par « Réinitialiser » ; jamais de filtrage à la saisie. La pagination conserve les filtres appliqués.
- Jamais de code technique à l'écran (`USER_CREATE`, `ADMIN`) : chaque code a un libellé, défini avec son modèle dans `shared/models`.
- Dates : `short` dans un tableau, `longDate` dans une fiche ou une phrase. Pas d'autre format.
- Sur petit écran, le tableau défile horizontalement dans `.table-wrapper` ; la page, jamais.

### Modales
- Réservées à la confirmation (`ConfirmDialog`, `<dialog>` natif). Un formulaire s'ouvre dans une page, pas dans une modale.
- Titre = l'action sous forme de question ; message = l'objet concerné et la conséquence ; bouton = le verbe de l'action.
- Focus initial sur « Annuler », Échap et clic sur le fond annulent, focus rendu au déclencheur.
- Règles de déclenchement : `frontend/CLAUDE.md`, « Confirmation des actions ».

### Messages
- **Toast** (`ToastStore`) : résultat d'une action qui vient d'avoir lieu (succès, échec). Court, sans action à mener.
- **Alerte** (`.alert`) : information liée à l'écran, qui reste tant qu'elle est vraie (erreur de chargement avec « Réessayer », avertissement).
- **Erreur de champ** : sous le champ. Ne jamais afficher la même information par deux canaux.
- Ton : factuel, à la forme de politesse, sans jargon technique ni code d'erreur seul. Dire ce qui s'est passé et quoi faire.

## États d'un écran

Tout écran qui charge des données traite les quatre, sans exception :

| État | Rendu |
|---|---|
| Chargement | `.loading-state` avec `role="status"` au premier chargement ; ensuite `aria-busy` sur le contenu, sans le vider |
| Erreur | `.alert--error` avec `role="alert"` et un bouton « Réessayer » |
| Vide | `.empty-state` : phrase qui dit pourquoi c'est vide et, si possible, quoi faire |
| Données | Le contenu |

## Responsive

- Fluide, sans points de rupture : `flex-wrap`, grilles `auto-fit`, largeurs maximales. Pas de `@media` par écran, sauf besoin démontré.
- Contrôlé à 375, 768 et 1280 px : aucun défilement horizontal de la page, aucun texte tronqué, cibles tactiles d'au moins `--control-height-sm`.
- Unités relatives (`rem`, `%`) et propriétés logiques (`inline-size`, `margin-block`...), comme dans le socle.

## Accessibilité

- HTML sémantique : `button` pour une action, `a` pour une navigation, `nav`, `main`, titres hiérarchisés, listes, `table` pour les données.
- Tout est utilisable au clavier, dans l'ordre visuel ; focus toujours visible (`:focus-visible` global, ne pas le retirer) ; lien d'évitement conservé.
- Nom accessible pour chaque contrôle ; `label` relié à son champ par `for`/`id`.
- Contrastes : texte 4,5:1, composants et focus 3:1. L'information ne repose jamais sur la couleur seule.
- Changements dynamiques annoncés : `role="status"` (information), `role="alert"` (erreur).
- Zoom à 200 % sans perte de contenu ; `prefers-reduced-motion` respecté si une transition est ajoutée.

## Parcours et architecture de l'information [projet]

Arborescence des écrans, parcours par profil et vocabulaire métier : à définir au bootstrap, à partir du cahier des charges.
