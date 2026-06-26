-- ============================================================
-- SEED DEMO — Module biologique (bassin B01 existant)
-- Base : orose
-- Prérequis : schéma db_v2 + db_modif.sql appliqués
-- ============================================================

BEGIN;

-- statut_bassin : 1=VIDE, 2=PREPARATION, 3=ACTIF

-- ------------------------------------------------------------
-- 1. Utilisateur technicien
-- ------------------------------------------------------------
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, statut)
VALUES ('Rakoto', 'Jean', 'technicien@baovola.mg', 'motdepasse', 'ACTIF')
ON CONFLICT (email) DO NOTHING;

-- ------------------------------------------------------------
-- 2. Espèce + courbe de croissance standard
-- ------------------------------------------------------------
INSERT INTO espece_crevette (nom_scientifique, nom_courant)
SELECT 'Fenneropenaeus indicus', 'Crevette blanche'
WHERE NOT EXISTS (
    SELECT 1 FROM espece_crevette WHERE nom_courant = 'Crevette blanche'
);

INSERT INTO evolution_hebdo_espece (id_espece, semaine, poids_cible_g, taille_cible_mm)
SELECT e.id, v.semaine, v.poids, v.taille
FROM espece_crevette e
CROSS JOIN (VALUES
    ( 1,  0.50,  8.00),
    ( 2,  1.00, 12.00),
    ( 3,  1.80, 18.00),
    ( 4,  2.80, 25.00),
    ( 5,  4.00, 32.00),
    ( 6,  5.50, 40.00),
    ( 7,  7.00, 48.00),
    ( 8,  8.50, 55.00),
    ( 9, 10.00, 62.00),
    (10, 11.50, 70.00),
    (11, 13.00, 78.00),
    (12, 14.50, 85.00),
    (13, 16.00, 95.00),
    (14, 17.50, 105.00),
    (15, 19.00, 112.00),
    (16, 20.00, 120.00)
) AS v(semaine, poids, taille)
WHERE e.nom_courant = 'Crevette blanche'
ON CONFLICT (id_espece, semaine) DO NOTHING;

-- ------------------------------------------------------------
-- 3. Passer B01 en ACTIF (obligatoire pour une pesée via l'app)
-- ------------------------------------------------------------
UPDATE bassin
SET id_statut_actuel = (SELECT id FROM statut_bassin WHERE code = 'ACTIF')
WHERE code = 'B01';

INSERT INTO histo_statut_bassin (id_bassin, id_statut_bassin, id_utilisateur, motif)
SELECT b.id, s.id, u.id, 'Passage en culture — seed demo biologique'
FROM bassin b
JOIN statut_bassin s ON s.code = 'ACTIF'
JOIN utilisateur u ON u.statut = 'ACTIF'
WHERE b.code = 'B01'
  AND NOT EXISTS (
      SELECT 1 FROM histo_statut_bassin h
      JOIN statut_bassin st ON st.id = h.id_statut_bassin
      WHERE h.id_bassin = b.id AND st.code = 'ACTIF'
  )
ORDER BY u.id
LIMIT 1;

-- ------------------------------------------------------------
-- 4. Cycle actif C01
-- ------------------------------------------------------------
INSERT INTO cycle (code_unique_cycle, id_espece, id_technicien, date_debut, date_fin_prevue, est_cloture, created_at)
SELECT 'C01', e.id, u.id, DATE '2026-05-24', DATE '2026-08-24', FALSE, NOW()
FROM espece_crevette e
JOIN utilisateur u ON u.statut = 'ACTIF'
WHERE e.nom_courant = 'Crevette blanche'
  AND NOT EXISTS (SELECT 1 FROM cycle WHERE code_unique_cycle = 'C01')
ORDER BY u.id
LIMIT 1;

-- ------------------------------------------------------------
-- 5. Lier B01 au cycle C01
-- ------------------------------------------------------------
INSERT INTO cycle_bassin_assoc (
    id_cycle, id_bassin, effectif_initial, densite_m2, cout_post_larves,
    poids_moyen_actuel, semaine_actuelle, est_cloture
)
SELECT c.id, b.id, 50000, 2500.00, 1500000.00, 0, 0, FALSE
FROM cycle c
JOIN bassin b ON b.code = 'B01'
WHERE c.code_unique_cycle = 'C01'
  AND NOT EXISTS (
      SELECT 1 FROM cycle_bassin_assoc cba
      WHERE cba.id_bassin = b.id AND cba.est_cloture = FALSE
  );

-- ------------------------------------------------------------
-- 6. Deux pesées d'exemple
-- ------------------------------------------------------------
INSERT INTO suivi_hebdo_bassin (
    id_cycle_bassin_assoc, date_suivi, semaine_actuelle,
    poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes
)
SELECT cba.id, DATE '2026-06-14', 4, 2.80, 25.00, 48500, 300, u.id, 'Pesée S4 — seed SQL'
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c ON c.id = cba.id_cycle
JOIN utilisateur u ON u.statut = 'ACTIF'
WHERE b.code = 'B01' AND c.code_unique_cycle = 'C01'
  AND NOT EXISTS (
      SELECT 1 FROM suivi_hebdo_bassin s
      WHERE s.id_cycle_bassin_assoc = cba.id AND s.date_suivi = DATE '2026-06-14'
  )
ORDER BY u.id LIMIT 1;

INSERT INTO suivi_hebdo_bassin (
    id_cycle_bassin_assoc, date_suivi, semaine_actuelle,
    poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes
)
SELECT cba.id, DATE '2026-06-21', 5, 4.00, 32.00, 47500, 250, u.id, 'Pesée S5 — seed SQL'
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c ON c.id = cba.id_cycle
JOIN utilisateur u ON u.statut = 'ACTIF'
WHERE b.code = 'B01' AND c.code_unique_cycle = 'C01'
  AND NOT EXISTS (
      SELECT 1 FROM suivi_hebdo_bassin s
      WHERE s.id_cycle_bassin_assoc = cba.id AND s.date_suivi = DATE '2026-06-21'
  )
ORDER BY u.id LIMIT 1;

UPDATE cycle_bassin_assoc cba
SET poids_moyen_actuel = 4.00, semaine_actuelle = 5
FROM bassin b, cycle c
WHERE cba.id_bassin = b.id AND cba.id_cycle = c.id
  AND b.code = 'B01' AND c.code_unique_cycle = 'C01';

COMMIT;

-- ------------------------------------------------------------
-- Vérification
-- ------------------------------------------------------------
SELECT b.code, s.code AS statut FROM bassin b
JOIN statut_bassin s ON s.id = b.id_statut_actuel WHERE b.code = 'B01';

SELECT cba.id AS id_cycle_bassin_assoc, c.code_unique_cycle, cba.effectif_initial, cba.semaine_actuelle
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c ON c.id = cba.id_cycle
WHERE b.code = 'B01';

SELECT date_suivi, semaine_actuelle, poids_moyen_gramme, nb_vivants, biomasse_calculee_kg
FROM suivi_hebdo_bassin s
JOIN cycle_bassin_assoc cba ON cba.id = s.id_cycle_bassin_assoc
JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B01'
ORDER BY date_suivi;
