-- ============================================================
-- FIX — Aligner la table cycle avec le schéma post db_modif
-- Erreur corrigée : effectif_initial NOT NULL sur cycle
-- À exécuter si db_modif.sql n'a pas été appliqué complètement
-- ============================================================

BEGIN;

-- Renommer cycle_bassin → cycle si pas encore fait
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'cycle_bassin')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'cycle' AND table_schema = 'public') THEN
        ALTER TABLE cycle_bassin RENAME TO cycle;
    END IF;
END $$;

-- Retirer les colonnes déplacées vers cycle_bassin_assoc
ALTER TABLE cycle DROP COLUMN IF EXISTS id_bassin;
ALTER TABLE cycle DROP COLUMN IF EXISTS effectif_initial;
ALTER TABLE cycle DROP COLUMN IF EXISTS densite_m2;
ALTER TABLE cycle DROP COLUMN IF EXISTS poids_moyen_actuel;
ALTER TABLE cycle DROP COLUMN IF EXISTS semaine_actuelle;
ALTER TABLE cycle DROP COLUMN IF EXISTS cout_post_larves;
ALTER TABLE cycle DROP COLUMN IF EXISTS date_fin_reelle;

DROP INDEX IF EXISTS idx_cycle_unique_actif;

-- Créer cycle_bassin_assoc si absente
CREATE TABLE IF NOT EXISTS cycle_bassin_assoc (
    id SERIAL PRIMARY KEY,
    id_cycle INTEGER NOT NULL REFERENCES cycle(id) ON DELETE CASCADE,
    id_bassin INTEGER NOT NULL REFERENCES bassin(id),
    effectif_initial INTEGER NOT NULL,
    densite_m2 DECIMAL(10,2),
    cout_post_larves DECIMAL(15,2) NOT NULL,
    poids_moyen_actuel DECIMAL(10,2) DEFAULT 0,
    semaine_actuelle INTEGER DEFAULT 0,
    date_fin_reelle DATE,
    est_cloture BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(id_cycle, id_bassin)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_bassin_unique_cycle_actif
    ON cycle_bassin_assoc(id_bassin) WHERE est_cloture = FALSE;

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
