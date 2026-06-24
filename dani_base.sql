-- ============================================
-- DATA DE TEST POUR DASHBOARD NOURRISSAGE
-- ============================================

-- Nettoyage des tables existantes (sauf aliment pour éviter conflits)
TRUNCATE TABLE distribution_nourriture CASCADE;
TRUNCATE TABLE entree_stock_aliment CASCADE;
TRUNCATE TABLE creneau_horaire CASCADE;
TRUNCATE TABLE cycle_bassin CASCADE;
TRUNCATE TABLE bassin CASCADE;
TRUNCATE TABLE statut_bassin CASCADE;
TRUNCATE TABLE espece_crevette CASCADE;
TRUNCATE TABLE utilisateur CASCADE;

-- ============================================
-- 1. STATUTS DE BASSIN
-- ============================================
INSERT INTO statut_bassin (code, libelle) VALUES
('PREPARATION', 'En préparation'),
('ACTIF', 'Actif'),
('EN_TRAITEMENT', 'En traitement'),
('RECOLTE', 'En récolte'),
('QUARANTAINE', 'Quarantaine');

-- ============================================
-- 2. ESPÈCES DE CREVETTES
-- ============================================
INSERT INTO espece_crevette (nom_scientifique, nom_courant) VALUES
('Litopenaeus stylirostris', 'Crevette Bleue'),
('Penaeus monodon', 'Albinos'),
('Macrobrachium rosenbergii', 'Géant');

-- ============================================
-- 3. RÔLES
-- ============================================
INSERT INTO role (code, libelle) VALUES
('TECHNICIEN', 'Technicien'),
('RESPONSABLE', 'ResponsABLE')
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- 4. UTILISATEURS
-- ============================================
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, statut) VALUES
('Rakoto', 'Jean', 'jean.rakoto@example.com', 'password123', 'ACTIF'),
('Rasoa', 'Marie', 'marie.rasoa@example.com', 'password123', 'ACTIF');

-- Association utilisateurs-rôles (utiliser les IDs réels après insertion)
-- INSERT INTO utilisateur_role (id_utilisateur, id_role) VALUES
-- (1, 1),
-- (2, 2);

-- ============================================
-- 5. ALIMENTS
-- ============================================
-- Skip aliment insertion since they already exist (IDs 7 and 8)

-- ============================================
-- 5. BASSINS ACTIFS
-- ============================================
INSERT INTO bassin (code, surface_m2, profondeur_metre, notes, id_statut_actuel, created_at) VALUES
('B01', 500.00, 1.50, 'Bassin principal', (SELECT id FROM statut_bassin WHERE code = 'ACTIF'), NOW()),
('B02', 400.00, 1.20, 'Bassin secondaire', (SELECT id FROM statut_bassin WHERE code = 'ACTIF'), NOW()),
('B03', 600.00, 1.80, 'Bassin reproduction', (SELECT id FROM statut_bassin WHERE code = 'ACTIF'), NOW()),
('B04', 350.00, 1.00, 'Bassin élevage', (SELECT id FROM statut_bassin WHERE code = 'ACTIF'), NOW());

-- ============================================
-- 6. CYCLES DE BASSIN
-- ============================================
INSERT INTO cycle_bassin (code_unique_cycle, id_bassin, id_espece, effectif_initial, cout_post_larves, densite_m2, id_technicien, date_debut, date_fin_prevue, est_cloture, created_at) VALUES
('B01-C01-2026', (SELECT id FROM bassin WHERE code = 'B01'), (SELECT id FROM espece_crevette WHERE nom_courant = 'Crevette Bleue'), 50000, 250000.00, 100.00, (SELECT id FROM utilisateur WHERE email = 'jean.rakoto@example.com'), '2026-01-01', '2026-06-30', false, NOW()),
('B02-C01-2026', (SELECT id FROM bassin WHERE code = 'B02'), (SELECT id FROM espece_crevette WHERE nom_courant = 'Albinos'), 40000, 200000.00, 100.00, (SELECT id FROM utilisateur WHERE email = 'jean.rakoto@example.com'), '2026-01-15', '2026-07-15', false, NOW()),
('B03-C01-2026', (SELECT id FROM bassin WHERE code = 'B03'), (SELECT id FROM espece_crevette WHERE nom_courant = 'Géant'), 30000, 150000.00, 50.00, (SELECT id FROM utilisateur WHERE email = 'jean.rakoto@example.com'), '2026-02-01', '2026-08-01', false, NOW()),
('B04-C01-2026', (SELECT id FROM bassin WHERE code = 'B04'), (SELECT id FROM espece_crevette WHERE nom_courant = 'Crevette Bleue'), 35000, 175000.00, 100.00, (SELECT id FROM utilisateur WHERE email = 'jean.rakoto@example.com'), '2026-02-15', '2026-08-15', false, NOW());

