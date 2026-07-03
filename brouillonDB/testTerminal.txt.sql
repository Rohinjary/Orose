-- ============================================================
-- DONNÉES DE TEST — Pesées + Aliment/Stock
-- À adapter aux IDs réels de ta base (bassin, cycle, cycle_bassin_assoc, utilisateur)
-- Cycles réels : C01 (30/06→30/10/2026), C02 (30/09/2026→30/01/2027), C03 (30/07→30/11/2026)
-- Répartition : C01=B01,B02,B03 / C02=B04,B05,B06 / C03=B07,B08,B09
-- ============================================================

-- ------------------------------------------------------------
-- ÉTAPE 0 — Vérifier les IDs réels avant de lancer les INSERT
-- ------------------------------------------------------------
-- Lance ces SELECT d'abord pour récupérer les bons id_cycle_bassin_assoc

SELECT cba.id AS id_cycle_bassin_assoc, b.code AS bassin, c.code_unique_cycle AS cycle,
       cba.effectif_initial, c.date_debut, c.date_fin_prevue
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
JOIN cycle c ON c.id = cba.id_cycle
ORDER BY c.code_unique_cycle, b.code;

SELECT id, nom, prenom FROM utilisateur;

-- ============================================================
-- PARTIR DU PRINCIPE QUE LES ID SONT (à ajuster si différents) :
-- cycle_bassin_assoc : 1=B01/C01, 2=B02/C01, 3=B03/C01,
--                       4=B04/C02, 5=B05/C02, 6=B06/C02
-- utilisateur : 1 = Admin (id_technicien à adapter si tu as créé Njary/Mickael)
-- ============================================================

-- ------------------------------------------------------------
-- MODULE 2 — PESÉES (dates recalées sur C01 : début 30/06/2026)
-- ------------------------------------------------------------

-- --- Bassin B01 (cycle_bassin_assoc id = 1) — cycle C01 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
VALUES
    (1, '2026-07-07', 1,  0.5,   8.0,  49800,  200, 1, 'Pesée S1'),
    (1, '2026-07-28', 4,  2.8,  25.0,  49200,  600, 1, 'Pesée S4'),
    (1, '2026-08-25', 8,  8.5,  55.0,  48500,  700, 1, 'Pesée S8'),
    (1, '2026-09-22', 12, 14.5, 85.0,  47800,  700, 1, 'Pesée S12'),
    (1, '2026-10-20', 16, 20.0, 120.0, 47000,  800, 1, 'Pesée S16 - calibre commercial atteint');

-- --- Bassin B02 (cycle_bassin_assoc id = 2) — cycle C01 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
VALUES
    (2, '2026-07-07', 1,  0.5,  8.0,  49700, 300, 1, 'Pesée S1'),
    (2, '2026-08-11', 6,  5.5,  40.0, 48900, 800, 1, 'Pesée S6'),
    (2, '2026-09-08', 10, 11.5, 70.0, 48200, 700, 1, 'Pesée S10'),
    (2, '2026-10-06', 14, 17.5, 105.0,47500, 700, 1, 'Pesée S14');

-- --- Bassin B03 (cycle_bassin_assoc id = 3) — cycle C01, bassin test quarantaine ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
VALUES
    (3, '2026-07-07', 1, 0.5, 8.0,  44800, 200,  1, 'Pesée S1'),
    (3, '2026-08-04', 5, 4.0, 32.0, 43000, 1800, 1, 'Pesée S5 - mortalité élevée constatée');

-- --- Bassin B04 (cycle_bassin_assoc id = 4) — cycle C02 (début 30/09/2026) ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
VALUES
    (4, '2026-10-07', 1,  0.5,  8.0,   44700, 300, 1, 'Pesée S1'),
    (4, '2026-10-28', 4,  2.8,  25.0,  44100, 600, 1, 'Pesée S4'),
    (4, '2026-11-25', 8,  8.5,  55.0,  43500, 600, 1, 'Pesée S8'),
    (4, '2026-12-23', 12, 14.5, 85.0,  42900, 600, 1, 'Pesée S12'),
    (4, '2027-01-20', 16, 20.0, 120.0, 42200, 700, 1, 'Pesée S16 - calibre commercial atteint');

