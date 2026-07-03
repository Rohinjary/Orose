
-- ============================================================
-- DONNÉES DE TEST
-- Cycles : C01 (30/06→30/10/2026), C02 (30/09/2026→30/01/2027), C03 (30/07→30/11/2026)
-- Répartition : C01=B01,B02,B03 / C02=B04,B05,B06 / C03=B07,B08,B09
-- ============================================================

-- 1. Aliment de référence
INSERT INTO aliment (libelle, seuil_minimum_kg)
VALUES ('Granules Croissance Elevee', 50.00)
ON CONFLICT DO NOTHING;


-- -- 2. Bassins B01 à B09
INSERT INTO bassin (code, surface_m2, profondeur_metre, notes, id_statut_actuel)
SELECT v.code, v.surface, v.profondeur, NULL,
       (SELECT id FROM statut_bassin WHERE code = 'ACTIF')
FROM (VALUES
    ('B01', 1000.00, 1.50),
    ('B02', 1000.00, 1.50),
    ('B03', 1000.00, 1.50),
    ('B04', 1200.00, 1.60),
    ('B05', 1200.00, 1.60),
    ('B06', 1200.00, 1.60),
    ('B07',  900.00, 1.40),
    ('B08',  900.00, 1.40),
    ('B09',  900.00, 1.40)
) AS v(code, surface, profondeur)
ON CONFLICT (code) DO NOTHING;


-- -- 3. Cycles C01, C02, C03
INSERT INTO cycle (code_unique_cycle, id_espece, id_technicien, date_debut, date_fin_prevue, est_cloture)
SELECT v.code, (SELECT id FROM espece_crevette WHERE nom_courant = 'Crevette blanche'),
       (SELECT id FROM utilisateur WHERE email = 'admin@baovola.mg'),
       v.date_debut::DATE, v.date_fin::DATE, FALSE
FROM (VALUES
    ('C01', '2026-06-30', '2026-10-30'),
    ('C02', '2026-09-30', '2027-01-30'),
    ('C03', '2026-07-30', '2026-11-30')
) AS v(code, date_debut, date_fin)
ON CONFLICT (code_unique_cycle) DO NOTHING;

-- -- 4. Associations cycle <-> bassin (cycle_bassin_assoc)
INSERT INTO cycle_bassin_assoc (id_cycle, id_bassin, effectif_initial, densite_m2, cout_post_larves)
SELECT (SELECT id FROM cycle WHERE code_unique_cycle = v.cycle),
       (SELECT id FROM bassin WHERE code = v.bassin),
       v.effectif, v.densite, v.cout
FROM (VALUES
    ('C01', 'B01', 50000, 50.00, 1500000.00),
    ('C01', 'B02', 50000, 50.00, 1500000.00),
    ('C01', 'B03', 45000, 45.00, 1350000.00),
    ('C02', 'B04', 45000, 37.50, 1350000.00),
    ('C02', 'B05', 60000, 50.00, 1800000.00),
    ('C02', 'B06', 60000, 50.00, 1800000.00),
    ('C03', 'B07', 40000, 44.44, 1200000.00),
    ('C03', 'B08', 40000, 44.44, 1200000.00),
    ('C03', 'B09', 40000, 44.44, 1200000.00)
) AS v(cycle, bassin, effectif, densite, cout)
ON CONFLICT (id_cycle, id_bassin) DO NOTHING;


-- ------------------------------------------------------------
-- VÉRIFICATION : récupère les vrais id_cycle_bassin_assoc avant de continuer
-- ------------------------------------------------------------

SELECT cba.id AS id_cycle_bassin_assoc, b.code AS bassin, c.code_unique_cycle AS cycle,
       cba.effectif_initial, c.date_debut, c.date_fin_prevue
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c ON c.id = cba.id_cycle
ORDER BY c.code_unique_cycle, b.code;


-- ------------------------------------------------------------
-- MODULE 2 — PESÉES (dates recalées sur C01 : début 30/06/2026)
-- NB : on récupère les id_cycle_bassin_assoc dynamiquement par code bassin,
-- ce qui évite de dépendre de numéros fixes (1,2,3...) qui pouvaient ne pas exister.
-- ------------------------------------------------------------

-- --- Bassin B01 — cycle C01 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-07-07', 1,  0.5,   8.0,  49800,  200, 'Pesee S1'),
    ('2026-07-28', 4,  2.8,  25.0,  49200,  600, 'Pesee S4'),
    ('2026-08-25', 8,  8.5,  55.0,  48500,  700, 'Pesee S8'),
    ('2026-09-22', 12, 14.5, 85.0,  47800,  700, 'Pesee S12'),
    ('2026-10-20', 16, 20.0, 120.0, 47000,  800, 'Pesee S16 - calibre commercial atteint')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B01';

