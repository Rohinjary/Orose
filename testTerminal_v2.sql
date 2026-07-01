-- ============================================================
-- DONNÉES DE TEST v2 — Auto-contenu
-- Crée bassins, cycles, associations, pesées et stock
-- ============================================================

-- ------------------------------------------------------------
-- 1. STATUTS (existants)
-- ------------------------------------------------------------
SELECT id, code FROM statut_bassin;

-- ------------------------------------------------------------
-- 2. BASSINS
-- ------------------------------------------------------------
INSERT INTO bassin (code, surface_m2, profondeur_metre, id_statut_actuel, notes)
SELECT 'B01', 500.00, 1.50, sb.id, 'Bassin test 1'
FROM statut_bassin sb WHERE sb.code = 'ACTIF';

INSERT INTO bassin (code, surface_m2, profondeur_metre, id_statut_actuel, notes)
SELECT 'B02', 400.00, 1.20, sb.id, 'Bassin test 2'
FROM statut_bassin sb WHERE sb.code = 'ACTIF';

-- ------------------------------------------------------------
-- 3. ESPÈCE CREVETTE
-- ------------------------------------------------------------
INSERT INTO espece_crevette (nom_courant, nom_scientifique)
SELECT 'Crevette d''eau douce', 'Macrobrachium rosenbergii'
WHERE NOT EXISTS (SELECT 1 FROM espece_crevette LIMIT 1);

-- ------------------------------------------------------------
-- 4. CYCLE
-- ------------------------------------------------------------
INSERT INTO cycle (code_unique_cycle, id_espece, id_technicien, date_debut, date_fin_prevue)
SELECT 'C01', ec.id, 1, '2026-07-01', '2026-10-30'
FROM espece_crevette ec
WHERE NOT EXISTS (SELECT 1 FROM cycle WHERE code_unique_cycle = 'C01');

-- ------------------------------------------------------------
-- 5. CYCLE_BASSIN_ASSOC
-- ------------------------------------------------------------
INSERT INTO cycle_bassin_assoc (id_cycle, id_bassin, effectif_initial, densite_m2, cout_post_larves)
SELECT c.id, b.id, 50000, 100.00, 500000.00
FROM cycle c, bassin b
WHERE c.code_unique_cycle = 'C01' AND b.code = 'B01'
AND NOT EXISTS (SELECT 1 FROM cycle_bassin_assoc WHERE id_cycle = c.id AND id_bassin = b.id);

INSERT INTO cycle_bassin_assoc (id_cycle, id_bassin, effectif_initial, densite_m2, cout_post_larves)
SELECT c.id, b.id, 50000, 125.00, 500000.00
FROM cycle c, bassin b
WHERE c.code_unique_cycle = 'C01' AND b.code = 'B02'
AND NOT EXISTS (SELECT 1 FROM cycle_bassin_assoc WHERE id_cycle = c.id AND id_bassin = b.id);

-- ------------------------------------------------------------
-- 6. PESÉES
-- ------------------------------------------------------------
--- B01 (S1, S4, S8, S12, S16) ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-07-07', 1,  0.5,   8.0,  49800,  200, 1, 'Pesée S1'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B01';

INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-07-28', 4,  2.8,  25.0, 49200,  600, 1, 'Pesée S4'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B01';

INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-08-25', 8,  8.5,  55.0, 48500,  700, 1, 'Pesée S8'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B01';

INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-09-22', 12, 14.5, 85.0, 47800,  700, 1, 'Pesée S12'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B01';

INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-10-20', 16, 20.0, 120.0, 47000,  800, 1, 'Pesée S16 - calibre commercial atteint'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B01';

--- B02 (S1, S6, S10, S14) ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-07-07', 1,  0.5,  8.0,  49700, 300, 1, 'Pesée S1'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B02';

INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-08-11', 6,  5.5,  40.0, 48900, 800, 1, 'Pesée S6'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B02';

INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-09-08', 10, 11.5, 70.0, 48200, 700, 1, 'Pesée S10'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B02';

INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-10-06', 14, 17.5, 105.0, 47500, 700, 1, 'Pesée S14'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin
WHERE b.code = 'B02';

-- ------------------------------------------------------------
-- 7. VÉRIFICATIONS
-- ------------------------------------------------------------
SELECT '--- BASSINS ---' AS info;
SELECT id, code FROM bassin;

SELECT '--- CYCLES ---' AS info;
SELECT id, code_unique_cycle FROM cycle;

SELECT '--- ASSOCIATIONS ---' AS info;
SELECT cba.id, b.code, c.code_unique_cycle, cba.effectif_initial
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c ON c.id = cba.id_cycle;

SELECT '--- PESÉES ---' AS info;
SELECT b.code, sh.date_suivi, sh.semaine_actuelle, sh.poids_moyen_gramme, sh.nb_vivants, sh.nb_morts
FROM suivi_hebdo_bassin sh
JOIN cycle_bassin_assoc cba ON cba.id = sh.id_cycle_bassin_assoc
JOIN bassin b ON b.id = cba.id_bassin
ORDER BY b.code, sh.date_suivi;

SELECT '--- VUE BIOMASSE ---' AS info;
SELECT b.code, sh.date_suivi, sh.semaine_actuelle, sh.poids_moyen_gramme,
       sh.biomasse_calculee_kg, sh.taux_survie_calcule, sh.taux_mortalite_calcule
FROM v_suivi_hebdo_bassin sh
JOIN cycle_bassin_assoc cba ON cba.id = sh.id_cycle_bassin_assoc
JOIN bassin b ON b.id = cba.id_bassin
ORDER BY b.code, sh.date_suivi;

SELECT '--- STOCK ALIMENT ---' AS info;
SELECT id, quantite_kg, quantite_restante_kg, date_reception, date_expiration
FROM entree_stock_aliment
ORDER BY date_expiration;