-- --- Bassin B05 (cycle_bassin_assoc id = 5) — cycle C02, bassin test EN_TRAITEMENT ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
VALUES
    (5, '2026-10-07', 1,  0.5,  8.0,  59700, 300, 1, 'Pesée S1'),
    (5, '2026-11-11', 6,  5.5,  40.0, 58800, 900, 1, 'Pesée S6');
-- -> place le bassin B05 en EN_TRAITEMENT entre S6 et S10 via l'interface (test manuel),
--    puis relance l'INSERT ci-dessous pour S10 une fois repassé en ACTIF :
-- INSERT INTO suivi_hebdo_bassin
--     (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
-- VALUES
--     (5, '2026-12-09', 10, 11.5, 70.0, 58000, 800, 1, 'Pesée S10 - reprise après traitement');

-- --- Bassin B06 (cycle_bassin_assoc id = 6) — cycle C02 ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, nb_vivants, nb_morts, id_technicien, notes)
VALUES
    (6, '2026-10-07', 1,  0.5,  8.0,   59800, 200,  1, 'Pesée S1'),
    (6, '2026-11-25', 8,  8.5,  55.0,  58600, 1200, 1, 'Pesée S8'),
    (6, '2026-12-30', 14, 17.5, 105.0, 57800, 800,  1, 'Pesée S14'),
    (6, '2027-01-13', 16, 20.0, 120.0, 57500, 300,  1, 'Pesée S16 - calibre commercial atteint');

-- Vérification rapide : biomasse calculée auto + taux survie/mortalité via la vue
SELECT id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme,
       biomasse_calculee_kg, taux_survie_calcule, taux_mortalite_calcule
FROM v_suivi_hebdo_bassin
ORDER BY id_cycle_bassin_assoc, date_suivi;

-- ------------------------------------------------------------
-- ALIMENT + STOCK MULTI-LOTS
-- ------------------------------------------------------------

INSERT INTO aliment (libelle, seuil_minimum_kg)
VALUES ('Granulés Croissance Élevée', 50.00)
ON CONFLICT DO NOTHING;

-- Récupère l'id de l'aliment créé
-- SELECT id FROM aliment WHERE libelle = 'Granulés Croissance Élevée';

INSERT INTO entree_stock_aliment
    (id_aliment, quantite_kg, quantite_restante_kg, prix_unitaire_ar, date_reception, date_expiration, id_responsable)
VALUES
    -- Lot 101 : reçu le premier, expire le plus tôt -> doit être consommé en premier (FEFO)
    ((SELECT id FROM aliment WHERE libelle = 'Granulés Croissance Élevée'),
     500.00, 500.00, 2200.00, '2026-07-02', '2027-01-01', 1),

    -- Lot 102 : reçu ensuite, expire après le lot 101
    ((SELECT id FROM aliment WHERE libelle = 'Granulés Croissance Élevée'),
     800.00, 800.00, 2100.00, '2026-08-01', '2027-04-01', 1),

    -- Lot 103 : reçu en dernier, mais expire le plus tard
    ((SELECT id FROM aliment WHERE libelle = 'Granulés Croissance Élevée'),
     600.00, 600.00, 2300.00, '2026-09-01', '2027-09-15', 1);

-- Vérification stock global + ordre FEFO attendu (101 puis 102 puis 103)
SELECT id, quantite_kg, quantite_restante_kg, prix_unitaire_ar, date_reception, date_expiration
FROM entree_stock_aliment
WHERE id_aliment = (SELECT id FROM aliment WHERE libelle = 'Granulés Croissance Élevée')
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
