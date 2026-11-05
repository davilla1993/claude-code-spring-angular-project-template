-- Donne le rôle ADMIN à un compte existant : à utiliser pour le premier administrateur de chaque projet.
--
-- 1. Inscrire le compte depuis l'application et vérifier son email.
-- 2. Remplacer l'adresse ci-dessous, puis exécuter ce script.
--
-- Prévu pour un compte fraîchement inscrit, qui n'a que le rôle USER.

UPDATE user_roles
SET role = 'ADMIN'
WHERE user_id = (SELECT id FROM users WHERE email = lower(trim('admin@example.com')));
