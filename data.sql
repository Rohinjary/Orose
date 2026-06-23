---Test module 1

-- 1. Ajout de quelques Rôles
INSERT INTO role (code, libelle) VALUES 
('ADMIN', 'Administrateur Système'),
('TECH', 'Technicien'),
('RS', 'Responsable Sanitaire'),
('DIR', 'Directeur');

-- 2. Ajout de quelques Utilisateurs supplémentaires
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, statut) VALUES
('Rakoto', 'Jean', 'jean.rakoto@baovola.mg', 'pass123', 'ACTIF'),
('Razafy', 'Marie', 'marie.razafy@baovola.mg', 'pass123', 'ACTIF');

-- Association des rôles (Admin et Technicien)
INSERT INTO utilisateur_role (id_utilisateur, id_role) VALUES 
(1, 1), -- Admin est Admin
(2, 2); -- Jean est Technicien

-- 3. Ajout de quelques Bassins
INSERT INTO bassin (code, surface_m2, profondeur_metre, notes, id_statut_actuel) VALUES 
('Bassin-05', 120.00, 1.50, 'Production principal', 2);


-- 4. Ajout d'aliments et entrées en stock
INSERT INTO aliment (libelle, seuil_minimum_kg) VALUES 
('nouveau 4.0mm', 100.00);

INSERT INTO entree_stock_aliment (id_aliment, quantite_kg, quantite_restante_kg, prix_unitaire_ar, prix_total_ar, date_expiration, id_responsable) VALUES 
(1, 500.00, 500.00, 1500, 750000, '2026-12-31', 1);

-- 5. Création d'un cycle de production (Lien avec le bassin et l'espèce)
INSERT INTO cycle_bassin (code_unique_cycle, id_bassin, id_espece, effectif_initial, cout_post_larves, date_debut, date_fin_prevue, id_technicien) VALUES 
('B01-C01-2026', 1, 1, 100000, 500000.00, '2026-06-01', '2026-10-01', 2);

-- 6. Simulation d'une distribution de nourriture (Trigger de décrémentation)
-- On valide la distribution pour déclencher le trigger
INSERT INTO distribution_nourriture (id_cycle, id_entree_aliment, id_creneau, quantite_prevue_kg, quantite_donnee_kg, id_responsable, est_valide) VALUES 
(1, 1, 1, 20.00, 20.00, 2, TRUE);

-- 7. Simulation d'un incident sanitaire (Trigger de quarantaine)
-- Comme le niveau est CRITIQUE, le bassin B01 devrait automatiquement passer en QUARANTAINE
INSERT INTO incident_sanitaire (id_cycle, type_incident, description, niveau_gravite, id_responsable) VALUES 
(1, 'MALADIE', 'Apparition de taches blanches sur les crevettes', 'CRITIQUE', 2);

-- Vérification : 
-- SELECT id_statut_actuel FROM bassin WHERE code = 'B01'; 
-- (Devrait retourner l'ID correspondant à 'QUARANTAINE')