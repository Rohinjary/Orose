-- 1. Suppression de l'ancienne colonne générée
ALTER TABLE recolte_declaration 
DROP COLUMN taux_survie_percent;

-- 2. Recréation de la colonne avec la limite maximale à 100%
ALTER TABLE recolte_declaration 
ADD COLUMN taux_survie_percent DECIMAL(5,2) GENERATED ALWAYS AS (
    CASE 
        WHEN recolte_estimee_kg > 0 THEN 
            LEAST(100.00, (recolte_reelle_kg * 100.00 / recolte_estimee_kg))
        ELSE 0.00 
    END
) STORED;