-- --- Bassin B02 — cycle C01 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-07-07', 1,  0.5,  8.0,  49700, 300, 'Pesee S1'),
    ('2026-08-11', 6,  5.5,  40.0, 48900, 800, 'Pesee S6'),
    ('2026-09-08', 10, 11.5, 70.0, 48200, 700, 'Pesee S10'),
    ('2026-10-06', 14, 17.5, 105.0,47500, 700, 'Pesee S14')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B02';

-- --- Bassin B03 — cycle C01, bassin test quarantaine ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-07-07', 1, 0.5, 8.0,  44800, 200,  'Pesee S1'),
    ('2026-08-04', 5, 4.0, 32.0, 43000, 1800, 'Pesee S5 - mortalite elevee constatee')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B03';

-- --- Bassin B04 — cycle C02 (début 30/09/2026) ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-10-07', 1,  0.5,  8.0,   44700, 300, 'Pesee S1'),
    ('2026-10-28', 4,  2.8,  25.0,  44100, 600, 'Pesee S4'),
    ('2026-11-25', 8,  8.5,  55.0,  43500, 600, 'Pesee S8'),
    ('2026-12-23', 12, 14.5, 85.0,  42900, 600, 'Pesee S12'),
    ('2027-01-20', 16, 20.0, 120.0, 42200, 700, 'Pesee S16 - calibre commercial atteint')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B04';

-- --- Bassin B05 — cycle C02, bassin test EN_TRAITEMENT ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-10-07', 1,  0.5,  8.0,  59700, 300, 'Pesee S1'),
    ('2026-11-11', 6,  5.5,  40.0, 58800, 900, 'Pesee S6')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B05';

-- -> place le bassin B05 en EN_TRAITEMENT entre S6 et S10 via l'interface (test manuel),
--    puis relance l'INSERT ci-dessous pour S10 une fois repassé en ACTIF :
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, '2026-12-09', 10, 11.5, 70.0, 58000, 800, 1, 'Pesee S10 - reprise apres traitement'
FROM cycle_bassin_assoc cba JOIN bassin b ON b.id = cba.id_bassin WHERE b.code = 'B05';

-- --- Bassin B06 — cycle C02 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, v.vivants, v.morts, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-10-07', 1,  0.5,  8.0,   59800, 200,  'Pesee S1'),
    ('2026-11-25', 8,  8.5,  55.0,  58600, 1200, 'Pesee S8'),
    ('2026-12-30', 14, 17.5, 105.0, 57800, 800,  'Pesee S14'),
    ('2027-01-13', 16, 20.0, 120.0, 57500, 300,  'Pesee S16 - calibre commercial atteint')
) AS v(date_suivi, semaine, poids, taille, vivants, morts, notes)
WHERE b.code = 'B06';


-- Vérification rapide : biomasse calculée auto + taux survie/mortalité via la vue
SELECT id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme,
       biomasse_calculee_kg, taux_survie_calcule, taux_mortalite_calcule
FROM v_suivi_hebdo_bassin
ORDER BY id_cycle_bassin_assoc, date_suivi;


-- ------------------------------------------------------------
-- ALIMENT + STOCK MULTI-LOTS
-- ------------------------------------------------------------

INSERT INTO entree_stock_aliment
    (id_aliment, quantite_kg, quantite_restante_kg, prix_unitaire_ar, date_reception, date_expiration, id_responsable)
VALUES
    -- Lot 1 : reçu le premier, expire le plus tôt -> doit être consommé en premier (FEFO)
    ((SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee'),
     500.00, 500.00, 2200.00, '2026-07-02', '2027-01-01', 1),

    -- Lot 2 : reçu ensuite, expire après le lot 1
    ((SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee'),
     800.00, 800.00, 2100.00, '2026-08-01', '2027-04-01', 1),

    -- Lot 3 : reçu en dernier, mais expire le plus tard
    ((SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee'),
     600.00, 600.00, 2300.00, '2026-09-01', '2027-09-15', 1);

-- Vérification stock global + ordre FEFO attendu
SELECT id, quantite_kg, quantite_restante_kg, prix_unitaire_ar, date_reception, date_expiration
FROM entree_stock_aliment
WHERE id_aliment = (SELECT id FROM aliment WHERE libelle = 'Granules Croissance Elevee')
ORDER BY date_expiration ASC;


-- ── Vérification ──────────────────────────────────────────────
SELECT
    b.code AS bassin,
    c.code_unique_cycle AS cycle,
    cba.semaine_actuelle,
    cba.poids_moyen_actuel
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c  ON c.id = cba.id_cycle
ORDER BY c.code_unique_cycle, b.code;

