-- Corriger suivi_hebdo_bassin : id_cycle → id_cycle_bassin_assoc
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'suivi_hebdo_bassin' AND column_name = 'id_cycle'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'suivi_hebdo_bassin' AND column_name = 'id_cycle_bassin_assoc'
    ) THEN
        ALTER TABLE suivi_hebdo_bassin RENAME COLUMN id_cycle TO id_cycle_bassin_assoc;
    END IF;
END $$;

ALTER TABLE suivi_hebdo_bassin DROP CONSTRAINT IF EXISTS suivi_hebdo_bassin_id_cycle_fkey;
ALTER TABLE suivi_hebdo_bassin DROP CONSTRAINT IF EXISTS fk_suivi_cycle_bassin_assoc;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_suivi_cycle_bassin_assoc'
    ) THEN
        ALTER TABLE suivi_hebdo_bassin
            ADD CONSTRAINT fk_suivi_cycle_bassin_assoc
            FOREIGN KEY (id_cycle_bassin_assoc) REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Corriger alerte : id_cycle_bassin → id_cycle_bassin_assoc
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'alerte' AND column_name = 'id_cycle_bassin'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'alerte' AND column_name = 'id_cycle_bassin_assoc'
    ) THEN
        ALTER TABLE alerte RENAME COLUMN id_cycle_bassin TO id_cycle_bassin_assoc;
    END IF;
END $$;

COMMIT;

-- Vérification
SELECT column_name, is_nullable, data_type
FROM information_schema.columns
WHERE table_name = 'cycle'
ORDER BY ordinal_position;

SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'cycle_bassin_assoc') AS cycle_bassin_assoc_exists;
