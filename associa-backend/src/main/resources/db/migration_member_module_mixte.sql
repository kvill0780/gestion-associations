-- ==================== Migration: Member Module MIXTE Architecture ====================
-- Date: 2026-02-05
-- Description: Implémente l'architecture MIXTE avec maxOccupants
--              - Ajoute contrainte unique sur mandats actifs
--              - Ajoute champs de traçabilité des rôles
--              - Configure valeurs par défaut pour maxOccupants

-- ==================== ÉTAPE 1: Vérification des Doublons ====================
-- ⚠️ EXÉCUTER CETTE REQUÊTE AVANT LA MIGRATION POUR DÉTECTER LES PROBLÈMES

SELECT user_id, post_id, COUNT(*) as count
FROM mandates
WHERE active = true
GROUP BY user_id, post_id
HAVING COUNT(*) > 1;

-- Si des doublons existent, les nettoyer manuellement :
-- UPDATE mandates SET active = false WHERE id IN (...);


-- ==================== ÉTAPE 2: Ajout des Nouveaux Champs ====================

-- Ajouter champ assign_role (indique si un rôle a été attribué)
ALTER TABLE mandates 
ADD COLUMN IF NOT EXISTS assign_role BOOLEAN DEFAULT false;

-- Ajouter champ assigned_role_id (ID du rôle attribué)
ALTER TABLE mandates 
ADD COLUMN IF NOT EXISTS assigned_role_id BIGINT;

-- Ajouter commentaires pour documentation
COMMENT ON COLUMN mandates.assign_role IS 
'Indique si un rôle a été automatiquement attribué lors de ce mandat (pour révocation auto)';

COMMENT ON COLUMN mandates.assigned_role_id IS 
'ID du rôle attribué lors du mandat (peut différer du rôle suggéré si override)';


-- ==================== ÉTAPE 3: Contrainte Unique Partielle ====================

-- Créer l'index unique partiel (PostgreSQL)
-- Empêche un utilisateur d'avoir plusieurs mandats actifs sur le même poste
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_active_mandate 
ON mandates (user_id, post_id) 
WHERE active = true;

-- Ajouter commentaire
COMMENT ON INDEX idx_unique_active_mandate IS 
'Contrainte unique : un utilisateur ne peut avoir qu''un seul mandat actif par poste';


-- ==================== ÉTAPE 4: Migration des Données - maxOccupants ====================

-- Mettre à jour les postes du bureau exécutif (maxOccupants = 1)
UPDATE posts 
SET max_occupants = 1 
WHERE max_occupants IS NULL 
  AND is_executive = true;

-- Mettre à jour les postes non-exécutifs (maxOccupants = 999 pour "illimité")
UPDATE posts 
SET max_occupants = 999 
WHERE max_occupants IS NULL 
  AND is_executive = false;

-- Ajouter contrainte NOT NULL sur maxOccupants
ALTER TABLE posts 
ALTER COLUMN max_occupants SET NOT NULL;

-- Ajouter contrainte CHECK (maxOccupants > 0)
ALTER TABLE posts 
ADD CONSTRAINT chk_max_occupants_positive 
CHECK (max_occupants > 0);


-- ==================== ÉTAPE 5: Vérifications Post-Migration ====================

-- Vérifier que tous les postes ont un maxOccupants défini
SELECT id, name, max_occupants 
FROM posts 
WHERE max_occupants IS NULL;
-- Résultat attendu : 0 lignes

-- Vérifier qu'il n'y a plus de doublons
SELECT user_id, post_id, COUNT(*) as count
FROM mandates
WHERE active = true
GROUP BY user_id, post_id
HAVING COUNT(*) > 1;
-- Résultat attendu : 0 lignes

-- Vérifier les nouveaux champs
SELECT 
    COUNT(*) as total_mandates,
    COUNT(assign_role) as with_assign_role_field,
    COUNT(assigned_role_id) as with_assigned_role_id_field
FROM mandates;


-- ==================== ROLLBACK (si nécessaire) ====================

-- ⚠️ Utiliser UNIQUEMENT en cas de problème critique

-- DROP INDEX IF EXISTS idx_unique_active_mandate;
-- ALTER TABLE mandates DROP COLUMN IF EXISTS assign_role;
-- ALTER TABLE mandates DROP COLUMN IF EXISTS assigned_role_id;
-- ALTER TABLE posts DROP CONSTRAINT IF EXISTS chk_max_occupants_positive;
-- ALTER TABLE posts ALTER COLUMN max_occupants DROP NOT NULL;


-- ==================== Notes d'Exécution ====================

/*
ORDRE D'EXÉCUTION RECOMMANDÉ :

1. Backup de la base de données
   pg_dump -U postgres associa_db > backup_before_migration.sql

2. Exécuter ÉTAPE 1 (vérification doublons)
   - Si doublons trouvés → nettoyer manuellement
   - Si aucun doublon → continuer

3. Exécuter ÉTAPES 2, 3, 4 dans une transaction
   BEGIN;
   -- Exécuter les commandes
   COMMIT;

4. Exécuter ÉTAPE 5 (vérifications)
   - Vérifier que tout est OK

5. Redémarrer l'application Spring Boot
   - Hibernate va détecter les nouveaux champs
   - Les index seront reconnus

IMPORTANT :
- Cette migration est compatible avec spring.jpa.hibernate.ddl-auto=update
- Les nouveaux champs seront automatiquement mappés par Hibernate
- La contrainte unique protège contre les erreurs applicatives
*/
