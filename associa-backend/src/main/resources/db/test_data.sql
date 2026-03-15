-- Create Test Association
INSERT INTO associations (name, slug, type, status, contact_email, created_at, updated_at) 
VALUES ('Association Test', 'asso-test', 'STUDENT', 'ACTIVE', 'test@example.com', NOW(), NOW())
ON CONFLICT (slug) DO NOTHING;

-- Get Association ID (assuming it's 1 if fresh, but let's use subquery or variable if possible in pure SQL script, typically separate statements)
-- Simplified for this context: assume ID 1 or adjust sequence.
-- Ideally we insert roles linking to the association slug if possible, but schema requires ID.

DO $$
DECLARE
    assoc_id BIGINT;
BEGIN
    SELECT id INTO assoc_id FROM associations WHERE slug = 'asso-test';

    -- President Role
    INSERT INTO roles (association_id, name, slug, description, type, permissions, is_default, display_order, created_at, updated_at)
    VALUES (assoc_id, 'Président', 'president', 'Accès complet à toutes les fonctionnalités', 'LEADERSHIP', '{"admin_all": true}'::jsonb, true, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING; -- Assuming adequate unique constraint or just ignoring duplicates

    -- Member Role
    INSERT INTO roles (association_id, name, slug, description, type, permissions, is_default, display_order, created_at, updated_at)
    VALUES (assoc_id, 'Membre', 'member', 'Accès de base pour tous les membres', 'MEMBER', '{"members.view": true, "events.view": true}'::jsonb, true, 0, NOW(), NOW())
    ON CONFLICT DO NOTHING;

END $$;
