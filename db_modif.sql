-- ============================================================
-- CORRECTION — Un cycle regroupe plusieurs bassins (3)
-- À exécuter APRÈS orose_v2.sql
-- ============================================================

-- ------------------------------------------------------------
-- ÉTAPE 1 — Renommer cycle_bassin en cycle (le regroupement pur)
-- ------------------------------------------------------------
-- cycle_bassin devient "cycle" : il ne référence plus directement
-- un seul bassin. Il garde les infos communes au cycle (dates, espèce).

ALTER TABLE cycle_bassin RENAME TO cycle;

-- On retire la colonne id_bassin (un cycle n'a plus UN bassin direct)
-- mais on la garde temporairement pour migration des données existantes
-- si tu as déjà des données, sinon tu peux la dropper directement.
ALTER TABLE cycle DROP COLUMN IF EXISTS id_bassin;
ALTER TABLE cycle DROP COLUMN IF EXISTS effectif_initial;
ALTER TABLE cycle DROP COLUMN IF EXISTS densite_m2;
ALTER TABLE cycle DROP COLUMN IF EXISTS poids_moyen_actuel;
ALTER TABLE cycle DROP COLUMN IF EXISTS semaine_actuelle;

-- Pourquoi on enlève effectif_initial, densite_m2, poids_moyen_actuel ?
-- Parce que CES VALEURS SONT PAR BASSIN, pas par cycle.
-- Chaque bassin dans le cycle peut avoir un effectif et une densité différents.
-- Elles déménagent dans la nouvelle table cycle_bassin_assoc ci-dessous.

DROP INDEX IF EXISTS idx_cycle_unique_actif;

-- ------------------------------------------------------------
-- ÉTAPE 2 — Nouvelle table d'association : quels bassins, dans quel cycle
-- ------------------------------------------------------------
-- C'est la table pivot N-N qui répond exactement à ton besoin :
-- "un cycle contient 3 bassins". Chaque ligne = 1 bassin participant au cycle,
-- avec SES propres effectif/densité/poids — propres à CE bassin.

CREATE TABLE cycle_bassin_assoc (
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

-- Un bassin ne peut être que dans UN SEUL cycle actif à la fois
CREATE UNIQUE INDEX idx_bassin_unique_cycle_actif
    ON cycle_bassin_assoc(id_bassin) WHERE est_cloture = FALSE;

DROP VIEW IF EXISTS v_suivi_hebdo_bassin;

-- cout_post_larves était dans "cycle" avant : on le déplace ici aussi
-- (chaque bassin peut avoir un coût d'achat post-larves différent)
ALTER TABLE cycle DROP COLUMN IF EXISTS cout_post_larves;

-- ------------------------------------------------------------
-- ÉTAPE 3 — Corriger suivi_hebdo_bassin
-- ------------------------------------------------------------
-- Ton instinct est juste : la pesée doit pointer vers le BASSIN
-- (via la ligne cycle_bassin_assoc), pas vers le cycle global.

ALTER TABLE suivi_hebdo_bassin RENAME COLUMN id_cycle TO id_cycle_bassin_assoc;

ALTER TABLE suivi_hebdo_bassin
    DROP CONSTRAINT IF EXISTS suivi_hebdo_bassin_id_cycle_fkey;

ALTER TABLE suivi_hebdo_bassin
    ADD CONSTRAINT fk_suivi_cycle_bassin_assoc
    FOREIGN KEY (id_cycle_bassin_assoc) REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE;

-- ------------------------------------------------------------
-- ÉTAPE 4 — Corriger les autres tables qui pointaient sur cycle_bassin
-- ------------------------------------------------------------
-- distribution_nourriture, incident_sanitaire, lot_crevette, alerte
-- pointaient vers "le cycle d'un bassin" -> maintenant ils doivent
-- pointer vers cycle_bassin_assoc 😊 LE bassin précis dans le cycle)

ALTER TABLE distribution_nourriture RENAME COLUMN id_cycle TO id_cycle_bassin_assoc;
ALTER TABLE distribution_nourriture
    DROP CONSTRAINT IF EXISTS distribution_nourriture_id_cycle_fkey;
ALTER TABLE distribution_nourriture
    ADD CONSTRAINT fk_distrib_cycle_bassin_assoc
    FOREIGN KEY (id_cycle_bassin_assoc) REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE;

-- la contrainte unique doit être mise à jour avec le nouveau nom de colonne
ALTER TABLE distribution_nourriture DROP CONSTRAINT IF EXISTS distribution_nourriture_id_cycle_date_distribution_id_crenea_key;
ALTER TABLE distribution_nourriture
    ADD CONSTRAINT uq_distribution_creneau_jour
    UNIQUE (id_cycle_bassin_assoc, date_distribution, id_creneau);

ALTER TABLE incident_sanitaire RENAME COLUMN id_cycle TO id_cycle_bassin_assoc;
ALTER TABLE incident_sanitaire
    DROP CONSTRAINT IF EXISTS incident_sanitaire_id_cycle_fkey;
ALTER TABLE incident_sanitaire
    ADD CONSTRAINT fk_incident_cycle_bassin_assoc
    FOREIGN KEY (id_cycle_bassin_assoc) REFERENCES cycle_bassin_assoc(id) ON DELETE CASCADE;

ALTER TABLE lot_crevette RENAME COLUMN id_cycle TO id_cycle_bassin_assoc;
ALTER TABLE lot_crevette
    DROP CONSTRAINT IF EXISTS lot_crevette_id_cycle_fkey;
ALTER TABLE lot_crevette
    ADD CONSTRAINT fk_lot_cycle_bassin_assoc
    FOREIGN KEY (id_cycle_bassin_assoc) REFERENCES cycle_bassin_assoc(id);

ALTER TABLE alerte RENAME COLUMN id_cycle_bassin TO id_cycle_bassin_assoc;
ALTER TABLE alerte
    DROP CONSTRAINT IF EXISTS alerte_id_cycle_bassin_fkey;
ALTER TABLE alerte
    ADD CONSTRAINT fk_alerte_cycle_bassin_assoc
    FOREIGN KEY (id_cycle_bassin_assoc) REFERENCES cycle_bassin_assoc(id);

-- ------------------------------------------------------------
-- ÉTAPE 5 — Recréer la vue v_suivi_hebdo_bassin avec les bons JOIN
-- ------------------------------------------------------------



CREATE VIEW v_suivi_hebdo_bassin AS
SELECT
    s.*,
    cba.id_bassin,
    cba.id_cycle,
    ROUND((s.nb_vivants::DECIMAL / cba.effectif_initial * 100), 2) AS taux_survie_calcule,
    ROUND((s.nb_morts::DECIMAL / cba.effectif_initial * 100), 2)   AS taux_mortalite_calcule
FROM suivi_hebdo_bassin s
JOIN cycle_bassin_assoc cba ON cba.id = s.id_cycle_bassin_assoc;

-- ------------------------------------------------------------
-- ÉTAPE 6 — Adapter les triggers existants au nouveau modèle
-- ------------------------------------------------------------

-- Trigger quarantaine : il prenait id_bassin via cycle_bassin,
-- maintenant il faut passer par cycle_bassin_assoc
CREATE OR REPLACE FUNCTION fn_quarantaine_auto()
RETURNS TRIGGER AS $$
DECLARE
    id_statut_quarantaine INTEGER;
    id_bassin_concerne INTEGER;
BEGIN
    IF NEW.niveau_gravite = 'CRITIQUE' THEN
        SELECT id INTO id_statut_quarantaine FROM statut_bassin WHERE code = 'QUARANTAINE';
        SELECT id_bassin INTO id_bassin_concerne
            FROM cycle_bassin_assoc WHERE id = NEW.id_cycle_bassin_assoc;

        UPDATE bassin
        SET id_statut_actuel = id_statut_quarantaine
        WHERE id = id_bassin_concerne;

        INSERT INTO histo_statut_bassin (id_bassin, id_statut_bassin, id_utilisateur, motif)
        VALUES (id_bassin_concerne, id_statut_quarantaine, NEW.id_responsable,
                'Quarantaine automatique - incident critique #' || NEW.id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- RESUME DE LA NOUVELLE STRUCTURE
-- ============================================================
-- cycle              : le cycle global (dates, espèce) — PAS lié à 1 bassin
-- cycle_bassin_assoc : 1 ligne = 1 bassin DANS un cycle (3 lignes pour 3 bassins)
--                      contient effectif/densité/coût/poids PROPRES à ce bassin
-- suivi_hebdo_bassin : pointe vers cycle_bassin_assoc -> donc vers UN bassin précis
-- ============================================================
cba.id
cba.id