
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

INSERT INTO suivi_hebdo_bassin
(
    id_cycle_bassin_assoc,
    date_suivi,
    semaine_actuelle,
    poids_moyen_gramme,
    taille_moyenne_mm,
    id_technicien,
    notes
)

SELECT 
    cba.id,
    v.date_suivi::DATE,
    v.semaine,
    v.poids,
    v.taille,
    1,
    v.notes

FROM cycle_bassin_assoc cba

JOIN bassin b
    ON b.id = cba.id_bassin

JOIN cycle c
    ON c.id = cba.id_cycle

CROSS JOIN (VALUES
    ('2026-04-13', 0, 0.50,  8.00, 'Début de cycle - acclimatation'),
    ('2026-04-20', 1, 1.00, 12.00, 'Adaptation correcte'),
    ('2026-04-27', 2, 1.80, 18.00, 'Croissance normale'),
    ('2026-05-04', 3, 2.80, 25.00, 'Bonne évolution'),
    ('2026-05-11', 4, 4.00, 32.00, 'Développement régulier'),
    ('2026-05-18', 5, 5.50, 40.00, 'Croissance satisfaisante'),
    ('2026-05-25', 6, 7.00, 48.00, 'Bonne prise de poids'),
    ('2026-06-01', 7, 8.50, 55.00, 'Bon développement'),
    ('2026-06-08', 8, 10.00, 62.00, 'Stade intermédiaire'),
    ('2026-06-15',9,11.50, 70.00, 'Croissance avancée'),
    ('2026-06-22',10,13.00, 78.00, 'Approche calibre commercial'),
    ('2026-06-29',11,14.50, 85.00, 'Fin du suivi')
) AS v(date_suivi, semaine, poids, taille, notes)

WHERE b.code IN ('B02','B03')
AND c.code_unique_cycle = 'C01';

