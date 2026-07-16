create table parametre(
    id SERIAL PRIMARY KEY,
    label VARCHAR(50) NOT NULL,
    valeur DECIMAL(10,2)
);

INSERT INTO parametre (label, valeur) VALUES 
('cycle_par_an', 3),
('bassin_par_cycle', 3),
('prix_kg_crevette', 40000),
('poids_cible_gr', 20),
('taille_cible_mm', 120.00),
('pl_initial', 25000.00);