# Checklist avant la mise en production

À dérouler pour chaque projet issu du template, avant le premier déploiement en production.
Cocher chaque point ; un point non applicable doit être justifié.

## 1. Profil et configuration du backend — BLOQUANT

- [ ] **`SPRING_PROFILES_ACTIVE=prod` est défini sur le serveur.**
      `application.yml` déclare `spring.profiles.default: dev` : sans cette variable, l'application démarre
      avec les valeurs de développement (base locale, secret JWT de dev, CORS localhost).
      Vérifier au démarrage la ligne de log : `The following 1 profile is active: "prod"`.
- [ ] Toutes les variables de `.env.example` sont définies sur le serveur (une variable manquante empêche le démarrage).
- [ ] `JWT_SECRET` : généré aléatoirement (≥ 32 caractères, ex. `openssl rand -base64 48`), propre à cet environnement,
      jamais réutilisé depuis `application-dev.yml`.
- [ ] Identifiants de base de données dédiés à la production (pas `postgres`), avec les droits minimum nécessaires.
- [ ] Aucun secret de production n'est commité dans le dépôt.

## 2. Réseau et sécurité

- [ ] HTTPS obligatoire : les cookies d'authentification sont `Secure` et ne sont pas envoyés en HTTP.
- [ ] Frontend et API servis sur le **même site** (même domaine ou sous-domaines), via un reverse proxy :
      les cookies sont `SameSite=Strict`.
- [ ] `CORS_ALLOWED_ORIGINS` = origine(s) exacte(s) du frontend (`https://…`), jamais `*` ni `localhost`.
- [ ] Derrière un reverse proxy : `FORWARD_HEADERS_STRATEGY=native` (IP réelle dans le journal d'audit).
- [ ] Le reverse proxy transmet les WebSockets sur `/api/ws` (en-têtes `Upgrade` / `Connection`).
- [ ] `REGISTRATION_ENABLED` : inscription publique voulue ou non ?
- [ ] Rate limiting en mémoire : une seule instance backend, ou remplacement par un store partagé (Redis, Bucket4j…).
- [ ] Audit des dépendances : `npm audit` (frontend), OWASP Dependency-Check ou équivalent (backend).
- [ ] Revue de sécurité du code spécifique au projet (voir `docs/SECURITY.md`).

## 3. Données

- [ ] Stratégie de schéma : le socle utilise `ddl-auto=update`. Décider si on le garde ou si on passe à `validate`
      avec des migrations (Flyway/Liquibase) avant d'avoir des données réelles.
- [ ] Sauvegardes de la base planifiées **et** restauration testée.
- [ ] `UPLOADS_DIR` sur un volume persistant, inclus dans les sauvegardes ; extensions autorisées (`app.storage.allowed-extensions` dans `application.yml`) adaptées au besoin.
- [ ] Premier compte ADMIN créé (inscription puis `UPDATE users SET role = 'ADMIN' WHERE email = '…'`).
- [ ] Aucune donnée de test ou de développement importée en production.
- [ ] Durée de conservation du journal d'audit définie (RGPD).

## 4. Emails

- [ ] Vrai serveur SMTP (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, TLS activé).
- [ ] `MAIL_FROM` sur un domaine du projet, avec SPF / DKIM / DMARC configurés (sinon les codes finissent en spam).
- [ ] Inscription → réception du code → vérification testées de bout en bout sur l'environnement cible.

## 5. Frontend

- [ ] `APP_NAME` (`shared/constants/app.constants.ts`) et `<title>` (`index.html`) personnalisés ; favicon remplacé.
- [ ] Pages légales rédigées et validées juridiquement (mentions légales, CGU, politique de confidentialité).
- [ ] Page d'accueil (landing) et tableau de bord : placeholders remplacés.
- [ ] Build de production : `npm run build` ; `environment.prod.ts` → `apiBaseUrl` cohérent avec le reverse proxy.

## 6. Projet et livraison

- [ ] Noms génériques remplacés : package `com.gfolly.backend`, `artifactId`, `spring.application.name`, nom de la base.
- [ ] CI activée : décommenter `.github/workflows/ci.yml` (voir README) et vérifier qu'elle passe.
- [ ] Niveaux de logs adaptés (pas de DEBUG ni de `show-sql` en production — c'est le cas par défaut hors profil `dev`).
- [ ] Supervision minimale : logs centralisés, alerte si l'application ne répond plus.
- [ ] Procédure de retour arrière (rollback) connue.
