
INSERT INTO bassin (code, surface_m2, profondeur_metre, notes, id_statut_actuel)
SELECT v.code, v.surface, v.profondeur, NULL,
       (SELECT id FROM statut_bassin WHERE code = 'ACTIF')
FROM (VALUES
    ('B01', 1000.00, 1.50),
    ('B02', 1000.00, 1.50),
    ('B03', 1000.00, 1.50)
    -- ('B04', 1200.00, 1.60),
    -- ('B05', 1200.00, 1.60),
    -- ('B06', 1200.00, 1.60),
    -- ('B07',  900.00, 1.40),
    -- ('B08',  900.00, 1.40),
    -- ('B09',  900.00, 1.40)
) AS v(code, surface, profondeur)
ON CONFLICT (code) DO NOTHING;

-- INSERT INTO suivi_hebdo_bassin
-- (
--     id_cycle_bassin_assoc,
--     date_suivi,
--     semaine_actuelle,
--     poids_moyen_gramme,
--     taille_moyenne_mm,
--     id_technicien,
--     notes
-- )

-- SELECT 
--     cba.id,
--     v.date_suivi::DATE,
--     v.semaine,
--     v.poids,
--     v.taille,
--     1,
--     v.notes

-- FROM cycle_bassin_assoc cba

-- JOIN bassin b
--     ON b.id = cba.id_bassin

-- JOIN cycle c
--     ON c.id = cba.id_cycle

-- CROSS JOIN (VALUES
--     ('2026-04-14', 0, 0.50,  8.00, 'Début de cycle - acclimatation'),
--     ('2026-04-21', 1, 1.00, 12.00, 'Adaptation correcte'),
--     ('2026-04-28', 2, 1.80, 18.00, 'Croissance normale'),
--     ('2026-05-05', 3, 2.80, 25.00, 'Bonne évolution'),
--     ('2026-05-12', 4, 4.00, 32.00, 'Développement régulier'),
--     ('2026-05-19', 5, 5.50, 40.00, 'Croissance satisfaisante'),
--     ('2026-05-26', 6, 7.00, 48.00, 'Bonne prise de poids'),
--     ('2026-06-02', 7, 8.50, 55.00, 'Bon développement'),
--     ('2026-06-09', 8, 10.00, 62.00, 'Stade intermédiaire'),
--     ('2026-06-16',9,11.50, 70.00, 'Croissance avancée'),
--     ('2026-06-23',10,13.00, 78.00, 'Approche calibre commercial'),
--     ('2026-06-30',11,14.50, 85.00, 'Fin du suivi')
-- ) AS v(date_suivi, semaine, poids, taille, notes)

-- WHERE b.code IN ('B02','B03')
-- AND c.code_unique_cycle = 'C01';

INSERT INTO public."cycle" (code_unique_cycle,id_espece,id_technicien,date_debut,date_fin_prevue,est_cloture,created_at) VALUES
	 ('C01',1,NULL,'2026-04-14','2026-08-14',false,'2026-07-15 08:27:02.160002');

INSERT INTO public.cycle_bassin_assoc (id_cycle,id_bassin,effectif_initial,densite_m2,cout_post_larves,poids_moyen_actuel,semaine_actuelle,date_fin_reelle,est_cloture) VALUES
	 (1,3,12000,4.00,40000.00,19.75,13,NULL,false),
	 (1,2,12000,4.00,40000.00,19.75,13,NULL,false),
     (1,1,12000,4.00,40000.00,19.75,13,NULL,false);

INSERT INTO public.suivi_hebdo_bassin (id_cycle_bassin_assoc,date_suivi,semaine_actuelle,poids_moyen_gramme,taille_moyenne_mm,id_technicien,notes) VALUES
	 (1,'2026-04-14',0,0.50,8.00,1,'Début de cycle - acclimatation'),
	 (1,'2026-04-21',1,1.00,12.00,1,'Adaptation correcte'),
	 (1,'2026-04-28',2,1.80,18.00,1,'Croissance normale'),
	 (1,'2026-05-05',3,2.80,25.00,1,'Bonne évolution'),
	 (1,'2026-05-12',4,4.00,32.00,1,'Développement régulier'),
	 (1,'2026-05-19',5,5.50,40.00,1,'Croissance satisfaisante'),
	 (1,'2026-05-26',6,7.00,48.00,1,'Bonne prise de poids'),
	 (1,'2026-06-02',7,8.50,55.00,1,'Bon développement'),
	 (1,'2026-06-09',8,10.00,62.00,1,'Stade intermédiaire'),
	 (1,'2026-06-16',9,11.50,70.00,1,'Croissance avancée');
