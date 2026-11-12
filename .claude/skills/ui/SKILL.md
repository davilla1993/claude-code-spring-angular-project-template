---
name: ui
description: Conçoit, modifie ou audite une interface Angular du projet selon docs/DESIGN_SYSTEM.md - sobre, harmonisée, ergonomique, sans fantaisie - avec vérification visuelle par capture d'écran. À utiliser pour tout écran nouveau ou modifié, tout changement de style, et pour un audit UI/UX (diagnostic et proposition).
---

Interface professionnelle : sobre, lisible, prévisible. Une seule façon de faire chaque chose ; l'utilisateur ne doit ni chercher ni hésiter. Tu n'apportes aucune direction artistique : tu appliques `docs/DESIGN_SYSTEM.md`.

Détermine le mode d'après la demande : **Écran** (créer ou modifier) ou **Audit** (diagnostiquer et proposer).

## Mode Écran

1. **Lire** dans `docs/DESIGN_SYSTEM.md` : « Principes », « Structure d'un écran », « Composants » et la sous-section du composant concerné. Pas le fichier entier.
2. **Trouver l'écran le plus proche** déjà présent (`Grep` dans `frontend/src/app/features`) et reprendre sa structure : liste → `admin/users-list`, formulaire → `admin/user-form`, écran d'authentification → `auth/login`.
3. **Construire avec l'existant** : classes de `styles.scss` et composants de `shared`. Le SCSS du composant ne contient que de la mise en page propre à l'écran, exprimée avec les tokens.
4. **S'il manque quelque chose** (variante, token, composant) : l'ajouter au socle (`styles.scss` ou `shared`) et à `docs/DESIGN_SYSTEM.md`, jamais en local dans l'écran. Si cela modifie l'apparence d'écrans existants, demander l'accord avant.
5. **Contrôler** avec la liste ci-dessous, puis faire la vérification visuelle.

### Liste de contrôle
- **Structure :** un `h1`, une seule action principale, ordre en-tête → message → filtres → contenu → pagination.
- **Harmonisation :** aucune valeur en dur (couleur, taille, espacement, rayon, ombre, `z-index`) ; aucune classe ou composant qui double l'existant. Contrôle rapide sur les fichiers touchés :
  `grep -nE '#[0-9a-fA-F]{3,8}\b|rgb\(|[0-9.]+(px|rem)' <fichiers .scss> | grep -v 'var(--'` — ne doivent rester que des dimensions de mise en page justifiées.
- **Libellés :** verbe + objet sur les boutons, même mot pour la même notion que dans le reste de l'application, messages qui disent quoi faire.
- **États :** chargement, erreur avec « Réessayer », vide, données.
- **Formulaire :** libellés visibles, aide avant la saisie, erreur sous le champ reliée par `aria-describedby`, saisie conservée après une erreur du serveur.
- **Écriture :** confirmée par `ConfirmDialogService` (`frontend/CLAUDE.md`).
- **Accessibilité :** parcours complet au clavier, focus visible, noms accessibles, information jamais portée par la couleur seule.
- **Rien de superflu :** ni animation, ni dégradé, ni icône décorative, ni nouvelle couleur.

## Mode Audit

Tu ne modifies aucun fichier avant validation.

1. **Périmètre :** l'écran, le module ou le thème demandé. Pour toute l'application : un seul passage jusqu'à une trentaine de gabarits HTML ; au-delà, découper en lots par module (`CLAUDE.md`, « Découpage en lots »), un rapport par lot.
2. **Relever les écarts** par rapport à `docs/DESIGN_SYSTEM.md`, dans cet ordre :
   - incohérences entre écrans : même action avec un libellé, un style ou un emplacement différent ; plusieurs styles de bouton, de champ, de tableau, de modale ou de message ;
   - valeurs en dur et styles dupliqués (commande `grep` ci-dessus, sur le périmètre) ;
   - ergonomie : action principale absente ou multiple, libellé ambigu, information difficile à trouver, parcours différent d'une ressource à l'autre, état manquant ;
   - accessibilité et responsive.
3. **Voir les écrans** (vérification visuelle ci-dessous) : un écart de rendu ne se constate pas dans le code.
4. **Rapport :**
   `[BLOQUANT|MAJEUR|MINEUR] écran — fichier:ligne — écart constaté — règle du design system — correction proposée`
   puis : les 5 corrections qui apportent le plus, les corrections à faire dans le socle (elles règlent plusieurs écrans d'un coup), les règles manquantes à ajouter au design system, ce qui n'a pas pu être vérifié.
5. **S'arrêter** et attendre la validation. Les corrections validées se font ensuite en mode Écran, lot par lot.

## Vérification visuelle (obligatoire dans les deux modes)

Juger une interface sans la voir ne vaut rien.

1. Lancer le frontend : `npm start` depuis `frontend/` (http://localhost:4200). Les écrans connectés demandent aussi le backend (`./mvnw spring-boot:run` depuis `backend/`, JDK 25, PostgreSQL démarré) et un compte de test : demander à l'utilisateur un compte existant, ou son accord pour en créer un avec `docs/scripts/create-user-two-roles.sql`. Ne jamais créer de compte ni modifier la base sans cet accord.
2. Capturer l'écran à **1280, 768 et 375 px** de large :
   - avec l'outil de navigateur de la session s'il y en a un (Claude in Chrome, navigateur intégré, MCP Playwright) ;
   - sinon avec le script du skill, qui pilote Chrome headless et mesure le débordement horizontal :
     `UI_EMAIL=... UI_PASSWORD=... UI_PROFILE=Administrateur node .claude/skills/ui/capture.mjs <dossier du scratchpad> /app /app/admin/users`
     (sans `UI_EMAIL` pour les écrans publics). Lire ensuite les images produites. Ne pas utiliser `chrome --screenshot` directement : la fenêtre headless ne descend pas sous 500 px et fausse les captures mobiles.
   - Passer tous les chemins en un seul lancement : la connexion est limitée en débit (réponse 429 après plusieurs essais ; redémarrer le backend remet le compteur à zéro).
3. Regarder chaque capture : alignements sur une même verticale, espacements réguliers, hiérarchie (le titre puis l'action principale se voient en premier), aucun défilement horizontal, aucun texte tronqué ou qui déborde, cohérence avec un écran voisin du même type.
4. Parcourir l'écran au clavier (Tab, Entrée, Échap) : ordre logique, focus visible.
5. Provoquer les états : vide, erreur (backend arrêté ou saisie invalide), chargement. Avec le script, seuls les états atteignables par une adresse sont capturés : signaler les autres comme non vus.
6. Corriger, puis capturer de nouveau uniquement ce qui a changé.

Si aucun outil de navigateur n'est disponible, ou si l'application ne peut pas être lancée : dis-le explicitement dans le compte rendu (« non vérifié visuellement ») et liste les écrans à contrôler à la main. Ne présente jamais comme vérifiée une interface que tu n'as pas vue.

## Compte rendu
Écrans touchés, ajouts au socle ou au design system, résultat de la vérification visuelle (largeurs vues, états vus), points non vérifiés.
