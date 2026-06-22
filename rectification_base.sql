-- Suppression forcée de la vue actuelle qui empêche les modifications
DROP VIEW IF EXISTS v_suivi_hebdo_bassin CASCADE;

-- Recréation propre de la vue
CREATE VIEW v_suivi_hebdo_bassin AS
SELECT
    s.*,
    cba.id_bassin,
    cba.id_cycle,
    ROUND((s.nb_vivants::DECIMAL / cba.effectif_initial * 100), 2) AS taux_survie_calcule,
    ROUND((s.nb_morts::DECIMAL / cba.effectif_initial * 100), 2)   AS taux_mortalite_calcule
FROM suivi_hebdo_bassin s
JOIN cycle_bassin_assoc cba ON cba.id = s.id_cycle_bassin_assoc;


---2 rectification 


ALTER TABLE creneau_horaire 
ADD COLUMN heure_debut TIME,
ADD COLUMN heure_fin TIME;






------rectification Bassin 

-- 1. Nettoyage : Assurez-vous que la table cycle n'a plus de contraintes NOT NULL inutiles
ALTER TABLE cycle ALTER COLUMN effectif_initial DROP NOT NULL; -- Si cela existe encore

-- 2. Insertion du cycle (en s'assurant de fournir les colonnes obligatoires)
-- Note : Vérifiez si 'id_technicien' est NOT NULL dans votre table. 
-- Si oui, assurez-vous que la sous-requête retourne bien un ID valide.
INSERT INTO cycle (code_unique_cycle, id_espece, id_technicien, date_debut, date_fin_prevue) 
VALUES (
    'CYC-2026-001', 
    (SELECT id FROM espece_crevette LIMIT 1), 
    (SELECT id FROM utilisateur LIMIT 1), 
    '2026-06-22', 
    '2026-10-22'
);

-- 3. Récupérer l'ID du cycle tout juste inséré pour l'utiliser dans l'association
-- C'est la méthode la plus sûre au lieu de faire un SELECT id WHERE code...
DO $$
DECLARE
    v_cycle_id INTEGER;
BEGIN
    SELECT id INTO v_cycle_id FROM cycle WHERE code_unique_cycle = 'CYC-2026-001';

    INSERT INTO cycle_bassin_assoc (id_cycle, id_bassin, effectif_initial, densite_m2, cout_post_larves, est_cloture) 
    VALUES
    (v_cycle_id, (SELECT id FROM bassin WHERE code = 'B01'), 5000, 50, 150000, FALSE),
    (v_cycle_id, (SELECT id FROM bassin WHERE code = 'B02'), 5000, 50, 150000, FALSE),
    (v_cycle_id, (SELECT id FROM bassin WHERE code = 'B03'), 5000, 50, 150000, FALSE);
END $$;