-- ============================================
-- 7. CRÉNEAUX HORAIRE
-- ============================================
INSERT INTO creneau_horaire (libelle, ordre) VALUES
('MATIN', 1),
('MIDI', 2),
('SOIR', 3),
('NUIT', 4);

-- ============================================
-- 8. ENTRÉES DE STOCK ALIMENT
-- ============================================
INSERT INTO entree_stock_aliment (id_aliment, quantite_kg, quantite_restante_kg, prix_unitaire_ar, prix_total_ar, date_reception, date_expiration, id_responsable) VALUES
((SELECT id FROM aliment WHERE libelle = 'Aliment Premium'), 1000.00, 450.00, 5000.00, 5000000.00, '2026-01-01', '2026-12-31', (SELECT id FROM utilisateur WHERE email = 'marie.rasoa@example.com')),
((SELECT id FROM aliment WHERE libelle = 'Aliment Standard'), 500.00, 200.00, 4000.00, 2000000.00, '2026-02-01', '2026-11-30', (SELECT id FROM utilisateur WHERE email = 'marie.rasoa@example.com'));

-- ============================================
-- 9. DISTRIBUTIONS DE NOURRITURE (AUJOURD'HUI)
-- ============================================
-- Date du jour
DO $$
DECLARE
    today_date DATE := CURRENT_DATE;
    cycle_b01_id INTEGER;
    cycle_b02_id INTEGER;
    cycle_b03_id INTEGER;
    cycle_b04_id INTEGER;
    aliment_premium_id INTEGER;
    creneau_matin_id INTEGER;
    creneau_midi_id INTEGER;
    creneau_soir_id INTEGER;
    creneau_nuit_id INTEGER;
    responsable_id INTEGER;
BEGIN
    -- Récupérer les IDs des cycles
    SELECT id INTO cycle_b01_id FROM cycle_bassin WHERE code_unique_cycle = 'B01-C01-2026';
    SELECT id INTO cycle_b02_id FROM cycle_bassin WHERE code_unique_cycle = 'B02-C01-2026';
    SELECT id INTO cycle_b03_id FROM cycle_bassin WHERE code_unique_cycle = 'B03-C01-2026';
    SELECT id INTO cycle_b04_id FROM cycle_bassin WHERE code_unique_cycle = 'B04-C01-2026';
    
    -- Récupérer les IDs des autres entités
    SELECT id INTO aliment_premium_id FROM entree_stock_aliment WHERE id_aliment = (SELECT id FROM aliment WHERE libelle = 'Aliment Premium') LIMIT 1;
    SELECT id INTO creneau_matin_id FROM creneau_horaire WHERE libelle = 'MATIN';
    SELECT id INTO creneau_midi_id FROM creneau_horaire WHERE libelle = 'MIDI';
    SELECT id INTO creneau_soir_id FROM creneau_horaire WHERE libelle = 'SOIR';
    SELECT id INTO creneau_nuit_id FROM creneau_horaire WHERE libelle = 'NUIT';
    SELECT id INTO responsable_id FROM utilisateur WHERE email = 'jean.rakoto@example.com';

    -- B01: Matin NOURRI, Midi EN_ATTENTE, Soir PRÉVU, Nuit PRÉVU
    INSERT INTO distribution_nourriture (id_cycle, id_entree_aliment, id_creneau, date_distribution, quantite_prevue_kg, quantite_donnee_kg, id_responsable, statut, est_valide) VALUES
    (cycle_b01_id, aliment_premium_id, creneau_matin_id, today_date, 12.50, 12.50, responsable_id, 'NOURRI', true),
    (cycle_b01_id, aliment_premium_id, creneau_midi_id, today_date, 12.50, 0.00, responsable_id, 'EN_ATTENTE', false),
    (cycle_b01_id, aliment_premium_id, creneau_soir_id, today_date, 12.50, 0.00, responsable_id, 'PRÉVU', false),
    (cycle_b01_id, aliment_premium_id, creneau_nuit_id, today_date, 10.00, 0.00, responsable_id, 'PRÉVU', false);

    -- B02: Matin RETARD, Midi EN_ATTENTE, Soir PRÉVU, Nuit PRÉVU
    INSERT INTO distribution_nourriture (id_cycle, id_entree_aliment, id_creneau, date_distribution, quantite_prevue_kg, quantite_donnee_kg, id_responsable, statut, est_valide) VALUES
    (cycle_b02_id, aliment_premium_id, creneau_matin_id, today_date, 10.00, 0.00, responsable_id, 'RETARD', false),
    (cycle_b02_id, aliment_premium_id, creneau_midi_id, today_date, 10.00, 0.00, responsable_id, 'EN_ATTENTE', false),
    (cycle_b02_id, aliment_premium_id, creneau_soir_id, today_date, 10.00, 0.00, responsable_id, 'PRÉVU', false),
    (cycle_b02_id, aliment_premium_id, creneau_nuit_id, today_date, 8.00, 0.00, responsable_id, 'PRÉVU', false);

    -- B03: Matin RUPTURE (pas de cycle actif - simulé par statut RUPTURE), autres annulés
    INSERT INTO distribution_nourriture (id_cycle, id_entree_aliment, id_creneau, date_distribution, quantite_prevue_kg, quantite_donnee_kg, id_responsable, statut, est_valide) VALUES
    (cycle_b03_id, aliment_premium_id, creneau_matin_id, today_date, 15.00, 0.00, responsable_id, 'RUPTURE', false),
    (cycle_b03_id, aliment_premium_id, creneau_midi_id, today_date, 15.00, 0.00, responsable_id, 'RUPTURE', false),
    (cycle_b03_id, aliment_premium_id, creneau_soir_id, today_date, 15.00, 0.00, responsable_id, 'RUPTURE', false),
    (cycle_b03_id, aliment_premium_id, creneau_nuit_id, today_date, 12.00, 0.00, responsable_id, 'RUPTURE', false);

    -- B04: Matin NOURRI, Midi NOURRI, Soir EN_ATTENTE, Nuit PRÉVU
    INSERT INTO distribution_nourriture (id_cycle, id_entree_aliment, id_creneau, date_distribution, quantite_prevue_kg, quantite_donnee_kg, id_responsable, statut, est_valide) VALUES
    (cycle_b04_id, aliment_premium_id, creneau_matin_id, today_date, 8.00, 8.00, responsable_id, 'NOURRI', true),
    (cycle_b04_id, aliment_premium_id, creneau_midi_id, today_date, 8.00, 8.00, responsable_id, 'NOURRI', true),
    (cycle_b04_id, aliment_premium_id, creneau_soir_id, today_date, 8.00, 0.00, responsable_id, 'EN_ATTENTE', false),
    (cycle_b04_id, aliment_premium_id, creneau_nuit_id, today_date, 6.00, 0.00, responsable_id, 'PRÉVU', false);
END $$;

-- ============================================
-- VÉRIFICATION
-- ============================================
SELECT 'Bassins actifs:' as info, COUNT(*) as count FROM bassin WHERE id_statut_actuel = (SELECT id FROM statut_bassin WHERE code = 'ACTIF');
SELECT 'Créneaux horaires:' as info, COUNT(*) as count FROM creneau_horaire;
SELECT 'Stock aliment restant (kg):' as info, COALESCE(SUM(quantite_restante_kg), 0) as total FROM entree_stock_aliment;
SELECT 'Distributions aujourd''hui:' as info, COUNT(*) as count FROM distribution_nourriture WHERE date_distribution = CURRENT_DATE;
