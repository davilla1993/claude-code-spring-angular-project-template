-- Crée un utilisateur avec deux rôles, pour tester le choix du profil. Réservé au développement.
--
-- 1. Remplacer l'adresse, les noms et le mot de passe temporaire ci-dessous, puis exécuter ce script.
-- 2. Se connecter : l'application demande de remplacer le mot de passe, puis de choisir un profil.
--
-- Le mot de passe doit contenir 8 à 25 caractères, avec majuscule, minuscule, chiffre et caractère spécial.
-- pgcrypto calcule le hash BCrypt ; sa création peut demander des droits sur la base.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

BEGIN;

WITH new_user AS (
    INSERT INTO users (public_id, email, password_hash, first_name, last_name,
                       active, email_verified, first_login, deleted,
                       created_at, updated_at, created_by, updated_by)
    VALUES (gen_random_uuid()::text,
            lower(trim('deux.roles@example.com')),
            crypt('A-Remplacer#2026', gen_salt('bf', 12)),
            'Prénom', 'Nom',
            true, true, true, false,
            now(), now(), 'SCRIPT', 'SCRIPT')
    RETURNING id
)
INSERT INTO user_roles (user_id, role)
SELECT new_user.id, roles.role
FROM new_user
CROSS JOIN (VALUES ('ADMIN'), ('USER')) AS roles(role);

COMMIT;
