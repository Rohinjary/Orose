-- ------------------------------------------------------------
-- DONNÉES DE RÉFÉRENCE
-- ------------------------------------------------------------

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

-- ------------------------------------------------------------
-- AUTHENTIFICATION
-- ------------------------------------------------------------

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
