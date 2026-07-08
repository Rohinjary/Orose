
INSERT INTO statut_bassin (code, libelle) VALUES
('VIDE',         'Vide'),
('PREPARATION',  'Préparation'),
('ACTIF',        'Actif'),
('EN_TRAITEMENT','En traitement'),
('RECOLTE',      'Récolté'),
('QUARANTAINE',  'Quarantaine');

INSERT INTO creneau_horaire (libelle, ordre) VALUES
('MATIN', 1), ('MIDI', 2), ('SOIR', 3), ('NUIT', 4);

INSERT INTO espece_crevette (nom_scientifique, nom_courant) VALUES
('Fenneropenaeus indicus', 'Crevette blanche');

INSERT INTO evolution_hebdo_espece (id_espece, semaine, poids_cible_g, taille_cible_mm)
SELECT e.id, v.semaine, v.poids, v.taille
FROM espece_crevette e
CROSS JOIN (VALUES
    ( 1,  0.50,   8.00),
    ( 2,  1.00,  12.00),
    ( 3,  1.80,  18.00),
    ( 4,  2.80,  25.00),
    ( 5,  4.00,  32.00),
    ( 6,  5.50,  40.00),
    ( 7,  7.00,  48.00),
    ( 8,  8.50,  55.00),
    ( 9, 10.00,  62.00),
    (10, 11.50,  70.00),
    (11, 13.00,  78.00),
    (12, 14.50,  85.00),
    (13, 16.00,  95.00),
    (14, 17.50, 105.00),
    (15, 19.00, 112.00),
    (16, 20.00, 120.00)
) AS v(semaine, poids, taille)
WHERE e.nom_courant = 'Crevette blanche';


INSERT INTO role (code, libelle) VALUES
('ADMIN', 'Administrateur'),
('DIR', 'Directeur'),
('TECH', 'Technicien'),
('RS', 'Responsable sanitaire')
ON CONFLICT (code) DO NOTHING;

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, statut)
VALUES ('Admin', 'OROSE', 'admin@baovola.mg', 'admin123', 'ACTIF')
ON CONFLICT (email) DO NOTHING;

INSERT INTO utilisateur_role (id_utilisateur, id_role)
SELECT u.id, r.id
FROM utilisateur u
JOIN role r ON r.code = 'ADMIN'
WHERE u.email = 'admin@baovola.mg'
ON CONFLICT DO NOTHING;

INSERT INTO aliment (libelle, seuil_minimum_kg)
SELECT 'Granules Croissance Elevee', 50.00
WHERE NOT EXISTS (
    SELECT 1 FROM aliment WHERE libelle = 'Granules Croissance Elevee'
);

INSERT INTO aliment (libelle, seuil_minimum_kg)
SELECT 'Granules finition', 20.00
WHERE NOT EXISTS (
    SELECT 1 FROM aliment WHERE libelle = 'Granules finition'
);

INSERT INTO bassin (code, surface_m2, profondeur_metre, notes, id_statut_actuel)
SELECT v.code, v.surface, v.profondeur, NULL,
       (SELECT id FROM statut_bassin WHERE code = 'VIDE')
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

INSERT INTO cycle (code_unique_cycle, id_espece, id_technicien, date_debut, date_fin_prevue, est_cloture)
SELECT v.code,
       (SELECT id FROM espece_crevette WHERE nom_courant = 'Crevette blanche'),
       (SELECT id FROM utilisateur WHERE email = 'admin@baovola.mg'),
       v.date_debut::DATE, v.date_fin::DATE, FALSE
FROM (VALUES
    ('C01', '2026-04-13', '2026-08-13')
) AS v(code, date_debut, date_fin)
ON CONFLICT (code_unique_cycle) DO NOTHING;


-- INSERT INTO suivi_hebdo_bassin
--     (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, id_technicien, notes)
-- SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, 1, v.notes
-- FROM cycle_bassin_assoc cba
-- JOIN bassin b ON b.id = cba.id_bassin
-- CROSS JOIN (VALUES
--     ('2026-07-07', 1, 0.6, 9.5, 'Début de cycle et adaptation'),
--     ('2026-07-28', 4, 2.8, 24.5, 'Croissance normale'),
--     ('2026-08-25', 8, 8.4, 54.0, 'Bon développement'),
--     ('2026-09-22', 12, 14.2, 84.0, 'Stade de croissance avancé'),
--     ('2026-10-20', 16, 20.3, 121.5, 'Calibre commercial atteint')
-- ) AS v(date_suivi, semaine, poids, taille, notes)
-- WHERE b.code = 'B01';

-- --- Bassin B02 — cycle C01 : croissance légèrement plus lente ---
INSERT INTO suivi_hebdo_bassin
    (id_cycle_bassin_assoc, date_suivi, semaine_actuelle, poids_moyen_gramme, taille_moyenne_mm, id_technicien, notes)
SELECT cba.id, v.date_suivi::DATE, v.semaine, v.poids, v.taille, 1, v.notes
FROM cycle_bassin_assoc cba
JOIN bassin b ON b.id = cba.id_bassin
CROSS JOIN (VALUES
    ('2026-07-07', 1, 0.5, 8.0, 'Installation du lot'),
    ('2026-08-11', 6, 5.2, 39.0, 'Croissance correcte'),
    ('2026-09-08', 10, 11.0, 68.0, 'Développement stable'),
    ('2026-10-06', 14, 17.0, 103.0, 'En attente de validation commerciale')
) AS v(date_suivi, semaine, poids, taille, notes)
WHERE b.code = 'B02';