Donnees de test pour la fonctionnalite Import (CSV/Excel) — module par module
================================================================================

Ces fichiers utilisent de vraies references existantes dans ta base locale
(orose, port 5432) au moment de leur generation (2026-07-13) :
  - Bassins existants : B01 (id 1, PREPARATION), B02/B03/B04 (id 2/3/4, ACTIFS)
  - Cycle actif : C01 (id 1), demarre le 2026-06-25, associe a B02/B03/B04
    -> cycle_bassin_assoc id 1 = B02, id 2 = B03, id 3 = B04
  - Utilisateurs : 1 (Admin OROSE), 6 (Admin Systeme), 7 (Directeur Demo),
    8 (Technicien Demo), 9 (Responsable Sanitaire)
  - Espece : 1 (Crevette blanche)
  - Aliments et medicaments : REFERENTIEL AJOUTE POUR CE TEST (voir plus bas)
    -> aliment 1/2/3, medicament 1/2/3

Ordre d'import conseille
-------------------------
1. 1_bassins.xlsx          -> Bassins > Importer   (role ADMIN)
2. 2_cycles.xlsx            -> Cycles > Importer    (role ADMIN)
3. 3_biologique_pesees.xlsx -> Biologique > Importer (ADMIN/TECH/RS)
4. 4_sanitaire_incidents.xlsx -> Sanitaire > Importer (ADMIN/TECH/RS)
5. 5_nourrissage_distributions.xlsx -> Nourrissage > Distribution > Importer (ADMIN/TECH/RS)
6. 6_stock_aliment_entrees.xlsx -> Stock aliment > Importer (ADMIN/TECH/RS)
7. 7_stock_medicament_entrees.xlsx -> Stock medicament > Importer (ADMIN/TECH/RS)

Les fichiers 3, 4, 5, 6, 7 sont INDEPENDANTS entre eux et de 1/2 : ils
s'appuient uniquement sur des donnees deja presentes en base (B02/B03/B04,
cycle C01). Tu peux les tester dans n'importe quel ordre, y compris avant
1 et 2.

Point d'attention : fichier 2_cycles.xlsx
------------------------------------------
Un cycle exige 3 bassins au statut VIDE. Aucun bassin existant n'est VIDE
actuellement (B01=PREPARATION, B02-04=ACTIFS) : ce fichier utilise donc
les 3 bassins du fichier 1 (B05, B06, B07), qui seront crees VIDE.

idBassin1/2/3 dans ce fichier = 6, 7, 8 — la prediction des IDs que
Postgres attribuera a B05/B06/B07 (prochaine valeur de sequence = 6 au
moment de la generation). Si tu as cree/supprime d'autres bassins entre
temps, ou importe le fichier 1 plusieurs fois, VERIFIE les IDs reels dans
Bassins > liste (survole "Details" d'un bassin, ou regarde en base) et
corrige idBassin1/2/3 dans le fichier avant d'importer 2_cycles.xlsx.

Referentiel ajoute en base pour rendre les tests stock possibles
-------------------------------------------------------------------
Les tables aliment et medicament etaient vides (aucun CRUD dans l'appli
pour les peupler), donc j'ai insere directement :
  aliment    : 1=Granules croissance 2mm, 2=Granules grossissement 3mm, 3=Farine post-larves
  medicament : 1=Oxytetracycline, 2=Vitamine C stabilisee, 3=Probiotique bassin

Formats attendus
------------------
  Dates  : AAAA-MM-JJ   (ex: 2026-07-15)
  Heures : HH:MM         (ex: 07:00)
  Decimaux : point (ex: 275.5), pas de virgule

Notes diverses
---------------
- Chaque fichier peut etre re-importe apres avoir modifie au moins un champ
  (l'import rejette les lignes strictement identiques a l'interieur d'un
  meme fichier, mais ne verifie pas les doublons contre la base existante
  sauf contrainte SQL, ex: code bassin unique).
- 1_bassins.xlsx ne peut etre importe qu'une seule fois tel quel (les codes
  B05/B06/B07 doivent etre libres). Pour retester, change les codes ou
  supprime les bassins crees.