INSERT INTO public.suivi_hebdo_bassin (id_cycle_bassin_assoc,date_suivi,semaine_actuelle,poids_moyen_gramme,taille_moyenne_mm,id_technicien,notes) VALUES
	 (1,'2026-06-23',10,13.00,78.00,1,'Approche calibre commercial'),
	 (1,'2026-06-30',11,14.50,85.00,1,'Suivi S11'),
	 (2,'2026-04-14',0,0.50,8.00,1,'Début de cycle - acclimatation'),
	 (2,'2026-04-21',1,1.00,12.00,1,'Adaptation correcte'),
	 (2,'2026-04-28',2,1.80,18.00,1,'Croissance normale'),
	 (2,'2026-05-05',3,2.80,25.00,1,'Bonne évolution'),
	 (2,'2026-05-12',4,4.00,32.00,1,'Développement régulier'),
	 (2,'2026-05-19',5,5.50,40.00,1,'Croissance satisfaisante'),
	 (2,'2026-05-26',6,7.00,48.00,1,'Bonne prise de poids');
INSERT INTO public.suivi_hebdo_bassin (id_cycle_bassin_assoc,date_suivi,semaine_actuelle,poids_moyen_gramme,taille_moyenne_mm,id_technicien,notes) VALUES
	 (2,'2026-06-02',7,8.50,55.00,1,'Bon développement'),
	 (2,'2026-06-09',8,10.00,62.00,1,'Stade intermédiaire'),
	 (2,'2026-06-16',9,11.50,70.00,1,'Croissance avancée'),
	 (2,'2026-06-23',10,13.00,78.00,1,'Approche calibre commercial'),
	 (2,'2026-06-30',11,14.50,85.00,1,'Suivi S11'),
	 (3,'2026-04-14',0,0.50,8.00,1,'Début de cycle - acclimatation'),
	 (3,'2026-04-21',1,1.00,12.00,1,'Adaptation correcte'),
	 (3,'2026-04-28',2,1.80,18.00,1,'Croissance normale'),
	 (3,'2026-05-05',3,2.80,25.00,1,'Bonne évolution');
INSERT INTO public.suivi_hebdo_bassin (id_cycle_bassin_assoc,date_suivi,semaine_actuelle,poids_moyen_gramme,taille_moyenne_mm,id_technicien,notes) VALUES
	 (3,'2026-05-12',4,4.00,32.00,1,'Développement régulier'),
	 (3,'2026-05-19',5,5.50,40.00,1,'Croissance satisfaisante'),
	 (3,'2026-05-26',6,7.00,48.00,1,'Bonne prise de poids'),
	 (3,'2026-06-02',7,8.50,55.00,1,'Bon développement'),
	 (3,'2026-06-09',8,10.00,62.00,1,'Stade intermédiaire'),
	 (3,'2026-06-16',9,11.50,70.00,1,'Croissance avancée'),
	 (3,'2026-06-23',10,13.00,78.00,1,'Approche calibre commercial'),
	 (3,'2026-06-30',11,14.50,85.00,1,'Suivi S11'),
	 (1,'2026-07-7',12,18.75,92.00,1,'ok');
INSERT INTO public.suivi_hebdo_bassin (id_cycle_bassin_assoc,date_suivi,semaine_actuelle,poids_moyen_gramme,taille_moyenne_mm,id_technicien,notes) VALUES
	 (3,'2026-07-7',12,18.75,92.00,1,'ok'),
	 (2,'2026-07-7',12,18.75,92.00,1,'ok');


